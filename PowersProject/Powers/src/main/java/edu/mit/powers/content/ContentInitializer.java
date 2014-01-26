package edu.mit.powers.content;

import android.os.AsyncTask;

/**
 * Created by Garrett on 1/8/14.
 *
 */
public class ContentInitializer extends AsyncTask<Void, Void, String>
{
//    private static final String FOLDER   = "ContentInitializer.folder";
//    private static final String FINISHED = "ContentInitializer.finished";
//
//    public static boolean contentIsInitialized(Settings settings)
//    {
//        return settings.getBool(FINISHED);
//    }
//    public static String contentFolder(Settings settings)
//    {
//        return settings.get(FOLDER);
//    }
    private ContentManager manager;

    protected ContentInitializer(ContentManager manager)
    {
        this.manager = manager;
    }
//
    @Override
    protected String doInBackground(Void... params)
    {
//        if(contentIsInitialized(manager.set))
//        AssetManager builtins = manager.context.getAssets();
//        assert builtins != null;
//
//        CueList originalCueList = readCueList(builtins);
//
//        File storage = findWriteableStorage(manager.context);
//        if (storage)
//
        return null;
    }
//
//    private File findWriteableStorage(Context context)
//    {
//        // May eventually check space on options.
//        if(this.canUseExternal()) {
//            return context.getExternalFilesDir(null);
//        } else {
//            return context.getDir("assets", Context.MODE_PRIVATE);
//        }
//    }
//
//    private CueList readCueList(AssetManager assetManager)
//    {
//        InputStream jsonStream  = null;
//        try {
//            assert manager != null;
//            assert manager.context != null;
//
//            jsonStream = builtins.open("default_cuelist.json", AssetManager.ACCESS_BUFFER);
//            assert jsonStream != null;
//
//            String json = slurp(jsonStream);
//            assert json != null;
//
//            JSONObject job = new JSONObject(json);
//            assert job != nil;
//
//            cueList = new CueList(job);
//            assert cueList != null;
//        } catch(JSONException jex) {
//            Log.e(LOG_TAG, "Error reading cue list JSON", jex);
//        } catch(NullPointerException nex) {
//            Log.e(LOG_TAG, "Null ptr...?", nex);
//        } catch(IOException ioe) {
//            Log.e(LOG_TAG, "IOException reading default cue list", ioe);
//        } catch(AssertionError ass) {
//            Log.e(LOG_TAG, "!!Bad assertion!!", ass);
//        } finally {
//            try {
//                jsonStream.close();
//            } catch(IOException e) {
//                Log.e(LOG_TAG, "Exception closing stream. Seriously, Android?", e);
//            }
//        }
//    }
//
//    // Reads entire stream into a string.
//    private static String slurp(InputStream stream) throws IOException
//    {
//        Reader reader = new InputStreamReader(stream);
//        StringBuilder builder = new StringBuilder();
//        char[] buffer = new char[10240];
//        int nread;
//        while((nread = reader.read(buffer)) > 0) {
//            builder.append(builder, 0, nread);
//        }
//        return builder.toString();
//    }
//
//    private boolean canUseExternal()
//    {
//        String state = Environment.getExternalStorageState();
//        return Environment.MEDIA_MOUNTED.equals(state);
//    }
}
