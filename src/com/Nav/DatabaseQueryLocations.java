/**
 * This class adds multiple entries to the database and pulls them back
 * Tutorial used: http://www.hdelossantos.com/2010/01/07/using-a-sqlite-database-in-android/  
 * Original Author Hanly De Los Santos (http://hdelossantos.com)
 * 
 * Edited by Eric R. Mixon (eraymix@gmail.com)
 * Note: I modified most of the functions in this class from the original 
 * to be more useful for our locations table in the database.
 */

package com.Nav;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class DatabaseQueryLocations {
	// Keys and Types for database entries
	private final String LOCATION_NAME_KEY = "location_name";
	private final String LOCATION_NAME_TYPE = "varchar(5)";

	private final String LOCATION_NAME_FULL_KEY = "location_name_full";
	private final String LOCATION_NAME_FULL_TYPE = "varchar(50)";

	private final String LONGITUDE_KEY = "longitude";
	private final String LONGITUDE_TYPE = "decimal";

	private final String LATITUDE_KEY = "latitude";
	private final String LATITUDE_TYPE = "decimal";

	private final String DESCRIPTION_KEY = "description";
	private final String DESCRIPTION_TYPE = "varchar(255)";

	// ArrayList for holding keys and types
	private ArrayList<String> databaseKeys = null;
	private ArrayList<String> databaseKeyType = null;
	
	// New DBAadapter
	private DBAdapter database;

	/**
	 * Initialize the ArrayList
	 * @param context Pass context from calling class.
	 */
	public DatabaseQueryLocations(Context context) {
		// Create an ArrayList of keys and one of the options/parameters
		// for the keys.
		databaseKeys = new ArrayList<String>();
		databaseKeyType = new ArrayList<String>();

		databaseKeys.add(LOCATION_NAME_KEY);
		databaseKeyType.add(LOCATION_NAME_TYPE);

		databaseKeys.add(LOCATION_NAME_FULL_KEY);
		databaseKeyType.add(LOCATION_NAME_FULL_TYPE);

		databaseKeys.add(LONGITUDE_KEY);
		databaseKeyType.add(LONGITUDE_TYPE);

		databaseKeys.add(LATITUDE_KEY);
		databaseKeyType.add(LATITUDE_TYPE);

		databaseKeys.add(DESCRIPTION_KEY);
		databaseKeyType.add(DESCRIPTION_TYPE);

		// Call the database adapter to create the database
		database = new DBAdapter(context, "Locations", databaseKeys,
				databaseKeyType);
		database.open();
	}

	// The original code would create an arraylist of all the entries and then
	// pass them in all at once.  Because we have more columns than the original
	// example, I modified it to go ahead and enter each entry every time the
	// appendData is called.
	
	/**
	 * @param loc_Name location name (abbreviated)
	 * @param loc_name_full full location name
	 * @param lng Longitude of the location
	 * @param lat Latitude of the location
	 * @param descript Description of the location
	 */
	public void appendData(String loc_Name, String loc_name_full, double lng,
			double lat, String descript) {
		// ContentValues for database entry
		ContentValues contentValues = new ContentValues();
		// Adds all keys and relevant data for the contentValues
		for (int i = 0; i < databaseKeys.size(); i++) {
			switch (i) {
			case 0:
				contentValues.put(databaseKeys.get(i), loc_Name);
				break;
			case 1:
				contentValues.put(databaseKeys.get(i), loc_name_full);
				break;
			case 2:
				contentValues.put(databaseKeys.get(i), lng);
				break;
			case 3:
				contentValues.put(databaseKeys.get(i), lat);
				break;
			case 4:
				contentValues.put(databaseKeys.get(i), descript);
				break;
			}
		}
		
		// Inserts contentValues in database
		database.insertLocationEntry(contentValues);
	}

	// Returns a list of all full location names.
	public ArrayList<String> getLocationData() {
		ArrayList<String> list = new ArrayList<String>();
		String keys[] = new String[databaseKeys.size()];

		for (int i = 0; i < databaseKeys.size(); i++) {
			keys[i] = databaseKeys.get(i);
		}

		Cursor results = database.getAllEntries(keys, null, null, null, null,
				LOCATION_NAME_FULL_KEY, " ASC");

		while (results.moveToNext()) {
			list.add(results.getString(results
					.getColumnIndex(LOCATION_NAME_FULL_KEY)));
		}

		return list;
	}

	// Returns list of all full location names containing the passed 
	// searchString in the description
	public ArrayList<String> getSearchedLocationData(String searchString) {
		ArrayList<String> list = new ArrayList<String>();
		String keys[] = new String[databaseKeys.size()];

		for (int i = 0; i < databaseKeys.size(); i++) {
			keys[i] = databaseKeys.get(i);
		}

		Cursor results = database.getAllEntries(keys, null, null, null, null,
				LOCATION_NAME_FULL_KEY, " ASC");

		while (results.moveToNext()) {
			if (results.getString(results.getColumnIndex(DESCRIPTION_KEY))
					.toUpperCase().contains(searchString.toUpperCase())) {
				list.add(results.getString(results
						.getColumnIndex(LOCATION_NAME_FULL_KEY)));
			}
		}

		return list;
	}

	// Returns cursor of all Locations containing the passed searchString
	// in the description
	public Cursor getSearchedLocationDataCursor(String searchString) {
		String keys[] = new String[databaseKeys.size()];

		for (int i = 0; i < databaseKeys.size() - 1; i++) {
			keys[i] = databaseKeys.get(i);
		}

		searchString = LOCATION_NAME_FULL_KEY + " = \"" + searchString + "\"";

		return database.getAllEntries(keys, searchString, null, null, null,
				LOCATION_NAME_FULL_KEY, " ASC");
	}

	// Returns cursor containing all location data
	// (basically returns entire Locations table)
	public Cursor getLocationDataCursor() {
		String keys[] = new String[databaseKeys.size()];

		for (int i = 0; i < databaseKeys.size(); i++) {
			keys[i] = databaseKeys.get(i);
		}

		return database.getAllEntries(keys, null, null, null, null,
				LOCATION_NAME_KEY, " ASC");
	}

	/**
	 * Get data from the table.
	 * @param keys List of columns to include in the result.
	 * @param selection Return rows with the following string only. Null returns all rows.
	 * @param selectionArgs Arguments of the selection.
	 * @param groupBy Group results by.
	 * @param having A filter declare which row groups to include in the cursor.
	 * @param sortBy Column to sort elements by.
	 * @param sortOption ASC for ascending, DESC for descending.
	 * @return Returns an ArrayList<String> with the results of the selected field.
	 */
	public ArrayList<String> getData(String[] keys, String selection,
			String[] selectionArgs, String groupBy, String having,
			String sortBy, String sortOption) {
		ArrayList<String> list = new ArrayList<String>();
		Cursor results = database.getAllEntries(keys, selection, selectionArgs,
				groupBy, having, sortBy, sortOption);
		while (results.moveToNext())
			list.add(results.getString(results.getColumnIndex(sortBy)));
		return list;
	}

	// Clears the database (used before an update)
	public void drop() {
		database.clearTable();
	}

	/**
	 * Destroy the reporter.
	 * @throws Throwable
	 */
	public void destroy() throws Throwable {
		database.close();
	}
}