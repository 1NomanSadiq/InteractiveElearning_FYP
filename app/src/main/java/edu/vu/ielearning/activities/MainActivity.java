package edu.vu.ielearning.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.eftimoff.androidplayer.Player;
import com.eftimoff.androidplayer.actions.property.PropertyAction;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

import edu.vu.ielearning.R;
import edu.vu.ielearning.adapters.CoursesAdapter;
import es.dmoral.toasty.Toasty;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final String LOGIN_DATA = "loginData";
    RecyclerView studentCoursesContainer;
    CoursesAdapter coursesAdapter;
    String[] courses;
    TextView studentName;
    TextView studentId;
    View iconLogout;
    String loggedInStudentId;
    DatabaseReference databaseReference;
    SharedPreferences sharedPreferences;

    long pressedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        studentCoursesContainer = findViewById(R.id.studentCoursesContainer);
        studentCoursesContainer.setLayoutManager(new LinearLayoutManager(this));
        studentName = findViewById(R.id.studentName);
        studentId = findViewById(R.id.studentId);
        iconLogout = findViewById(R.id.iconLogout);
        Intent intent = getIntent();
        loggedInStudentId = intent.getStringExtra("loggedInStudentId");

        animateActivity(studentName, iconLogout, studentId, studentCoursesContainer);

        if (isConnected()) {
            Log.d(TAG, "Accessing firebase...");
            databaseReference = FirebaseDatabase.getInstance().getReference().child("Student").child(loggedInStudentId);
            databaseReference.child("course").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    Iterator<DataSnapshot> iterator = snapshot.getChildren().iterator();
                    int length = (int) snapshot.getChildrenCount();
                    courses = new String[length];
                    for (int i = 0; i < length; i++) {
                        courses[i] = Objects.requireNonNull(iterator.next().getValue()).toString();
                    }
                    Arrays.sort(courses);
                    coursesAdapter = new CoursesAdapter(courses, loggedInStudentId);
                    studentCoursesContainer.setAdapter(coursesAdapter);
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                }
            });

            studentId.setText(loggedInStudentId);
            setStudentNameText();

        } else {
            Toasty.info(getApplicationContext(), "No internet!", Toast.LENGTH_LONG).show();
        }

        iconLogout.setOnClickListener(v ->
        {
            clearLoginData();
            Intent LoginActivity = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(LoginActivity);
            Animatoo.animateCard(this);
            finish();
        });
    }

    private void clearLoginData() {
        sharedPreferences = getSharedPreferences(LOGIN_DATA, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("loggedInStudentId");
        editor.remove("savedPassword");
        editor.apply();
    }

    public void setStudentNameText() {
        databaseReference.child("name").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                assert snapshot.getValue() != null;
                studentName.setText(snapshot.getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });
    }


    @Override
    public void onBackPressed() {

        if (pressedTime + 2000 > System.currentTimeMillis()) {
            super.onBackPressed();
            finish();
        } else {
            Toasty.warning(getBaseContext(), "Press back again to exit", Toast.LENGTH_SHORT).show();
        }
        pressedTime = System.currentTimeMillis();
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

    private void animateActivity(View animate1, View animate2, View animate3, View animate4) {
        final PropertyAction name = PropertyAction.newPropertyAction(animate1).translationY(-500).duration(300).alpha(0f).build();
        final PropertyAction logout = PropertyAction.newPropertyAction(animate2).translationY(-500).duration(300).alpha(0f).build();
        final PropertyAction id = PropertyAction.newPropertyAction(animate3).translationX(-500).duration(300).alpha(0f).build();
        final PropertyAction layout = PropertyAction.newPropertyAction(animate4).translationX(-500).duration(300).alpha(0f).build();
        Player.init().
                animate(layout).
                then().
                animate(name).
                then().
                animate(logout).
                then().
                animate(id).
                play();
    }

}