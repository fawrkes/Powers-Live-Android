package edu.mit.powers.net.packets;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by Garrett on 1/10/14.
 *
 */
public class TriggersPacket
{
    public final String[] triggers;

    public TriggersPacket(JSONArray json) throws JSONException
    {
        this.triggers = process(json);
    }

    public String[] process(JSONArray json) throws JSONException
    {
        String[] ary = new String[json.length()];
        for (int i = 0; i < ary.length; i++)
        {
            ary[i] = json.getString(i);
        }
        return ary;
    }
}
