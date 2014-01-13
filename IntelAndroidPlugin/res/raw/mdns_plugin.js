// Handle Loading of appMobi Library
document.addEventListener("appMobi.device.ready",MDNS_PluginRegister,false);

function MDNS_PluginRegister()
{
	if( AppMobi.device.platform == "Android" )
		AppMobi.device.registerLibrary("com.fortytwo.mdns_plugin.MDNS_PluginModule");
	if( AppMobi.device.platform == "iOS" )
		AppMobi.device.registerLibrary("MDNS_PluginModule");
}

function MDNS_PluginLoaded()
{	
	while (MDNS_Plugin._constructors.length > 0) {
		var constructor = MDNS_Plugin._constructors.shift();
		try {
			constructor();
		} catch(e) {
			alert("Failed to run constructor: " + e.message);
		}
	}
    
	// all constructors run, now fire the ready event
	MDNS_Plugin.available = true;
	var e = document.createEvent('Events');
	//AppMobi.device.hideSplashScreen(); 
	e.initEvent('mdns_plugin.ready',true,true);
	document.dispatchEvent(e);
}

// Global name-prefixed object to store initialization info
if (typeof(MDNS_PluginInfo) != 'object')
    MDNS_PluginInfo = {};

/**
 * This provides a global namespace for accessing information
 */
MDNS_Plugin = {
    queue: {
        ready: true,
        commands: [],
        timer: null
    },
    _constructors: []
};

/**
 * Add an initialization function to a queue that ensures our JavaScript 
 * object constructors will be run only once our module has been loaded
 */
MDNS_Plugin.addConstructor = function(func) {
    var state = document.readyState;
    if ( ( state == 'loaded' || state == 'complete' ) && MDNS_PluginInfo.ready != "undefined" )
	{
		func();
	}
    else
	{
        MDNS_Plugin._constructors.push(func);
	}
};

// Begin Javascript definition of MDNS_Plugin.Worker which bridges to MDNS_PluginWorker
MDNS_Plugin.Worker = function() 
{
}

MDNS_Plugin.Worker.prototype.startWork = function(message, interval)
{
	if( AppMobi.device.platform == "Android" )
		MDNS_PluginWorker.startWork(message, interval);
	if( AppMobi.device.platform == "iOS" )
		AppMobi.exec("MDNS_PluginWorker.startWork", message, interval);
}

MDNS_Plugin.Worker.prototype.stopWork = function()
{
	if( AppMobi.device.platform == "Android" )
		MDNS_PluginWorker.stopWork();
	if( AppMobi.device.platform == "iOS" )
		AppMobi.exec("MDNS_PluginWorker.stopWork");
}

MDNS_Plugin.addConstructor(function() {
    if (typeof MDNS_Plugin.worker == "undefined") MDNS_Plugin.worker = new MDNS_Plugin.Worker();
});

// Begin Javascript definition of MDNS_Plugin.Setup which bridges to MDNS_PluginSetup
MDNS_Plugin.Setup = function() 
{
    this.ready = null;
    try 
	{
		this.ready = MDNS_PluginInfo.ready;
    } 
	catch(e) 
	{
    }
}

MDNS_Plugin.addConstructor(function() {
    if (typeof MDNS_Plugin.setup == "undefined") MDNS_Plugin.setup = new MDNS_Plugin.Setup();
});
