package edu.vu.ielearning.fragments;

import android.app.Dialog;
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
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.text.util.LinkifyCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.himanshurawat.hasher.HashType;
import com.himanshurawat.hasher.Hasher;

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
import edu.vu.ielearning.adapters.models.NotesModel;
import es.dmoral.toasty.Toasty;

public class NotesFragment extends Fragment {
    static final String TAG = "NotesFragment";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    final Handler handler = new Handler(Looper.getMainLooper());
    FirebaseFirestore firestore;
    RecyclerView notesContainer;
    FirestoreRecyclerAdapter<NotesModel, NotesViewHolder> notesAdapter;
    StaggeredGridLayoutManager staggeredGridLayoutManager;
    Random mRandom = new Random(System.currentTimeMillis());
    String loggedInStudentId;
    String selectedCourse;
    int myColor;
    FloatingActionButton addNoteButton;
    private String mParam1;
    private String mParam2;

    public NotesFragment() {
    }

    public static NotesFragment newInstance(String param1, String param2) {
        NotesFragment fragment = new NotesFragment();
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
        View v = inflater.inflate(R.layout.fragment_notes, container, false);
        addNoteButton = v.findViewById(R.id.addNoteButton);

        Bundle bundle = this.getArguments();

        assert bundle != null;
        loggedInStudentId = bundle.getString("loggedInStudentId");
        selectedCourse = bundle.getString("selectedCourse");
        firestore = FirebaseFirestore.getInstance();
        notesContainer = v.findViewById(R.id.notesContainer);
        staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        notesContainer.setLayoutManager(staggeredGridLayoutManager);
        displayAllNotes();

        addNoteButton.setOnClickListener(v1 -> showAddNoteDialog());

        return v;
    }

    public void displayAllNotes() {
        Log.d(TAG, "Displaying notes...");
        Query query = firestore.collection("Notes").document(Hasher.Companion.hash(loggedInStudentId + "_" + selectedCourse, HashType.SHA_384)).collection("userNotes").orderBy("createdOn", Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<NotesModel> options = new FirestoreRecyclerOptions.Builder<NotesModel>().setQuery(query, NotesModel.class).build();
        notesAdapter = new FirestoreRecyclerAdapter<NotesModel, NotesViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull @NotNull NotesViewHolder holder, int position, @NonNull @NotNull NotesModel model) {
                holder.noteColor.setBackgroundColor(model.getColor());
                holder.noteTitle.setText(model.getNoteTitle());
                holder.noteContent.setText(model.getNoteContent());
                if (timeAgo(model.getDate()).equals("0 minutes ago"))
                    holder.noteDate.setText(R.string.just_now);
                else
                    holder.noteDate.setText(timeAgo(model.getDate()));

                String documentId = notesAdapter.getSnapshots().getSnapshot(position).getId();

                holder.itemView.setOnClickListener(v -> showViewNoteDialog(model.getNoteTitle(), model.getNoteContent(), "Created on " + model.getDate(), model.getColor()));

                holder.noteMenu.setOnClickListener(v ->
                {
                    PopupMenu popupMenu = new PopupMenu(requireActivity(), v);
                    popupMenu.getMenu().add("Edit").setOnMenuItemClickListener(item ->
                    {
                        showEditNoteDialog(model.getNoteTitle(), model.getNoteContent(), documentId, model.getColor());
                        return false;
                    });

                    popupMenu.getMenu().add("Delete").setOnMenuItemClickListener(item ->
                    {
                        showDeleteNoteDialog(model.getNoteTitle(), model.getNoteContent(), documentId, model.getColor());
                        return false;
                    });

                    popupMenu.show();
                });

            }

            @NonNull
            @NotNull
            @Override
            public NotesViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_design, parent, false);
                return new NotesViewHolder(view);
            }
        };


        notesContainer.setAdapter(notesAdapter);
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

    public void showAddNoteDialog() {
        myColor = generateRandomLightColor();
        Dialog dialog = new Dialog(requireActivity(), R.style.WideDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.note_add_dialog);
        EditText inputNoteTitle = dialog.findViewById(R.id.noteTitle_AddNote);
        EditText inputNoteContent = dialog.findViewById(R.id.noteContent_AddNote);
        inputNoteContent.setMovementMethod(new ScrollingMovementMethod());
        FloatingActionButton addButton = dialog.findViewById(R.id.add_button);
        addButton.setOnClickListener(v ->
        {
            String typedNoteTitle = inputNoteTitle.getText().toString().trim();
            String typedNoteContent = inputNoteContent.getText().toString().trim();
            if (typedNoteContent.isEmpty()) {
                Toasty.info(requireActivity(), "Content is Required", Toast.LENGTH_SHORT).show();
            } else {

                DocumentReference documentReference = firestore.collection("Notes").document(Hasher.Companion.hash(loggedInStudentId + "_" + selectedCourse, HashType.SHA_384)).collection("userNotes").document();
                Map<String, Object> note = new HashMap<>();
                SimpleDateFormat inputFormat = new SimpleDateFormat("E, dd MMM, yyyy 'at' h:mm a", Locale.getDefault());
                Date now = new Date();
                String date = inputFormat.format(now);
                if (typedNoteTitle.isEmpty()) {
                    typedNoteTitle = "Untitled";
                }
                note.put("noteTitle", typedNoteTitle);
                note.put("noteContent", typedNoteContent);
                note.put("date", date);
                note.put("createdOn", FieldValue.serverTimestamp());
                note.put("color", myColor);
                documentReference.set(note).addOnSuccessListener(unused -> Toasty.success(requireActivity(), "Note Added", Toast.LENGTH_SHORT).show()).addOnFailureListener(e -> Toasty.error(requireActivity(), "Failed to add a Note", Toast.LENGTH_SHORT).show());
                dialog.dismiss();
                handler.postDelayed(() -> notesContainer.smoothScrollToPosition(0), 500);
            }
        });

        dialog.show();
        inputNoteContent.setFocusableInTouchMode(true);
        inputNoteContent.requestFocus();
        dialog.getWindow().findViewById(R.id.addNote_color).setBackgroundColor(myColor);
    }


    public void showViewNoteDialog(String title, String content, String notedate, int color) {
        Dialog dialog = new Dialog(requireActivity(), R.style.WideDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.note_view_dialog);
        TextView noteTitle = dialog.findViewById(R.id.noteTitle_ViewNote);
        TextView noteContent = dialog.findViewById(R.id.noteContent_ViewNote);
        noteContent.setMovementMethod(new ScrollingMovementMethod());
        TextView noteDate = dialog.findViewById(R.id.noteDate_ViewNote);
        noteTitle.setText(title);
        noteContent.setText(content);
        noteDate.setText(notedate);
        LinkifyCompat.addLinks(noteTitle, Linkify.ALL);
        LinkifyCompat.addLinks(noteContent, Linkify.ALL);
        dialog.show();
        dialog.getWindow().findViewById(R.id.viewNote_color).setBackgroundColor(color);
    }


    public void showEditNoteDialog(String title, String content, String documentId, int color) {
        Dialog dialog = new Dialog(requireActivity(), R.style.WideDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.note_edit_dialog);
        EditText inputNoteTitle = dialog.findViewById(R.id.noteTitle_EditNote);
        EditText inputNoteContent = dialog.findViewById(R.id.noteContent_EditNote);
        inputNoteContent.setMovementMethod(new ScrollingMovementMethod());
        inputNoteTitle.setText(title);
        inputNoteContent.setText(content);
        FloatingActionButton updateButton = dialog.findViewById(R.id.update_button);

        updateButton.setOnClickListener(v ->
        {
            Log.d(TAG, "Editing the note...");
            String typedNoteTitle = inputNoteTitle.getText().toString().trim();
            String typedNoteContent = inputNoteContent.getText().toString().trim();
            if (typedNoteTitle.isEmpty() || typedNoteContent.isEmpty()) {
                Toasty.info(requireActivity(), "Both Fields are Required", Toast.LENGTH_SHORT).show();
            } else {
                DocumentReference documentReference = firestore.collection("Notes").document(Hasher.Companion.hash(loggedInStudentId + "_" + selectedCourse, HashType.SHA_384)).collection("userNotes").document(documentId);
                Map<String, Object> note = new HashMap<>();
                note.put("noteTitle", typedNoteTitle);
                note.put("noteContent", typedNoteContent);
                documentReference.update(note).addOnSuccessListener(unused -> Toasty.success(requireActivity(), "Note Updated", Toast.LENGTH_SHORT).show()).addOnFailureListener(e -> Toasty.error(requireActivity(), "Failed to Update", Toast.LENGTH_SHORT).show());
                dialog.dismiss();
            }
        });
        dialog.show();
        dialog.getWindow().findViewById(R.id.editNote_color).setBackgroundColor(color);
        inputNoteContent.setFocusableInTouchMode(true);
        inputNoteContent.requestFocus();
    }

    public void showDeleteNoteDialog(String title, String content, String documentId, int color) {
        Dialog dialog = new Dialog(requireActivity(), R.style.WideDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.note_delete_dialog);
        TextView noteTitle = dialog.findViewById(R.id.noteTitle_DeleteNote);
        TextView noteContent = dialog.findViewById(R.id.noteContent_DeleteNote);
        noteContent.setMovementMethod(new ScrollingMovementMethod());
        FloatingActionButton deleteButton = dialog.findViewById(R.id.delete_button);
        noteTitle.setText(title);
        LinkifyCompat.addLinks(noteTitle, Linkify.ALL);
        noteContent.setText(content);
        LinkifyCompat.addLinks(noteContent, Linkify.ALL);
        deleteButton.setOnClickListener(v ->
        {
            Log.d(TAG, "Deleting the note...");
            DocumentReference documentReference = firestore.collection("Notes").document(Hasher.Companion.hash(loggedInStudentId + "_" + selectedCourse, HashType.SHA_384)).collection("userNotes").document(documentId);
            documentReference.delete().addOnSuccessListener(unused -> Toasty.success(requireActivity(), "Note Deleted", Toast.LENGTH_SHORT).show()).addOnFailureListener(e -> Toast.makeText(requireActivity(), "Failed to Delete", Toast.LENGTH_SHORT).show());
            dialog.dismiss();
        });
        dialog.show();
        dialog.getWindow().findViewById(R.id.deleteNote_color).setBackgroundColor(color);
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
        notesAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (notesAdapter != null)
            notesAdapter.stopListening();
    }


    private static class NotesViewHolder extends RecyclerView.ViewHolder {
        TextView noteTitle;
        TextView noteContent;
        TextView noteDate;
        ImageView noteMenu;
        LinearLayout noteColor;

        public NotesViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            noteTitle = itemView.findViewById(R.id.noteTitle);
            noteContent = itemView.findViewById(R.id.noteContent);
            noteDate = itemView.findViewById(R.id.noteDate);
            noteColor = itemView.findViewById(R.id.noteColor);
            noteMenu = itemView.findViewById(R.id.noteMenu);
        }
    }


}