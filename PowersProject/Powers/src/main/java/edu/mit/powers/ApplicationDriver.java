package edu.mit.powers;

import android.os.Handler;
import android.os.Looper;

import com.mogoweb.chrome.ChromeInitializer;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import edu.mit.powers.activity.FacebookActivity;
import edu.mit.powers.activity.HoldingScreenActivity;
import edu.mit.powers.activity.PowersView;
import edu.mit.powers.activity.ProductionContentDownloadActivity;
import edu.mit.powers.activity.WebViewActivity;
import edu.mit.powers.activity.WelcomeAndInfoActivity;
import edu.mit.powers.activity.WifiActivity;
import edu.mit.powers.content.ContentManager;
import edu.mit.powers.content.cuelist.CueList;
import edu.mit.powers.content.cuelist.CueListLocation;
import edu.mit.powers.device.Connectivity;
import edu.mit.powers.device.Device;
import edu.mit.powers.media.Cue;
import edu.mit.powers.net.SocketHandler;
import edu.mit.powers.net.packets.ContentVersionPacket;
import edu.mit.powers.net.packets.TriggersPacket;

/**
 * Created by Garrett on 1/7/14.
 *
 */
public class ApplicationDriver
{
    private static final String SELECTED_VENUE = "selectedvenue";
    private static final String wsAddress = "ws://oscar.media.mit.edu:80";

    private static final String FACEBOOK_OPTOUT = "opted out";
    private static final String FACEBOOK_DONE = "completed";

    private static final int WATCHDOG_INTERVAL = 5000;
    private static final int MAX_RETRIES = 5;
    private int socketRetries;

    private boolean initialized;

    private Handler mainThread;

    public  final Settings  settings;
    public  final Device    device;
    public  final ContentManager contentManager;
    private final WebSocketConnection socket = new WebSocketConnection();

    public ContentVersionPacket contentVersion;

    private PowersView currentView;
    public PowersView getCurrentView()
    {
        return currentView;
    }

    // TODO: There's gotta be a better way...
    private WebViewActivity webView;

    private CueListLocation venue;
    public synchronized CueListLocation getVenue()
    {
        if(venue == null)
        {
            venue = contentManager.getVenue(getVenueName());
        }
        return venue;
    }

    public Cue cueForTrigger(String trigger)
    {
        try {
            return contentManager.getCueList().cues.get(trigger);
        } catch(NullPointerException npe) {
            return null;
        }
    }

    private String venueName;

    public String getVenueName()
    {
        if(venueName == null)
        {
            venueName = settings.get(SELECTED_VENUE);
        }
        return venueName;
    }

    public void setVenueName(String name)
    {
        venueName = name;
        settings.set(SELECTED_VENUE, name);
        // Invalidate cached venue
        venue = null;
    }

    public CueListLocation[] getVenues()
    {
        return contentManager.getVenues();
    }

    public String getWebviewAddress()
    {
        try
        {
            return contentManager.getCueList().page;
        }
        catch(NullPointerException npe)
        {
            return null;
        }
    }

    public boolean checkedFacebook()
    {
        return settings.getBool(FACEBOOK_OPTOUT) || settings.getBool(FACEBOOK_DONE);
    }

    private static ApplicationDriver instance;
    public static synchronized ApplicationDriver application(PowersView view)
    {
        if(instance == null)
        {
            instance = new ApplicationDriver(view);
        }
        if(view != null)
        {
            instance.currentView = view;
            if(view instanceof WebViewActivity) instance.webView = (WebViewActivity)view;
        }
        return instance;
    }

    private ApplicationDriver(PowersView currentView)
    {
        this.currentView = currentView;

        // Note: settings must be created before device.
        // Should probably make that not order-dependent. Eventually.
        this.settings   = new Settings(currentView);
        this.device     = new Device(this);
        this.contentManager = new ContentManager(this);
    }

    public void init()
    {
        Log.info("Application.init");

        assert Looper.myLooper() == Looper.getMainLooper();

        mainThread = new Handler();

        ChromeInitializer.initialize(currentView);
        launchWatchdog();
    }

    private void launchWatchdog()
    {
        Timer watchdog = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
//                Log.info("Checking socket, up? %b", socket.isConnected());
                if(!socket.isConnected()) {
                    Log.info("Socket down, restarting.");
                    connectSocket();
                }
            }
        };
        watchdog.schedule(task, WATCHDOG_INTERVAL, WATCHDOG_INTERVAL);
    }

    private void connectSocket()
    {
        if(socket.isConnected())
        {
            Log.warn("Tried to connect while already connected");
            return;
        }

        try
        {
            socket.connect(wsAddress, new SocketHandler(this));
        }
        catch(WebSocketException wex)
        {
            Log.error(wex, "Error connecting to websocket");
        }
    }

    public void socketConnected()
    {
        Log.info("Socket connected");
        // send device info. Should receive content version shortly.
        try {
            JSONObject info = new JSONObject();
            info.put("device_type",     device.type);
            info.put("localized_model", device.model);
            info.put("system_version",  device.systemVersion);
            info.put("system_name",     device.systemName);
            info.put("uuid",            device.uuid);
            info.put("device_content_version", contentManager.cachedVersion());
            JSONObject pkt = new JSONObject();
            pkt.put("address", "/device");
            pkt.put("arguments", info);
            socket.sendTextMessage(pkt.toString());
        } catch(JSONException je) {
            Log.warn(je, "Could not send device packet");
        }
        socketRetries = 0;
    }

    public void socketConnectionFailed(String message)
    {
        Log.warn("Connection failed, %s", message);
        socketError();
    }

    public void socketDisconnected(boolean clean)
    {
        // Not sure we'd ever want/get clean.
        Log.info("Socket d/c'd - clean? %b", clean);
        if(!clean)socketError();
    }

    private void socketError()
    {
        if (++socketRetries > MAX_RETRIES) {
            final String message = currentView.getString(R.string.socket_error_body);
            final String title   = currentView.getString(R.string.socket_error_title);
            final Runnable completion = new Runnable() {
                @Override
                public void run() {
                    socketRetries = 0;
                    connectSocket();
                }
            };
            mainThread.post(new Runnable() {
                @Override
                public void run() {
                    currentView.showMessage(message, title, completion);
                }
            });
        } else {
            connectSocket();
        }
    }

    public void received(ContentVersionPacket cvp)
    {
        contentVersion = cvp;
        contentManager.downloadCueList(cvp.cueList, cvp.version);
    }

    public void cueListDownloaded(CueList list)
    {
        if(list == null)
        {
            // TODO: Is this the right error message?
            currentView.showMessage("Please connect to a wifi network.", true);
            // Retry. May want a pause/backoff.
            // ?
        }
        navigateUser();
    }

    private void navigateUser()
    {
        CueListLocation venue = getVenue();
        Log.info("Venue is %s", venue == null? null : venue.name);
        Class<? extends PowersView> next;
        if(venue == null) {
            Log.info("No venue chosen, going to venue selection");
            next = WelcomeAndInfoActivity.class;
        } else if(!canStartDownloading()) {
            Log.info("Not on correct wifi, going to wifi screen");
            next = WifiActivity.class;
        } else if(contentVersion.webView) {
            Log.info("Webview override");
            next = WebViewActivity.class;
        } else if(!checkedFacebook()) {
            Log.info("Hasn't chosen facebook, going to facebook screen");
            next = FacebookActivity.class;
        } else if(contentManager.requiresRefresh()) {
            Log.info("Need to check content, going to downloading screen");
            next = ProductionContentDownloadActivity.class;
        } else if(contentVersion.holdingScreen) {
            Log.info("All ready but holding");
            next = HoldingScreenActivity.class;
        } else {
            Log.info("Ready for the show");
            next = WebViewActivity.class;
        }
        navigateTo(next);
        if(canStartDownloading()) {
            contentManager.refreshIfNeeded();
        }
    }

    private void navigateTo(Class<? extends PowersView> nextScreen)
    {
        if(nextScreen != null && currentView.getClass() != nextScreen)
        {
            currentView.goTo(nextScreen);
        }
    }

    public void setVenue(CueListLocation venue)
    {
        String name = venue == null ? null : venue.name;
        setVenueName(name);
        navigateUser();
    }

    public String getRequiredSSID()
    {
        if(contentVersion == null || !contentVersion.venueWifi) return null;
        if(getVenue() == null || !getVenue().wifiRequired) return null;
        return getVenue().ssid;
    }

    public void received(final TriggersPacket packet)
    {
        if (currentView.getClass() == WebViewActivity.class)
        {
            mainThread.post(new Runnable() {
                @Override
                public void run() {
                    webView.executeTrigger(packet);
                }
            });
            ackTrigger(packet);
        }
    }

    private void ackTrigger(TriggersPacket packet)
    {
        for (String trigger : packet.triggers) {
            try {
                JSONObject info = new JSONObject();
                info.put("cueNumber", trigger);
                JSONObject pkt = new JSONObject();
                pkt.put("address", "/status");
                pkt.put("arguments", info);
                Log.info("Acking trigger %s", trigger);
                socket.sendTextMessage(pkt.toString());
            } catch(JSONException je) {
                Log.warn(je, "Error sending ack to server");
            }
        }
    }

    public void webviewOverride()
    {
        // TODO: Check if they're on wifi?
        currentView.goTo(WebViewActivity.class);
    }

    private boolean canStartDownloading()
    {
        // Anything else?
        return correctWifi(getVenue());
    }

    private boolean correctWifi(CueListLocation venue)
    {
        Log.info("Checking wifi");

        // If we're not on wifi at all, we're obviously not on the right wifi.
        if(device.connection.state() != Connectivity.CONNECTED_WIFI) {
            Log.info("Not connected to wifi");
            return false;
        }

        // If the global flag is off, we don't need to enforce a particular network.
        if(!contentVersion.venueWifi) {
            Log.info("Global require venue wifi flag off");
            return true;
        }

        // If we haven't selected a venue, we can't say it's correct yet.
        if(venue == null) {
            Log.info("No venue selected, don't know if wifi is good");
            return false;
        }

        // If the venue doesn't requre wifi, we're good.
        if(!venue.wifiRequired) {
            Log.info("Venue doesn't require wifi");
            return true;
        }

        // Actually have to check the network.
        String network = device.connection.ssid();
        boolean connected = network != null && network.equals(venue.ssid);

        if (connected)
        {
            Log.info("Connected to correct ssid.");
        }
        else
        {
            Log.info("Connected to incorrect ssid.");
        }
        return connected;
    }

    public boolean userSaysCorrectWifi()
    {
        if(correctWifi(getVenue()))
        {
            // TODO: Actually ping something?
            Log.info("Correct wifi, moving on");
            navigateUser();
            return true;
        }
        else
        {
            Log.info("User says correct wifi, we don't");
            return false;
        }
    }

    public void contentUpdateProgress(double progress)
    {
        currentView.setProgress(progress);
    }

    public void contentUpdateFinished(boolean success)
    {
        Log.info("Content finished, success? %b", success);
        navigateUser();
    }

    public void skipFacebook()
    {
        settings.set(FACEBOOK_OPTOUT, true);
        navigateUser();
    }

    public void facebookFinished()
    {
        settings.set(FACEBOOK_DONE, true);
        navigateUser();
    }

    public void onPause()
    {
        Log.info("Pause");
    }

    public synchronized void onResume()
    {
        Log.info("Resume");
        if(!initialized) {
            initialized = true;
            init();
        }
    }
}
