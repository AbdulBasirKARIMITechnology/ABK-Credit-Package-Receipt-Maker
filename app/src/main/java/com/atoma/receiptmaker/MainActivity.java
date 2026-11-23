package com.atoma.receiptmaker;

import android.app.Activity;
import android.os.Bundle;
import android.os.Build;
import android.os.Environment;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.JavascriptInterface;
import android.content.ContentValues;
import android.provider.MediaStore;
import android.net.Uri;
import android.util.Base64;
import java.io.OutputStream;

public class MainActivity extends Activity {
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView = new WebView(this);
        setContentView(webView);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);

        webView.addJavascriptInterface(new GalleryBridge(), "Android");

        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());

        webView.loadUrl("file:///android_asset/index.html");
    }

    public class GalleryBridge {
        @JavascriptInterface
        public String saveImage(String base64Image) {
            Uri imageUri = null;

            try {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    return "UNSUPPORTED_ANDROID_VERSION";
                }

                byte[] imageBytes = Base64.decode(
                    base64Image,
                    Base64.DEFAULT
                );

                ContentValues values = new ContentValues();
                values.put(
                    MediaStore.Images.Media.DISPLAY_NAME,
                    "ABK_Credit_Package_Receipt_" + System.currentTimeMillis() + ".jpg"
                );
                values.put(
                    MediaStore.Images.Media.MIME_TYPE,
                    "image/jpeg"
                );
                values.put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES + "/ABK Credit & Package Receipt Maker"
                );
                values.put(
                    MediaStore.Images.Media.IS_PENDING,
                    1
                );

                imageUri = getContentResolver().insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    values
                );

                if (imageUri == null) {
                    return "SAVE_FAILED";
                }

                OutputStream outputStream =
                    getContentResolver().openOutputStream(imageUri);

                if (outputStream == null) {
                    getContentResolver().delete(imageUri, null, null);
                    return "SAVE_FAILED";
                }

                outputStream.write(imageBytes);
                outputStream.close();

                ContentValues completed = new ContentValues();
                completed.put(MediaStore.Images.Media.IS_PENDING, 0);

                getContentResolver().update(
                    imageUri,
                    completed,
                    null,
                    null
                );

                return "SUCCESS";

            } catch (Exception e) {
                if (imageUri != null) {
                    getContentResolver().delete(imageUri, null, null);
                }
                return "ERROR";
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
