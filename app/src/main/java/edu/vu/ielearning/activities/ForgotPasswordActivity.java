package edu.vu.ielearning.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.eftimoff.androidplayer.Player;
import com.eftimoff.androidplayer.actions.property.PropertyAction;

import edu.vu.ielearning.R;

public class ForgotPasswordActivity extends AppCompatActivity {
    Button compose;
    View goBack;
    TextView forgotPassword;
    View fp1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        goBack = findViewById(R.id.back);
        forgotPassword = findViewById(R.id.forgotPassword);
        fp1 = findViewById(R.id.fp1);
        compose = findViewById(R.id.sendEmail);
        animateActivity(fp1, forgotPassword, compose);
        compose.setOnClickListener(v -> Compose());
        goBack.setOnClickListener(v -> goBack());
    }

    public void goBack() {
        setClickablesFalse();
        Intent LoginActivity = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(LoginActivity);
        Animatoo.animateSwipeRight(this);
        finish();
    }

    public void Compose() {
        setClickablesFalse();
        Intent Email = new Intent(getApplicationContext(), EmailActivity.class);
        startActivity(Email);
        Animatoo.animateSwipeLeft(this);
        finish();
    }

    @Override
    public void onBackPressed() {
        goBack();
    }

    public void setClickablesFalse() {
        goBack.setClickable(false);
        compose.setClickable(false);
    }

    private void animateActivity(View animate1, View animate2, View animate3) {
        final PropertyAction logo = PropertyAction.newPropertyAction(animate1).translationY(-500).duration(400).alpha(0f).build();
        final PropertyAction text = PropertyAction.newPropertyAction(animate2).duration(400).alpha(0f).build();
        final PropertyAction button = PropertyAction.newPropertyAction(animate3).duration(400).alpha(0f).build();
        Player.init().
                animate(logo).
                then().
                animate(text).
                then().
                animate(button).
                play();
    }

}
