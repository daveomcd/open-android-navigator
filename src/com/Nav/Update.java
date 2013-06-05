/**
 *  Description: Update Activity for reading information from remote server and storing to database
 *	
 *	Notes: This procedure is very rough for the moment.  Currently we only host a text file containing
 *	the information dumped from our locations database stored on a remote server and then read from the
 *	flat text file into the local database on the phone.
 *
 *	We are currently working on better ways for this procedure, such as using ksoap, but any examples
 *	anyone else would like to share would be welcome to us.
 *
 *	The current format of the text file we read from is:
 *
 *	Abbreviated Location Name
 *	Full Location Name
 *	Longitude (Decimal Format)
 *	Latitude (Decimal Format)
 *	Description (List of keywords used for searching)
 *
 *	This just repeats for each item in the database.  We are bouncing between several methods of improving
 *	and securing this process, but for the time being, we are simply hosting this text file on our webserver	
 *
 *  Author: Eric R. Mixon (eraymix@gmail.com) & David W. McDonald (daveomcd@gmail.com)
 */

package com.Nav;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;
import org.apache.http.util.ByteArrayBuffer;

public class Update extends Activity {
	
	// Database query Object for accessing Locations database
	private DatabaseQueryLocations query;
	
	// String for storing the information from off the webpage
	private String myString;
	
	// Scanner for parsing the string of information
	private Scanner s;
	
	// ProgressDialog to show an updating bar
	private ProgressDialog dialog;
	
	// Called when the activity is first created. 
	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
		// Instantiate a new dialog with message "Updating..."
        dialog = new ProgressDialog(this);
        dialog.setMessage("Updating...");
        dialog.setCancelable(false);
        
        // Show the dialog
        dialog.show();

        // Start a new background thread to read coordinates  and
        // information from remote webpage
		new URLConnect().execute();
	}
	
	@SuppressWarnings("unchecked")
	
	// Class for creating background update thread
	private class URLConnect extends AsyncTask {

		// Called whenever class is executed, runs code in background
		@Override
		protected Object doInBackground(Object... params) {
			
			// Instantiate new databaseQueryLocations 
			query = new DatabaseQueryLocations(Update.this);
			
			// Drop all current information in preparation of updating new information
			// Note: This will be replaced in the future whenever the update class
			// 		 is redone to be more practical and more secure.
			query.drop();
			
			// Try to connect
			try {
				
				// Define the URL we want to load data from.
				URL myURL = new URL("http://www.mydomain.com/location_of_file.txt");
				
				// Open a connection to that URL.
				URLConnection ucon = myURL.openConnection();

				// Define InputStreams to read from the URLConnection.
				InputStream is = ucon.getInputStream();
				BufferedInputStream bis = new BufferedInputStream(is);

				// Read bytes to the Buffer until there is nothing more to read(-1).
				ByteArrayBuffer baf = new ByteArrayBuffer(50);
				int current = 0;
				while ((current = bis.read()) != -1) {
					baf.append((byte) current);
				}

				// Convert the Bytes read to a String.
				myString = new String(baf.toByteArray());
			} catch (Exception e) {
				// On any Error we want to display it.
				myString = e.getMessage();
			}
			
			// instantiate the scanner to the string we read from the webpage
			s = new Scanner(myString);

			// Variables for parsing data from string
			String locName; 	// Location Name (short version)
			double lng; 		// Longitude
			double lat; 		// Latitude
			String desc;		// Description (used for searching database)
			String locNameFull; // The full Location Name

			// Parse through entire string
			while (s.hasNext()) {
				locName = s.nextLine();
				locNameFull = s.nextLine();
				lng = s.nextDouble();
				lat = s.nextDouble();
				s.nextLine();
				desc = s.nextLine();
				
				// Append this row of data to database
				query.appendData(locName, locNameFull, lng, lat, desc);
			}

			// Destroy database query object
			try {
				query.destroy();
			} catch (Throwable e) {
				e.printStackTrace();
			}
			
			// Dismiss updating Dialog
			dialog.dismiss();
			
			// Update Successful so return RESULT_OK
			Update.this.setResult(RESULT_OK);
			
			// Finish Update
			Update.this.finish();
			
			return null;
		}
	}
}

