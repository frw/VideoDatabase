/**
 * Class: SearchResult.java
 * Author: Frederick Widjaja
 * School: Jakarta International School
 * Language: Java SE 7
 * IDE: Eclipse
 */
package com.frederickw.vdb.search;

import java.util.HashMap;
import java.util.Map;

import com.frederickw.vdb.data.Type;
import com.frederickw.vdb.data.Video;

/**
 * A data structure which holds the information required for display
 * 
 * @author Frederick Widjaja
 * 
 */
public class SearchResult {
    public static final int POSTER_THUMBNAIL = 0;
    public static final int POSTER_PROFILE = 1;
    public static final int POSTER_DETAILED = 2;
    public static final int POSTER_ORIGINAL = 3;
    
    private final String title;
    private final int year;
    
    private Type type = Type.VIDEO;
    private String[] posterURLs = new String[4];
    private Map<String, Object> infos = new HashMap<>();
    
    public SearchResult(String title, int year) {
	this.title = title;
	this.year = year;
    }
    
    public String getTitle() {
	return title;
    }
    
    public int getYear() {
	return year;
    }
    
    public Type getType() {
	return type;
    }
    
    public void setType(Type type) {
	if (type == null) {
	    throw new IllegalArgumentException("type == null");
	}
	this.type = type;
    }
    
    public String getPosterURL(int type) {
	return posterURLs[type];
    }
    
    public String[] getPosterURLs() {
	return posterURLs;
    }
    
    public void setPosterURL(int type, String url) {
	posterURLs[type] = url;
    }
    
    @SuppressWarnings("unchecked")
    public <E> E getInfo(String key) {
	return (E) infos.get(key);
    }
    
    public void setInfo(String key, Object info) {
	infos.put(key, info);
    }
    
    public void merge(SearchResult result) {
	if (type == Type.VIDEO) {
	    type = result.type;
	}
	for (int i = 0; i < 4; i++) {
	    if (posterURLs[i] == null) {
		posterURLs[i] = result.posterURLs[i];
	    }
	}
	infos.putAll(result.infos);
    }
    
    public Video grab() {
	return SearchEngine.grab(this);
    }
    
    @Override
    public boolean equals(Object o) {
	if (o == null) {
	    return false;
	} else if (o == this) {
	    return true;
	} else if (o instanceof SearchResult) {
	    SearchResult result = (SearchResult) o;
	    return title.equals(result.title) && year == result.year;
	}
	return false;
    }
    
    @Override
    public int hashCode() {
	return title.hashCode() ^ year;
    }
}
