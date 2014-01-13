package com.fortytwo.mdns_plugin;

import com.appMobi.appMobiLib.AppMobiActivity;
import com.appMobi.appMobiLib.AppMobiWebView;

public class MDNS_PluginSetup extends MDNS_PluginCommand
{
	public MDNS_PluginSetup(AppMobiActivity activity, AppMobiWebView webview)
	{
		super(activity, webview);
    }

    public String initialize()
    {
		return "{ 'ready' : 1 }";
	}
}
