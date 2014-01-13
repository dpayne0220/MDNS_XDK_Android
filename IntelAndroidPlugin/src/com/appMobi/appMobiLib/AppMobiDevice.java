package com.appMobi.appMobiLib;

import android.content.Context;
import android.content.res.Configuration;
import android.telephony.TelephonyManager;

public class AppMobiDevice extends AppMobiCommand {
	public AppMobiActivity appMobiActivity = null;

	public static String platform = "Android";
    
	public AppMobiDevice(AppMobiActivity activity, AppMobiWebView webview){
		super(activity, webview);
		appMobiActivity = AppMobiActivity.sharedActivity();
    }
	
	public String getPlatform()
	{
		return platform;
	}

	public String getOSVersion()
	{
		return android.os.Build.VERSION.RELEASE;
	}
	
	public String getUuid()
	{
		TelephonyManager operator = (TelephonyManager) appMobiActivity.getSystemService(Context.TELEPHONY_SERVICE);
		return operator.getDeviceId();
	}
	
	public String getModel()
	{
		return android.os.Build.MODEL;
	}
	
	public String getVersion()
	{
		return "3.0.0";
	}
    
    public String getOrientation()
    {
    	return (appMobiActivity.getResources().getConfiguration().orientation==Configuration.ORIENTATION_PORTRAIT)?"0":"-90";
    }
    
    public String getConnection() 
    {
    	return appMobiActivity.GetConnectivityType();
    }
	
	public void registerLibrary(String strDelegateName)
	{
		AppMobiModule module = null;
		
		try {
			module = (AppMobiModule) Class.forName(strDelegateName).newInstance();
		} catch(Exception e) {}
		
		if( module != null )
		{
			module.initialize(activity, webview);
		}
	}
}
