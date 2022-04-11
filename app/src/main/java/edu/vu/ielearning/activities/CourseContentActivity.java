package edu.vu.ielearning.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.eftimoff.androidplayer.Player;
import com.eftimoff.androidplayer.actions.property.PropertyAction;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import edu.vu.ielearning.R;
import edu.vu.ielearning.fragments.DiscussionBoardFragment;
import edu.vu.ielearning.fragments.HelpingMaterialFragment;
import edu.vu.ielearning.fragments.NotesFragment;
import edu.vu.ielearning.fragments.ProfessorDetailsFragment;
import edu.vu.ielearning.fragments.VideosFragment;

public class CourseContentActivity extends AppCompatActivity {
    private static final String TAG = "CourseContent";
    TextView selectedCourseText;
    TextView courseTitle;
    BottomNavigationView courseDetailsSection;
    String loggedInStudentId;
    String selectedCourse;
    DatabaseReference databaseReference;
    Bundle bundle = new Bundle();

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_content);
        courseTitle = findViewById(R.id.courseTitle);
        databaseReference = FirebaseDatabase.getInstance().getReference();

        Intent intent = getIntent();
        loggedInStudentId = intent.getStringExtra("loggedInStudentId");
        selectedCourse = intent.getStringExtra("selectedCourse");
        selectedCourseText = findViewById(R.id.selectedCourse);
        selectedCourseText.setText(selectedCourse);
        databaseReference.child("Course").child(selectedCourse).child("title").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                assert snapshot.getValue() != null;
                courseTitle.setText(snapshot.getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });


        bundle.putString("loggedInStudentId", loggedInStudentId);
        bundle.putString("selectedCourse", selectedCourse);

        Log.d(TAG, "Setting up the initial Fragment...");
        animateActivity(courseTitle, selectedCourseText);
        ProfessorDetailsFragment professorDetailsFragment = new ProfessorDetailsFragment();
        professorDetailsFragment.setArguments(bundle);

        getSupportFragmentManager().beginTransaction().replace(R.id.selectedCourseSectionContainer, professorDetailsFragment).commit();
        courseDetailsSection = findViewById(R.id.courseDetailsSection);

        courseDetailsSection.setOnItemSelectedListener(item ->
        {
            Fragment temp = null;
            switch (item.getItemId()) {
                case R.id.professorDetailsSection:
                    temp = new ProfessorDetailsFragment();
                    bundle.putString("selectedCourse", selectedCourse);
                    temp.setArguments(bundle);
                    break;
                case R.id.lectureVideosSection:
                    temp = new VideosFragment();
                    bundle.putString("selectedCourse", selectedCourse);
                    temp.setArguments(bundle);
                    break;
                case R.id.helpingMaterialSection:
                    temp = new HelpingMaterialFragment();
                    bundle.putString("selectedCourse", selectedCourse);
                    temp.setArguments(bundle);
                    break;
                case R.id.notesSection:
                    temp = new NotesFragment();
                    bundle.putString("loggedInStudentId", loggedInStudentId);
                    bundle.putString("selectedCourse", selectedCourse);
                    temp.setArguments(bundle);
                    break;
                case R.id.DiscussionBoardSection:
                    temp = new DiscussionBoardFragment();
                    bundle.putString("loggedInStudentId", loggedInStudentId);
                    bundle.putString("selectedCourse", selectedCourse);
                    temp.setArguments(bundle);
            }
            if (temp != null) {
                Log.d(TAG, "Starting up the Required Fragment...");
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(R.animator.scalexy_enter, R.animator.scalexy_exit, R.animator.scalexy_enter, R.animator.scalexy_exit);
                fragmentTransaction.replace(R.id.selectedCourseSectionContainer, temp);
                fragmentTransaction.commit();
            }
            return true;
        });

        courseDetailsSection.setOnItemReselectedListener(item -> {

        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Animatoo.animateSwipeRight(this);
    }

    public void animateActivity(View animate1, View animate2) {
        final PropertyAction courseName = PropertyAction.newPropertyAction(animate1).translationY(-500).duration(400).alpha(0f).build();
        final PropertyAction course = PropertyAction.newPropertyAction(animate2).duration(300).alpha(0f).build();
        Player.init().
                animate(courseName).
                then().
                animate(course).
                play();
    }

}
