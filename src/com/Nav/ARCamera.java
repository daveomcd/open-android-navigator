/**
 *  Description: This is the Camera view with added augmentations drawn on top
 *  relative to the bearing to the users destination
 *  
 *  Tutorial Used: http://www.devx.com/wireless/Article/42482/1954 
 *  Tutorial Used: http://marakana.com/forums/android/android_examples/39.html
 *  
 *  Authors: Eric R. Mixon (eraymix@gmail.com) & David W. McDonald (daveomcd@gmail.com)
 *  
 *  Note: This activity is still in early stages of development, much of what we have
 *  is only proof of concept and for testing purposes on our part.  Updates and new additions
 *  to this file will be soon to come.
 */

package com.Nav;

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.Camera.Parameters;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;

// ----------------------------------------------------------------------

public class ARCamera extends Activity {
	
	// Id numbers for Menu Items
	private static final int MENU_SEARCH = 0;
	private static final int MENU_MAP = 1;
	private static final int MENU_QUIT = 2;
	
	// Id's for Intent results passed to and from Activities
	private static final String resultTitle = "resultTitle";
	private static final String resultLat = "resultLat";
	private static final String resultLng = "resultLng";
	
	// Id number for Search Activity Result
	private static final int SEARCH_RESULT = 0;
	
	// The degrees of orientation for the overlay to be shown on the camera ( + or - the value)
	private static final Float viewAngleY = (float)40;
	private static final Float viewAngleX = (float)20;
	
	// The degrees of orientation for the Y axis in which the 
	// Application will switch to mapView ( + or - the value)
	private static final int viewAngleS = 10;
	
	// Camera View
	private Preview cv;
	
	// FrameLayout for holding Camera and Draw on Top view's together
	private FrameLayout rl;
	
	// Draw on Top view for drawing on top of the camera
	private DrawOnTop mDraw;
	
	// Location Manager
	private LocationManager lm; 
	
	// Location Listener for GPS
	private GPSListener locationListener; 

	// variables for holding the destination latitude and longitude
	private double destLatPoint = 0;
	private double destLngPoint = 0;
	
	// Sensor Manager and orientationListener to get the phone's Orientation
	private SensorManager sm;
	private SensorEventListener orientationListener = new myEventListener();	
	
	// Variable for holding bearing to destination
	private Float bearing = (float)0;
	
	// Variable for holding distance to destination
	private Float distance = (float)0;
	
	// Variables for holding the pixel positions to draw the Overlay item
	private Integer pixel1 = 0;
	private Integer pixel2 = 0;

	// Intent for savedData that is being passed between Activities
	private Intent savedData = new Intent();
	
	// Direction to turn phone toward destination ( can be 0, 1, or 2)
	// 0 - Straight
	// 1 - left
	// 2 - Right
	private int direction = 0;

	// Width and Height of screen in pixels
	// Used to keep all placements percentage of resolution based
	// meaning that resolution of the phone does not matter
	private Integer resolutionX = 0;
	private Integer resolutionY = 0;
	
	// Called when activity is first created
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// check if the intent has extra data to see if this activity has
		// already been given search information, otherwise it will start
		// a new search
		if(this.getIntent().hasExtra(resultTitle))
		{
			// If so, save Intent into savedData then extract relevant data
			savedData = this.getIntent();
			destLatPoint = savedData.getDoubleExtra(resultLat, 0);
			destLngPoint = savedData.getDoubleExtra(resultLng, 0);
		}
		else
		{
			// create a searchIntent for search
			Intent SearchIntent = new Intent(ARCamera.this, Search.class);
			// start search activity
			startActivityForResult(SearchIntent, SEARCH_RESULT);
		}

		// Create the camera view
		cv = new Preview(this.getApplicationContext());
		
		// Create the drawOnTop view
        mDraw = new DrawOnTop(this);
        
        // Create the FrameLayout and add the camera and mDraw to it
		rl = new FrameLayout(this.getApplicationContext());
        rl.addView(cv);
		rl.addView(mDraw, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        
		// Set the content view to the FrameLayout rl
		setContentView(rl);
		
		// Get Sensor Service for Manager
        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);			
        
        // Register Listener for Orientation sensor
        sm.registerListener(orientationListener, 								
        		sm.getDefaultSensor(Sensor.TYPE_ORIENTATION),
        		SensorManager.SENSOR_DELAY_NORMAL); // sets the speed to NORMAL, 
        		//(GAME and FASTEST can also be used for faster sensing
        
        // Register the Location Manager
		lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		// Create the Location Listener as a GPSListener
		locationListener = new GPSListener()
		{
			// Override onLocationChanged to set bearing and distance to destination
		    public void onLocationChanged(Location loc) 
		    {
		    	if (loc != null)
		    	{
		    		try {
		    			// create a new location l
		    			Location l = new Location(loc);
		    			
		    			// set the latitude and longitude of l to the destination lat and long
		    			l.setLatitude(destLatPoint);
		    			l.setLongitude(destLngPoint);
		    			
		    			// get the bearing from current location to l
		    			bearing = loc.bearingTo(l);
		    			// Note: bearing is given from -180 - 180 in degrees
		    			// The following lines will convert it to 0 - 360 instead
		    			if(bearing < 1)
		    			{
		    				bearing = 360 + bearing;
		    			}
		    			
		    			// set distance to the distance to destination l
		    			distance = loc.distanceTo(l);
		    		} catch (Exception e) {
		    		}
		    	}
		    }
		}; // Create Listener object
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener); // Register Provider
	}
	
	// Implementation of event listener for orientation of the phone
    private class myEventListener implements SensorEventListener				
    {	
    	// Can be used to tell when sensor needs to be reset
    	// Some programs will prompt the user to move their
    	// phone in a figure 8 to reset the sensor when
    	// accuracy gets too low, for our purposes we just
    	// leave this alone
    	public void onAccuracyChanged(Sensor arg0, int arg1)
    	{}

    	// Called when orientation is updated
    	public void onSensorChanged(SensorEvent evt)
    	{
    		// retrieve orientation vales from evt and store them
    		// in vals[]
    		float vals[] = evt.values;
    		
    		// vals[0] contains orientation of the phone in degrees from true north from 0 - 360
    		// subtract the orientation from the bearing to find the difference
    		pixel1 = ((int)(vals[0] - bearing));
    		
    		// For handling the case that bearing and vals[0] are close
    		// but end up being values like 10 and 350 which give
    		// a large or small difference in value.
    		// These statements will normalize the difference
    		// to the proper value.
    		if(pixel1 > (360 - viewAngleX))
    		{
    			pixel1 -= 360;
    		}
    		else if(pixel1 < (-1 * (360 - viewAngleX)))
    		{
    			pixel1 += 360;
    		}
    		
    		// If the pixel1 is within + or - the viewAngleX (facing the destination) then 
    		// set pixel1 to the appropriate position on the screen to lay on top of the building.
    		// Note: pixel1 represents the x pixel <--->
    		if(pixel1 < viewAngleX && pixel1 > (-1 * viewAngleX))
    		{
    			pixel1 = (int) (((-1 * pixel1) + viewAngleX) / (2 * viewAngleX) * resolutionX);
    		
    			// set the direction to 0 because the pixel will be on the screen
    			// and will not need a left or right arrow
    			direction = 0;
    		}
    		else
    		{
    			// otherwise set the direction to 1 or 2 based on whether the pixel is too far right or left
    			if(pixel1 > viewAngleX)
    			{
    				direction = 1;
    			}
    			else
    			{
    				direction = 2;
    			}
    			
    			// set the pixel1 to -1 so it will not try to draw it off the screen
    			pixel1 = -1;
    		}
    		
    		// set pixel2 to the y value of vals[] ( vals[1] )
    		pixel2 = (int)vals[1];
    		
    		// if the screen is laying flat within the viewAngleS
    		// switch to the Map activity
    		if(pixel2 < viewAngleS && pixel2 > -1 * viewAngleS)
    		{
    			setResult(Activity.RESULT_OK, savedData);
    			finish();
    		}
    		
    		// if phone is held upright within + or - viewAngleY, then set pixel2
    		// to be on the appropriate position of the screen
    		if(pixel2 < (-1 * (90 - viewAngleY)) && pixel2 > (-1 * (90 + viewAngleY)))
    		{
    			float pixelHolder = (pixel2 * -1) - (90 - viewAngleY);
    			pixel2 = (int) (pixelHolder / (2 * viewAngleY) * resolutionY);
    		}
    		else
    		{	
    			// Otherwise set pixel2 to -1 so it will not try to be drawn off the screen
    			pixel2 = -1;
    		}
    		
    		// invalidate mDraw, so the Draw On Top view will be updated and onDraw will be called.
    		mDraw.invalidate();
    	}
    }

	class DrawOnTop extends View {

		// Constructor for DrawOnTop
        public DrawOnTop(Context context) {
                super(context);
        }

        // Called when view is set or invalidated (updates what is drawn on the screen)
        protected void onDraw(Canvas canvas) {
        	
        		// paint is for both vertical lines (transparent); paint2 is for the text and bitmaps (no transparency)
                Paint paint = new Paint(); 
                Paint paint2 = new Paint();
                
                // Set the style to Fill, color to white with AA transparency
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(0xAAFFFFFF);
                
                // Set the style to Fill, color white, and text size 25.5
                paint2.setStyle(Paint.Style.FILL);
                paint2.setColor(Color.WHITE);                
                paint2.setTextSize((float) 25.5);
                
                // get the icon picture and store in image
                Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.icon);
                Bitmap l_arrow = BitmapFactory.decodeResource(getResources(), R.drawable.left_arrow);
                Bitmap r_arrow = BitmapFactory.decodeResource(getResources(), R.drawable.right_arrow);                   
                
                // if pixel1 and pixel2 are not = to -1, then draw the image on the screen
                // at location pixel1, pixel2
                if(pixel1 != -1 && pixel2 != -1)
                {
                	canvas.drawBitmap(image, pixel1 - (image.getWidth()/2), pixel2 - (image.getHeight()/2), paint);
                }
                
                // if resolution has not been retrieved
                // get the resolution of this canvas and store in
                // resolutionX and resolutionY
                // (used to keep resolution of the phone from mattering.
                if(resolutionX == 0)
                {
                	resolutionY = canvas.getHeight();
                	resolutionX = canvas.getWidth();
                }
                
                // Determine whether to draw a Right or Left arrow, or two vertical bars to center the icon
                switch(direction)
                {
                	case 0:
                		//Draw Parallel Vertical Lines
                		paint.setStrokeWidth(5);
                		canvas.drawLine((float)(resolutionX * .3), (float)(resolutionY * .3), (float)(resolutionX * .3), (float)(resolutionY * .5), paint);
                		canvas.drawLine((float)(resolutionX * .7), (float)(resolutionY * .3), (float)(resolutionX * .7), (float)(resolutionY * .5), paint);
                		break;
                	case 1:
                		//Draw Left Arrow
                		canvas.drawBitmap(l_arrow, (float)((resolutionX * .3) + (image.getWidth()/2)), (float)((resolutionY * .7) + (image.getHeight())/2), paint2);
                		break;
                	case 2:
                		//Draw Right Arrow
                		canvas.drawBitmap(r_arrow, (float)((resolutionX * .3) + (image.getWidth()/2)), (float)((resolutionY * .7) + (image.getHeight())/2), paint2);
                		break;
                }

                // set distance string = "Distance: distance" then draw text to the screen
                String distanceString = String.format("Distance: %.2f m", distance);
                canvas.drawText(distanceString, (float) (resolutionX * .1), (float) (resolutionY * .9), paint);
                
                // do normal onDraw
                super.onDraw(canvas);
        }

} 

	// Preview for viewing the camera
	public class Preview extends SurfaceView {
		Camera camera;
		SurfaceHolder previewHolder;

		public Preview(Context ctx) {
			super(ctx);

			// Install a SurfaceHolder.Callback so we get notified when the
			// underlying surface is created and destroyed.
			previewHolder = this.getHolder();
			previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
			previewHolder.addCallback(surfaceHolderListener);
		}

		SurfaceHolder.Callback surfaceHolderListener = new SurfaceHolder.Callback() {
			public void surfaceCreated(SurfaceHolder holder) {
				// The Surface has been created, acquire the camera and tell it where
				// to draw.
				camera = Camera.open();

				try {
					camera.setPreviewDisplay(previewHolder);
				} catch (IOException exception) {
				}
			}

			public void surfaceChanged(SurfaceHolder holder, int format,
					int width, int height) {
				// Now that the size is known, set up the camera parameters and begin
				// the preview.
				Parameters params = camera.getParameters();
				params.setPreviewSize(width, height);
				params.setPreviewFormat(PixelFormat.JPEG);
				camera.setParameters(params);
				camera.startPreview();
			}

			public void surfaceDestroyed(SurfaceHolder arg0) {
				// Surface will be destroyed when we return, so stop the preview.
				// Because the CameraDevice object is not a shared resource, it's very
				// important to release it when the activity is paused.
				camera.stopPreview();
				camera.release();
			}
		};

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
				destLatPoint = data.getDoubleExtra(resultLat, 0);
				destLngPoint = data.getDoubleExtra(resultLng, 0);
			}
			else if(!savedData.hasExtra(resultTitle))
			{
				finish();
			}
		}
	}
	
	// Creates the menu items that appear when Menu button is pressed
	public boolean onCreateOptionsMenu(Menu menu){
		menu.add(0, MENU_SEARCH, 0, "search");
		menu.add(0, MENU_MAP, 0, "Map");
		menu.add(0, MENU_QUIT, 0, "Quit");
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
			Intent SearchIntent = new Intent(ARCamera.this, Search.class);
			startActivityForResult(SearchIntent, SEARCH_RESULT);
	        return true;
	    case MENU_MAP:
			// If map button, package data into result and return to menu
			// so menu can then activate Map view with the saved data
			setResult(Activity.RESULT_OK, savedData);
			finish();
	        return true;
	    case MENU_QUIT:
	    	// If quit button, finish the activity return to menu with no results
	    	finish();
	    	return true;
	    }
	    return false;
	}
	
	// Called whenever a physical Key is pressed
	// keyCode - Integer ID of the key pressed (KeyEvent.KEYCODE_KEYNAME)
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		// If search button is pressed, start a new search
		if (keyCode == KeyEvent.KEYCODE_SEARCH && event.getRepeatCount() == 0)
		{
			Intent SearchIntent = new Intent(ARCamera.this, Search.class);
			startActivityForResult(SearchIntent, SEARCH_RESULT);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}