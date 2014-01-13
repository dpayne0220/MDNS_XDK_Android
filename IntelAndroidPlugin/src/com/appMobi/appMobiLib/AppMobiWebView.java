package com.appMobi.appMobiLib;

import java.io.File;
import java.util.Map;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.ViewGroup;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.widget.LinearLayout;


public class AppMobiWebView extends WebView  {
	
	AppMobiActivity activity;
	Map<String, AppMobiCommand> commandObjects;

	AppMobiWebViewClient awvc = null;
	public AppMobiDevice device;
	
	boolean isReady = false;
	boolean wasLoadingStopped;

	public AppMobiWebView(Context context) {
		super(context);
		activity = AppMobiActivity.sharedActivity();
		init();
	
		bindBrowser();
		isReady = true;
	}
	
	private void init() {
        //setBackgroundColor(Color.TRANSPARENT);
        LinearLayout.LayoutParams webviewParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
        		ViewGroup.LayoutParams.FILL_PARENT, 1.0F);
        setLayoutParams(webviewParams);
        
        setWebChromeClient(new AppMobiChromeClient(activity));
        awvc = new AppMobiWebViewClient();
        setWebViewClient(awvc);
        
        setInitialScale(100);
        setVerticalScrollBarEnabled(false);
        setHorizontalScrollBarEnabled(false);
        
        WebSettings settings = getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setLayoutAlgorithm(LayoutAlgorithm.NORMAL);
        settings.setGeolocationEnabled(true);

    	//clear the cache - needed so that javascript is reloaded - possibly optimize later
    	clearCache(true);
    	Log.i("appmobi", "called clearCache");
	}
	
	
	private void bindBrowser()
	{
		device = new AppMobiDevice(activity, this);
		addJavascriptInterface(device, "AppMobiDevice");

		for(String moduleName:getResources().getStringArray(R.array.modules))
		{
			if(moduleName == null || moduleName.length()==0) continue;			

			AppMobiModule module = null;
			
			try {
				module = (AppMobiModule) Class.forName(moduleName).newInstance();
			}
			catch(Exception e)
			{
					Log.d("[appMobi]", "unable to load module: " + e.getMessage(), e);
			}
			
			if( module != null )
			{
				module.setup(activity, this);
			}
		}
	}
	
	@Override
	public void loadUrl(final String url)
	{
		super.loadUrl(url);
	}
    public File appDirectory()
    {
    	File temp = activity.getApplicationContext().getFilesDir();
    	return temp;
    }
    
    public File baseDirectory()
    {
    	File temp = new File(appDirectory(), "root");
    	return temp;
    }
    
	public void registerCommand(AppMobiCommand command, String name)
	{
		addJavascriptInterface(command, name);
	}

	private class AppMobiWebViewClient extends WebViewClient {
	}		
    
    /**
     * Provides a hook for calling "alert" from javascript. Useful for debugging
     * your javascript.
     */
    private class AppMobiChromeClient extends WebChromeClient {

        Context mCtx;
        AppMobiChromeClient(Context ctx) {
                mCtx = ctx;
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                AlertDialog.Builder alertBldr = new AlertDialog.Builder(mCtx);
                OKDialog okHook = new OKDialog();
                alertBldr.setMessage(message);
                alertBldr.setTitle(url.substring(url.lastIndexOf('/') + 1));
                alertBldr.setPositiveButton("OK", okHook);
                alertBldr.show();
                result.confirm();
                return true;
        }
        
        public class OKDialog implements DialogInterface.OnClickListener {
                public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                }
        }
    	public void addMessageToConsole(String message, int lineNumber, String sourceID) {
    		Log.d("AppMobiLog", sourceID + ": Line " + Integer.toString(lineNumber) + " : " + message);
    	}
    }	
}


