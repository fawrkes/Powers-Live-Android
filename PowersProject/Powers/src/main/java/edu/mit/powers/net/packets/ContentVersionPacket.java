package edu.mit.powers.net.packets;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Garrett on 1/10/14.
 *
 */
public class ContentVersionPacket
{
    public final int version;
    public final String cueList;
    public final boolean venueWifi;
    public final boolean webView;
    public final boolean holdingScreen;

    public ContentVersionPacket(JSONObject json) throws JSONException
    {
        this.version   = json.getInt("version");
        this.cueList   = json.getString("cue_list");
        this.venueWifi = json.getBoolean("venue_wifi");
        this.webView   = json.getBoolean("web_view");
        this.holdingScreen = json.getBoolean("holding_screen");
    }
}
