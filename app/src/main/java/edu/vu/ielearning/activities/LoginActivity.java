package edu.vu.ielearning.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.eftimoff.androidplayer.Player;
import com.eftimoff.androidplayer.actions.property.PropertyAction;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.himanshurawat.hasher.HashType;
import com.himanshurawat.hasher.Hasher;

import org.jetbrains.annotations.NotNull;

import edu.vu.ielearning.R;
import es.dmoral.toasty.Toasty;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final String LOGIN_DATA = "loginData";
    EditText inputStudentId, inputPassword;
    View logo;
    TextView text;
    TextInputLayout studentId;
    TextInputLayout password;
    TextView forgotPassword;
    Button buttonLogin;
    CheckBox checkbox;
    ProgressBar progressBar;
    DatabaseReference databaseReference;
    SharedPreferences sharedPreferences;
    String savedStudentId;
    String savedPassword;
    String typedPassword;
    String typedStudentId;

    long pressedTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inputStudentId = findViewById(R.id.inputStudentId);
        inputPassword = findViewById(R.id.inputPassword);
        forgotPassword = findViewById(R.id.forgotPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        studentId = findViewById(R.id.studentId);
        password = findViewById(R.id.password);
        logo = findViewById(R.id.animate1);
        text = findViewById(R.id.animate2);

        checkbox = findViewById(R.id.checkbox);
        progressBar = findViewById(R.id.progressBar);


        databaseReference = FirebaseDatabase.getInstance().getReference().child("Student");

        sharedPreferences = getSharedPreferences(LOGIN_DATA, MODE_PRIVATE);
        savedStudentId = sharedPreferences.getString("loggedInStudentId", "");
        savedPassword = sharedPreferences.getString("savedPassword", "");

        inputStudentId.setText(savedStudentId);
        inputPassword.setText(savedPassword);

        if (!savedStudentId.equals("")) {
            buttonLogin.post(() -> buttonLogin.performClick());
        } else {
            animateActivity(logo, text, studentId, password, checkbox, buttonLogin, forgotPassword);
        }

        forgotPassword.setOnClickListener(this::forgotPassword);

        buttonLogin.setOnClickListener(v ->
        {
            progressBar.setVisibility(View.VISIBLE);
            setClickables(false);
            typedPassword = inputPassword.getText().toString();
            typedStudentId = inputStudentId.getText().toString().toLowerCase();
            if (typedPassword.isEmpty() || typedStudentId.isEmpty()) {
                progressBar.setVisibility(View.INVISIBLE);
                setClickables(true);
                Toasty.info(getApplicationContext(), "Both Fields are Required", Toast.LENGTH_SHORT).show();
            } else {
                if (isConnected()) {
                    Log.d(TAG, "Accessing Firebase Database...");
                    databaseReference.child(typedStudentId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                            if (Hasher.Companion.hash(typedPassword, HashType.SHA_384).equals(snapshot.child("password").getValue())) {
                                if (checkbox.isChecked()) {
                                    saveLoginData(typedStudentId, typedPassword);
                                }
                                Toasty.success(getApplicationContext(), "Login Successful", Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.INVISIBLE);
                                startMainActivity(typedStudentId);
                                finish();
                            } else {
                                setClickables(true);
                                progressBar.setVisibility(View.INVISIBLE);
                                Toasty.error(getApplicationContext(), "Wrong ID or Password", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull @NotNull DatabaseError error) {
                        }

                    });
                } else {
                    setClickables(true);
                    progressBar.setVisibility(View.INVISIBLE);
                    Toasty.info(getApplicationContext(), "No Internet!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void animateActivity(View animate1, View animate2, View animate3, View animate4, View animate5, View animate6, View animate7) {
        final PropertyAction logo = PropertyAction.newPropertyAction(animate1).translationY(-500).duration(400).alpha(0f).build();
        final PropertyAction text = PropertyAction.newPropertyAction(animate2).duration(300).alpha(0f).build();
        final PropertyAction studentId = PropertyAction.newPropertyAction(animate3).translationX(-500).duration(300).alpha(0f).build();
        final PropertyAction password = PropertyAction.newPropertyAction(animate4).translationX(500).duration(300).alpha(0f).build();
        final PropertyAction checkbox = PropertyAction.newPropertyAction(animate5).translationX(-500).duration(300).alpha(0f).build();
        final PropertyAction button = PropertyAction.newPropertyAction(animate6).duration(300).translationY(500).alpha(0f).build();
        final PropertyAction forgot = PropertyAction.newPropertyAction(animate7).duration(300).translationY(500).alpha(0f).build();
        Player.init().
                animate(logo).
                then().
                animate(text).
                then().
                animate(studentId).
                then().
                animate(password).
                then().
                animate(checkbox).
                then().
                animate(button).
                then().
                animate(forgot).
                play();
    }

    public void startMainActivity(String typedStudentId) {
        Intent MainActivity = new Intent(getApplicationContext(), MainActivity.class);
        MainActivity.putExtra("loggedInStudentId", typedStudentId);
        startActivity(MainActivity);
        Animatoo.animateSwipeLeft(this);
    }

    @Override
    public void onBackPressed() {
        if (pressedTime + 2000 > System.currentTimeMillis()) {
            super.onBackPressed();
            finish();
        } else {
            Toasty.warning(getApplicationContext(), "Press back again to exit", Toast.LENGTH_SHORT).show();
        }
        pressedTime = System.currentTimeMillis();
    }

    private void saveLoginData(String typedStudentId, String typedPassword) {
        SharedPreferences.Editor editor = getSharedPreferences(LOGIN_DATA, MODE_PRIVATE).edit();
        editor.putString("loggedInStudentId", typedStudentId);
        editor.putString("savedPassword", typedPassword);
        editor.apply();
    }


    public void forgotPassword(View view) {
        progressBar.setVisibility(View.VISIBLE);
        Intent ForgotPass = new Intent(getApplicationContext(), ForgotPasswordActivity.class);
        startActivity(ForgotPass);
        progressBar.setVisibility(View.INVISIBLE);
        Animatoo.animateSwipeLeft(this);
        finish();
    }

    public boolean isConnected() {
        boolean connected = false;
        try {
            ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo nInfo = cm.getActiveNetworkInfo();
            connected = nInfo != null && nInfo.isAvailable() && nInfo.isConnected();
            return connected;
        } catch (Exception e) {
            Log.e("Connectivity Exception", e.getMessage());
        }
        return connected;
    }


    @SuppressLint("SetTextI18n")
    public void setClickables(boolean flag) {
        if (flag) {
            buttonLogin.setText("LOGIN");
        } else {
            buttonLogin.setText("LOGGING IN...");
        }
        forgotPassword.setClickable(flag);
        buttonLogin.setClickable(flag);
    }
}