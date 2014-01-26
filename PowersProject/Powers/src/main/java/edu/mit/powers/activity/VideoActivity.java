package edu.mit.powers.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.WindowManager;
import android.widget.VideoView;

import java.io.IOException;

import edu.mit.powers.Log;
import edu.mit.powers.R;

public class VideoActivity extends Activity{

        String filePath;

        static MediaPlayer mediaPlayer;
        static VideoActivity thisActivity;

    // Apparently there's a bug in 4.0-4.1 where if you don't hold onto this it can get GC'd before the video plays.
    SurfaceHolder sh;
    Surface sv;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_video);
            thisActivity = this;

            //Making VideoActivity extend PowersView produced other bugs, so I just reused this flag
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            filePath = getIntent().getStringExtra(Intent.EXTRA_SUBJECT);
            final boolean loop = getIntent().getBooleanExtra(Intent.EXTRA_KEY_EVENT, false);

            final VideoView videoView = (VideoView)findViewById(R.id.video_view);
            mediaPlayer = new MediaPlayer();

            sv = videoView.getHolder().getSurface();
            sh = videoView.getHolder();

            try {
                mediaPlayer.setDataSource(filePath);
                int currentapiVersion = android.os.Build.VERSION.SDK_INT;
                if (currentapiVersion >= Build.VERSION_CODES.JELLY_BEAN){
                    mediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
                }
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                    @TargetApi(Build.VERSION_CODES.JELLY_BEAN) //The video scaling method only works with jelly bean and above
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        Log.info("Video ready, beginning playback.");
                        try {

                            mediaPlayer.setDisplay(sh);



                            mediaPlayer.start();
                        } catch(IllegalArgumentException iex) {
                            Log.error(iex, "Could not play video");
                            finish();
                        }
                    }
                });
                mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    @Override
                    public boolean onError(MediaPlayer mp, int what, int extra) {
                        Log.info("Video error %d:%d", what, extra);
                        finish();
                        return true;
                    }
                });
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        if(loop) {
                            Log.info("Looping");
                            mediaPlayer.start();
                        } else {
                            Log.info("Done playing");
                            finish();
                        }
                    }
                });
                mediaPlayer.prepareAsync();
            } catch (IllegalArgumentException|
                     IllegalStateException   |
                     IOException e) {
                Log.warn(e, "Exception playing video '%s'", filePath);
                finish();
            }

        }

        public static void stop(){
            if(mediaPlayer != null) {
                mediaPlayer.release();
            }
            if(thisActivity != null) {
                thisActivity.finish();
            }
        }

    @Override
    protected void onPause() {
        super.onPause();
        stop();
        finish();
    }
}
