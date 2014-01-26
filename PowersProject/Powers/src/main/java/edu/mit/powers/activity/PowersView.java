package edu.mit.powers.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.TextView;

import edu.mit.powers.ApplicationDriver;
import edu.mit.powers.Log;

/**
 * Created by Garrett on 1/7/14.
 *
 */
public abstract class PowersView extends FragmentActivity
{
    protected ApplicationDriver application;
    protected double progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        this.application = ApplicationDriver.application(this);
    }

    public void goTo(Class<? extends PowersView> cls)
    {
        goTo(cls, null);
    }

    public void goTo(Class<? extends PowersView> cls, Bundle args)
    {
        Intent intent = new Intent(this, cls);
        if(args != null) intent.putExtras(args);
        startActivity(intent);
    }

    public void showMessage(String message, boolean error)
    {
        showMessage(message, error?"Error":"info", null);
    }

    public void showMessage(int messageID, int titleID, final Runnable completion)
    {
        showMessage(getResources().getString(messageID),
                    getResources().getString(titleID),
                    completion);
    }
    public void showMessage(String message, String title, final Runnable completion)
    {
        TextView titleText = new TextView(this);
        titleText.setText(title);
        titleText.setGravity(Gravity.CENTER);

        TextView messageText = new TextView(this);
        messageText.setText(Gravity.CENTER);
        messageText.setGravity(Gravity.CENTER);

        AlertDialog.Builder alertConstructor = new AlertDialog.Builder(this);
        alertConstructor.setTitle(title);
        alertConstructor.setMessage(message);
        alertConstructor.setCancelable(true);
        alertConstructor.setPositiveButton("Okay",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        if(completion != null) {
                            completion.run();
                        }

                        dialog.cancel();
                    }
                });

        AlertDialog alert = alertConstructor.create();
        alert.show();

    }

    @SuppressWarnings("unused")
    public void showMessage(String message)
    {
        showMessage(message, true);
    }

    // May be overridden to show on gui.
    public void setProgress(double progress)
    {
        Log.info("Progress update: %f", progress);
        this.progress = progress;
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        application.onPause();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        application.onResume();
    }

    @Override
    public void onBackPressed() {
        // Intercepted
    }
}
