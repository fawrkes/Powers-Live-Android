package edu.mit.powers.device;

import android.os.Build;

import java.util.UUID;

import edu.mit.powers.ApplicationDriver;

/**
 * Created by kevin on 1/6/14.
 *
 */
public class Device
{
    private static final String UUID_KEY = "UUIDKey";

    private final ApplicationDriver app;

    public final Connectivity connection;

    public final String uuid;
    public final String model = Build.MODEL;
    public final String type  = Build.TYPE;
    public final String systemVersion = Build.VERSION.RELEASE;
    public final String systemName = Build.USER;

    public Device(ApplicationDriver driver)
    {
        this.app = driver;
        this.uuid = getOrCreateUUID();
        this.connection = new Connectivity(driver.getCurrentView());
    }

    private String getOrCreateUUID()
    {
        // Note: This is shadowing this.uuid. I don't care. If you do, rename it
        String uuid = app.settings.get(UUID_KEY);
        if(uuid == null) {
            uuid = newUUID();
            app.settings.set(UUID_KEY, uuid);
        }
        assert uuid != null;
        return uuid;
    }

    private String newUUID()
    {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }
}
