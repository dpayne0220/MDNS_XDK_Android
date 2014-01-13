package com.appMobi.appMobiLib;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class AppMobiActivity extends Activity
{
	public FrameLayout root;
	public LinearLayout webRoot;
	public AppMobiWebView appView;

	private static AppMobiActivity sharedActivity = null;
	public boolean launchedChildActivity = false; 

	@Override
	public void onCreate(Bundle savedInstanceState)
	{		
		super.onCreate(savedInstanceState);
		sharedActivity = this;

		getWindow().requestFeature(Window.FEATURE_NO_TITLE); 
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN, WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN); 

		try {
			setupApplication();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		startApplication();
	}

	private void setupApplication() throws FileNotFoundException, IOException
	{
		File root = getRootDirectory();
		if( !root.exists()) root.mkdirs();

		File base = new File(root, "_appMobi");
		if( !base.exists()) base.mkdir();

		InputStream isBundleJS = null;
		FileOutputStream osModuleJS = null;
		File moduleJS = null;

		isBundleJS = getResources().openRawResource(R.raw.appmobi_android);
		moduleJS = new File(base, "appmobi.js");
		if( moduleJS.exists() ) moduleJS.delete();
		osModuleJS = new FileOutputStream(moduleJS, false);
		copyInputStream(isBundleJS, osModuleJS);

		for(String moduleName:getResources().getStringArray(R.array.javascripts))
		{
			if(moduleName == null || moduleName.length()==0) continue;

			try {
				Field module = R.raw.class.getField(moduleName);
				int id = module.getInt(null);
				isBundleJS = getResources().openRawResource(id);
				moduleJS = new File(base, moduleName+".js");
				if( moduleJS.exists() ) moduleJS.delete();
				osModuleJS = new FileOutputStream(moduleJS, false);
				copyInputStream(isBundleJS, osModuleJS);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		isBundleJS = getResources().openRawResource(R.raw.index);		
		moduleJS = new File(root, "index.html");
		if( moduleJS.exists() ) moduleJS.delete();
		osModuleJS = new FileOutputStream(moduleJS, false);
		copyInputStream(isBundleJS, osModuleJS);
	}

	public static AppMobiActivity sharedActivity()
	{
		return sharedActivity;
	}
	
	private static void copyInputStream(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int len = in.read(buffer);
		while (len >= 0) {
			out.write(buffer, 0, len);
			len = in.read(buffer);
		}
		in.close();
		out.close();
	}

	private void startApplication()
	{        
		root = new FrameLayout(this);

		LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, 
				ViewGroup.LayoutParams.FILL_PARENT, 0.0F);

		LinearLayout.LayoutParams webviewParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.FILL_PARENT, 1.0F);

		webRoot = new LinearLayout(this);
		webRoot.setBackgroundColor(Color.TRANSPARENT);        
		webRoot.setLayoutParams(containerParams);

		root.addView(webRoot);

		appView = new AppMobiWebView(AppMobiActivity.this);
		appView.setBackgroundColor(Color.TRANSPARENT);
		appView.setLayoutParams(webviewParams);
		//	        appView.setWebChromeClient(new AppMobiChromeClient(this));

		appView.setInitialScale(100);
		appView.setVerticalScrollBarEnabled(false);
		appView.setHorizontalScrollBarEnabled(false);

		WebSettings settings = appView.getSettings();
		settings.setJavaScriptEnabled(true);
		settings.setJavaScriptCanOpenWindowsAutomatically(true);
		settings.setLayoutAlgorithm(LayoutAlgorithm.NORMAL);        

		appView.clearCache(true);
		//        bindBrowser(appView);
		webRoot.addView(appView);

		try {
			File index = new File(getRootDirectory(), "index.html");
			loadUrl(index.toURL().toString());
		} catch (Exception e) { e.printStackTrace(); }
	}

	private void loadUrl(String url)
	{
		setContentView(root);
		webRoot.requestFocusFromTouch();
		appView.loadUrl(url);
	}

	private File getInstallDirectory()
	{
		File temp = getApplicationContext().getFilesDir();
		return temp;
	}

	private File getRootDirectory()
	{
		File temp = new File(getInstallDirectory(), "root");
		return temp;
	}

	public String GetConnectivityType() {
		WifiManager wifiMgr = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		if (wifiMgr.isWifiEnabled() == true) {
			return "wifi";
		}

		TelephonyManager telMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		int CurrentNetworkType = telMgr.getNetworkType();
		if (CurrentNetworkType == TelephonyManager.NETWORK_TYPE_UMTS ||
				CurrentNetworkType == TelephonyManager.NETWORK_TYPE_EDGE ||
				CurrentNetworkType == 5 ||
				CurrentNetworkType == 6 ||
				CurrentNetworkType == 7 ||
				CurrentNetworkType == TelephonyManager.NETWORK_TYPE_UNKNOWN) {
			return "cell";
		} else {
			return "none";
		}
	}
}
