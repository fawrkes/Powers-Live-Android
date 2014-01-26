package edu.mit.powers;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by kevin on 1/7/14.
 *
 */
public class Settings
{
    private static final String SETTINGS = "DatP Settings";

    private final SharedPreferences        getSetting;
    private final SharedPreferences.Editor setSetting;

    public Settings(Context context)
    {
        this.getSetting = context.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
        this.setSetting = this.getSetting.edit();
    }

    public boolean has(String setting)
    {
        return this.getSetting.contains(setting);
    }

    public String get(String setting)
    {
        return this.getSetting.getString(setting, null);
    }
    public boolean getBool(String setting)
    {
        return this.getSetting.getBoolean(setting, false);
    }
    public int getInt(String setting)
    {
        return this.getSetting.getInt(setting, 1);
    }

    public String get(String setting, String dflt)
    {
        return this.getSetting.getString(setting, dflt);
    }
    public  boolean get(String setting, boolean dflt)
    {
        return this.getSetting.getBoolean(setting, dflt);
    }
    public int get(String setting, int dflt)
    {
        return this.getSetting.getInt(setting, dflt);
    }

    public void set(String setting, String value)
    {
        this.setSetting.putString(setting, value);
        this.setSetting.apply();
    }
    public void set(String setting, boolean value)
    {
        this.setSetting.putBoolean(setting, value);
        this.setSetting.apply();
    }
    public void set(String setting, int value)
    {
        this.setSetting.putInt(setting, value);
        this.setSetting.apply();
    }
}
