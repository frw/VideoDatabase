/**
 * Class: RottenTomatoes.java
 * Author: Frederick Widjaja
 * School: Jakarta International School
 * Language: Java SE 7
 * IDE: Eclipse
 */
package com.frederickw.vdb.search.source;

import java.util.LinkedList;
import java.util.List;

import org.json.simple.JSONObject;

import com.frederickw.vdb.IOUtils;
import com.frederickw.vdb.data.Type;
import com.frederickw.vdb.data.Video;
import com.frederickw.vdb.search.DataSource;
import com.frederickw.vdb.search.SearchResult;

/**
 * rottentomatoes.com Data Source
 * 
 * @author Frederick Widjaja
 * 
 */
public class RottenTomatoes implements DataSource {
    
    public static final String INFO_KEY = "rotten_tomatoes";
    private static final String SEARCH_URL = "http://api.rottentomatoes.com/api/public/v1.0/movies.json";
    private static final String API_KEY = "2uxtgp79xjqaagb2h25ygs2p";
    
    @SuppressWarnings("unchecked")
    @Override
    public SearchResult[] search(String search) {
	JSONObject response;
	try {
	    response = (JSONObject) IOUtils.getJSON(SEARCH_URL, "apikey",
		    API_KEY, "q", search);
	} catch (Exception e) {
	    e.printStackTrace();
	    return null;
	}
	
	List<SearchResult> results = new LinkedList<>();
	for (JSONObject movie : (List<JSONObject>) response.get("movies")) {
	    String title = (String) movie.get("title");
	    int year = ((Number) movie.get("year")).intValue();
	    SearchResult result = new SearchResult(title, year);
	    result.setType(Type.MOVIE);
	    
	    JSONObject posters = (JSONObject) movie.get("posters");
	    String[] s = { (String) posters.get("thumbnail"),
		    (String) posters.get("profile"),
		    (String) posters.get("detailed"),
		    (String) posters.get("original") };
	    for (int n = 0; n < 4; n++) {
		String poster = s[n];
		if (!poster.contains("poster_default")) {
		    result.setPosterURL(n, poster);
		}
	    }
	    
	    result.setInfo(INFO_KEY,
		    ((JSONObject) movie.get("links")).get("self"));
	    
	    results.add(result);
	}
	return results.toArray(new SearchResult[results.size()]);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Video grab(SearchResult result) {
	String infoUrl = result.getInfo(INFO_KEY);
	if (infoUrl == null) {
	    return null;
	}
	
	JSONObject info, cast;
	try {
	    info = (JSONObject) IOUtils.getJSON(infoUrl, "apikey", API_KEY);
	    cast = (JSONObject) IOUtils.getJSON(
		    (String) ((JSONObject) info.get("links")).get("cast"),
		    "apikey", API_KEY);
	} catch (Exception e) {
	    e.printStackTrace();
	    return null;
	}
	Video video = new Video(result);
	
	int releaseDate;
	JSONObject releaseDates = (JSONObject) info.get("release_dates");
	if (releaseDates.containsKey("theater")) {
	    releaseDate = Integer.parseInt(((String) releaseDates.get("theater")).replaceAll(
		    "-", ""));
	} else {
	    releaseDate = result.getYear() * 10000 + 101;
	}
	video.setReleaseDate(releaseDate);
	video.setRated((String) info.get("mpaa_rating"));
	video.setRating(Math.round(((Number) ((JSONObject) info.get("ratings")).get("audience_score")).intValue() / 10.0F));
	Object r = info.get("runtime");
	if (r instanceof Number) {
	    video.setRuntime(((Number) r).intValue());
	}
	String plot = (String) info.get("synopsis");
	video.setPlot(plot.contains(" ~ ") ? plot.substring(0,
	        plot.indexOf(" ~ ")) : plot);
	video.addGenres((List<String>) info.get("genres"));
	
	for (JSONObject actor : (List<JSONObject>) cast.get("cast")) {
	    video.addActor((String) actor.get("name"),
		    (List<String>) actor.get("characters"));
	}
	
	if (info.containsKey("abridged_directors")) {
	    for (JSONObject director : (List<JSONObject>) info.get("abridged_directors")) {
		video.addCrewMember((String) director.get("name"), "Director");
	    }
	}
	
	return video;
    }
}
