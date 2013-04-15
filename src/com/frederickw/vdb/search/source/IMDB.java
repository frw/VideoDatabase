/**
 * Class: IMDB.java
 * Author: Frederick Widjaja
 * School: Jakarta International School
 * Language: Java SE 7
 * IDE: Eclipse
 */
package com.frederickw.vdb.search.source;

import java.util.LinkedList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.frederickw.vdb.IOUtils;
import com.frederickw.vdb.data.TVSeries;
import com.frederickw.vdb.data.TVSeries.Episode;
import com.frederickw.vdb.data.Type;
import com.frederickw.vdb.data.Video;
import com.frederickw.vdb.search.DataSource;
import com.frederickw.vdb.search.SearchResult;

/**
 * An IMDB data source
 * 
 * @author Frederick Widjaja
 * 
 */
public class IMDB implements DataSource {
    
    public static final String INFO_KEY = "imdb";
    private static final String BASE_URL = "http://imdbapi.org/";
    
    @SuppressWarnings("unchecked")
    @Override
    public SearchResult[] search(String search) {
	Object o;
	try {
	    o = IOUtils.getJSON(BASE_URL, "title", search, "episode", 0,
		    "limit", 10);
	} catch (Exception e) {
	    e.printStackTrace();
	    return null;
	}
	
	if (!(o instanceof JSONArray)) {
	    return null;
	}
	
	List<JSONObject> response = (List<JSONObject>) o;
	List<SearchResult> results = new LinkedList<>();
	for (JSONObject video : response) {
	    Type type = Type.parse((String) video.get("type"));
	    if (type == null) {
		continue;
	    }
	    
	    String title = (String) video.get("title");
	    int year = ((Number) video.get("year")).intValue();
	    SearchResult result = new SearchResult(title, year);
	    result.setType(type);
	    
	    if (video.containsKey("poster")) {
		String poster = (String) video.get("poster");
		result.setPosterURL(SearchResult.POSTER_DETAILED, poster);
	    }
	    
	    result.setInfo(INFO_KEY, video.get("imdb_id"));
	    
	    results.add(result);
	}
	return results.toArray(new SearchResult[results.size()]);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Video grab(SearchResult result) {
	String id = result.getInfo(INFO_KEY);
	if (id == null) {
	    return null;
	}
	
	JSONObject response;
	try {
	    response = (JSONObject) IOUtils.getJSON(BASE_URL, "id", id, "plot",
		    "full");
	} catch (Exception e) {
	    e.printStackTrace();
	    return null;
	}
	
	Video video;
	switch (result.getType()) {
	case TV_SERIES:
	    TVSeries series = new TVSeries(result);
	    for (JSONObject ep : (List<JSONObject>) response.get("episodes")) {
		Number s = (Number) ep.get("season");
		int season = s != null ? s.intValue() : 0;
		Number e = (Number) ep.get("episode");
		int episode = e != null ? e.intValue() : 0;
		String title = (String) ep.get("title");
		series.addEpisode(new Episode(season, episode, title));
	    }
	    video = series;
	    break;
	default:
	    video = new Video(result);
	    break;
	}
	
	int releaseDate;
	if (response.containsKey("release_date")) {
	    releaseDate = ((Number) response.get("release_date")).intValue();
	} else {
	    releaseDate = result.getYear() * 10000 + 101;
	}
	video.setReleaseDate(releaseDate);
	if (response.containsKey("rated")) {
	    video.setRated((String) response.get("rated"));
	}
	if (response.containsKey("rating")) {
	    video.setRating(Math.round(((Number) response.get("rating")).floatValue()));
	}
	if (response.containsKey("runtime")) {
	    video.setRuntime(Integer.parseInt(((List<String>) response.get("runtime")).get(
		    0).replaceAll("\\D", "")));
	}
	if (response.containsKey("plot")) {
	    video.setPlot((String) response.get("plot"));
	}
	if (response.containsKey("genres")) {
	    video.addGenres((List<String>) response.get("genres"));
	}
	if (response.containsKey("language")) {
	    video.addLanguages((List<String>) response.get("language"));
	}
	
	if (response.containsKey("actors")) {
	    for (String actor : (List<String>) response.get("actors")) {
		video.addActor(actor);
	    }
	}
	
	if (response.containsKey("directors")) {
	    for (String director : (List<String>) response.get("directors")) {
		video.addCrewMember(director, "Director");
	    }
	}
	if (response.containsKey("writers")) {
	    for (String writer : (List<String>) response.get("writers")) {
		video.addCrewMember(writer, "Writer");
	    }
	}
	
	return video;
    }
}
