package edu.vu.ielearning.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.eftimoff.androidplayer.Player;
import com.eftimoff.androidplayer.actions.property.PropertyAction;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import edu.vu.ielearning.R;

public class HelpingMaterialFragment extends Fragment {
    static String TAG = "HelpingMaterialFragment";
    String selectedCourse;
    LinearLayout handouts;
    LinearLayout ppt;
    View lineBelowPpt;
    View lineBelowHandouts;
    DatabaseReference databaseReference;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public HelpingMaterialFragment() {
    }

    public static HelpingMaterialFragment newInstance(String param1, String param2) {
        HelpingMaterialFragment fragment = new HelpingMaterialFragment();
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
        View v = inflater.inflate(R.layout.fragment_helping_material, container, false);
        Bundle bundle = this.getArguments();
        assert bundle != null;
        selectedCourse = bundle.getString("selectedCourse");
        handouts = v.findViewById(R.id.handouts);
        lineBelowPpt = v.findViewById(R.id.lineBelowPpt);
        lineBelowHandouts = v.findViewById(R.id.lineBelowHandouts);
        ppt = v.findViewById(R.id.ppt);

        animate(handouts, ppt);

        setAvailability("handouts");
        setAvailability("ppt");

        return v;
    }

    public void setAvailability(String download) {
        Log.d(TAG, "Setting up firebase data...");
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Course").child(selectedCourse).child("hmaterial");
        databaseReference.child(download).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                assert snapshot.getValue() != null;
                if (snapshot.getValue().toString().equals("null")) {
                    if (download.equals("ppt")) {
                        ppt.setVisibility(View.GONE);
                        lineBelowPpt.setVisibility(View.GONE);
                    } else if (download.equals("handouts")) {
                        handouts.setVisibility(View.GONE);
                        lineBelowHandouts.setVisibility(View.GONE);
                    }
                } else if (download.equals("ppt")) {
                    ppt.setOnClickListener(v ->
                            goToURL(snapshot.getValue().toString()));
                } else if (download.equals("handouts")) {
                    handouts.setOnClickListener(v ->
                            goToURL(snapshot.getValue().toString()));
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });
    }

    void goToURL(String url) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    private void animate(View animate1, View animate2) {
        final PropertyAction material1 = PropertyAction.newPropertyAction(animate1).translationX(-500).duration(700).alpha(0f).build();
        final PropertyAction material2 = PropertyAction.newPropertyAction(animate2).translationX(-500).duration(700).alpha(0f).build();
        Player.init().
                animate(material1).
                then().
                animate(material2).
                play();
    }

}