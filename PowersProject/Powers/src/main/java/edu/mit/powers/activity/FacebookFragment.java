package edu.mit.powers.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.facebook.widget.ProfilePictureView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;

import edu.mit.powers.ApplicationDriver;
import edu.mit.powers.Log;
import edu.mit.powers.R;



/**
 * Created by jlmart88 on 1/20/14.
 *
 */
public class FacebookFragment extends Fragment{

    private TextView loggedInStatus;
    private LinearLayout loggedInLayout;
    private ProfilePictureView profilePictureView;
    private TextView userNameView;
    private ProgressBar picturesProgressBar;

    private Session mSession;

    private UiLifecycleHelper uiHelper;
    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(final Session session, final SessionState state, final Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uiHelper = new UiLifecycleHelper(getActivity(), callback);
        uiHelper.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
    ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_facebook, container, false);

        if (view == null) {
            Log.error("Facebook fragment null!");
            return null;
        }

        // Check for an open session
        Session session = Session.getActiveSession();
        if (session != null && session.isOpened()) {
            // Get the user's data
            Log.info("FB perms: %s", session.getPermissions().toString());
            makeMeRequest(session);
            makePhotoRequest(session);
        }

        // Find the user's profile picture custom view
        profilePictureView = (ProfilePictureView) view.findViewById(R.id.selection_profile_pic);
        profilePictureView.setCropped(true);

        // Find the user's name view
        userNameView = (TextView) view.findViewById(R.id.selection_user_name);

        loggedInStatus = (TextView) view.findViewById(R.id.fbStatusLabel);
        loggedInLayout = (LinearLayout) view.findViewById(R.id.facebook_logged_in_layout);

        picturesProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        LoginButton authButton = (LoginButton) view.findViewById(R.id.login_button);
        authButton.setSessionStatusCallback(callback);
        authButton.setFragment(this);
        authButton.setReadPermissions(Arrays.asList("basic_info", "user_photos"));

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        uiHelper.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        uiHelper.onSaveInstanceState(bundle);
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    private void makeMeRequest(final Session session) {
        // Make an API call to get user data and define a
        // new callback to handle the response.
        Request request = Request.newMeRequest(session, new Request.GraphUserCallback() {
            @Override
            public void onCompleted(GraphUser user, Response response) {
                // If the response is successful

                if(session == Session.getActiveSession()) {
                    if(user != null) {
                        //Log.d(TAG, user.toString());
                        // Set the id for the ProfilePictureView
                        // view that in turn displays the profile picture.
                        profilePictureView.setProfileId(user.getId());
                        // Set the Textview's text to the user's name.
                        userNameView.setText(user.getName());
                    }
                }
                if(response.getError() != null) {
                    Log.warn("Facebook error: %s", response.getError());
                }
            }
        });
        request.executeAsync();
    }

    private void makePhotoRequest(final Session session) {
        // Make an API call to get user data and define a
        // new callback to handle the response.

        Request request = Request.newGraphPathRequest(session, "/me/photos", new Request.Callback() {
            @Override
            public void onCompleted(Response response) {
                // If the response is successful

                if (session == Session.getActiveSession()) {
                    if(response != null) {
                        Log.info("FB photo response: '%s'", response.toString());
                        DownloadFacebookPicturesTask task = new DownloadFacebookPicturesTask();
                        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, response);

                    }
                }
                if (response != null && response.getError() != null) {
                    Log.warn("Facebook error: %s", response.getError());
                }
            }
        });
        request.executeAsync();
    }

    // Make the graph '/me' request if the session is open
    private void onSessionStateChange(final Session session, SessionState state, Exception exception) {
        if (session != null){
            if (state.isOpened()) {
                if (mSession == null || isSessionChanged(session)) {
                    mSession = session;

                    Log.info("OPEN SESSION");
                    // Get the user's data.
                    makeMeRequest(session);
                    makePhotoRequest(session);

                    loggedInLayout.setVisibility(View.VISIBLE);
                    loggedInStatus.setText(R.string.facebook_logged_in);
                }
            }
            if (state.isClosed()) {
                loggedInLayout.setVisibility(View.INVISIBLE);
                loggedInStatus.setText(R.string.facebook_not_logged_in);
            }
        }
        if (exception != null) {
            Log.warn(exception, "FB state exception");
        }
    }
    private boolean isSessionChanged(Session session) {

        // Check if session state changed
        if (mSession.getState() != session.getState())
            return true;

        // Check if accessToken changed
        if (mSession.getAccessToken() != null) {
            if (!mSession.getAccessToken().equals(session.getAccessToken()))
                return true;
        }
        else if (session.getAccessToken() != null) {
            return true;
        }

        // Nothing changed
        return false;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data);

    }

    private class DownloadFacebookPicturesTask extends AsyncTask<Response, Integer, Boolean> {
        // Do the long-running work in here
        protected Boolean doInBackground(Response... response) {
            JSONObject photosObject = response[0].getGraphObject().getInnerJSONObject();
            Log.info("Received photosObject: %s", photosObject.toString());

            File photosDirectory = ApplicationDriver.application(null).contentManager.assetsDirectory();
            try {
                JSONArray photosArray = photosObject.getJSONArray("data");
                int numOfPictures = Math.min(20,photosArray.length());
                for(int i=0; i < numOfPictures; i++) {
                    String photoURL = photosArray.getJSONObject(i).getString("source");
                    String fileName = String.format("FB%02d.jpg",i+1);
                    Log.info("Trying to download picture at: %s", photoURL);
                    File saveFile = new File(photosDirectory, fileName);

                    URL url = new URL(photoURL);//.replaceFirst("https://","http://"));
                    InputStream input = url.openConnection().getInputStream();
                    // URLConnection connection = url.openConnection();
                    // connection.setConnectTimeout(20000);
                    // connection.connect();
                    // Getting file length
                    // int lenghtOfFile = connection.getContentLength();
                    // Create a Input stream to read file - with 8k buffer
                    // InputStream input = new BufferedInputStream(url.openStream(),8192);
                    // Output stream to write file
//                    FileOutputStream output = thisFragment.getActivity().getApplicationContext().openFileOutput(fileName, Context.MODE_PRIVATE);
                    FileOutputStream output = new FileOutputStream(saveFile);

                    byte data[] = new byte[1024];
                    int count;
                    Log.info("Writing photo from: %s", photoURL);
                    while ((count = input.read(data)) != -1) {
                        // writing data to file
                        output.write(data, 0, count);
                    }

                    // flushing output
                    output.flush();
                    // closing streams
                    output.close();
                    input.close();

                    Log.info("Finished photo from: %s", photoURL);

                    publishProgress(i+1,numOfPictures);



                }
            } catch (JSONException|IOException e) {
                Log.error(e, "Exception downloading facebook photos");
                return false;
            }

            return true;
        /*
        try {
            Log.d(TAG,photosObject.getJSONArray("data").getJSONObject(0).getString("source"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        */
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
           picturesProgressBar.setProgress((progress[0]*100)/progress[1]);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            ApplicationDriver.application(null).facebookFinished();
        }
    }
}
