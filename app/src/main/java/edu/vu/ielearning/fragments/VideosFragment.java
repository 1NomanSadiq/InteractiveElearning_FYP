package edu.vu.ielearning.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Objects;

import edu.vu.ielearning.R;
import edu.vu.ielearning.adapters.VideosAdapter;

public class VideosFragment extends Fragment {
    final static String TAG = "VideosFragment";
    RecyclerView videosContainer;
    String[] videoIds;
    VideosAdapter videosAdapter;
    String selectedCourse;
    DatabaseReference databaseReference;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public VideosFragment() {
    }

    public static VideosFragment newInstance(String param1, String param2) {
        VideosFragment fragment = new VideosFragment();
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
        View v = inflater.inflate(R.layout.fragment_videos, container, false);
        Bundle bundle = this.getArguments();
        assert bundle != null;
        selectedCourse = bundle.getString("selectedCourse");

        videosContainer = v.findViewById(R.id.videosContainer);
        videosContainer.setLayoutManager(new LinearLayoutManager(requireActivity()));

        Log.d(TAG, "Accessing firebase...");
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Course").child(selectedCourse);
        databaseReference.child("videos").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Iterator<DataSnapshot> iterator = snapshot.getChildren().iterator();
                int length = (int) snapshot.getChildrenCount();
                videoIds = new String[length];
                for (int i = 0; i < length; i++) {
                    videoIds[i] = Objects.requireNonNull(iterator.next().getValue()).toString();
                }
                videosAdapter = new VideosAdapter(videoIds);
                videosContainer.setAdapter(videosAdapter);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });


        return v;
    }
}