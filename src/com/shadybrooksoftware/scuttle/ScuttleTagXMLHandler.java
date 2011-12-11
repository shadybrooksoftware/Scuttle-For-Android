package com.shadybrooksoftware.scuttle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class ScuttleTagXMLHandler extends DefaultHandler {
	private List<HashMap<String, String>> tags = new ArrayList<HashMap<String, String>>();
	
	public List<HashMap<String, String>> getTags() {
		return(this.tags);
	}
	@Override
	public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
		if( localName.equalsIgnoreCase("tag") ) {
			HashMap<String, String> curTag = new HashMap<String, String>();
			curTag.put("tag", attributes.getValue("tag"));
			curTag.put("count", attributes.getValue("count"));
			this.tags.add(curTag);
		}
	}

}
