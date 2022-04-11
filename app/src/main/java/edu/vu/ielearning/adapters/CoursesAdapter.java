package edu.vu.ielearning.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
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

import edu.vu.ielearning.R;
import edu.vu.ielearning.activities.CourseContentActivity;

public class CoursesAdapter extends RecyclerView.Adapter<CoursesAdapter.ViewHolder> {
    String[] courses;
    String loggedInStudentId;

    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

    public CoursesAdapter(String[] courses, String loggedInStudentId) {
        this.courses = courses;
        this.loggedInStudentId = loggedInStudentId;
    }

    @NonNull
    @NotNull
    @Override
    public CoursesAdapter.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.course_selection_singlerow_design, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull CoursesAdapter.ViewHolder holder, int position) {
        holder.course.setText(courses[position]);
        databaseReference.child("Course").child(courses[position]).child("title").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                assert snapshot.getValue() != null;
                holder.courseName.setText(snapshot.getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });

        holder.viewCourse.setOnClickListener(v ->
        {
            String selectedCourse = courses[position].toLowerCase();

            Intent courseContent = new Intent(v.getContext(), CourseContentActivity.class);
            courseContent.putExtra("loggedInStudentId", loggedInStudentId);
            courseContent.putExtra("selectedCourse", selectedCourse);
            v.getContext().startActivity(courseContent);
            Animatoo.animateSwipeLeft(v.getContext());
        });
    }

    @Override
    public int getItemCount() {
        return courses.length;
    }

    private void animate(View animate2, View animate3, View animate4) {
        final PropertyAction courseName = PropertyAction.newPropertyAction(animate2).translationY(-500).duration(500).alpha(0f).build();
        final PropertyAction course = PropertyAction.newPropertyAction(animate3).translationY(-500).duration(500).alpha(0f).build();
        final PropertyAction viewCourse = PropertyAction.newPropertyAction(animate4).translationY(500).translationX(500).duration(500).alpha(0f).build();
        Player.init().
                animate(courseName).
                then().
                animate(course).
                then().
                animate(viewCourse).
                play();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView course;
        TextView courseName;
        Button viewCourse;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            course = itemView.findViewById(R.id.courseRec);
            courseName = itemView.findViewById(R.id.courseNameRec);
            viewCourse = itemView.findViewById(R.id.viewRec);
            animate(courseName, course, viewCourse);
        }
    }
}
