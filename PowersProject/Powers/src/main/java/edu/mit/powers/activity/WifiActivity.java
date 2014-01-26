package edu.mit.powers.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import edu.mit.powers.R;

public class WifiActivity extends PowersView
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);

        configureUI();
    }

    private void configureUI()
    {
        String message;
        String ssid = application.getRequiredSSID();
        if (ssid == null)
        {
            message = getResources().getString(R.string.join_wifi);
        }
        else
        {
            String fmt = getResources().getString(R.string.join_specific_wifi);
            message = String.format(fmt, ssid);
        }

        TextView wifiMessage = (TextView) this.findViewById(R.id.wifiMessage);
        wifiMessage.setText(message);

        Button startOver = (Button) this.findViewById(R.id.wifiStartOver);
        startOver.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                application.setVenue(null);
            }
        });

        Button connected = (Button) this.findViewById(R.id.wifiContinueButton);
        connected.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                checkNetworkFromWifi();
            }
        });
    }

    private void checkNetworkFromWifi()
    {
        if(!application.userSaysCorrectWifi())
        {
            int titleID;
            int messageID;
            if(application.getRequiredSSID() == null)
            {
                titleID   = R.string.wrong_wifi_title;
                messageID = R.string.wrong_wifi_message;
            }
            else
            {
                titleID   = R.string.wrong_specific_wifi_title;
                messageID = R.string.wrong_specific_wifi_message;
            }
            showMessage(messageID, titleID, null);
        }
    }
}
