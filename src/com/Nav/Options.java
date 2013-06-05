/**
 *  Description: Provides Options for user to update database,
 *  could be used for other options such as settings and logins...
 *  Layout File: res/layout/options.xml 
 *	Authors: Eric R. Mixon (eraymix@gmail.com) & David W. McDonald (daveomcd@gmail.com)
 */

package com.Nav;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class Options extends Activity {

	// ID number for Update activity
	private final static int UPDATE = 0;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		// ContentView is set to specified layout in "res/layout/options.xml"
        setContentView(R.layout.options);
        
        // 'updatemsg' is described in "res/layout/options.xml"
        TextView tv = (TextView)findViewById(R.id.updatemsg);	
        
        // The HEX color value is constructed like so... 0xAARRGGBB where,
		// AA = alpha(transparency) value, RR = red color value, GG = green color value, BB = blue value
        tv.setTextColor(0xFF726c06);
        
        // here is a msg that appears on the options page below our button
        tv.setText("Update: Manually gather the lastest data provided by your school."); 
        
        // Create the Update button
        Button SearchButton = (Button)findViewById(R.id.Update); 
        
        // Create a new listener for button "Options"
        SearchButton.setOnClickListener(new OnClickListener() {	
        	public void onClick(View v) {
        		// Create a new Intent for the component Update
        		Intent UpdateIntent = new Intent(Options.this , Update.class);
        		
        		// Start the Update Activity
        		startActivityForResult(UpdateIntent, UPDATE);	
        	}
        });
	}
	
    // Called whenever an Activity that was started from Menu is finished.
    // requestCode - The integer ID of the Activity that has finished
    // resultCode - Whether the Activity ended with RESULT_OK or RESULT_CANCELLED
    // data - The data that is being passed between activities
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == UPDATE) {
			if (resultCode == RESULT_OK) {
				// Displays a message that Update was successful if Update returns RESULT_OK
				Toast.makeText(this, "Update Successful!", Toast.LENGTH_SHORT).show();
			}
		}
	}
}