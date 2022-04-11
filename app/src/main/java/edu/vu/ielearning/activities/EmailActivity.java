package edu.vu.ielearning.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;

import edu.vu.ielearning.R;

public class EmailActivity extends AppCompatActivity {

    @SuppressLint("IntentReset")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email);
        View goBack;

        Intent i = new Intent(Intent.ACTION_SENDTO);
        i.setType("text/plain");
        i.setData(Uri.parse("mailto:"));
        i.putExtra(Intent.EXTRA_EMAIL, new String[]{"bc180200907@vu.edu.pk"});
        i.putExtra(Intent.EXTRA_SUBJECT, "Reset Password");
        i.putExtra(Intent.EXTRA_TEXT, "Respected Sir, I forgot my password. Please reset my password to ");
        i.setPackage("com.google.android.gm");
        try {
            startActivity(Intent.createChooser(i, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/html");
            String[] recipients = {"bc180200907@vu.edu.pk"};
            intent.putExtra(Intent.EXTRA_EMAIL, recipients);
            intent.putExtra(Intent.EXTRA_SUBJECT, "Reset Password");
            intent.putExtra(Intent.EXTRA_TEXT, "Respected Sir, I forgot my password. Please reset my password to ");
            startActivity(Intent.createChooser(intent, "Send mail"));
        }

        goBack = findViewById(R.id.back);
        goBack.setOnClickListener(v ->
        {
            goBack.setClickable(false);
            goBack();
        });
    }

    public void goBack() {
        Intent ForgotPass = new Intent(getApplicationContext(), ForgotPasswordActivity.class);
        startActivity(ForgotPass);
        Animatoo.animateSwipeRight(this);
        finish();
    }

    @Override
    public void onBackPressed() {
        goBack();
    }

}