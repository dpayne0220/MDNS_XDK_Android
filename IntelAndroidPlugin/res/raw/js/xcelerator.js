/**
 *	@class Toca 
 *  @classdesc This class is for communicating with a physical device from the onscreen display.
 *	
 *	@author Nuvation Research Corp. (Canada)
 *	@version 0.3
 */
var Toca = {
    baseUrl: 'http://localhost/jsondump.php', // http://192.168.1.88/jsondump.php  http://192.168.1.64/service
    userDetails: {
    	firstName: null,
    	lastName: null,
    	admin: 0,
    	mode: null,
    },
    userKey: null,
    userDrills: null,
    selectedNetwork: null,
    drillFilters: [],
	trajectories: [{"id":0,"name":"Foot (Ground)"},{"id":1,"name":"Foot (Air)"},{"id":2,"name":"Thigh"},{"id":3,"name":"Chest"},{"id":4,"name":"Head"}],
	locations: [{"id":1,"name":"Left"},{"id":2,"name":"Center"},{"id":3,"name":"Right"}],
	categories: [{"id":1,"name":"Receiving"},{"id":2,"name":"Heading"},{"id":3,"name":"Finish/Shooting"},{"id":4,"name":"Goal Keeping"}],
	asynchronous: false,
    /*
    * Authentication
    */

    /**
     *	Authenticates a user with the master website.<br />
     *	<br />
     *	Sucess Object: {status: "pass", firstName: "FIRST_NAME", lastName: "LAST_NAME"}<br />
     *	Failure Object: {status: "fail"}
     *	
     *  @public
     *  @method
     *  @memberof Toca
     *	@param {string} username - the username of the person to login as
     *	@param {string} password - the password for the user
     *	@returns a JSON object with a status property indicating "pass" or "fail"
    */
    login: function(username, password) {
	
    	var authenticationStatus = this._doAction('login', {userName: username, password: password});
		authenticationStatus.done(function(authenticationStatus) {
    	if (authenticationStatus) {
		//alert("pass"+JSON.stringify(Toca.userDetails.firstName));
        	if (authenticationStatus.status == 'pass') {
			//change to local storage
			localStorage.setItem("firstName", JSON.stringify(authenticationStatus.payload.firstName));
        		//Toca.userDetails.firstName = authenticationStatus.payload.firstName;
        		//Toca.userDetails.lastName = authenticationStatus.payload.lastName;
			localStorage.setItem("lastName", JSON.stringify(authenticationStatus.payload.lastName));
        		//Toca.userDetails.admin = authenticationStatus.payload.admin;
			localStorage.setItem("admin", JSON.stringify(authenticationStatus.payload.admin));
        		//Toca.userDetails.mode = authenticationStatus.payload.offlineMode;
			localStorage.setItem("offlineMode", JSON.stringify(authenticationStatus.payload.offlineMode));
        		//Toca.userKey = authenticationStatus.payload.userKey;
				
			localStorage.setItem("userKey", authenticationStatus.payload.userKey);
        		//Toca.userDrills = authenticationStatus.payload.userDrills;
			localStorage.setItem("userDrills", JSON.stringify(authenticationStatus.payload.userDrills));
        		//Toca.trajectories = authenticationStatus.payload.trajectories;
			localStorage.setItem("trajectories", JSON.stringify(authenticationStatus.payload.trajectories));
        		//Toca.locations = authenticationStatus.payload.locations;
			localStorage.setItem("locations", JSON.stringify(authenticationStatus.payload.locations));
        		//Toca.categories = authenticationStatus.payload.categories;
			localStorage.setItem("categories", JSON.stringify(authenticationStatus.payload.categories));
				
        	}    		
    	}
		
		});

        return authenticationStatus;
    }, 
    /**
     *	Logs a user out of the local device.
     *	
     *  @public
     *  @method
     *  @memberof Toca
     *	@returns true on success
     */
    logout: function() {
        var logoutStatus = this._doAction('logout');
        
        if (logoutStatus) {
            if (logoutStatus.status == 'pass') {
				
			localStorage.setItem("firstName", JSON.stringify(authenticationStatus.payload.firstName));
			localStorage.setItem("lastName", null);
			localStorage.setItem("admin", null);
			localStorage.setItem("offlineMode", null);
			localStorage.setItem("userKey", null);
			localStorage.setItem("userDrills", null);
			localStorage.setItem("trajectories", null);
			localStorage.setItem("locations", null);
			localStorage.setItem("categories", null);
            }        	
        }
        
        return logoutStatus;
    },
    isAdmin: function() {
    	return this.userDetails.admin;
    },
    /*
    * Device
    */    
    
    /**
     *	Polls a device for general status.  Returns a JSON object that has the status 
     *	of multiple internal states.
     *	
     *  @public
     *  @method
     *  @memberof Toca
     *	@returns a JSON object indicating the states of various internal processes and 
     *	hardware.
     */
    pollDeviceStatus: function(suppressSpinner) {
    	suppressSpinner = suppressSpinner === undefined ? false : true;

        return this._doAction('pollDeviceStatus', null, suppressSpinner);
    },  
    
    /*
    * Wifi
    */    
    
    /**
     *	Scans the devices' surrounding area for visible Wifi networks.
     *	
     *  @public
     *  @method
     *  @memberof Toca
     *	@returns a JSON object representing all surrounding networks
     */
    scanWifi: function() {
        return this._doAction('wifiScan');
    },
    /**
     *	Attempts to connect the device to a given network using the supplied password.
     *	
     *  @public
     *  @method
     *  @memberof Toca
     *	@param {string} ssid - the network SSID to attempt to connect with
     *	@param {string} password - the password used to connect to a protected network
     *	@returns a JSON object containing information on the connection attempt
     */
    connectWifi: function(password) {
        return this._doAction('wifiConnect', {"network":Toca.selectedNetwork, "password": password});
    },
    /**
     *	Disconnects the device from it's current Wifi network.
     *	
     *  @public
     *  @method
     *  @memberof Toca
     *	@returns true on successful disconnect or false on failure
     */
    disconnectWifi: function() {
        return this._doAction('wifiDisconnect');
    },  
    resetEthernet: function() {
    	return this._doAction('resetEthernet');
    },
    /*
    * Drills
    */    
    
    /**
     *	Synchronizes all drills from the master website with the device for the 
     *	given user.
     *	
     *  @public
     *  @method
     *  @memberof Toca
     *	@returns true if the devices' drills were synced with the master website
     */
    cloudSyncDrills: function() {    	
        return this._syncDrills('syncDrills');
    },
    usbImportDrills: function() {
    	return this._syncDrills('syncDrillsFromUsb');
    },
    _syncDrills: function(mode) {
    	mode = mode ? mode : 'syncDrills';
		
    	var syncResult = this._doAction(mode == 'syncDrills' ? 'syncDrills' : 'syncDrillsFromUsb');
    	
    	if (syncResult) {
    		if (syncResult.status == 'pass') {
				localStorage.setItem("userDrills", JSON.stringify(syncResult.payload.userDrills));
    			//this.userDrills = syncResult.payload.userDrills;
			
    		}
    	}
    	
        return syncResult;    	
    },
    /**
     *	Adds a drill to the devices' queue.  This will only work if the device is 
     *	currently not running a drill or already has a drill waiting to be queued.
     *	
     *  @public
     *  @method
     *  @memberof Toca
     *	@param {string} - the UUID of the drill to queue
     *	@returns true when the drill is added successfully or false otherwise
     */
    queueDrill: function(drillUUID) {
        return this._doAction('queueDrill', {drill: this.getDrill(drillUUID)});
    },
    clearQueue: function() {
        return this._doAction('clearDrillQueue');
    },
    /**
     *	Runs the currently queued drill.
     *	
     *  @public
     *  @method
     *  @memberof Toca
     *  @param {string} - the UUID of the drill to run
     *	@returns the result of the drill that was ran
     */
    runDrill: function() {
        return this._doAction('runDrill');;
    },
    /**
     *	Stops the currently running drill.
     *	
     *  @public
     *  @method
     *  @memberof Toca
     *  @param {string} - the UUID of the drill to stop
     *	@returns true on success or false on failure
     */
    stopDrill: function() {
        return this._doAction('stopDrill');
    },
    /**
     *	Retrives any results stored on the device for a drill.
     *	
     *  @public
     *  @method
     *  @memberof Toca
     *	@param {string} - the UUID of the drill to find results for
     *	@returns a JSON object with all results for a given drill
     */
    getDrillResults: function(drillUuid) {
        return this._doAction('drillResults', {drillUuid: drillUuid});
    },
    /**
     * Looks in the local cache of user drills for a drill with a matching drillUUID
     * and returns it when found.
     * 
     * @public
     * @method
     * @memberof Toca
     * @param {string} drillUUID - the UUID of the drill to retreive from the local datastore
     * @returns a JSON object representing a drill or null if no match is found
     */
    getDrill: function(drillUUID) {
    	var drill = null;
		var userDrills = JSON.parse(localStorage.getItem("userDrills"));
    	if (drillUUID && userDrills.length > 0) {
    		for (var i = 0; i < userDrills.length; i++) {
    			if (userDrills[i].uuid == drillUUID) {
    				drill = userDrills[i];
    				break;
    			}
    		}
    	}
    	
    	return drill;
    },
    /**
     * Looks up a human readable trajectory for a given trajectory id.
     * 
     * @param {string} trajectoryId - the id of the trajectory to translate
     * @returns a human readable string that corresponds to the given trajectory id
     */
    getTrajectory: function(trajectoryId) {
    	var trajectoryName = '';
    	
    	$.each(this.trajectories, function(index, trajectory) {
    		if (trajectory.id == trajectoryId) {
    			trajectoryName = trajectory.name;
    			return false;
    		}
    	});
    	
    	return trajectoryName;
    },
    /**
     * Looks up a human readable location for a given location id.
     * 
     * @param {string} locationId - the id of the location to translate
     * @returns a human readable string that corresponds to the given location id
     */
    getLocation: function(locationId) {
    	var locationName = '';
    	
    	$.each(this.locations, function(index, location) {
    		if (location.id == locationId) {
    			locationName = location.name;
    			return false;
    		}
    	});
    	
    	return locationName;    	
    },
    
    /*
     * System
     */
    
    /**
     * Upgrades the device firmware providing that a USB stick containing a new 
     * firmware build is connected to the device.
     */
    upgradeDeviceFromUsb: function() {
    	return this._doAction('upgrade');
    },
    upgradeDeviceFromCloud: function() {
    	return this._doAction('cloudUpgrade');
    },    
    getOptions: function() {
    	return this._doAction('getUnitOptions');
    },
    setOptions: function(options) {
    	return this._doAction('setUnitOptions', options);
    },
    calibrateMax: function() {
    	return this._doAction('calibrateMax');
    },
    calibrateHome: function() {
    	return this._doAction('calibrateHome');
    },
    getIpInfo: function() {
		return this._doAction('ipList');

    },
    shutdown: function() {
    	return this._doAction('shutdown');
    },
    setAsynchronous: function(asynchronous) {
    	this.asynchronous = asynchronous == undefined ? false : asynchronous;
    },
    setFilter: function(drillCategoryId) {
    	if (!isNaN(drillCategoryId)) {
    		drillCategoryId = parseInt(drillCategoryId);
    		
        	if (this.drillFilters.length == 0) {
        		this.drillFilters.push(drillCategoryId);
        	} else {
        		if ($.inArray(drillCategoryId, this.drillFilters) == -1) {
        			this.drillFilters.push(drillCategoryId);
        		}
        	}    		
    	}
    },
    removeFilter: function(drillCategoryId) {
    	var itemIndex = $.inArray(parseInt(drillCategoryId), this.drillFilters);
    	
    	if (itemIndex >= 0) {
    		this.drillFilters.splice(itemIndex, 1);
    	}
    },
    getFilteredDrills: function() {
    	var filteredDrills = [];
    	var drillFilters = this.drillFilters;

    	if (this.drillFilters.length == 0) {
    		filteredDrills = this.userDrills;
    	} else {
    		$.each(this.userDrills, function(drillIndex, drill) {
    			$.each(drill.drillCategories, function(categoryIndex, category) {
    				if ($.inArray(category.id, drillFilters) >= 0) {
    					var listHasDrill = false;
    					
    					// only add the drill if it's not already present
    					$.each(filteredDrills, function(filteredDrillIndex, filteredDrill) {
    						if (filteredDrill.uuid == drill.uuid) {
    							listHasDrill = true;
    							return 0;
    						}
    					});
    					
    					if (!listHasDrill) {
    						filteredDrills.push(drill);
    					}
    				}
    			});
    		});
    	}

    	return filteredDrills;
    },
    /**
     *	Performs an callback to the Toca device with the JSON data provided 
     *	as the request body.
     *	
     *  @private
     *  @method
     *  @memberof Toca
     *	@param {string} requestType - a value the machine will use to determine what action(s) it needs to perform
     *	@param {object} data - a JSON object
     *  @param {boolean} asynchronous - a flag to tell the function to do a blocking call, defaults to false
     *	@returns a JSON object
     */
    _doAction: function(requestType, data, suppressSpinner) {    	
        var status = null;
        data = data == null ? [] : data;
        suppressSpinner = suppressSpinner === undefined ? false : true;
        if (!suppressSpinner) {
        	//$.mobile.showPageLoadingMsg();
        }
		
		return $.ajax({
            url: this.baseUrl,
			jsonp: 'callback',
            dataType: 'jsonp',
			crossDomain: true,
            data: 'jdata='+JSON.stringify({
            	requestType: requestType,
                userKey: localStorage.getItem("userKey"),
                payload: data
            }),
            success: function(result) {
				status = result;
				localStorage.setItem("status", JSON.stringify(status));
				
				//alert("success:");			
			},
            error: function(result) {alert('error:'+JSON.stringify(result));} 
			
        });

		//var status = JSON.parse(localStorage.getItem("status"));
		//alert("return: "+JSON.stringify(status));
        //return status;
        

    },
	doRequest: function(data,requestType) {
	//return  
	
	}
};