package com.shadybrooksoftware.scuttle;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Xml;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;


public class ScuttleBookmarkList extends ListActivity {
	private String scuttleUsername;
	private String scuttlePassword;
	private String scuttleURL;
	private String filterTag;

	static InputStream callScuttleURL(String url, String user, String pass) throws IOException {
        Authenticator.setDefault(new ScuttleAuthenticator(user, pass));
        HttpURLConnection c = (HttpURLConnection)(new URL(url).openConnection());
        c.setUseCaches(false);
        c.setConnectTimeout(1500);
        c.setReadTimeout(300);
        c.connect();
        InputStream is = c.getInputStream();
        return(is);
	}
	static class ScuttleAuthenticator extends Authenticator {
		private String username, password;
		
		public ScuttleAuthenticator(String user, String pass) {
			this.username = user;
			this.password = pass;
		}
		protected PasswordAuthentication getPasswordAuthentication() {
			return( new PasswordAuthentication(this.username, this.password.toCharArray()) );
		}
	}
	private class RetrieveTags extends AsyncTask<String, Void, List<HashMap<String, String>>> {
		private AlertDialog alertLoading;
		private String errorMsg = "";

		protected void onPreExecute() {
	        AlertDialog.Builder builder = new AlertDialog.Builder(ScuttleBookmarkList.this);
	        builder.setMessage("Loading tags...");
	        alertLoading = builder.create();
	        alertLoading.show();
		}
		protected void onPostExecute(List<HashMap<String, String>> tags) {
			alertLoading.dismiss();
			if( tags != null ) {
				displayTagChooser(tags);
			}
			else {
				Toast.makeText(ScuttleBookmarkList.this, this.errorMsg, Toast.LENGTH_LONG).show();
			}
		}
		protected List<HashMap<String, String>> doInBackground(String... args) {
			String url = args[0];
			String user = args[1];
			String pass = args[2];
			try {
		        url = url+"api/tags_get.php";
		        InputStream is = ScuttleBookmarkList.callScuttleURL(url, user, pass);
		        ScuttleTagXMLHandler handler = new ScuttleTagXMLHandler();
		        Xml.parse(is, Xml.Encoding.ISO_8859_1, handler);
		        return(handler.getTags());
			} catch( SocketTimeoutException ste ) {
				this.errorMsg = "Username and/or password is incorrect.";
				return(null);
			} catch( FileNotFoundException fnfe ) {
				this.errorMsg = "Unable to load URL.  Please check your URL in the Settings.";
				return(null);
			} catch( IOException ioe ) {
				this.errorMsg = "ioe:"+ioe.getMessage();
		    	return(null);
			} catch( Exception e ) {
				this.errorMsg = "e:"+e.getMessage();
		    	return(null);
		    }
		}
	}
	private class RetrieveBookmarks extends AsyncTask<String, Void, List<HashMap<String, String>>> {
		private AlertDialog alertLoading;
		private String errorMsg = "";
		
		protected void onPreExecute() {
	        AlertDialog.Builder builder = new AlertDialog.Builder(ScuttleBookmarkList.this);
	        builder.setMessage("Loading bookmarks...");
	        alertLoading = builder.create();
	        alertLoading.show();
		}
		protected void onPostExecute(List<HashMap<String, String>> bmarks) {
			alertLoading.dismiss();
			if( bmarks != null ) {
				displayBookmarks(bmarks);
			}
			else {
				Toast.makeText(ScuttleBookmarkList.this, this.errorMsg, Toast.LENGTH_LONG).show();
			}
		}
		protected List<HashMap<String, String>> doInBackground(String... args) {
			String url = args[0];
			String user = args[1];
			String pass = args[2];
			try {
		        url = url+"api/posts_all.php";
		        // If a valid tag has been specified by the user then we only 
		        // get bookmarks with that tag.
		        if( filterTag != null && !filterTag.equals("") && !filterTag.equals(getResources().getString(R.string.tagslist_notags)) ) {
		        	url += "?tag="+filterTag;
		        }
		        InputStream is = ScuttleBookmarkList.callScuttleURL(url, user, pass);
		        ScuttleBookmarkXMLHandler handler = new ScuttleBookmarkXMLHandler();
		        Xml.parse(is, Xml.Encoding.ISO_8859_1, handler);
		        return(handler.getBookmarks());
			} catch( SocketTimeoutException ste ) {
				this.errorMsg = "Username and/or password is incorrect.";
				return(null);
			} catch( FileNotFoundException fnfe ) {
				this.errorMsg = "Unable to load URL.  Please check your URL in the Settings.";
				return(null);
			} catch( IOException ioe ) {
				this.errorMsg = "ioe:"+ioe.getMessage();
		    	return(null);
			} catch( Exception e ) {
				this.errorMsg = "e:"+e.getMessage();
		    	return(null);
		    }
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
	private void loadBookmarks() {
		(new RetrieveBookmarks()).execute(this.scuttleURL, this.scuttleUsername, this.scuttlePassword);
	}
	private void loadTags() {
		(new RetrieveTags()).execute(this.scuttleURL, this.scuttleUsername, this.scuttlePassword);
	}
	private void displayTagChooser(List<HashMap<String, String>> tags) {
		// Create the dialog.
		final Dialog dlgTagList = new Dialog(this);
		dlgTagList.setContentView(R.layout.dialog_taglist);
		dlgTagList.setTitle("Tag List");
		dlgTagList.setCancelable(true);
		// Sort the tags alphabetically.
		Collections.sort(tags, new Comparator<HashMap<String, String>>() {
			public int compare(HashMap<String, String> object1, HashMap<String, String> object2) {
				return( object1.get("tag").compareTo(object2.get("tag")) );
			}
		});
		// Add an item at the beginning of the list that will show all bookmarks.
		HashMap<String, String> notags = new HashMap<String, String>();
		notags.put("tag", getResources().getString(R.string.tagslist_notags));
		tags.add(0, notags);
		// Create the list in the ListView from the tags.
    	SimpleAdapter adapter = new SimpleAdapter(this, 
    			tags, 
    			R.layout.tag_list_item,
    			new String[] { "tag" }, 
    			new int[] { R.id.taglist_tag });
		ListView lv = (ListView)dlgTagList.findViewById(R.id.listview_tags);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				String tag = ((TextView)view.findViewById(R.id.taglist_tag)).getText().toString();
				dlgTagList.dismiss();
				ScuttleBookmarkList.this.filterTag = tag;
				loadBookmarks();
			}
		});
		dlgTagList.show();
	}
	private void displayBookmarks(List<HashMap<String, String>> bookmarks) {
    	SimpleAdapter adapter = new SimpleAdapter(this, 
        			bookmarks, 
        			R.layout.bookmark_list_item,
        			new String[] { "description", "href" }, 
        			new int[] { R.id.bookmark_description, R.id.bookmark_url });
        setListAdapter(adapter);
        
        ListView lv = getListView();
        lv.setOnItemClickListener(new OnItemClickListener() {
        	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        		String url = ((TextView)view.findViewById(R.id.bookmark_url)).getText().toString();
        		Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        		startActivity(i);
        	}
        });
	}
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        getPrefs();
        
        if( this.scuttleURL.equals("") ) {
    		startActivity(new Intent(this, ScuttlePreferences.class));
        }
        else {
        	this.loadBookmarks();
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater i = getMenuInflater();
        i.inflate(R.menu.list_menu, menu);
        return(true);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch( item.getItemId() ) {
    	case R.id.listmenu_preferences:
    		startActivity(new Intent(this, ScuttlePreferences.class));
    		return(true);
    	case R.id.listmenu_refresh:
    		this.getPrefs();
    		this.loadBookmarks();
    		return(true);
    	case R.id.listmenu_tags:
    		this.getPrefs();
    		this.loadTags();
    		return(true);
    	}
    	return(super.onOptionsItemSelected(item));
    }
}