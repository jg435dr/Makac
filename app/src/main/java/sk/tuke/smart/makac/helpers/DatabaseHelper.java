package sk.tuke.smart.makac.helpers;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.math.BigDecimal;
import java.util.List;

import sk.tuke.smart.makac.R;
import sk.tuke.smart.makac.services.TrackerService;

/**
 * Created by Jakub on 17.11.2017.
 *
 */

public class DatabaseHelper {

    private Context context;
    private boolean result;

    public DatabaseHelper(Context context) {
        this.context = context;
    }

    private void initializeDb() {
        Parse.initialize(new Parse.Configuration.Builder(context)
                .applicationId("RNDLFRxWgZAfzQOowjcUZn17AH8UQFSwlqCgXjcE")
                .clientKey("SFfsBYIojHklYwb4XwMVqWrTRKC2MG9e70lmCZxq")
                .server("https://parseapi.back4app.com/")
                .build()
        );
        Parse.setLogLevel(Parse.LOG_LEVEL_VERBOSE);
        MainHelper.dbIsInitialized = true;
    }

    public boolean verifyIdentity(String login, String password) {
        result = false;
        if (login == null || password == null || !isDbReady()) {
            return result;
        }

        ParseQuery<ParseObject> query = ParseQuery.getQuery("user");
        query.whereEqualTo("login", login);
        query.whereEqualTo("password", password);
        try {
            List<ParseObject> users = query.find();
            if(users == null || users.size() == 0){
                return false;
            }
            ParseObject user = users.get(0);
            if (user != null) {
                setMainHelper(user.getString("login"), user.getString("password"), user.getInt("age"), user.getString("gender"), user.getInt("weight"));
                result = true;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return result;
    }

    public boolean updateUserValues(final String login, final String password, final String gender, final int age, final int weight) {
        result = false;
        if(!isDbReady()){
            return result;
        }
        ParseQuery<ParseObject> query = ParseQuery.getQuery("user");
        query.whereEqualTo("login", MainHelper.user);

        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject user, ParseException e) {
                if (e == null) {
                    if(!(MainHelper.user.equals(login))){
                        updateLogin(login);
                    }
                    setParseObject(user, login, password, gender, age, weight).saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                setMainHelper(login, password, age, gender, weight);
                                context.sendBroadcast((new Intent()).setAction("sk.tuke.smart.makac.UPDATED"));
                                result = true;
                            }
                        }
                    });
                } else {
                    e.printStackTrace();
                }
            }
        });
        return result;
    }


    public boolean insertUserDB(String login, String password, String gender, int age, int weight) {
        if(!isDbReady()){
            return false;
        }
        ParseObject user = new ParseObject("user");
        setParseObject(user, login, password, gender, age, weight).saveInBackground();

        try {
            ParseQuery<ParseObject> query = ParseQuery.getQuery("user");
            query.whereEqualTo("login", login);
            ParseObject parseObject = (query.find()).get(0);
            if (parseObject != null) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public void insertLocationDB(Bundle extras) {
        try {
            Location location = getLastLoc(extras);
            if (location == null) {
                return;
            }
            ParseObject user = new ParseObject("Location");
            user.put("user", MainHelper.user);
            user.put("trackId", MainHelper.trackId);
            user.put("sportActivity", extras.getInt("Sport"));
            user.put("latitude", location.getLatitude());
            user.put("longitude", location.getLongitude());
            BigDecimal temp = new BigDecimal(extras.getDouble("Distance"));
            user.put("distance", temp.setScale(2, BigDecimal.ROUND_HALF_UP));
            temp = new BigDecimal(extras.getDouble("Pace"));
            user.put("speed", temp.setScale(2, BigDecimal.ROUND_HALF_UP)); // m/s
            user.put("time", extras.getLong("Duration"));
            temp = new BigDecimal(extras.getDouble("Calories"));
            user.put("calories", temp.setScale(2, BigDecimal.ROUND_HALF_UP));
            user.saveInBackground();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private Location getLastLoc(Bundle extras) {
        try {
            if(!isDbReady()){
                return null;
            }
            if (extras.getInt("State") == TrackerService.STATE_STOPPED) {
                List<List<Location>> locationList = (List<List<Location>>) extras.getSerializable("PositionList");
                if(locationList != null) {
                    int size = locationList.size() - 1;
                    return locationList.get(size).get(locationList.get(size).size() - 1);
                }

            } else {
                List<Location> locationList = (List<Location>) extras.getSerializable("PositionList");
                if(locationList != null) {
                    return locationList.get(locationList.size() - 1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    public List<ParseObject> getListOfWorkouts(){
        if(!isDbReady()){
            return null;
        }
        List<ParseObject> parseObjectList = null;
        try {
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Location");
            query.whereEqualTo("user", MainHelper.user);
            query.orderByDescending("createdAt");
            parseObjectList = query.find();
        } catch (Exception e){
            e.printStackTrace();
        }
        return parseObjectList;
    }

    public boolean deleteTrack(int trackId){
        if(!isDbReady()) {
            return false;
        }
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Location");
        query.whereEqualTo("trackId", trackId);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                for(ParseObject object : objects){
                    object.deleteInBackground();
                }
            }
        });
        return true;
    }

    public boolean isDbReady(){
        if(!MainHelper.isNetworkOnline(context)) {
            Toast.makeText(context, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
            return false;
        }
        if(!MainHelper.dbIsInitialized) {
            initializeDb();
        }
        return true;
    }

    public int getLastID() {
        int trackId = 0;
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Location");
        try {
            query.whereEqualTo("user", MainHelper.user);
            query.orderByDescending("createdAt");
            trackId = query.getFirst().getInt("trackId");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return trackId;
    }

    private void updateLogin(final String login){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Location");
        query.whereEqualTo("user", MainHelper.user);
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject user, ParseException e) {
                if (e == null) {
                    user.put("user", login);
                    user.saveInBackground();
                } else {
                    e.printStackTrace();
                }
            }
        });
    }

    private ParseObject setParseObject(ParseObject user, String login, String password, String gender, int age, int weight){
        user.put("login", login);
        user.put("password", password);
        user.put("gender", gender);
        user.put("age", age);
        user.put("weight", weight);
        return user;
    }

    private void setMainHelper(String login, String password, int age, String gender, int weight){
        MainHelper.user = login;
        MainHelper.userPassword = password;
        MainHelper.userAge = age;
        MainHelper.userGender = gender;
        MainHelper.userWeight = weight;
    }
}
