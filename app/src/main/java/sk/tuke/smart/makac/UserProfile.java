package sk.tuke.smart.makac;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.ContextThemeWrapper;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import sk.tuke.smart.makac.helpers.MainHelper;

public class UserProfile extends AppCompatActivity {

    @BindView(R.id.user_profile_password_value)
    TextView tvPassword;
    @BindView(R.id.user_profile_username_value)
    TextView tvUserName;
    @BindView(R.id.user_profile_weight_value)
    TextView tvWeight;
    @BindView(R.id.user_profile_age_value)
    TextView tvAge;

    private BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        ButterKnife.bind(this);

        setVariables();
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                setVariables();
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, new IntentFilter("sk.tuke.smart.makac.UPDATED"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    private void setVariables(){
        tvAge.setText(String.valueOf(MainHelper.userAge));
        tvPassword.setText("******");
        tvUserName.setText(MainHelper.user);
        tvWeight.setText(String.valueOf(MainHelper.userWeight));
    }

    public void updateValuesButton(View view){

        ContextThemeWrapper ctw = new ContextThemeWrapper(this, R.style.ThemeOverlay_AppCompat_Dark);
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ctw);
        alertDialog.setTitle(R.string.confirm_the_password);

        final EditText input = new EditText(UserProfile.this);
        input.setTransformationMethod(PasswordTransformationMethod.getInstance());
        input.setTextColor(Color.WHITE);
        alertDialog.setView(input);
        //alertDialog.setIcon(R.drawable.key);
        alertDialog.setPositiveButton(R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (input.getText().toString().equals(MainHelper.userPassword)) {
                            startActivity(new Intent(getApplicationContext(), UpdateProfile.class)); // cez getextras poslat vsetky udaje
                        } else {
                            Toast.makeText(getApplicationContext(), R.string.wrong_password, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        alertDialog.setNegativeButton(R.string.no,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        alertDialog.show();
    }
}
