package sk.tuke.smart.makac;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import sk.tuke.smart.makac.helpers.DatabaseHelper;
import sk.tuke.smart.makac.helpers.MainHelper;

public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.login_text)
    EditText loginTv;
    @BindView(R.id.password_text)
    EditText passwordTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.login_button)
    public void signIn() {
        DatabaseHelper database = new DatabaseHelper(this);
        if (database.verifyIdentity(loginTv.getText().toString(), passwordTv.getText().toString())) {
            MainHelper.trackId = database.getLastID() + 1;
            startActivity(new Intent(getApplicationContext(), StopwatchActivity.class));
        } else {
            loginTv.setTextColor(Color.RED);
            passwordTv.setTextColor(Color.RED);
        }
    }

    @OnClick(R.id.stopwatch_button)
    public void withoutLogin() {
        startActivity(new Intent(getApplicationContext(), StopwatchActivity.class));
    }

    @OnClick(R.id.register_button)
    public void register() {
        startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
    }
}

