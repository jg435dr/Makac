package sk.tuke.smart.makac;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import sk.tuke.smart.makac.helpers.DatabaseHelper;

public class RegisterActivity extends Editable {


    @BindView(R.id.register_activity_login_text)
    EditText etLogin;
    @BindView(R.id.register_activity_password_text)
    EditText etPassword;
    @BindView(R.id.register_activity_confirm_password_text)
    EditText etConfirmPassword;
    @BindView(R.id.register_activity_weight_text)
    EditText etWeight;
    @BindView(R.id.register_activity_age_text)
    EditText etAge;

    private String gender, login, password, confirmPassword;
    private int age, weight;
    private RadioButton femaleRB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);

        femaleRB = (RadioButton)findViewById(R.id.update_profile_female);
        gender = "male";
    }

    private void getValues() {
        login = etLogin.getText().toString();
        password = etPassword.getText().toString();
        confirmPassword = etConfirmPassword.getText().toString();
        try {
            age = Integer.parseInt(etAge.getText().toString());
        } catch(Exception e){
            e.printStackTrace();
            age = 0;
        }
        try {
            weight = Integer.parseInt(etWeight.getText().toString());
        } catch(Exception e){
            e.printStackTrace();
            weight = 0;
        }
        if(femaleRB != null) {
            if (femaleRB.isChecked()) {
                gender = "female";
            } else {
                gender = "male";
            }
        }
    }

    @OnClick(R.id.register_activity_register_button)
    public void setButton() {
        getValues();
        if(checkVariables(login, password, confirmPassword, weight, age)) {
            if ((new DatabaseHelper(this).insertUserDB(login, password, gender, age, weight))) {
                Toast.makeText(getApplicationContext(), R.string.success, Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            } else {
                Toast.makeText(getApplicationContext(), R.string.failure, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @OnClick(R.id.register_activity_login_button)
    public void loginButton(){
        startActivity(new Intent(this, LoginActivity.class));
    }
}
