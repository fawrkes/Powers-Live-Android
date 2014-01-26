package edu.mit.powers.activity;

import android.webkit.WebResourceResponse;

import com.mogoweb.chrome.WebView;
import com.mogoweb.chrome.WebViewClient;

import java.io.IOException;
import java.net.URLConnection;

import edu.mit.powers.Log;
import edu.mit.powers.content.ContentManager;

/**
 * Client to load locally stored resources using custom protocol
 * <p/>
 * Should be used to set the client of the WebView, i.e.:
 * WebView.setWebViewClient(LocalWebViewClient.newLocalWebViewClient(Activity activity);
 *
 * @author jlmart88
 */
public class LocalWebViewClient extends WebViewClient
{
//    private AssetManager assets;
    private final ContentManager content;

    private static final String CUSTOM_PROTOCOL = "cpimg://";

    public LocalWebViewClient(ContentManager contentManager)
    {
        this.content = contentManager;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url)
    {
        boolean intercept = url != null && url.startsWith(CUSTOM_PROTOCOL);
        Log.info("Override %s? %b", url, intercept);
        return intercept;
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, String url)
    {
        Log.info("Intercepting %s", url);
        WebResourceResponse response = super.shouldInterceptRequest(view, url);
        if(url != null && url.contains(CUSTOM_PROTOCOL)) {
            String assetPath = url.substring(url.indexOf(CUSTOM_PROTOCOL) + CUSTOM_PROTOCOL.length(), url.length());
            Log.info("\tTransformed path: %s", assetPath);
            try {
                //determine the mimetype
                String mimeType = URLConnection.guessContentTypeFromName(assetPath);

                Log.info("\tMIME: %s", mimeType);

                response = new WebResourceResponse(
                        mimeType,
                        "UTF8",
                        content.gimme(assetPath)
                );
            } catch(IOException e) {
                Log.error(e, "Error intercepting asset %s", url);
            }
        }
        return response;
    }
}
