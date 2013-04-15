/**
 * Class: TheMovieDB.java
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
import com.frederickw.vdb.data.Type;
import com.frederickw.vdb.data.Video;
import com.frederickw.vdb.search.DataSource;
import com.frederickw.vdb.search.SearchResult;

/**
 * themoviedb.org Data Source
 * 
 * @author Frederick Widjaja
 * 
 */
public class TheMovieDB implements DataSource {
    
    public static final String INFO_KEY = "the_movie_db";
    private static final String BASE_URL = "http://private-cf22-themoviedb.apiary.io/3/";
    private static final String API_KEY = "3fec114805a460fec1293fd91d1f3ee4";
    private static final String[] POSTER_SIZES = new String[4];
    
    private static String imageBaseURL;
    
    @SuppressWarnings("unchecked")
    @Override
    public SearchResult[] search(String search) {
	if (imageBaseURL == null && !configure()) {
	    return null;
	}
	
	JSONObject response;
	try {
	    response = (JSONObject) IOUtils.getJSON(BASE_URL + "search/movie",
		    "api_key", API_KEY, "query", search);
	} catch (Exception e) {
	    e.printStackTrace();
	    return null;
	}
	
	List<SearchResult> results = new LinkedList<>();
	for (JSONObject movie : (List<JSONObject>) response.get("results")) {
	    String title = (String) movie.get("original_title");
	    String releaseDate = (String) movie.get("release_date");
	    if (releaseDate == null) {
		continue;
	    }
	    int year = Integer.parseInt(releaseDate.substring(0,
		    releaseDate.indexOf('-')));
	    SearchResult result = new SearchResult(title, year);
	    result.setType(Type.MOVIE);
	    
	    String posterPath = (String) movie.get("poster_path");
	    if (posterPath != null) {
		for (int k = 0; k < 4; k++) {
		    result.setPosterURL(k, imageBaseURL + POSTER_SIZES[k]
			    + posterPath);
		}
	    }
	    
	    result.setInfo(INFO_KEY, movie.get("id"));
	    
	    results.add(result);
	}
	return results.toArray(new SearchResult[results.size()]);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Video grab(SearchResult result) {
	Long id = result.getInfo(INFO_KEY);
	if (id == null) {
	    return null;
	}
	
	JSONObject info, casts, keywords;
	try {
	    info = (JSONObject) IOUtils.getJSON(BASE_URL + "movie/" + id,
		    "api_key", API_KEY);
	    casts = (JSONObject) IOUtils.getJSON(BASE_URL + "movie/" + id
		    + "/casts", "api_key", API_KEY);
	    keywords = (JSONObject) IOUtils.getJSON(BASE_URL + "movie/" + id
		    + "/keywords", "api_key", API_KEY);
	} catch (Exception e) {
	    e.printStackTrace();
	    return null;
	}
	
	Video video = new Video(result);
	
	video.setReleaseDate(Integer.parseInt(((String) info.get("release_date")).replaceAll(
	        "-", "")));
	video.setRating(Math.round(((Number) info.get("vote_average")).floatValue()));
	video.setRuntime(((Number) info.get("runtime")).intValue());
	video.setPlot((String) info.get("overview"));
	for (JSONObject genre : (List<JSONObject>) info.get("genres")) {
	    video.addGenre((String) genre.get("name"));
	}
	for (JSONObject language : (List<JSONObject>) info.get("spoken_languages")) {
	    video.addLanguage((String) language.get("name"));
	}
	
	for (JSONObject actor : (List<JSONObject>) casts.get("cast")) {
	    String name = (String) actor.get("name");
	    String role = (String) actor.get("character");
	    if (role == null) {
		video.addActor(name);
	    } else {
		video.addActor(name, role);
	    }
	}
	for (JSONObject crewMember : (List<JSONObject>) casts.get("crew")) {
	    String name = (String) crewMember.get("name");
	    String job = (String) crewMember.get("job");
	    if (job == null) {
		video.addCrewMember(name);
	    } else {
		video.addCrewMember(name, job);
	    }
	}
	
	for (JSONObject keyword : (List<JSONObject>) keywords.get("keywords")) {
	    video.addPlotKeyword((String) keyword.get("name"));
	}
	
	return video;
    }
    
    public static boolean configure() {
	JSONObject response;
	try {
	    response = (JSONObject) IOUtils.getJSON(BASE_URL + "configuration",
		    "api_key", API_KEY);
	} catch (Exception e) {
	    e.printStackTrace();
	    return false;
	}
	JSONObject images = (JSONObject) response.get("images");
	imageBaseURL = (String) images.get("base_url");
	JSONArray posterSizes = (JSONArray) images.get("poster_sizes");
	for (int i = 0; i < 3; i++) {
	    POSTER_SIZES[i] = (String) posterSizes.get(i);
	}
	POSTER_SIZES[3] = (String) posterSizes.get(posterSizes.size() - 1);
	return true;
    }
}
