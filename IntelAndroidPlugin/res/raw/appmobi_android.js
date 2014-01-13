if (typeof(AppMobiInit) != 'object')
    AppMobiInit = {};
    
/**
 * This represents the AppMobi API itself, and provides a global namespace for accessing
 * information about the state of AppMobi.
 * @class
 */
AppMobi = {
    queue: {
        ready: true,
        commands: [],
        timer: null
    },
    _constructors: [],
    jsVersion: '3.0.0'
};

/**
 * Boolean flag indicating if the AppMobi API is available and initialized.
 */
AppMobi.available = false;

/**
 * Add an initialization function to a queue that ensures it will run and initialize
 * application constructors only once AppMobi has been initialized.
 * @param {Function} func The function callback you want run once AppMobi is initialized
 */
AppMobi.addConstructor = function(func) {
    var state = document.readyState;
    if (state == 'loaded' || state == 'complete')
        func();
    else
        AppMobi._constructors.push(func);
};

(function() {
    var timer = setInterval(function() {
        var state = document.readyState;
		if (state == 'loaded' || state == 'complete') {
			clearInterval(timer);
			//run constructors
			while (AppMobi._constructors.length > 0) {
				var constructor = AppMobi._constructors.shift();
				try {
					constructor();
				} catch(e) {
					if (typeof(AppMobi.debug['log']) == 'function')
						AppMobi.debug.log("Failed to run constructor: " + AppMobi.debug.processMessage(e));
					else
						alert("Failed to run constructor: " + e.message);
				}
			}

			// all constructors run, now fire the deviceready event
			AppMobi.available = true;
			var e = document.createEvent('Events');
			e.initEvent('appMobi.device.ready',true,true);
			document.dispatchEvent(e);	
			}
    }, 100);
})();

/**
 * Execute a AppMobi command in a queued fashion, to ensure commands do not
 * execute with any race conditions, and only run when AppMobi is ready to
 * recieve them.
 * @param {String} command Command to be run in AppMobi, e.g. "ClassName.method"
 * @param {String[]} [args] Zero or more arguments to pass to the method
 */
AppMobi.exec = function() {
	//compatibility placeholder on android - needed for iphone
};

/**
 * Internal function used to dispatch the request to AppMobi.  This needs to be implemented per-platform to
 * ensure that methods are called on the phone in a way appropriate for that device.
 * @private
 */
AppMobi.run_command = function() {
	//compatibility placeholder on android - needed for iphone
};

/**
 * This class provides access to the debugging console.
 * @constructor
 */
AppMobi.Debug = function() {
}

/**
 * Utility function for rendering and indenting strings, or serializing
 * objects to a string capable of being printed to the console.
 * @param {Object|String} message The string or object to convert to an indented string
 * @private
 */
AppMobi.Debug.prototype.processMessage = function(message) {
    if (typeof(message) != 'object') {
        return message;
    } else {
        function indent(str) {
            return str.replace(/^/mg, "    ");
        }
        function makeStructured(obj) {
            var str = "";
            for (var i in obj) {
                try {
                    if (typeof(obj[i]) == 'object') {
                        str += i + ":\n" + indent(makeStructured(obj[i])) + "\n";
                    } else {
                        str += i + " = " + indent(String(obj[i])).replace(/^    /, "") + "\n";
                    }
                } catch(e) {
                    str += i + " = EXCEPTION: " + e.message + "\n";
                }
            }
            return str;
        }
        return "Object:\n" + makeStructured(message);
    }
};

/**
 * Print a normal log message to the console
 * @param {Object|String} message Message or object to print to the console
 */
AppMobi.Debug.prototype.log = function(message) {
    if (AppMobi.available)
        AppMobiDebug.log(AppMobi.debug.processMessage(message), { logLevel: 'INFO' });
    else
        console.log(message);
};

/**
 * Print a warning message to the console
 * @param {Object|String} message Message or object to print to the console
 */
AppMobi.Debug.prototype.warn = function(message) {
    if (AppMobi.available)
        AppMobiDebug.log(AppMobi.debug.processMessage(message), { logLevel: 'WARN' });
    else
        console.error(message);
};

/**
 * Print an error message to the console
 * @param {Object|String} message Message or object to print to the console
 */
AppMobi.Debug.prototype.error = function(message) {
    if (AppMobi.available)
        AppMobiDebug.log(AppMobi.debug.processMessage(message), { logLevel: 'ERROR' });
    else
        console.error(message);
};

AppMobi.addConstructor(function() {
    if (typeof AppMobi.debug == "undefined") AppMobi.debug = new AppMobi.Debug();
});

/**
 * this represents the mobile device, and provides properties for inspecting the model, version, UUID of the
 * phone, etc.
 * @constructor
 */
AppMobi.Device = function() {
    this.available = AppMobi.available;
    this.platform = null;
    this.osversion = null;
    this.model = null;
    this.uuid = null;
    this.appmobiversion = null;
    this.orientation = null;
    this.connection = null;

    try {
        this.platform = AppMobiDevice.getPlatform();
        this.osversion = AppMobiDevice.getOSVersion();
        this.model = AppMobiDevice.getModel();
        this.uuid = AppMobiDevice.getUuid();
        this.appmobiversion = AppMobiDevice.getVersion();
        this.orientation = AppMobiDevice.getOrientation();
        this.connection = AppMobiDevice.getConnection();
    } catch(e) {
        this.available = false;
    }
}

AppMobi.Device.prototype.registerLibrary = function(strDelegateName) {
	AppMobiDevice.registerLibrary(strDelegateName);
};

AppMobi.addConstructor(function() {
    if (typeof AppMobi.device == "undefined") AppMobi.device = new AppMobi.Device();
});
