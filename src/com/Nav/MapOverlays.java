/**
 *  Description: Draws the overlay items on top of the Map View
 *  Note: This is the file to edit if you want design your own navigation
 *  	  You may need to change the constructor, current it only accepts 
 *  	  current location and destination and draws a straight line between
 * 		  the two. If you want to do more, you will probably want to accept
 * 		  a list of points and draw lines between all of them. Google also
 * 		  offers built in turn by turn which we do not use for this project. 
 * 		  (but it can be added)
 *  
 *  Tutorial Used: http://developer.android.com/guide/tutorials/views/hello-mapview.html 
 *  Authors: Eric R. Mixon (eraymix@gmail.com) & Mark A. Rahaim (ramko601@hotmail.com)
 */

package com.Nav;

import java.util.ArrayList;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.ItemizedOverlay;

@SuppressWarnings("unchecked")
public class MapOverlays extends ItemizedOverlay {

	// List of added overlays
	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	
	// Geopoints of location and destination
	private GeoPoint myLocation = new GeoPoint(0, 0);
	private GeoPoint destination = new GeoPoint(0, 0);
	
	// Mapview that called this object
	private MapView mapView;
	private Context mContext;

	// Default overlay constructor that only accepts a drawable
	public MapOverlays(Drawable defaultMarker) {
		super(boundCenterBottom(defaultMarker));
	}

	// Adds an overlay to the list
	public void addOverlay(OverlayItem overlay) {
		mOverlays.add(overlay);
		populate();
	}

	// Constructor for taking in drawable and two geoPoints
	public MapOverlays(Drawable defaultMarker, GeoPoint p1, GeoPoint p2,
			MapView mapview) {
		// drawable for our purposes is just a blank picture
		super(boundCenterBottom(defaultMarker));
		
		// Set the Geopoints and mapView
		myLocation = p1;
		destination = p2;
		mapView = mapview;
		mContext = mapView.getContext();
	}

	// Used to Draw a Compass based on bearing
	protected void drawCompass(android.graphics.Canvas canvas, float bearing) {
	}

	@Override
	protected OverlayItem createItem(int i) {
		return mOverlays.get(i);
	}

	// Returns the size of mOverlays
	@Override
	public int size() {
		return mOverlays.size();
	}

	// Constructor that passes in defaultMarker and context
	public MapOverlays(Drawable defaultMarker, Context context) {
		super(defaultMarker);
		mContext = context;
	}

	// Clears overlays
	public void clear() {
		mOverlays.clear();
	}

	// This is the draw function that is called when ever the overlay is displayed
	// This is where the points and and line between them are drawn
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		super.draw(canvas, mapView, shadow);

		// New paint and screen coordinates for drawing
		Paint paint = new Paint();
		Point screenCoords = new Point();
		Point screenCoords1 = new Point();

		// Get the project to where myLocation picture should be drawn based on map
		// position and gps location
		mapView.getProjection().toPixels(myLocation, screenCoords);
		int x1 = screenCoords.x;
		int y1 = screenCoords.y;

		// Get the project to where destination picture should be drawn based on map
		// position and destination location
		mapView.getProjection().toPixels(destination, screenCoords1);
		int x2 = screenCoords1.x;
		int y2 = screenCoords1.y;
		
		// Set paint color to black and width to 4
		paint.setColor(Color.BLACK);
		paint.setStrokeWidth(4);
		
		// Draw a black line from location to destination
		canvas.drawLine(x1, y1, x2, y2, paint);

		// set paint color to yellow and width to 2
		paint.setStrokeWidth(2);
		paint.setColor(Color.YELLOW);
		
		// draw a thin yellow line on top of the black one
		canvas.drawLine(x1, y1, x2, y2, paint);
		
		// Get and draw images of small compass and small eagle in appropriate positions
        Bitmap image = BitmapFactory.decodeResource(mapView.getResources(), R.drawable.flag_green);
        canvas.drawBitmap(image, x1 - (image.getWidth()/2), y1 - (image.getHeight()/2), paint);
        Bitmap image2 = BitmapFactory.decodeResource(mapView.getResources(), R.drawable.flag_red);
        canvas.drawBitmap(image2, x2 - (image2.getWidth()/2), y2 - (image2.getHeight()/2), paint);
	}

	// Called whenever one of the positions is touched
	// This will display a dialog describe the point that was touched
	// index - ID of the item that was touched
	@Override
	protected boolean onTap(int index) {
		OverlayItem item = mOverlays.get(index);
		AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
		dialog.setTitle(item.getTitle());
		dialog.setMessage(item.getSnippet());
		dialog.show();
		return true;
	}
}