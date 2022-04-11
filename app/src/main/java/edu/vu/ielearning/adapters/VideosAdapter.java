package edu.vu.ielearning.adapters;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.eftimoff.androidplayer.Player;
import com.eftimoff.androidplayer.actions.property.PropertyAction;

import org.jetbrains.annotations.NotNull;

import edu.vu.ielearning.R;

public class VideosAdapter extends RecyclerView.Adapter<VideosAdapter.ViewHolder> {

    String[] videoIds;

    public VideosAdapter(String[] videoIds) {
        this.videoIds = videoIds;
    }

    @NonNull
    @NotNull
    @Override
    public VideosAdapter.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.lecture_videos_singlerow_design, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull @NotNull VideosAdapter.ViewHolder holder, int position) {
        holder.webView.setVisibility(View.GONE);
        holder.lectureNumber.setText("Lecture " + (position + 1));
        String videoId;
        videoId = videoIds[position];
        String frameVideo = "<html><body><center><iframe width=\"1280\" height=\"720\" src=\"https://www.youtube.com/embed/" + videoId + "\" frameborder=\"0\" allowfullscreen></iframe></body></html>";
        holder.lectureNumber.setOnClickListener(v ->
        {
            if (holder.webView.getVisibility() == View.VISIBLE) {
                holder.webView.loadData("", "text/html", "utf-8");
                holder.webView.setVisibility(View.GONE);
            } else {
                holder.webView.loadData(frameVideo, "text/html", "utf-8");
                holder.webView.setVisibility(View.VISIBLE);
            }

        });
    }

    @Override
    public int getItemCount() {
        return videoIds.length;
    }

    private void animate(View animate1) {
        final PropertyAction button = PropertyAction.newPropertyAction(animate1).translationX(-500).duration(500).alpha(0f).build();
        Player.init().
                animate(button).
                play();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView lectureNumber;
        WebView webView;

        @SuppressLint("SetJavaScriptEnabled")
        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            webView = itemView.findViewById(R.id.mWebView);
            lectureNumber = itemView.findViewById(R.id.lectureNumber);
            webView.setWebChromeClient(new WebChromeClient());
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setAppCacheEnabled(true);
            webView.setInitialScale(1);
            webView.getSettings().setLoadWithOverviewMode(true);
            webView.getSettings().setUseWideViewPort(true);
            webView.setBackgroundColor(Color.parseColor("#27182D"));
            animate(lectureNumber);
        }
    }
}
