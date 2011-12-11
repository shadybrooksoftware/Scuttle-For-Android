package com.shadybrooksoftware.scuttle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class ScuttleBookmarkXMLHandler extends DefaultHandler {
	private List<HashMap<String, String>> bookmarks = new ArrayList<HashMap<String, String>>();
	
	public List<HashMap<String, String>> getBookmarks() {
		return(this.bookmarks);
	}
	@Override
	public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
		if( localName.equalsIgnoreCase("post") ) {
			HashMap<String, String> curBookmark = new HashMap<String, String>();
			curBookmark.put("href", attributes.getValue("href"));
			curBookmark.put("description", attributes.getValue("description"));
			curBookmark.put("hash", attributes.getValue("hash"));
			curBookmark.put("tag", attributes.getValue("tag"));
			curBookmark.put("time", attributes.getValue("time"));
			this.bookmarks.add(curBookmark);
		}
	}
}
