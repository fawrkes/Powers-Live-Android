package edu.mit.powers.device;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import java.util.HashSet;
import java.util.Set;

import edu.mit.powers.Log;
import edu.mit.powers.activity.PowersView;

/**
 * Created by Garrett on 1/7/14.
 *
 */
public class Connectivity extends BroadcastReceiver
{
    public static final int NOT_CONNECTED  = 0;
    public static final int CONNECTED_CELL = 1;
    public static final int CONNECTED_WIFI = 2;

    private final ConnectivityManager manager;
    private final WifiManager wifiManager;
    private Set<ConnectivityListener> listeners = new HashSet<>();

    public Connectivity(PowersView context)
    {
        this.manager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        this.wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
//        context.addListener(this, ConnectivityManager.CONNECTIVITY_ACTION);
    }

//    public void addListener(ConnectivityListener listener)
//    {
//        // Maybe make this weak refs at some point.
//        listeners.add(listener);
//    }
//
//    public void removeListener(ConnectivityListener listener)
//    {
//        listeners.remove(listener);
//    }

    public int state()
    {
        int state = NOT_CONNECTED;
        NetworkInfo wifi = this.manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if(wifi != null && wifi.isConnected())
        {
            state = CONNECTED_WIFI;
        }
        else
        {
            NetworkInfo info = this.manager.getActiveNetworkInfo();
            if(info != null && info.isConnected())
            {
                state = CONNECTED_CELL;
            }
        }
        Log.info("Connection state: %d", state);
        return state;
    }

    public String ssid()
    {
        WifiInfo info = wifiManager.getConnectionInfo();
        if(info != null) Log.info("Wifi connected to %s", info.getSSID());
        return info != null ? info.getSSID() : null;
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        assert ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction());

        Bundle extras = intent.getExtras();
        Log.info("Extras: %s", extras);
        int newState = state();
        for(ConnectivityListener listener : listeners)
        {
            listener.connectionStatusChanged(newState);
        }
    }
}
