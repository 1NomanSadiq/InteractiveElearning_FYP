package edu.vu.ielearning.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.eftimoff.androidplayer.Player;
import com.eftimoff.androidplayer.actions.property.PropertyAction;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import edu.vu.ielearning.R;

public class ProfessorDetailsFragment extends Fragment {
    static final String TAG = "ProfessorDetails";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    ImageView profilePicture;
    TextView[] professorDetails = new TextView[5];
    String selectedCourse;
    DatabaseReference databaseReference;
    private String mParam1;
    private String mParam2;

    public ProfessorDetailsFragment() {
    }

    public static ProfessorDetailsFragment newInstance(String param1, String param2) {
        ProfessorDetailsFragment fragment = new ProfessorDetailsFragment();
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

    @SuppressLint("IntentReset")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_professor_detials, container, false);
        Bundle bundle = this.getArguments();
        assert bundle != null;
        selectedCourse = bundle.getString("selectedCourse");
        profilePicture = v.findViewById(R.id.profilePicture);
        professorDetails[0] = v.findViewById(R.id.name);
        professorDetails[1] = v.findViewById(R.id.study);
        professorDetails[2] = v.findViewById(R.id.uni);
        professorDetails[3] = v.findViewById(R.id.number);
        professorDetails[4] = v.findViewById(R.id.email);

        animateFragment(profilePicture, professorDetails[0], professorDetails[1], professorDetails[2], professorDetails[3], professorDetails[4]);

        Log.d(TAG, "Setting up data from firebase...");
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Teacher").child(selectedCourse);
        databaseReference.child("purl").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                String purl = snapshot.getValue(String.class);
                Glide.with(v.getContext()).load(purl).circleCrop().into(profilePicture);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });

        for (int i = 0; i < 5; i++) {
            int finalI = i;
            databaseReference.child(getTextViewId(i)).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    assert snapshot.getValue() != null;
                    professorDetails[finalI].setText(snapshot.getValue().toString());
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                }
            });
        }


        professorDetails[4].setOnClickListener(v1 -> {
            Intent i = new Intent(Intent.ACTION_SENDTO);
            i.setType("text/plain");
            i.setData(Uri.parse("mailto:"));
            i.setPackage("com.google.android.gm");
            i.putExtra(Intent.EXTRA_EMAIL, new String[]{professorDetails[4].getText().toString()});
            try {
                startActivity(Intent.createChooser(i, "Send mail..."));
            } catch (android.content.ActivityNotFoundException ex) {

                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/html");
                intent.setData(Uri.parse("mailto:"));
                String[] recipients = {professorDetails[4].getText().toString()};
                intent.putExtra(Intent.EXTRA_EMAIL, recipients);
                startActivity(Intent.createChooser(intent, "Send mail"));
            }
        });

        professorDetails[3].setOnClickListener(v12 -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + professorDetails[3].getText().toString()));
            startActivity(intent);
        });


        return v;

    }

    public String getTextViewId(int number) {
        return getResources().getResourceEntryName(professorDetails[number].getId());
    }

    private void animateFragment(View animate1, View animate2, View animate3, View animate4, View animate5, View animate6) {
        final PropertyAction pic = PropertyAction.newPropertyAction(animate1).translationY(-500).duration(150).alpha(0f).build();
        final PropertyAction name = PropertyAction.newPropertyAction(animate2).translationY(500).duration(150).alpha(0f).build();
        final PropertyAction study = PropertyAction.newPropertyAction(animate3).translationY(500).duration(150).alpha(0f).build();
        final PropertyAction uni = PropertyAction.newPropertyAction(animate4).translationY(500).duration(150).alpha(0f).build();
        final PropertyAction number = PropertyAction.newPropertyAction(animate5).translationY(500).duration(150).alpha(0f).build();
        final PropertyAction email = PropertyAction.newPropertyAction(animate6).translationY(500).duration(150).alpha(0f).build();
        Player.init().
                animate(pic).
                then().
                animate(name).
                then().
                animate(study).
                then().
                animate(uni).
                then().
                animate(number).
                then().
                animate(email).
                play();
    }

}