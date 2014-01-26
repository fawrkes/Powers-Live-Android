package edu.mit.powers.content;

import android.content.Context;
import android.os.AsyncTask;
/**
 * Created by Garrett on 1/6/14.
 *
 */
//Params, Progress, Result
public class ContentDownloader extends AsyncTask<String, Integer, Void>
{
    public final Context context;

    public ContentDownloader(Context context)
    {
        this.context = context;
    }

    @Override
    protected Void doInBackground(String... files)
    {
        // TODO
        return null;
    }
}
