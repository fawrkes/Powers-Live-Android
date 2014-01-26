package edu.mit.powers.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;

import edu.mit.powers.R;

public class FacebookActivity extends PowersView{

    private UiLifecycleHelper uiHelper;
    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session,
                         SessionState state, Exception exception) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facebook);

        uiHelper = new UiLifecycleHelper(this, callback);
        uiHelper.onCreate(savedInstanceState);


        configureUI();
    }

    private void configureUI()
    {
        Button skipButton = (Button) this.findViewById(R.id.facebookSkip);
        skipButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                application.skipFacebook();
            }
        });

        Button startOver = (Button) this.findViewById(R.id.facebookStartOver);
        startOver.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                application.setVenue(null);
            }
        });


    }

    @Override
    public void onResume() {
        super.onResume();
        uiHelper.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }
}

