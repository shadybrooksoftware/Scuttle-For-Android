package com.shadybrooksoftware.scuttle;

public class ScuttleTag implements Comparable<ScuttleTag> {
	public String tag = "";
	public int count = 0;
	
	public int compareTo(ScuttleTag o) {
		return( this.tag.compareTo(o.tag) );
	}
	public boolean equals(ScuttleTag o) {
		return( this.tag.equals(o.tag) );
	}
}
