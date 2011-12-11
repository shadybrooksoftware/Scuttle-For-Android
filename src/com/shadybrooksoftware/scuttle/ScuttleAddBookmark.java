package com.shadybrooksoftware.scuttle;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class ScuttleAddBookmark extends Activity {
	private String scuttleUsername;
	private String scuttlePassword;
	private String scuttleURL;

	private class SaveBookmark extends AsyncTask<String, Void, Boolean> {
		private String errorMsg = "";

		protected void onPreExecute() {
		}
		protected void onPostExecute(Boolean success) {
			String msg = "";
			if( success == false ) {
		        msg = "Error saving bookmark: " + this.errorMsg;
			}
			else {
				msg = "Bookmark saved";
			}
			Toast.makeText(ScuttleAddBookmark.this, msg, Toast.LENGTH_SHORT).show();
		}
		protected Boolean doInBackground(String... args) {
			String url = args[0];
			String desc = args[1];
			String tags = args[2];
			String status = args[3];
			String scuttleUrl = args[4];
			String scuttleUsername = args[5];
			String scuttlePassword = args[6];
			try {
		        url = scuttleUrl+"api/posts_add.php?url="+URLEncoder.encode(url, "UTF-8")+
		        	"&description="+URLEncoder.encode(desc, "UTF-8")+
		        	"&tags="+URLEncoder.encode(tags, "UTF-8")+
		        	"&status="+URLEncoder.encode(status, "UTF-8");
		        InputStream is = ScuttleBookmarkList.callScuttleURL(url, scuttleUsername, scuttlePassword);
			} catch( SocketTimeoutException ste ) {
				this.errorMsg = "Username and/or password is incorrect.";
				return(false);
			} catch( FileNotFoundException fnfe ) {
				this.errorMsg = "Unable to load URL.  Please check your URL in the Settings.";
				return(false);
			} catch( IOException ioe ) {
				this.errorMsg = "ioe:"+ioe.getMessage();
		    	return(false);
			} catch( Exception e ) {
				this.errorMsg = "e:"+e.getMessage();
		    	return(false);
		    }
			return(true);
		}
	}

	private void getPrefs() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getBaseContext());
		this.scuttleURL = prefs.getString("url", "");
		this.scuttleUsername = prefs.getString("username", "");
		this.scuttlePassword = prefs.getString("password", "");
		// Make sure the last character of the URL is a slash.
		if( this.scuttleURL.length() > 0 ) {
			String last = this.scuttleURL.substring(this.scuttleURL.length()-1);
			if( !last.equals("/") ) {
				this.scuttleURL += "/";
			}
		}
	}
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addbookmark);

        // Setup the Privacy (status) spinner.
        Spinner spinner = (Spinner) findViewById(R.id.addbookmark_status);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.status_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // Get the extras data passed in with the intent.
        Bundle b = this.getIntent().getExtras();
        // Set the text fields to the values of the passed in data.
        EditText txtUrl = (EditText)findViewById(R.id.addbookmark_url);
        txtUrl.setText(b.getCharSequence(Intent.EXTRA_TEXT));
        EditText txtDesc = (EditText)findViewById(R.id.addbookmark_description);
        txtDesc.setText(b.getString(Intent.EXTRA_SUBJECT));
        // Handle when the user presses the save button.
        Button btnSave = (Button)findViewById(R.id.addbookmark_btnsave);
        btnSave.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		getPrefs();
                String strTags = ((EditText)findViewById(R.id.addbookmark_tags)).getText().toString();
                String strUrl = ((EditText)findViewById(R.id.addbookmark_url)).getText().toString();
                String strDesc = ((EditText)findViewById(R.id.addbookmark_description)).getText().toString();
                String strStatus = ((String)((Spinner)findViewById(R.id.addbookmark_status)).getSelectedItem());
        		(new SaveBookmark()).execute(strUrl, strDesc, strTags, strStatus, scuttleURL, scuttleUsername, scuttlePassword);
        		finish();
        	}
        });
    }
}
