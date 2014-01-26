package edu.mit.powers.content.cuelist;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import edu.mit.powers.Log;
import edu.mit.powers.media.Cue;

/**
 * Created by kevin on 1/9/14.
 *
 */
public class CueList
{
    public final int version;
    public final String[] assets;
    public final Map<String, CueListLocation> venues;
    public final String host;
    public final String path;
    public final String page;
    public final String controlHost;
    public final String controlPort;
    public final Map<String, Cue> cues;

    public CueList(JSONObject json) throws JSONException
    {
        this.version = json.optInt("version");

        this.assets = getAssets(json.optJSONArray("preload"));
        this.venues = getLocations(json.optJSONObject("locations"));

        this.host = json.optString("asset-host");
        this.path = json.optString("asset-path");
        this.page = json.optString("client-page");

        this.controlHost = json.optString("control-host");
        this.controlPort = json.optString("contorl-port"); //[sic]

        this.cues = getCues(json.optJSONObject("cues"));
    }

    private Map<String, Cue> getCues(JSONObject json) throws JSONException
    {
        Log.info("Parsing cues");
        Map<String, String> redirects = new HashMap<>();
        Map<String, Cue> cues = new HashMap<>();
        Iterator it = json.keys();
        while(it.hasNext()) {
            String cueID = (String)it.next();
            Object obj = json.get(cueID);
            if(obj instanceof String) {
                redirects.put(cueID, (String)obj);
            } else {
                assert obj instanceof JSONObject;
                cues.put(cueID, new Cue((JSONObject)obj));
            }
        }

        Log.info("%d base cues, %d redirects", cues.size(), redirects.size());
        Log.info("Resolving redirects");
        for(Map.Entry<String, String> entry : redirects.entrySet()) {
            String key = entry.getKey();
            Cue resolved;
            do {
                resolved = cues.get(key);
                key = redirects.get(key);
            } while(resolved == null && key != null);
            if(resolved == null) {
                Log.warn("Null resolution of cue %s", entry.getKey());
            } else {
                cues.put(entry.getKey(), resolved);
            }
        }
        Log.info("Total %d cues", cues.size());
        return cues;
    }

    private String[] getAssets(JSONArray json) throws JSONException
    {
        if(json == null) return null;

        String[] strings = new String[json.length()];
        for(int i = 0; i < strings.length; i++) {
            strings[i] = json.getString(i);
            if(strings[i].endsWith(".mov")) {
                strings[i] = strings[i].substring(0, strings[i].length()-4) + ".mp4";
            }
        }
        return strings;
    }

    private Map<String, CueListLocation> getLocations(JSONObject json) throws JSONException
    {
        if(json == null) return null;

        Map<String, CueListLocation> locations = new HashMap<>();
        Iterator keys = json.keys();
        while(keys.hasNext())
        {
            String key = (String) keys.next();
            locations.put(key, new CueListLocation(key, json.getJSONObject(key)));
        }
        return locations;
    }
}
