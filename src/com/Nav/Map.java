/**
 *  Description: Accesses Google maps and GPS to show current location and a path to destination.
 *  Layout File: res/layout/map.xml
 *  Note: The Layout file has to be edited to contain your own map key before the 
 *  	  maps will work properly. You can obtain your own key by following the
 *  	  intstructions given at http://code.google.com/android/add-ons/google-apis/mapkey.html
 *  
 *  Tutorial Used: http://developer.android.com/guide/tutorials/views/hello-mapview.html 
 *  Authors: Eric R. Mixon (eraymix@gmail.com) & Mark A. Rahaim (ramko601@hotmail.com)
 */

package com.Nav;

import java.util.List;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class Map extends MapActivity {

	// ID Numbers for Menu Items
	private static final int MENU_SEARCH = 0;
	private static final int MENU_CAMERA = 1;
	private static final int MENU_QUIT = 2;
	private static final int MENU_VIEW = 3;
	private static final int MENU_FIND = 4;

	// Result Indexes for retrieving search data
	private static final String resultTitle = "resultTitle";
	private static final String resultLat = "resultLat";
	private static final String resultLng = "resultLng";

	// Search Activity ID
	private static final int SEARCH_RESULT = 0;

	// Location Manager for GPS
	private LocationManager lm;

	// Location Listener for GPS
	private GPSListener locationListener;

	// Drawable for Overlay (this one will point to an empty picture, my actual
	// pictures are retrieved in the MapOverlays class
	private Drawable drawable;

	// Initial default geopoints and overlays
	// These will typically be overwritten before they are used
	// but to prevent Null Exception, I go ahead and initialize them
	private GeoPoint myLocation = new GeoPoint(31327486, -89334391);
	private GeoPoint destination = new GeoPoint(31326330, -89331851);
	private OverlayItem myLocationOverlay = new OverlayItem(myLocation, "Current Location", "You are here");
	private OverlayItem destinationOverlay = new OverlayItem(destination, "", "");

	// Intent for holding data passed between Activities
	private Intent savedData = new Intent();

	// Map overlay object
	private MapOverlays itemizedoverlay;

	// Map Controller
	private MapController mc;

	// Map View
	private MapView mapView;

	// List for Map overlays ( used to draw lines between multiple points in one
	// MapOverlay )
	private List<Overlay> mapOverlays;
	
	// Layout/view for zoom controls
	private LinearLayout zoomLayout;
	private View zoomView;

	/** Called when the activity is first created. */
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// sets map.xml as the content view
		setContentView(R.layout.map);

		// sets the map view
		mapView = (MapView) findViewById(R.id.mapview);
		
		// sets the zoom layout and controls
		zoomLayout = (LinearLayout) findViewById(R.id.zoom);
		zoomView = mapView.getZoomControls();
		zoomLayout.addView(zoomView, new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		
		// Display the zoom controls
		mapView.displayZoomControls(true);
		mc = mapView.getController();

		// initialize the mapOverlays
		mapOverlays = mapView.getOverlays();

		// set the drawable to a picture of nothing (the actual pictures are contained in mayOverlays)
		drawable = this.getResources().getDrawable(R.drawable.nothing);

		// check if the intent has extra data to see if this activity has
		// already been given search information, otherwise it will start
		// a new search
		if (this.getIntent().hasExtra(resultTitle)) {
			// If so, save Intent into savedData then extract relevant data
			savedData = this.getIntent();
			destination = new GeoPoint(
					(int) (savedData.getDoubleExtra(resultLat, 0) * 1000000),
					(int) (savedData.getDoubleExtra(resultLng, 0) * 1000000));
			
			// Set the destination overlay to the searched data
			destinationOverlay = new OverlayItem(destination, "USM", savedData.getStringExtra(resultTitle));
		} else {
			// create a searchIntent for search
			Intent SearchIntent = new Intent(Map.this, Search.class);
			// start search activity
			startActivityForResult(SearchIntent, SEARCH_RESULT);
		}

		// Register the Location Manager
		lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		// Create the Location Listener as a GPSListener
		locationListener = new GPSListener() {
			// Override onLocationChanged to set new geopoint for myLocation and update Overlay
			public void onLocationChanged(Location loc) {
				if (loc != null) {
					myLocation = new GeoPoint((int) (loc.getLatitude() * 1000000),
							(int) (loc.getLongitude() * 1000000));
					myLocationOverlay = new OverlayItem(myLocation, "Current Location","You are here");

					updateMapOverlay();
				}
			}
		}; // Create Listener object

		// Tell location manager to update from GPS_PROVIDER, as often as possible
		// and for whatever distance change is made.
		// If you want to tell the gps to update less often or to not update if
		// the distance changes too much, the you can edit the 0,0
		// in this line, the first 0 is time, the second is distance
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
				locationListener); // Register Provider

		// Update the mapOverlay (refer to function below for what this does specifically)
		updateMapOverlay();

		// animate to position of myLocation (puts myLocation at center of screen)
		mc.animateTo(myLocation);
		
		// set the zoom level to 16
		mc.setZoom(16);
		
		// Draw the mapView
		mapView.invalidate();
	}

	
    // Called whenever an Activity that was started from Map is finished.
    // requestCode - The integer ID of the Activity that has finished
    // resultCode - Whether the Activity ended with RESULT_OK or RESULT_CANCELLED
    // data - The data that is being passed between map and camera activities
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// If result of search and ok, it will extract and set data for Map Activity
		// and then update the overlay.  Otherwise it will try to access saved data
		// and if none exists will will end the Activity.
		if (requestCode == SEARCH_RESULT) {
			if (resultCode == RESULT_OK) {
				savedData = data;
				destination = new GeoPoint(
						(int) (data.getDoubleExtra(resultLat, 0) * 1000000),
						(int) (data.getDoubleExtra(resultLng, 0) * 1000000));
				destinationOverlay = new OverlayItem(destination, "USM", data
						.getStringExtra(resultTitle));

				updateMapOverlay();
			} else if (!savedData.hasExtra(resultTitle)) {
				finish();
			}
		}
	}

	// Creates the menu items that appear when Menu button is pressed
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_SEARCH, 0, "Search");
		menu.add(0, MENU_CAMERA, 0, "Camera");
		menu.add(0, MENU_QUIT, 0, "Quit");
		menu.add(0, MENU_VIEW, 0, "View");
		menu.add(0, MENU_FIND, 0, "Find Me");
		return true;
	}

	// Called whenever Menu item is selected
	// Item is the button that was pressed
	// therefore item.getItemId is the integer id
	// of the button pressed
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_SEARCH:
			// If search button, then start a new search
			Intent SearchIntent = new Intent(Map.this, Search.class);
			startActivityForResult(SearchIntent, SEARCH_RESULT);
			return true;
		case MENU_CAMERA:
			// If camera button, package data into result and return to menu
			// so menu can then activate Camera view with the saved data
			setResult(Activity.RESULT_OK, savedData);
			finish();
			return true;
		case MENU_QUIT:
			// If quit button, finish the activity return to menu with no results
			finish();
			return true;
		case MENU_VIEW:
			// If View button, switch between Satellite and StreetView depending
			// on which one is currently active
			if (mapView.isSatellite()) {
				mapView.setSatellite(false);
				mapView.setStreetView(true);
			} else {
				mapView.setStreetView(false);
				mapView.setSatellite(true);
			}
			return true;
		case MENU_FIND:
			// If Find Me button, animate to myLocation (center myLocation on screen)
			mc.animateTo(myLocation);
			return true;
		}
		return false;
	}

	// Called whenever a physical Key is pressed
	// keyCode - Integer ID of the key pressed (KeyEvent.KEYCODE_KEYNAME)
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		
		// If search button is pressed, start a new search
		if (keyCode == KeyEvent.KEYCODE_SEARCH && event.getRepeatCount() == 0) {
			Intent SearchIntent = new Intent(Map.this, Search.class);
			startActivityForResult(SearchIntent, SEARCH_RESULT);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	// Updates the overlay with the changed overlay items (when a search is ended
	// or when the GPS is updated)
	private void updateMapOverlay() {
		
		// Clears the current overlays from the map
		mapOverlays.clear();
		
		// Re-instantiate itemizedoverlay with new locations
		itemizedoverlay = new MapOverlays(drawable, myLocation, destination, mapView);
		
		// Add updated overlays
		itemizedoverlay.addOverlay(myLocationOverlay);
		itemizedoverlay.addOverlay(destinationOverlay);
		
		// Add new overlays to the map
		mapOverlays.add(itemizedoverlay);
	}

	// Required for a mapActivity
	// unused at this point in development
	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
}