/**
 *  Description: Provides a menu of buttons that direct the user upon starting the application.
 *  Layout File: res/layout/main.xml
 *  Tutorial Used: http://www.droidnova.com/creating-game-menus-in-android,518.html 
 *  Authors: Eric R. Mixon (eraymix@gmail.com) & David W. McDonald (daveomcd@gmail.com)
 */

package com.Nav;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class Menu extends Activity {
	
	// Saves data between Camera and Map views
	private Intent savedData = new Intent(); 
	
	// ID's for Camera, Map, and GPS Activities
	private static final int CAMERA_RESULT = 0;
	private static final int MAP_RESULT = 1;
	private static final int GPS_ENABLE = 2;
	
	// Location Manager for testing if GPS is enabled
	private LocationManager lm;
	
	// Dialog for when GPS is disabled
	private AlertDialog.Builder dialog;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // ContentView is set to specified layout in "res/layout/main.xml"
        setContentView(R.layout.main);	
        
        // Set up dialog for GPS being Disabled
		dialog = new AlertDialog.Builder(this);
		dialog.setMessage("This application requires GPS, would you like to enable it?");
		dialog.setCancelable(false);
		
		// Set up Yes button of Dialog to take user to settings page to enable gps
		dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
		    	Intent GPSIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		    	startActivityForResult(GPSIntent, GPS_ENABLE);				
			}
		});
		
		// Set up No button of Dialog to end the program
		dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
		    	Menu.this.finish();			
			}
		});
        
		// Initialize the Location Manager
		lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        
		// Use Location manager to see if GPS is enabled, if not, show dialog
		if(!lm.isProviderEnabled(LocationManager.GPS_PROVIDER))
		{			
			dialog.show();
		}
		
        /* The below code creates our 3 Menu Buttons */
		
		// Create Button with layout in "res/drawable/camera_button.xml"
        Button SearchButton = (Button)findViewById(R.id.Camera);  	
        
        // Create a new listener for button "Camera"
        SearchButton.setOnClickListener(new OnClickListener() {		
        	
        	// Execute code once the user clicks on "ARCamera" button
        	public void onClick(View v) {	
        		
        		// Create a new Intent for the component ARCamera
        		Intent ARCameraIntent = new Intent(Menu.this,ARCamera.class);
        		
        		// Start the "ARCamera" Activity
        		startActivityForResult(ARCameraIntent, CAMERA_RESULT);	
        	}
        });
        
        // Create Button with layout in "res/drawable/campusmap_button.xml"
        Button MapButton = (Button)findViewById(R.id.Map);	
        
        // Create a new listener for button "Map"
        MapButton.setOnClickListener(new OnClickListener() {
        	
        	// Execute code once the user clicks on "Map" button
        	public void onClick(View v) {						
        		
        		// Create a new Intent for the component CampusMap
        		Intent MapIntent = new Intent(Menu.this,Map.class);
        		
        		// Start the "Map" Activity
        		startActivityForResult(MapIntent, MAP_RESULT);	
        	}
        });
        
        // Create Button with layout in "res/drawable/options_button.xml"
        Button OptionsButton = (Button)findViewById(R.id.Options);		
        
        // Create a new listener for button "Options"
        OptionsButton.setOnClickListener(new OnClickListener() {	
        	
        	// Execute code once the user clicks on "Options" button
        	public void onClick(View v) {							
        		
        		// Create a new Intent for the component Options
        		Intent OptionsIntent = new Intent(Menu.this,Options.class);	
        		
        		// Start the "Map" Activity
        		startActivity(OptionsIntent);							
        	}
        });
    }
    
    // Called whenever an Activity that was started from Menu is finished.
    // requestCode - The integer ID of the Activity that has finished
    // resultCode - Whether the Activity ended with RESULT_OK or RESULT_CANCELLED
    // data - The data that is being passed between map and camera activities
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Determine which Activity was finished
		switch(requestCode)
		{
			case CAMERA_RESULT:
				// If Camera, then backup data and pass it to Map Activity
				if (resultCode == RESULT_OK) {
					savedData = data;
	        		Intent MapIntent = new Intent(Menu.this,Map.class);
	        		MapIntent.putExtras(savedData);
	        		startActivityForResult(MapIntent, MAP_RESULT);
				}
				break;
				
			case MAP_RESULT:
				// If Map, then backup data and pass it to Camera Activity
				if (resultCode == RESULT_OK) {
					savedData = data;
	        		Intent CameraIntent = new Intent(Menu.this,ARCamera.class);
	        		CameraIntent.putExtras(savedData);
	        		startActivityForResult(CameraIntent, CAMERA_RESULT);
				}
				break;
				
			case GPS_ENABLE:
				// If GPS_ENABLE, then re-check to make sure GPS was turned on
				if(!lm.isProviderEnabled(LocationManager.GPS_PROVIDER))
				{
					dialog.show();
				}
				break;
		}
	}
	
	// Called whenever a physical Key is pressed
	// keyCode - Integer ID of the key pressed (KeyEvent.KEYCODE_KEYNAME)
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		// If the back Key was pressed, then finish the program.
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)
		{
			finish();
		}
		
		// else return the normal function of whatever key was pressed
		return super.onKeyDown(keyCode, event);
	}
}