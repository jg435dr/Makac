package sk.tuke.smart.makac;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import sk.tuke.smart.makac.helpers.MainHelper;

/**
 * Created by Jakub on 11.1.2018.
 *
 */

public class MenuActivity extends AppCompatActivity {

    private Menu menu;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(menu == null){
            return false;
        }

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.action_buttons, menu);

        this.menu = menu;
        return toggleMenuItem();
    }


    private boolean toggleMenuItem(){
        if (menu == null){
            return false;
        }
        MenuItem logIn = menu.findItem(R.id.item_log_in);
        MenuItem logOut = menu.findItem(R.id.item_log_out);
        MenuItem userProfile = menu.findItem(R.id.item_user);
        MenuItem listOfWorkouts = menu.findItem(R.id.item_list);

        if(logIn != null && logOut != null) {
            if (MainHelper.user == null) {
                logIn.setVisible(true);
                logOut.setVisible(false);
                userProfile.setVisible(false);
                listOfWorkouts.setVisible(false);
            } else {
                logIn.setVisible(false);
                logOut.setVisible(true);
                userProfile.setVisible(true);
                listOfWorkouts.setVisible(true);
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(this, StopwatchActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return true;
            case R.id.item_list:
                finish();
                Intent intent1 = new Intent(getApplicationContext(),MyWorkoutsHistory.class);
                startActivity(intent1);
                return true;
            case R.id.item_user:
                startActivity(new Intent(getApplicationContext(),UserProfile.class));
                return true;
            case R.id.item_log_in:
                startActivity(new Intent(getApplicationContext(),LoginActivity.class));
                return true;
            case R.id.item_log_out:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.log_out)
                        .setMessage(R.string.want_log_out)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                MainHelper.user = null;
                                toggleMenuItem();
                            }
                        }).setNegativeButton(R.string.no, null);
                AlertDialog dialog = builder.create();
                dialog.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
        //return super.onOptionsItemSelected(item);
    }
}
