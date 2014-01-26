package edu.mit.powers.media;

import org.json.JSONException;
import org.json.JSONObject;

import edu.mit.powers.Log;

/**
 * Created by kevin on 1/20/14.
 *
 */
public class Cue
{
    public class DeviceCue
    {
        public final boolean dim;
        public final boolean loop;
        public final String audio;
        public final String video;
        public final boolean vibrate;
        public final int duration;

        protected DeviceCue(JSONObject json)
        {
            dim      = json.optBoolean("dim");
            loop     = json.optBoolean("loop");
            audio    = json.optString("audio", null);
            video    = changeFormat(json.optString("video", null));
            vibrate  = json.has("vibrate");
            duration = json.optInt("vibrate");
        }
    }

    // Changes from .mov to .mp4
    private String changeFormat(String file)
    {
        if(file == null) return null;
        if(!file.endsWith(".mov")) {
            Log.warn("Non-mov video: '%s'", file);
            return null;
        }
        return file.substring(0, file.length()-4) + ".mp4";
    }

    private final double probability;
    public final boolean probabilistic;
    public final String next;
    public final DeviceCue device;
    public final double delay;

    // In the cue list, not in the current mobile implementation.
    // Possibly for the webview only?
    // styleClass, renderer, time, color1, color2, colors, rows, cols, _next, _follow,
    // intensity, threshold, forceClear, clearAfter, scale, invert, ref, robotMode

    public Cue(JSONObject json) throws JSONException
    {
        probabilistic = json.has("probability");
        probability = json.optDouble("probability", 1);
        next        = json.optString("next", null);
        delay       = json.optDouble("follow");
        device      = json.has("device") ? new DeviceCue(json.getJSONObject("device")) : null;
    }

    // Based on probability if it exists
    public boolean shouldRun()
    {
        if(probability == 1) return true;

        double rand = Math.random();
        boolean good = rand < probability;
        Log.info("Probability: %f < %f ? %b", rand, probability, good);
        return good;
    }
}
