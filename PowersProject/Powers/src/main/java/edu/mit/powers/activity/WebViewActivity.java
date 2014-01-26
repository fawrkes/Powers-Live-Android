package edu.mit.powers.activity;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.View;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;

import com.mogoweb.chrome.WebChromeClient;
import com.mogoweb.chrome.WebSettings;
import com.mogoweb.chrome.WebView;

import java.io.IOException;

import edu.mit.powers.Log;
import edu.mit.powers.media.Cue;
import edu.mit.powers.net.packets.TriggersPacket;

public class WebViewActivity extends PowersView
{
    private String lastVideoCue;
    private WebView chromeView;
    private Runnable nextCue;
    private Handler mainThread;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mainThread = new Handler();
        chromeView = new WebView(this);
        setContentView(chromeView);
        setupContent();
    }

    private void setupContent()
    {
        WebSettings settings = chromeView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        chromeView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        chromeView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.info("%s", consoleMessage.message());
                return super.onConsoleMessage(consoleMessage);
            }
        });
        chromeView.setWebViewClient(new LocalWebViewClient(application.contentManager));
        String addr = String.format("%s?uuid=%s&location=%s",
                application.getWebviewAddress(),
                application.device.uuid,
                application.getVenue().id);
        Log.info("Opening %s", addr);
        chromeView.loadUrl(addr);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        chromeView.reload();
    }

    public void executeTrigger(TriggersPacket triggerPacket)
    {
        Log.info("Packet: %s", triggerPacket.toString());

        for(String trigger : triggerPacket.triggers) {
            runCue(trigger);
        }
    }

    private synchronized void scheduleNextCue(final String cue, long delay)
    {
        if(nextCue != null) {
            mainThread.removeCallbacks(nextCue);
        }
        if(cue == null) {
            return;
        }
        if(delay < 0) {
            Log.warn("Negative delay, %d", delay);
            delay = 0;
        }
        Log.info("Queueing next cue = '%s' after %d ms", cue, delay);
        nextCue = new Runnable() {
            @Override
            public void run() {
                nextCue = null;
                Log.info("Running queued cue '%s'", cue);
                runCue(cue);
            }
        };
        mainThread.postDelayed(nextCue, delay);
    }

    private void runCue(String trigger)
    {
        Log.info("Running cue for trigger '%s'", trigger);
        Cue cue = application.cueForTrigger(trigger);

        if(cue == null) {
            Log.info("\tCue not found");
            return;
        }

        // Needs to be before probability.
        setDim(cue.device != null && cue.device.dim);

        if(cue.probabilistic && !cue.shouldRun()) {
            if(cue.next != null) {
                runCue(cue.next);
            }
            Log.info("\tCue failed probability test");
            return;
        }
        if(cue.delay > 0 && cue.next != null) {
            scheduleNextCue(cue.next, (long) (cue.delay * 1000));
            Log.info("\tScheduled next after %f s", cue.delay);
        }

        if(cue.device != null) {
            runDeviceCues(cue.device);
        } else {
            cutVideo();
        }
    }

    private void runDeviceCues(Cue.DeviceCue cue)
    {
        Log.info("\tRunning device cues");

        if(cue.audio != null) {
            playAudio(cue.audio);
        }

        if(cue.video != null) {
            playVideo(cue.video, cue.loop);
        } else {
            cutVideo();
        }

        // Vibrate
        if(cue.vibrate) {
            vibrate(cue.duration);
        }
    }

    private void setDim(boolean dim)
    {
        float brightness;
        if (dim){
            brightness = 0.2f;
        } else {
            brightness = 1.0f;
        }
        Log.info("Setting brightness to %f", brightness);
        WindowManager.LayoutParams lparams = getWindow().getAttributes();
        lparams.screenBrightness = brightness;
        getWindow().setAttributes(lparams);
    }

    private void playAudio(final String audioFileName)
    {
        Log.info("Playing audio '%s'", audioFileName);
        MediaPlayer mp = new MediaPlayer();
        try {
            String file = application.contentManager.pathForFile(audioFileName);
            if(file == null) {
                Log.warn("Could not find audio file '%s'", audioFileName);
                return;
            }
            mp.setDataSource(file);
            mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    Log.info("Audio %s prepared, playing", audioFileName);
                    mp.start();
                }
            });
            mp.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    Log.error("Audio error: %d:%d", what, extra);
                    return false;
                }
            });
            mp.prepareAsync();

        } catch (IOException e) {
            Log.warn(e, "Error playing audio '%s'", audioFileName);
        }
    }

    private void playVideo(String file, boolean loop)
    {
        Log.info("\t%sing video '%s'", loop?"Loop":"Play", file);

        // If not the same cue
        if(!file.equals(lastVideoCue)) {
            Log.info("\tPlaying");

            String path = application.contentManager.pathForFile(file);
            if(path == null) {
                Log.warn("Could not find video '%s'", file);
                return;
            }
            Intent intent = new Intent(this,VideoActivity.class);

            intent.putExtra(Intent.EXTRA_SUBJECT, path);
            intent.putExtra(Intent.EXTRA_KEY_EVENT, loop);
            startActivity(intent);
        } else {
            Log.info("\tAlready playing");
        }

        if(loop) lastVideoCue = file;
    }

    private void cutVideo()
    {
        Log.info("Stopping video");
        VideoActivity.stop();
        lastVideoCue = null;
    }

    private void vibrate(long duration)
    {
        Log.info("Vibrating for %d ms", duration);
        // takes in integer of millisecond duration
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(duration);
    }
}
