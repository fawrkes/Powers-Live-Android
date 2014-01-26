package edu.mit.powers.content.cuelist;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Garrett on 1/11/14.
 *
 */
public class CueListLocation
{
    public final String name;
    public final String longName;
    public final String id;
    public final String ssid;
    public final boolean wifiRequired;
    public final String showTime;

    public CueListLocation(String name, JSONObject json) throws JSONException
    {
        this.name = name;
        this.longName = json.getString("long-name");
        this.id   = json.getString("id");
        this.ssid = json.getString("ssid");
        this.wifiRequired = json.getBoolean("wifi-required");
        this.showTime = json.getString("show-time");
    }
}
