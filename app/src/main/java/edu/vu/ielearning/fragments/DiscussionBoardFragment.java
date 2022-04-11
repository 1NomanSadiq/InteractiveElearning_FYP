package edu.vu.ielearning.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.text.method.ScrollingMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.text.util.LinkifyCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import edu.vu.ielearning.R;
import edu.vu.ielearning.activities.StudentsDiscussionActivity;
import edu.vu.ielearning.adapters.models.DiscussionBoardQuestionModel;
import es.dmoral.toasty.Toasty;

public class DiscussionBoardFragment extends Fragment {
    static final String TAG = "DiscussionBoardFragment";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    Button newQuestion;
    RecyclerView questionsContainer;
    String loggedInStudentId;
    String selectedCourse;
    Random mRandom = new Random(System.currentTimeMillis());

    FirebaseFirestore firestore;
    FirestoreRecyclerAdapter<DiscussionBoardQuestionModel, DiscussionBoardFragment.QuestionHolder> adapter;
    private String mParam1;
    private String mParam2;

    public DiscussionBoardFragment() {
    }

    public static DiscussionBoardFragment newInstance(String param1, String param2) {
        DiscussionBoardFragment fragment = new DiscussionBoardFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_discussion_board, container, false);
        newQuestion = v.findViewById(R.id.newQuestion);
        firestore = FirebaseFirestore.getInstance();
        questionsContainer = v.findViewById(R.id.questionsContainer);
        questionsContainer.setLayoutManager(new LinearLayoutManager(requireActivity()));

        Log.d(TAG, "Receiving data from Bundle...");
        Bundle bundle = this.getArguments();
        assert bundle != null;
        loggedInStudentId = bundle.getString("loggedInStudentId");
        selectedCourse = bundle.getString("selectedCourse");
        newQuestion.setOnClickListener(v1 -> addNewQuestion());
        displayAllQuestions();


        return v;

    }

    public void displayAllQuestions() {
        Log.d(TAG, "Accessing firestore database...");
        Query query = firestore.collection("Question").document(selectedCourse).collection("question").orderBy("createdOn", Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<DiscussionBoardQuestionModel> options = new FirestoreRecyclerOptions.Builder<DiscussionBoardQuestionModel>().setQuery(query, DiscussionBoardQuestionModel.class).build();
        adapter = new FirestoreRecyclerAdapter<DiscussionBoardQuestionModel, QuestionHolder>(options) {
            @SuppressLint("SetTextI18n")
            @Override
            protected void onBindViewHolder(@NonNull @NotNull QuestionHolder holder, int position, @NonNull @NotNull DiscussionBoardQuestionModel model) {
                holder.gone1.setVisibility(View.GONE);
                holder.gone2.setVisibility(View.GONE);
                holder.gone3.setVisibility(View.GONE);
                holder.questionAsked.setVisibility(View.GONE);

                holder.questionSubject.setText(model.getQuestion_subject());
                String answer = model.getAnswer().replace("<br>", "\n");
                holder.answer.setText(answer);
                LinkifyCompat.addLinks(holder.answer, Linkify.ALL);
                if (timeAgo(model.getAsked_at()).equals("0 minutes ago"))
                    holder.dateAsked.setText(R.string.just_now);
                else
                    holder.dateAsked.setText(timeAgo(model.getAsked_at()));
                holder.questionAsked.setText(model.getQuestion());
                LinkifyCompat.addLinks(holder.questionAsked, Linkify.ALL);
                holder.questionStudentId.setText(model.getStudent_id());
                String documentId = adapter.getSnapshots().getSnapshot(position).getId();


                if (model.getStudent_id().equals(loggedInStudentId)) {
                    holder.questionSubject.setBackgroundColor(Color.parseColor("#4CAF50"));
                    holder.questionStudentId.setBackgroundColor(Color.parseColor("#4CAF50"));
                    holder.dateAsked.setBackgroundColor(Color.parseColor("#4CAF50"));
                } else {
                    holder.questionSubject.setBackgroundColor(Color.parseColor("#6583FE"));
                    holder.questionStudentId.setBackgroundColor(Color.parseColor("#6583FE"));
                    holder.dateAsked.setBackgroundColor(Color.parseColor("#6583FE"));
                }


                holder.visible1.setOnClickListener(v -> {
                    if (holder.gone1.getVisibility() == View.GONE) {
                        holder.gone1.setVisibility(View.VISIBLE);
                        holder.questionAsked.setVisibility(View.VISIBLE);
                        holder.gone2.setVisibility(View.VISIBLE);
                        holder.gone3.setVisibility(View.VISIBLE);
                    } else {
                        holder.gone1.setVisibility(View.GONE);
                        holder.gone2.setVisibility(View.GONE);
                        holder.gone3.setVisibility(View.GONE);
                        holder.questionAsked.setVisibility(View.GONE);
                    }

                });

                holder.questionSubject.setOnClickListener(v -> {
                    if (holder.gone1.getVisibility() == View.GONE) {
                        holder.gone1.setVisibility(View.VISIBLE);
                        holder.questionAsked.setVisibility(View.VISIBLE);
                        holder.gone2.setVisibility(View.VISIBLE);
                        holder.gone3.setVisibility(View.VISIBLE);
                    } else {
                        holder.gone1.setVisibility(View.GONE);
                        holder.gone2.setVisibility(View.GONE);
                        holder.gone3.setVisibility(View.GONE);
                        holder.questionAsked.setVisibility(View.GONE);
                    }
                });

                firestore.collection("Question").document(selectedCourse).collection("question").document(documentId).collection("replies").get().addOnCompleteListener(task -> {
                    assert task.getResult() != null;
                    int count = task.getResult().size();
                    holder.viewReply.setText("View Other Student's Replies (" + count + ")");
                });


                holder.viewReply.setOnClickListener(v -> {
                    Intent studentsDiscussion = new Intent(requireActivity(), StudentsDiscussionActivity.class);
                    studentsDiscussion.putExtra("loggedInStudentId", loggedInStudentId);
                    studentsDiscussion.putExtra("selectedCourse", selectedCourse);
                    studentsDiscussion.putExtra("documentId", documentId);
                    studentsDiscussion.putExtra("subject", model.getQuestion_subject());
                    studentsDiscussion.putExtra("question", model.getQuestion());
                    studentsDiscussion.putExtra("questionStudentId", model.getStudent_id());
                    studentsDiscussion.putExtra("dateAsked", model.getAsked_at());
                    startActivity(studentsDiscussion);
                    Animatoo.animateSwipeLeft(requireActivity());
                });

            }

            @NonNull
            @NotNull
            @Override
            public QuestionHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.design_discussion_board_single_row, parent, false);
                return new QuestionHolder(view);
            }

        };

        questionsContainer.setAdapter(adapter);
    }


    public void addNewQuestion() {
        Log.d(TAG, "Adding a new Question...");
        Dialog dialog = new Dialog(requireActivity(), R.style.WideDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.db_ask_question_dialog);
        EditText inputQuestionSubject = dialog.findViewById(R.id.questionSubject_addQuestion);
        EditText inputQuestionContent = dialog.findViewById(R.id.questionContent_addQuestion);
        inputQuestionContent.setMovementMethod(new ScrollingMovementMethod());
        FloatingActionButton sendButton = dialog.findViewById(R.id.send_button);
        sendButton.setOnClickListener(v ->
        {
            String typedQuestionSubject = inputQuestionSubject.getText().toString();
            String typedQuestionContent = inputQuestionContent.getText().toString();
            if (typedQuestionSubject.equals("") || typedQuestionContent.equals("")) {
                Toasty.info(requireActivity(), "Both Fields are Required", Toast.LENGTH_SHORT).show();
            } else {

                DocumentReference documentReference = firestore.collection("Question").document(selectedCourse).collection("question").document();
                Map<String, Object> question = new HashMap<>();
                SimpleDateFormat inputFormat = new SimpleDateFormat("E, dd MMM, yyyy 'at' h:mm a", Locale.getDefault());
                Date now = new Date();
                String date = inputFormat.format(now);

                question.put("question_subject", typedQuestionSubject);
                question.put("student_id", loggedInStudentId);
                question.put("asked_at", date);
                question.put("createdOn", FieldValue.serverTimestamp());
                question.put("question", typedQuestionContent);
                question.put("answer", "Not answered yet");

                documentReference.set(question).addOnSuccessListener(unused -> Toasty.success(requireActivity(), "Success", Toast.LENGTH_SHORT).show()).addOnFailureListener(e -> Toasty.error(requireActivity(), "An error occurred", Toast.LENGTH_SHORT).show());
                dialog.dismiss();
            }
        });


        dialog.show();
        inputQuestionSubject.requestFocus();
        dialog.getWindow().findViewById(R.id.questionAdd).setBackgroundColor(generateRandomLightColor());
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


    String timeAgo(String pastDate) {
        final SimpleDateFormat inputFormat = new SimpleDateFormat("E, dd MMM, yyyy 'at' h:mm a", Locale.getDefault());
        Date date = null;
        try {
            date = inputFormat.parse(pastDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        assert date != null;
        return DateUtils.getRelativeTimeSpanString(date.getTime(), Calendar.getInstance().getTimeInMillis(), DateUtils.MINUTE_IN_MILLIS).toString();
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null)
            adapter.stopListening();
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    private static class QuestionHolder extends RecyclerView.ViewHolder {
        TextView questionSubject;
        TextView questionStudentId;
        TextView dateAsked;
        TextView viewReply;
        TextView answer;
        TextView questionAsked;
        LinearLayout visible1;
        LinearLayout gone1;
        LinearLayout gone2;
        LinearLayout gone3;

        public QuestionHolder(View itemView) {
            super(itemView);
            questionSubject = itemView.findViewById(R.id.questionSubject);
            questionStudentId = itemView.findViewById(R.id.questionStudentId);
            dateAsked = itemView.findViewById(R.id.dateAsked);
            viewReply = itemView.findViewById(R.id.viewReply);
            answer = itemView.findViewById(R.id.answer);
            questionAsked = itemView.findViewById(R.id.questionAsked);
            visible1 = itemView.findViewById(R.id.visible1);
            gone1 = itemView.findViewById(R.id.gone1);
            gone2 = itemView.findViewById(R.id.gone2);
            gone3 = itemView.findViewById(R.id.gone3);
        }
    }

}