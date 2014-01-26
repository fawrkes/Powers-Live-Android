package edu.mit.powers.content;

import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;

import edu.mit.powers.ApplicationDriver;
import edu.mit.powers.Log;
import edu.mit.powers.content.cuelist.CueList;
import edu.mit.powers.content.cuelist.CueListLocation;

/**
 * Created by kevin on 1/6/14.
 *
 */
public class ContentManager
{
    private static final String ASSET_DIR = "cm.assets";
    private static final String LAST_VER  = "cm.lastver";

    protected final ApplicationDriver application;

    private CueList cueList;
    private boolean downloading;

    public ContentManager(ApplicationDriver context)
    {
        this.application = context;
    }

    public CueListLocation[] getVenues()
    {
        try {
            return cueList.venues.values().toArray(new CueListLocation[1]);
        } catch(NullPointerException npe) {
            return null;
        }
    }
    public CueListLocation getVenue(String name)
    {
        try {
            return cueList.venues.get(name);
        } catch(NullPointerException npe) {
            return null;
        }
    }

    public synchronized void downloadCueList(String claddr, int version)
    {
        if(downloading) return;
        downloading = true;

        final String address = String.format("%s%02d.json", claddr, version);

        Log.info("Final cue list address: %s", address);

        new AsyncTask<Void, Void, CueList>()
        {
            protected CueList doInBackground(Void...v)
            {
                String json = download(address);
                CueList cl = null;
                try
                {
                    cl = new CueList(new JSONObject(json));
                }
                catch(JSONException e)
                {
                    Log.error(e, "Error downloading cue list");
                }

                return cl;
            }

            protected void onPostExecute(CueList cl)
            {
                downloading = false;
                Log.info("Cue List Downloaded");
                cueList = cl;
                application.cueListDownloaded(cl);
            }
        }.execute();
    }

    public boolean requiresRefresh()
    {
        return cueList != null && cueList.version > cachedVersion();
    }

    public int cachedVersion()
    {
        return application.settings.getInt(LAST_VER);
    }

    public CueList getCueList()
    {
        return cueList;
    }

    public boolean refreshIfNeeded()
    {
        if(this.requiresRefresh()) {
            getNewestContent();
            return true;
        }
        return false;
    }

    public synchronized void getNewestContent()
    {
        if(downloading) return;
        downloading = true;
        new AsyncTask<String, Double, Boolean>() {
            @Override
            protected Boolean doInBackground(String... files)
            {
                boolean good = true;
                File destination = assetsDirectory();
                double nfiles = files.length;
                int ndone = 0;
                int ngood = 0;
                for(String asset : files)
                {
                    boolean gotIt = acquireVersioned(asset, destination);
                    if(gotIt)
                    {
                        ++ngood;
                    }
                    else
                    {
                        Log.warn("Could not acquire asset %s", asset);
                        good = false;
                    }
                    publishProgress(++ndone/nfiles);
                }
                Log.info("Found %d/%d files", ngood, files.length);
                if(good) {
                    application.settings.set(LAST_VER, cueList.version);
                }
                return good;
            }

            @Override
            protected void onProgressUpdate(Double... values) {
                if(values.length > 0) {
                    double progress = values[0];
                    application.contentUpdateProgress(progress);
                } else {
                    Log.warn("Update with no progress");
                }
                super.onProgressUpdate(values);
            }

            @Override
            protected void onPostExecute(Boolean aBoolean)
            {
                downloading = false;
                application.contentUpdateFinished(aBoolean);
                super.onPostExecute(aBoolean);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, cueList.assets);
    }

    private boolean acquireVersioned(String asset, File destinationDirectory)
    {
        int vstart = asset.lastIndexOf('_')+1;
        int vend   = asset.lastIndexOf('.');
        // Apparently, the default facebook pics are FBxx.jpg, not FB_xx.jpg
        if(vstart <= 0) {
            vstart = vend - 2;
        }
        int ver     = Integer.parseInt(asset.substring(vstart, vend));
        String fmt  = asset.substring(0, vstart) + "%02d" + asset.substring(vend);
        Log.info("Fetching asset %s", asset);
        // MAYBE: If we have a cached version older than the one we find, delete it.
        while(ver > 0) {
            Log.info("\tTrying version %d...", ver);
            String name = String.format(fmt, ver);
            if(acquire(name, destinationDirectory)) {
                Log.info("\tGot version %d", ver);
                return true;
            }
            --ver;
        }
        Log.warn("Could not acquire asset %s", asset);
        return false;
    }

    private boolean acquire(String name, File destinationDirectory)
    {
        File out = new File(destinationDirectory, name);
        Log.info("\tCached? %b", out.exists());
        if(isCached(out)) {
            Log.info("\tFound cached.");
            return true;
        }
        if(copyFromAssets(name, out)) {
            Log.info("\tFound in resources");
            return true;
        }
        String addr = String.format("http://%s%s%s", cueList.host, cueList.path, name);
        if(download(addr, out)) {
            Log.info("\tDownloaded");
            return true;
        }
        return false;
    }

    private boolean download(String addr, File out)
    {
        Log.info("\tTrying to download %s", addr);
        AndroidHttpClient client = AndroidHttpClient.newInstance("powers app");
        HttpGet get = new HttpGet(addr);
        boolean good = true;
        try {
            InputStream stream = getResponse(client, get);
            pipe(stream, new FileOutputStream(out));
        } catch(IOException e) {
            Log.error(e, "Error downloading %s", addr);
            good = false;
        } finally {
            client.close();
        }
        return good;
    }

    private String download(String addr)
    {
        AndroidHttpClient client = AndroidHttpClient.newInstance("powers app");
        HttpGet get = new HttpGet(addr);
        try {
            InputStream data = getResponse(client, get);
            return slurp(data);
        } catch(IOException e) {
            Log.error(e, "Error downloading %s", addr);
            return null;
        } finally {
            client.close();
        }
    }

    private InputStream getResponse(AndroidHttpClient client, HttpGet get) throws IOException
    {
        HttpResponse response = client.execute(get);
        int code = response.getStatusLine().getStatusCode();
        String reason = response.getStatusLine().getReasonPhrase();
        // Accept HTTP codes 200-299, success
        if(code / 100 != 2) {
            Log.error("Bad status code: %d: %s", code, reason);
            return null;
        }
        if(response.getStatusLine().getStatusCode() != 200) {
            Log.warn("Non-200 success code: %d: %s", code, reason);
        }
        return response.getEntity().getContent();
    }

    private boolean copyFromAssets(String name, File destination)
    {
        InputStream in;
        try {
            in = application.getCurrentView().getAssets().open(name);
        } catch(IOException ex) {
            Log.info("\tNot in package");
            // We don't have it. I haven't found a way to check first and avoid the exception.
            return false;
        }
        try {
            Log.info("\tFound in package");
            pipe(in, new FileOutputStream(destination));
            return true;
        } catch(IOException e) {
            Log.warn(e, "Exception trying to copy file %s", name);
        }
        return false;
    }

    private boolean isCached(File file)
    {
        if(file.exists()) {
            if(file.isFile()) {
                return true;
            } else {
                Log.warn("\tAsset %s exists but is a directory!?", file.getAbsolutePath());
                if(!file.delete()) Log.warn("And failed to delete it");
            }
        }
        return false;
    }

    public File assetsDirectory()
    {
        String dirname = application.settings.get(ASSET_DIR);
        File dir;
        if(dirname != null) {
            Log.info("Cached asset dir: %s", dirname);
            dir = new File(dirname);
        } else {
            dir = application.getCurrentView().getExternalFilesDir(null);
            if(dir == null) {
                Log.warn("No external files dir, trying internal");
                dir = application.getCurrentView().getFilesDir();
                if(dir == null) {
                    Log.error("Can't find anywhere to put assets!");
                    return null;
                }
            }
            application.settings.set(ASSET_DIR, dir.getAbsolutePath());
            Log.info("Generated asset dir: %s", dir.getAbsolutePath());
        }
        return dir;
    }

    private void pipe(InputStream in, OutputStream out) throws IOException
    {
        byte[] buffer = new byte[4096];
        int nbytes;
        while((nbytes = in.read(buffer)) > 0) {
            out.write(buffer, 0, nbytes);
        }
    }

    // Reads entire stream into a string.
    // Shouldn't be used for anything too big, say ~1MB
    // Also probably shouldn't be used for binary data (i.e. assets).
    private static String slurp(InputStream stream) throws IOException
    {
        Reader reader = new InputStreamReader(stream);
        StringBuilder builder = new StringBuilder();
        char[] buffer = new char[10240];
        int nread;
        while((nread = reader.read(buffer)) > 0) {
            builder.append(buffer, 0, nread);
        }
        return builder.toString();
    }

    public InputStream gimme(String filename) throws IOException
    {
        File file = new File(assetsDirectory(), filename);
        if(!file.exists()) {
            Log.error("Could not find file %s", filename);
        }
        return new FileInputStream(file);
    }

    public String pathForFile(String basename)
    {
        Log.info("Searching for '%s'", basename);
        File root = assetsDirectory();
        File file = new File(root, basename);
        if(file.exists()) {
            Log.info("\tFound direct at %s", file.getAbsolutePath());
            return file.getAbsolutePath();
        }

        int vstart = basename.lastIndexOf('_')+1;
        int vend   = basename.lastIndexOf('.');

        int ver     = Integer.parseInt(basename.substring(vstart, vend));
        String fmt  = basename.substring(0, vstart) + "%02d" + basename.substring(vend);
        while(ver --> 0) {
            Log.info("\tChecking for version %d", ver);
            file = new File(root, String.format(fmt, ver));
            if(file.exists()) {
                Log.info("\tFound version %d at '%s'", ver, file.getAbsolutePath());
                return file.getAbsolutePath();
            }
        }
        return null;
    }
}
