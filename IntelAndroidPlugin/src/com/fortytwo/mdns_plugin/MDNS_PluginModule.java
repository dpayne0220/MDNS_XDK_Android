package com.fortytwo.mdns_plugin;

import com.appMobi.appMobiLib.AppMobiActivity;
import com.appMobi.appMobiLib.AppMobiModule;
import com.appMobi.appMobiLib.AppMobiWebView;

public class MDNS_PluginModule extends AppMobiModule
{
	static private MDNS_PluginSetup mysetup;
	static private MDNS_PluginWorker myworker;

	@Override
	public void setup(AppMobiActivity activity, AppMobiWebView webview)
	{
		super.setup(activity, webview);
		
		mysetup = new MDNS_PluginSetup(activity, webview);
		myworker = new MDNS_PluginWorker(activity, webview);
		
		// You can get the application's shared activity with the following code 
		// AppMobiActivity parent = AppMobiActivity.sharedActivity();
		
		webview.registerCommand(mysetup, "MDNS_PluginSetup");
		webview.registerCommand(myworker, "MDNS_PluginWorker");
	}

	@Override
	public void initialize(AppMobiActivity activity, AppMobiWebView webview)
	{
		super.initialize(activity, webview);
		
		String result = "MDNS_PluginInfo = " + mysetup.initialize() + ";";
		result += "MDNS_PluginLoaded();";

		webview.loadUrl("javascript:" + result);
	}
}
