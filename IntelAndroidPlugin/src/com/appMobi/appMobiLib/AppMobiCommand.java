package com.appMobi.appMobiLib;
import com.appMobi.appMobiLib.AppMobiActivity;
import com.appMobi.appMobiLib.AppMobiWebView;

abstract public class AppMobiCommand {
	public AppMobiWebView webview = null;
	public AppMobiActivity activity = null;
	
	public AppMobiCommand(AppMobiActivity activity, AppMobiWebView webview){
		this.activity = activity;
		this.webview = webview;
	}	
}
