/**
 *  Description: Provides a user input for users to enter a search string
 *  Layout File: res/layout/getinput.xml
 *  Tutorial Used: http://developer.android.com
 *  Authors: Eric R. Mixon (eraymix@gmail.com) & David W. McDonald (daveomcd@gmail.com)
 */

package com.Nav;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class GetInput extends Activity
{
	// The text box for user input
	private EditText search = null;
	
	// The button to begin search
	private Button mButton_search = null;
	
	// The intent to store the resulting search string
	private Intent resultIntent = null;
	
	// The index in the intent to store the search string
	private final static String resultString = "searchString";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Set content view to getinput.xml
		setContentView(R.layout.getinput);
		
		// Set the textbox
		search = (EditText)findViewById(R.id.SearchBox);
		search.setText("");

		// Set the search button
        mButton_search = (Button)findViewById(R.id.SearchButton);
		mButton_search.setText("Search");
	
		// Set the search button OnClickListener
		// Called when search button is pressed
		mButton_search.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) 
			{
				// Instantiate the resultIntent
				resultIntent = new Intent();
				
				// Add the search String to the intent by resultString index
				resultIntent.putExtra(resultString, search.getText().toString());
				
				// setResult to RESULT_OK
				setResult(Activity.RESULT_OK, resultIntent);
				
				// Finish which will pass the result back to the calling activity
				finish();
			}
		});
	}
}
