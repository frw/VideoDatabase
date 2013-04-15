/**
 * Class: SearchEngine.java
 * Author: Frederick Widjaja
 * School: Jakarta International School
 * Language: Java SE 7
 * IDE: Eclipse
 */
package com.frederickw.vdb.search;

import java.util.LinkedHashMap;
import java.util.Map;

import com.frederickw.vdb.data.TVSeries;
import com.frederickw.vdb.data.Video;
import com.frederickw.vdb.search.source.IMDB;
import com.frederickw.vdb.search.source.RottenTomatoes;
import com.frederickw.vdb.search.source.TheMovieDB;

/**
 * A class used to organize video searches and video grabbing
 * 
 * @author Frederick Widjaja
 * 
 */
public final class SearchEngine {
    
    private static final DataSource[] SOURCES = { new IMDB(), new TheMovieDB(),
	    new RottenTomatoes() };
    
    private SearchEngine() {
    }
    
    /**
     * Searches for videos from sources
     * 
     * @param search
     *            The search terms
     * @return SearchResults which matches the search terms
     */
    public static SearchResult[] search(String search) {
	Map<SearchResult, SearchResult> results = new LinkedHashMap<>();
	for (DataSource source : SOURCES) {
	    SearchResult[] sr = source.search(search);
	    if (sr != null) {
		for (SearchResult result : sr) {
		    if (result != null) {
			SearchResult existing = results.get(result);
			if (existing == null) {
			    results.put(result, result);
			} else {
			    existing.merge(result);
			}
		    }
		}
	    }
	}
	return results.values().toArray(new SearchResult[results.size()]);
    }
    
    /**
     * Grabs a video from the SearchResult given
     * 
     * @param result
     *            The SearchResult
     * @return A Video
     */
    static Video grab(SearchResult result) {
	Video video = null;
	for (DataSource source : SOURCES) {
	    Video temp = source.grab(result);
	    if (temp != null) {
		if (video == null) {
		    video = temp;
		} else if (!(video instanceof TVSeries)
		        && temp instanceof TVSeries) {
		    temp.merge(video);
		    video = temp;
		} else {
		    video.merge(temp);
		}
	    }
	}
	return video;
    }
    
}
