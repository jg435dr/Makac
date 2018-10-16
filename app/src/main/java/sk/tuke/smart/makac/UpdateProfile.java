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
import sk.tuke.smart.makac.helpers.MainHelper;

public class UpdateProfile extends Editable {

    @BindView(R.id.update_profile_login_text)
    EditText loginET;
    @BindView(R.id.update_profile_password_text)
    EditText passwordET;
    @BindView(R.id.update_profile_password_text_repeat)
    EditText confirmPasswordET;
    @BindView(R.id.update_profile_age_text)
    EditText ageET;
    @BindView(R.id.update_profile_weight_text)
    EditText weightET;

    private String gender, login, password, confirmPassword;
    private int age, weight;
    private RadioButton maleRB, femaleRB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);
        ButterKnife.bind(this);
        
        maleRB = (RadioButton)findViewById(R.id.update_profile_male);
        femaleRB = (RadioButton)findViewById(R.id.update_profile_female);
        setValues();
    }

    private void setValues(){
        loginET.setText(MainHelper.user);
        passwordET.setText(MainHelper.userPassword);
        confirmPasswordET.setText(MainHelper.userPassword);
        ageET.setText(String.valueOf(MainHelper.userAge));
        weightET.setText(String.valueOf(MainHelper.userWeight));
        if(MainHelper.userGender.equals("female")){
           femaleRB.setChecked(true);
        } else {
            maleRB.setChecked(true);
        }
    }

    private void getValues() {
        login = loginET.getText().toString();
        password = passwordET.getText().toString();
        confirmPassword = confirmPasswordET.getText().toString();
        try {
            age = Integer.parseInt(ageET.getText().toString());
        } catch(Exception e){
            e.printStackTrace();
            age = 0;
        }
        try {
            weight = Integer.parseInt(weightET.getText().toString());
        } catch(Exception e){
            e.printStackTrace();
            weight = 0;
        }
        if(femaleRB.isChecked()){
            gender = "female";
        } else {
            gender = "male";
        }
    }

    @OnClick(R.id.update_profile_update_button)
    public void updateValues(){
        getValues();
        if(checkVariables(login, password, confirmPassword, weight, age)) {
            if((new DatabaseHelper(this)).updateUserValues(login, password, gender, age, weight)){
                Toast.makeText(this, R.string.update_success, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.update_failure, Toast.LENGTH_SHORT).show();
            }
            startActivity(new Intent(this, UserProfile.class));
        }
    }
}
