package edu.mit.powers.activity;

import android.os.Bundle;

import edu.mit.powers.R;

public class HoldingScreenActivity extends PowersView
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_holding_screen);
//=======
//        setContentView(R.layout.activity_facebook);
//
//        // Set message according to time of the venue
//        String time = application.settings.get("time");
//        String formattedMessage = String.format("You’re ready to begin your journey into The System! Come back to this app on Feb. 16th at %s to experience the Death and the Powers Global Simulcast.", time);
//
//        TextView message = (TextView) this.findViewById(R.id.holdingMessage);
//        message.setText(formattedMessage);
//
//        // Start over button
//        Button startOver = (Button) this.findViewById(R.id.holdingStartOver);
//        startOver.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                application.returnToVenuePicker();
//            }
//        });
//
//>>>>>>> c74752419b4b93479cc597aa80e9704f94b4cad7
    }
}

