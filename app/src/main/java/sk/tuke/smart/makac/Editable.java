package sk.tuke.smart.makac;

import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;

import sk.tuke.smart.makac.helpers.DatabaseHelper;
import sk.tuke.smart.makac.helpers.MainHelper;

/**
 * Created by Jakub on 13.1.2018.
 *
 */

public abstract class Editable extends AppCompatActivity {

    boolean checkVariables(String login, String password, String confirmPassword, int weight, int age) {
        boolean result = true;

        if (login == null || login.equals("")) {
            Toast.makeText(this, R.string.write_login, Toast.LENGTH_SHORT).show();
            result = false;
        } else if (password == null || password.equals("")) {
            Toast.makeText(this, R.string.write_password, Toast.LENGTH_SHORT).show();
            result = false;
        } else if (!password.equals(confirmPassword)) {
            Toast.makeText(this, R.string.repeat_password_again, Toast.LENGTH_SHORT).show();
            result = false;
        } else if (weight <= 0) {
            Toast.makeText(this, R.string.write_weight, Toast.LENGTH_SHORT).show();
            result = false;
        } else if (age <= 0) {
            Toast.makeText(this, R.string.write_age, Toast.LENGTH_SHORT).show();
            result = false;
        } else if (MainHelper.user != null && !(MainHelper.user.equalsIgnoreCase(login))) {
            try {
                DatabaseHelper database = new DatabaseHelper(this);
                if(!database.isDbReady()){
                    return false;
                }
                ParseQuery<ParseObject> query = ParseQuery.getQuery("user");
                query.whereEqualTo("login", login);
                List<ParseObject> parseObjectList = query.find();
                if (parseObjectList != null && parseObjectList.size() > 0) {
                    Toast.makeText(this, R.string.login_already_exist, Toast.LENGTH_SHORT).show();
                    return false;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}
