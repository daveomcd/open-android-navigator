/**
 *  Description: GPS Listener class used to access GPS
 *  Tutorial Used: http://www.developer.android.com
 *  Author: Eric R. Mixon (eraymix@gmail.com)
 */

package com.Nav;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

/***************************************************************************************************************
FOR USE INSTRUCTIONS:

IMPORTS:
import android.location.LocationManager;

PRIVATE VARIABLES:
private LocationManager lm;															// Location Manager
private MyLocationListener locationListener;										// Location Listener for GPS

ACTION FOR REGISTRATION:
lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);					// Get Location Service    
locationListener = new MyLocationListener();										// Create Listener object
lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);	// Register Provider

RETRIEVING COORDINATES:
Implement onLocationChanged(Location loc) function inline with instantiation ex:

		locationListener = new GPSListener()
		{
		    public void onLocationChanged(Location loc) 
		    {
		    	if (loc != null)
		    	{
		    		p = new GeoPoint((int) (loc.getLatitude() * 1000000),
		    				(int) (loc.getLongitude() * 1000000));
		    	}
		    }
		};
		
	Note: Coordinates are in Decimal format by default and to store in a GeoPoint you must do IE6 conversion to integer format
 	- Use loc.getLatitude() to get latitude
 	- Use loc.getLongitude() to get longitude
***************************************************************************************************************/

// Class for accessing LocationListener from Android GPS
public class GPSListener implements LocationListener
{   	
    // Called when Location is updated
    public void onLocationChanged(Location loc) {
    	//GPS Location Changed code here
    }
    
    // Called when GPS is disabled
    public void onProviderDisabled(String arg0) {
    	//Gps Disabled Message Goes Here
    }
    
    // Called when GPS is enabled
    public void onProviderEnabled(String arg0) {
    	//Auto
    }
    
    // Called when GPS Status is changed
    public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
    	//Auto
    }
}