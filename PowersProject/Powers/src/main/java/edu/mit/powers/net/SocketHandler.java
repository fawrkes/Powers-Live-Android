package edu.mit.powers.net;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.tavendo.autobahn.WebSocketHandler;
import edu.mit.powers.ApplicationDriver;
import edu.mit.powers.Log;
import edu.mit.powers.net.packets.ContentVersionPacket;
import edu.mit.powers.net.packets.TriggersPacket;

/**
 * Created by Garrett on 1/10/14.
 *
 */
public class SocketHandler extends WebSocketHandler
{
    private final ApplicationDriver application;

    public SocketHandler(ApplicationDriver application)
    {
        this.application = application;
    }

    @Override
    public void onOpen()
    {
        Log.info("Socket opened");
        application.socketConnected();
    }

    @Override
    public void onClose(int code, String reason)
    {
        Log.info("Socket closed code: %d: %s", code, reason);
        switch(code)
        {
            case CLOSE_NORMAL:
                application.socketDisconnected(true);
                break;

            case CLOSE_CANNOT_CONNECT:
                application.socketConnectionFailed(reason);
                break;

            default:
                application.socketDisconnected(false);
                break;
        }
    }

    @Override
    public void onTextMessage(String message)
    {
        Log.info("Received: '%s'", message);

        try {
            JSONObject object = new JSONObject(message);
            String type = object.getString("address");
            JSONObject data = object.optJSONObject("arguments");
            JSONArray array = object.optJSONArray("arguments");
            switch(type) {
                case "/content_version":
                    application.received(new ContentVersionPacket(data));
                    break;

                case "/trigger":
                    application.received(new TriggersPacket(array));
                    break;

                case "/web_view":
                    application.webviewOverride();
                    break;

                default:
                    Log.warn("Unknown WS packet type: %s", type);
                    break;
            }
        } catch(JSONException je) {
            Log.error(je, "Error parsing json from server: '%s'", message);
        } catch(NullPointerException npe) {
            Log.error(npe, "Oops");
        }
    }

    // There are also a "rawText" and a "binary" Message method if we want them.
}
