package com.example.smartoffice.media;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.example.smartoffice.R;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ads.AdsMediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import static com.example.smartoffice.media.Fragment_Video.VIDEO_ROOT;

public class Activity_Video_Player extends AppCompatActivity
{
    private String TAG = "Activity_Video_Player";
    private PlayerView exoPlayerView;
    private SimpleExoPlayer player;

    private Boolean playWhenReady = true;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__video__player);

        // ViewFind
        exoPlayerView = findViewById(R.id.exoPlayerView);

        if (player == null)
        {

            player = ExoPlayerFactory.newSimpleInstance(this.getApplicationContext());

            //플레이어 연결
            exoPlayerView.setPlayer(player);
        }

        MediaSource mediaSource = buildMediaSource(Uri.parse(VIDEO_ROOT));

        //prepare
        player.prepare(mediaSource, true, false);

        //start,stop
        player.setPlayWhenReady(playWhenReady);

        DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(this.getApplicationContext());
        DefaultTrackSelector trackSelector = new DefaultTrackSelector();
        DefaultLoadControl loadControl = new DefaultLoadControl();

        player = ExoPlayerFactory.newSimpleInstance(
                this.getApplicationContext(),
                renderersFactory,
                trackSelector,
                loadControl);
    }

    private MediaSource buildMediaSource(Uri uri)
    {
        String userAgent = Util.getUserAgent(this, "blackJin");

        return new ExtractorMediaSource.Factory(new DefaultHttpDataSourceFactory(userAgent))
                .createMediaSource(uri);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        finish();
    }
}
