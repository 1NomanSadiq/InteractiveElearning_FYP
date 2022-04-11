package edu.vu.ielearning.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.format.DateUtils;
import android.text.method.ScrollingMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.util.LinkifyCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import edu.vu.ielearning.R;
import edu.vu.ielearning.adapters.models.StudentRepliesModel;

public class StudentsDiscussionActivity extends AppCompatActivity {
    static final String TAG = "StudentsDiscussion";
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    Random mRandom = new Random(System.currentTimeMillis());
    FirebaseFirestore firestore;
    FirestoreRecyclerAdapter<StudentRepliesModel, StudentsDiscussionActivity.ReplyHolder> adapter;
    final Handler handler = new Handler(Looper.getMainLooper());
    String loggedInStudentId;
    String selectedCourse;
    String documentId;
    String getQuestionSubject;
    String getQuestionAsked;
    String getQuestionStudentId;
    String getDateAsked;
    TextView selectedCourseName;
    TextView course;
    TextView questionStudentId;
    TextView questionAsked;
    TextView dateAsked;
    TextView questionSubject;
    EditText reply;
    FloatingActionButton sendButton;
    RecyclerView repliesContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_discussion);


        Intent intent = getIntent();
        dateAsked = findViewById(R.id.dateAsked_);
        questionAsked = findViewById(R.id.questionAsked_);
        questionAsked.setMovementMethod(new ScrollingMovementMethod());
        reply = findViewById(R.id.reply_);
        reply.setMovementMethod(new ScrollingMovementMethod());
        questionStudentId = findViewById(R.id.questionStudentId_);
        questionSubject = findViewById(R.id.questionSubject_);
        sendButton = findViewById(R.id.sendButton);
        selectedCourseName = findViewById(R.id.selectedCourseName);
        course = findViewById(R.id.course);
        repliesContainer = findViewById(R.id.repliesContainer);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        repliesContainer.setLayoutManager(layoutManager);
        firestore = FirebaseFirestore.getInstance();

        Log.d(TAG, "Gathering intent data...");
        loggedInStudentId = intent.getStringExtra("loggedInStudentId");
        selectedCourse = intent.getStringExtra("selectedCourse");
        documentId = intent.getStringExtra("documentId");
        getQuestionSubject = intent.getStringExtra("subject");
        getQuestionAsked = intent.getStringExtra("question");
        getQuestionStudentId = intent.getStringExtra("questionStudentId");
        getDateAsked = intent.getStringExtra("dateAsked");

        Log.d(TAG, "Setting up the database...");
        Query query = firestore.collection("Question").document(selectedCourse).collection("question").document(documentId).collection("replies").orderBy("createdOn", Query.Direction.ASCENDING);
        FirestoreRecyclerOptions<StudentRepliesModel> options = new FirestoreRecyclerOptions.Builder<StudentRepliesModel>().setQuery(query, StudentRepliesModel.class).build();
        adapter = new FirestoreRecyclerAdapter<StudentRepliesModel, ReplyHolder>(options) {
            @SuppressLint("SetTextI18n")
            @Override
            protected void onBindViewHolder(@NonNull @NotNull ReplyHolder holder, int position, @NonNull @NotNull StudentRepliesModel model) {
                if (timeAgo(model.getReplied_at()).equals("0 minutes ago"))
                    holder.dateAsked.setText("Just Now");
                else
                    holder.dateAsked.setText(timeAgo(model.getReplied_at()));

                holder.reply.setText(model.getReply());
                LinkifyCompat.addLinks(holder.reply, Linkify.ALL);
                holder.questionStudentId.setText(model.getStudent_id());

                if (model.getStudent_id().equals(loggedInStudentId))
                    holder.replyColor.setBackgroundColor(Color.WHITE);
                else
                    holder.replyColor.setBackgroundColor(generateRandomLightColor());
            }

            @NonNull
            @NotNull
            @Override
            public ReplyHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.db_show_replies_singlerow, parent, false);
                return new ReplyHolder(view);
            }
        };

        repliesContainer.setAdapter(adapter);

        handler.postDelayed(() -> repliesContainer.smoothScrollToPosition(adapter.getItemCount()), 1000);

        databaseReference.child("Course").child(selectedCourse).child("title").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                assert snapshot.getValue() != null;
                selectedCourseName.setText(snapshot.getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });

        Log.d(TAG, "Setting the values of texts...");
        course.setText(selectedCourse);
        questionStudentId.setText(getQuestionStudentId);
        questionSubject.setText(getQuestionSubject);
        questionAsked.setText(getQuestionAsked);
        LinkifyCompat.addLinks(questionAsked, Linkify.ALL);
        dateAsked.setText(getDateAsked);
        reply.setOnClickListener(v -> handler.postDelayed(() -> repliesContainer.smoothScrollToPosition(adapter.getItemCount()), 500));

        sendButton.setOnClickListener(v -> {
            String reply = this.reply.getText().toString().trim();
            if (!reply.isEmpty()) {
                addReply(reply);
            }
            this.reply.setText("");
            handler.postDelayed(() -> repliesContainer.smoothScrollToPosition(adapter.getItemCount()), 500);
        });


    }


    public void addReply(String reply) {

        Log.d(TAG, "Adding a reply...");

        DocumentReference documentReference = FirebaseFirestore.getInstance().collection("Question").document(selectedCourse).collection("question").document(documentId).collection("replies").document();
        Map<String, Object> replyData = new HashMap<>();
        SimpleDateFormat inputFormat = new SimpleDateFormat("E, dd MMM, yyyy 'at' h:mm a", Locale.getDefault());
        Date now = new Date();
        String date = inputFormat.format(now);
        replyData.put("student_id", loggedInStudentId);
        replyData.put("replied_at", date);
        replyData.put("createdOn", FieldValue.serverTimestamp());
        replyData.put("reply", reply);
        documentReference.set(replyData);
    }


    String timeAgo(String pastDate) {
        SimpleDateFormat inputFormat = new SimpleDateFormat("E, dd MMM, yyyy 'at' h:mm a", Locale.getDefault());
        Date date = null;
        try {
            date = inputFormat.parse(pastDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        assert date != null;
        return DateUtils.getRelativeTimeSpanString(date.getTime(), System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS).toString();
    }

    public int generateRandomLightColor() {
        final int baseColor = Color.WHITE;
        final int baseRed = Color.red(baseColor);
        final int baseGreen = Color.green(baseColor);
        final int baseBlue = Color.blue(baseColor);
        final int red = (baseRed + mRandom.nextInt(256)) / 2;
        final int green = (baseGreen + mRandom.nextInt(256)) / 2;
        final int blue = (baseBlue + mRandom.nextInt(256)) / 2;
        return Color.rgb(red, green, blue);
    }


    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Animatoo.animateSwipeRight(this);
    }

    static class ReplyHolder extends RecyclerView.ViewHolder {
        TextView dateAsked;
        TextView questionStudentId;
        TextView reply;
        LinearLayout replyColor;

        public ReplyHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            dateAsked = itemView.findViewById(R.id.db_date);
            questionStudentId = itemView.findViewById(R.id.db_student_id);
            reply = itemView.findViewById(R.id.db_reply);
            replyColor = itemView.findViewById(R.id.replyColor);
        }
    }

}