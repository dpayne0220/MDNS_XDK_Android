package com.fortytwo.mdns_plugin;

import java.io.IOError;
import java.io.IOException;
import java.net.InetAddress;

import org.json.JSONArray;
import org.json.JSONException;

import com.appMobi.appMobiLib.AppMobiActivity;
import com.appMobi.appMobiLib.AppMobiWebView;
import com.fortytwo.mdns_plugin.ChatConnection;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Fragment;
//import android.annotation.TargetApi;
import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Handler;
//import android.os.Build;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.TextView;
//import android.app.Activity;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import com.fortytwo.mdns_plugin.NsdHelper;
import android.content.Context;
import android.net.nsd.NsdServiceInfo;
import android.net.nsd.NsdManager;
import android.util.Log;




@SuppressLint("HandlerLeak")
public class MDNS_PluginWorker extends MDNS_PluginCommand
{
    Context mContext;
    NsdHelper mNsdHelper;
    MDNS_PluginWorker mMDNS_PluginWorker;
    NsdManager mNsdManager;
   private TextView mStatusView;
   private Handler mUpdateHandler;
	public static final String TAG = "Mdns_Plugin";
    ChatConnection mConnection;
    NsdServiceInfo mService;
    public static final String SERVICE_TYPE = "_http._tcp.";
    public static String mServiceName = "toca";
    public static boolean doResolve;


    
    NsdManager.ResolveListener mResolveListener;
    NsdManager.DiscoveryListener mDiscoveryListener;
    NsdManager.RegistrationListener mRegistrationListener;


    
	public MDNS_PluginWorker(AppMobiActivity activity, AppMobiWebView webview)
	{
		super(activity, webview);
        mContext = activity;
        mNsdManager = (NsdManager) activity.getSystemService(Context.NSD_SERVICE);

    }

	
public void success() {
	
	 Log.d(TAG, "Thread Execute Success>>");
}
	public void initializeWork(){
		


        mUpdateHandler = new Handler() {
            @Override
        public void handleMessage(Message msg) {
            //String chatLine = msg.getData().getString("msg");

            }
        };
        mConnection = new ChatConnection(mUpdateHandler);

		
        initializeDiscoveryListener();
        initializeResolveListener();
        initializeRegistrationListener();

	}
	
// functions go here
	   public void initializeDiscoveryListener() {
	        mDiscoveryListener = new NsdManager.DiscoveryListener() {

	            @Override
	            public void onDiscoveryStarted(String regType) {

	                Log.d(TAG, "Service discovery started");
	            }

	            @Override
	            public void onServiceFound(NsdServiceInfo service) {

	                Log.d(TAG, "Service discovery success" + service);
	                
					  if (!service.getServiceType().equals(SERVICE_TYPE)) {

	                 } else if (service.getServiceName().contains(mServiceName)){
	                    //
	                if (MDNS_PluginWorker.doResolve==true) {
	                	mNsdManager.resolveService(service, mResolveListener);
	                } else {
	                
	                
	                	String serviceName = service.getServiceName();
	                   
	                    String json = "{\"services\":[{\"serviceName\":\""+serviceName+"\"}]}";
		                webview.loadUrl("javascript:var json = '"+json+"';var ev = document.createEvent('Events');ev.initEvent('mdns_plugin.workstart',true,true);document.dispatchEvent(ev);");
	                }
	                }
	            }

	            @Override
	            public void onServiceLost(NsdServiceInfo service) {
	                Log.e(TAG, "service lost" + service);
	                if (mService == service) {
	                    mService = null;
	                }
	            }
	            
	            @Override
	            public void onDiscoveryStopped(String serviceType) {
	                Log.i(TAG, "Discovery stopped: " + serviceType);        
	            }

	            @Override
	            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
	                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
	                mNsdManager.stopServiceDiscovery(this);
	            }

	            @Override
	            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
	                Log.e(TAG, "Discovery failed: Error code:" + serviceType+" : "+errorCode);
	                mNsdManager.stopServiceDiscovery(this);
	            }
	        };
	    }
	   
	   public void initializeResolveListener() {
	        mResolveListener = new NsdManager.ResolveListener() {

	            @Override
	            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
	                Log.e(TAG, "Resolve failed" + errorCode);
	            }

	            @Override
	            public void onServiceResolved(NsdServiceInfo serviceInfo) {
	                Log.e(TAG, "Resolve Succeeded. " + serviceInfo);
	                String serviceName = "name";//serviceInfo.getServiceName();
	                InetAddress serviceHostNet = serviceInfo.getHost();
	                String serviceHost = serviceHostNet.getHostAddress();
	                int servicePort = serviceInfo.getPort();
	               
	                String json = "{\"services\":[{\"serviceName\":\""+serviceName+"\",\"serviceHost\":\""+serviceHost+"\",\"servicePort\":\""+servicePort+"\"}]}";
	                webview.loadUrl("javascript:var service = '"+json+"';var ev = document.createEvent('Events');ev.initEvent('mdns_plugin.workstop',true,true);document.dispatchEvent(ev);");
	                mService = serviceInfo;
	            }
	        };
	    }

	    public void initializeRegistrationListener() {
	        mRegistrationListener = new NsdManager.RegistrationListener() {

	            @Override
	            public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
	                mServiceName = NsdServiceInfo.getServiceName();
	            }
	            
	            @Override
	            public void onRegistrationFailed(NsdServiceInfo arg0, int arg1) {
	            }

	            @Override
	            public void onServiceUnregistered(NsdServiceInfo arg0) {
	            }
	            
	            @Override
	            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
	            }
	            
	        };
	    }

	    public void registerService(int port) {
	        NsdServiceInfo serviceInfo  = new NsdServiceInfo();
	        serviceInfo.setPort(port);
	        serviceInfo.setServiceName(mServiceName);
	        serviceInfo.setServiceType(SERVICE_TYPE);
	        
	        mNsdManager.registerService(
	                serviceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener);
	        
	    }

	    public void discoverServices() {
	        mNsdManager.discoverServices(
	                SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
	    }
	    
	    public void stopDiscovery() {
	        mNsdManager.stopServiceDiscovery(mDiscoveryListener);
	    }

	    public NsdServiceInfo getChosenServiceInfo() {
	        return mService;
	    }
	    
	    public void tearDown() {
	        mNsdManager.unregisterService(mRegistrationListener);
	    }
	    
	    public void clickDiscover() {
	    	mNsdHelper.discoverServices();
	    }
	    
	    public void clickAdvertise() {
	        // Register service
	    	
	        if(mConnection.getLocalPort() > -1) {
	        	Log.d(TAG, "ServerSocket binding.");
	            mNsdHelper.registerService(mConnection.getLocalPort());
	            Log.d(TAG, "ServerSocket is bound.");
	        } else {
	            Log.d(TAG, "ServerSocket isn't bound.");
	        }
	    }

	   
	   

	@JavascriptInterface
	public void startWork(String mServiceName, boolean doResolve)
	{
		MDNS_PluginWorker.mServiceName = mServiceName;
		MDNS_PluginWorker.doResolve = doResolve;
		
	    
        this.activity.runOnUiThread(new Runnable() {
        	
            public void run() {	
            	
            	initializeWork();
            	
            	discoverServices();
            	
            	
            }
            
        });
        
	}

	@JavascriptInterface
	public void stopWork()
	{
		
		webview.loadUrl("javascript:var ev = document.createEvent('Events');ev.initEvent('MDNS_Plugin.workstop',true,true);document.dispatchEvent(ev);");
		stopDiscovery();
	}
}
