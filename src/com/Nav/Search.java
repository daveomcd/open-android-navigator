/**
 *  Description: ListActivty used for users to search the database and select a location.
 *  Tutorial Used: http://www.developer.android.com
 *  Tutorial used: http://www.hdelossantos.com/2010/01/07/using-a-sqlite-database-in-android/  
 *  Author: Eric R. Mixon (eraymix@gmail.com)
 */

package com.Nav;

import java.util.ArrayList;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class Search extends ListActivity {
	
	// List for holding the resulting Location Names
	private ArrayList<String> queryString;
	
	// Database query Object for accessing Locations database
	private DatabaseQueryLocations query;
	
	// Intent ID for GetInput Activity
	private static final int SEARCH_RESULT = 0;
	
	// Titles for Intent Extras
	private final static String resultInput = "searchString";
	private final static String resultTitle = "resultTitle";
	private final static String resultLat = "resultLat";
	private final static String resultLng = "resultLng";
	
	// Column Indexes for Database Cursor
	private final String LATITUDE_KEY = "latitude";
	private final String LONGITUDE_KEY = "longitude";
	private final String LOCATION_NAME_FULL_KEY = "location_name_full";
	
	// Intent for storing results and passing back to calling Activity
	private Intent resultIntent;
	
	// Message for ListView whenever no results are found in a search
	private final static String noResults = "No Results Found";
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Instantiate a new DatabaseQueryLocations which is used to access Locations database
		query = new DatabaseQueryLocations(this);
		
		// Instantiate new queryString
		queryString = new ArrayList<String>();
		
		// Pulls all data from Location database
		queryString = query.getLocationData();

		// Test if database has anything in it, if not, call an update
		if (queryString.size() < 1) {
			Intent UpdateIntent = new Intent(Search.this, Update.class);
			startActivity(UpdateIntent);
		}

		// Create an Intent for the GetInput Activity
		Intent InputIntent = new Intent(Search.this, GetInput.class);
		
		// Start the GetInput Activity
		startActivityForResult(InputIntent, SEARCH_RESULT);
	}

    // Called whenever an Activity that was started from Menu is finished.
    // requestCode - The integer ID of the Activity that has finished
    // resultCode - Whether the Activity ended with RESULT_OK or RESULT_CANCELLED
    // data - The data that is being passed from getInput
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == SEARCH_RESULT) {
			if (resultCode == RESULT_OK) {
				//Re-instantiate queryString to store the results
				queryString = new ArrayList<String>();
				
				// Pull the data from the database that contains the searchString
				// data.getStringExtra(resultInput) gets the search string out of data
				queryString = query.getSearchedLocationData(data.getStringExtra(resultInput));
				
				// If there are no results for the search, add a "No Results" String to the queryString
				if(queryString.isEmpty())
				{
					queryString.add(noResults);
				}
				
				// Set the ListView to show everything in queryString in a List
				setListAdapter(new ArrayAdapter<String>(this,
						android.R.layout.simple_list_item_1, queryString));
				
				// Displays the listview
				getListView().setTextFilterEnabled(true);
			}
			else
			{
				// If user canceled the search, finish the activity without doing anything
				setResult(Activity.RESULT_CANCELED);
				finish();
			}
		}
	}

	// Called whenever a selection is made from the listView
	// position is the integer position in the list that was selected
	protected void onListItemClick(ListView l, View v, int position, long id) {
		
		// If "No Results" was selected, start a new getInput
		if(queryString.get(position) == noResults)
		{
			Intent InputIntent = new Intent(Search.this, GetInput.class);
			startActivityForResult(InputIntent, SEARCH_RESULT);
			return;
		}
		
		// perform a new query of the returned selection to retrieve the entire row for that item in the database
		Cursor dataCursor = query.getSearchedLocationDataCursor(queryString.get(position));
		
		// Set the position of the cursor to first (there should only be one row)
		dataCursor.moveToFirst();
		
		// instantiate a new Intent
		resultIntent = new Intent();
		
		// Add extra data to the intent for the location name that was selected
		// along with the Latitude and Longitude of the location
		resultIntent.putExtra(resultTitle, dataCursor.getString(dataCursor.getColumnIndex(LOCATION_NAME_FULL_KEY)));
		resultIntent.putExtra(resultLat, dataCursor.getDouble(dataCursor.getColumnIndex(LATITUDE_KEY)));
		resultIntent.putExtra(resultLng, dataCursor.getDouble(dataCursor.getColumnIndex(LONGITUDE_KEY)));
		
		// Set the result of the Activity to RESULT_OK, and append the intent
		// containing all the location data
		setResult(Activity.RESULT_OK, resultIntent);
		
		// Finish the activity
		finish();
	}
	
	// Called whenever a physical Key is pressed
	// keyCode - Integer ID of the key pressed (KeyEvent.KEYCODE_KEYNAME)
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		// If the back key is pressed, called a new GetInput to start new search
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)
		{
			Intent InputIntent = new Intent(Search.this, GetInput.class);
			startActivityForResult(InputIntent, SEARCH_RESULT);
			return true;
		}
		
		// else return the normal function of whatever key was pressed
		return super.onKeyDown(keyCode, event);
	}
}