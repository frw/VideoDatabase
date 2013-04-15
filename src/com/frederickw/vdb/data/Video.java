/**
 * Class: Video.java
 * Author: Frederick Widjaja
 * School: Jakarta International School
 * Language: Java SE 7
 * IDE: Eclipse
 */
package com.frederickw.vdb.data;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.frederickw.vdb.IOUtils;
import com.frederickw.vdb.data.Database.Chunk;
import com.frederickw.vdb.search.SearchResult;
import com.frederickw.vdb.ui.controls.StarRating;

/**
 * A data structure used to store all the video information
 * 
 * @author Frederick Widjaja
 * 
 */
public class Video implements Comparable<Video> {
    
    private String title = "";
    private int releaseDate = -1;
    private Type type = Type.VIDEO;
    private String posterURL = "";
    private String rated = "";
    private int rating = StarRating.UNRATED;
    private int runtime = -1;
    private String plot = "";
    private Set<String> plotKeywords = new TreeSet<>();
    private Set<String> genres = new TreeSet<>();
    private Set<String> languages = new TreeSet<String>();
    private Map<String, Set<String>> cast = new LinkedHashMap<>();
    private Map<String, Set<String>> crew = new LinkedHashMap<>();
    
    private long index = -1;
    private int capacity = 0;
    
    public Video() {
    }
    
    public Video(SearchResult result) {
	title = result.getTitle();
	type = result.getType();
	for (int i : new int[] { 2, 1, 0, 3 }) {
	    String s = result.getPosterURL(i);
	    if (s != null) {
		posterURL = s;
		break;
	    }
	}
    }
    
    public String getTitle() {
	return title;
    }
    
    public void setTitle(String title) {
	if (title != null && !title.isEmpty()) {
	    this.title = title;
	}
    }
    
    public int getReleaseDate() {
	return releaseDate;
    }
    
    public int getYear() {
	return releaseDate / 10000;
    }
    
    public int getMonth() {
	return releaseDate / 100 % 100;
    }
    
    public int getDate() {
	return releaseDate % 100;
    }
    
    public void setReleaseDate(int releaseDate) {
	this.releaseDate = releaseDate;
    }
    
    public void setReleaseDate(int year, int month, int date) {
	releaseDate = year * 10000 + month * 100 + date;
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
    
    public String getPosterURL() {
	return posterURL;
    }
    
    public void setPosterURL(String url) {
	posterURL = url;
    }
    
    public String getRated() {
	return rated;
    }
    
    public void setRated(String rated) {
	if (rated != null) {
	    this.rated = rated.replace('_', '-');
	}
    }
    
    public int getRating() {
	return rating;
    }
    
    public void setRating(int rating) {
	this.rating = rating;
    }
    
    public int getRuntime() {
	return runtime;
    }
    
    public void setRuntime(int runtime) {
	this.runtime = runtime;
    }
    
    public String getPlot() {
	return plot;
    }
    
    public void setPlot(String plot) {
	if (plot != null) {
	    this.plot = plot;
	}
    }
    
    public Set<String> getPlotKeywords() {
	return plotKeywords;
    }
    
    public void addPlotKeyword(String plotKeyword) {
	plotKeywords.add(plotKeyword);
    }
    
    public void addPlotKeywords(String[] plotKeywords) {
	this.plotKeywords.addAll(Arrays.asList(plotKeywords));
    }
    
    public void addPlotKeywords(Collection<String> plotKeywords) {
	this.plotKeywords.addAll(plotKeywords);
    }
    
    public void removePlotKeyword(String plotKeyword) {
	plotKeywords.remove(plotKeyword);
    }
    
    public Set<String> getGenres() {
	return genres;
    }
    
    public void addGenre(String genre) {
	genres.add(genre);
    }
    
    public void addGenres(String[] genres) {
	this.genres.addAll(Arrays.asList(genres));
    }
    
    public void addGenres(Collection<String> genres) {
	this.genres.addAll(genres);
    }
    
    public void removeGenre(String genre) {
	genres.remove(genre);
    }
    
    public Set<String> getLanguages() {
	return languages;
    }
    
    public void addLanguage(String language) {
	languages.add(language);
    }
    
    public void addLanguages(String[] languages) {
	this.languages.addAll(Arrays.asList(languages));
    }
    
    public void addLanguages(Collection<String> languages) {
	this.languages.addAll(languages);
    }
    
    public void removeLanguage(String language) {
	languages.remove(language);
    }
    
    public Map<String, Set<String>> getCast() {
	return cast;
    }
    
    public void addActor(String name) {
	if (!cast.containsKey(name)) {
	    cast.put(name, new LinkedHashSet<String>());
	}
    }
    
    public void addActor(String name, String role) {
	Set<String> roles = cast.get(name);
	if (roles == null) {
	    roles = new LinkedHashSet<>();
	    cast.put(name, roles);
	}
	roles.add(role);
    }
    
    public void addActor(String name, Collection<String> c) {
	Set<String> roles = cast.get(name);
	if (roles == null) {
	    roles = new LinkedHashSet<>();
	    cast.put(name, roles);
	}
	roles.addAll(c);
    }
    
    public void removeActor(String name, String role) {
	Set<String> roles = cast.get(name);
	if (roles != null && roles.remove(role) && roles.isEmpty()) {
	    cast.remove(name);
	}
    }
    
    public Map<String, Set<String>> getCrew() {
	return crew;
    }
    
    public void addCrewMember(String name) {
	if (!crew.containsKey(name)) {
	    crew.put(name, new LinkedHashSet<String>());
	}
    }
    
    public void addCrewMember(String name, String job) {
	Set<String> jobs = crew.get(name);
	if (jobs == null) {
	    jobs = new LinkedHashSet<>();
	    crew.put(name, jobs);
	}
	jobs.add(job);
    }
    
    public void addCrewMember(String name, Collection<String> c) {
	Set<String> jobs = crew.get(name);
	if (jobs == null) {
	    jobs = new LinkedHashSet<>();
	    crew.put(name, jobs);
	}
	jobs.addAll(c);
    }
    
    public void removeCrewMember(String name, String job) {
	Set<String> roles = crew.get(name);
	if (roles != null && roles.remove(job) && roles.isEmpty()) {
	    crew.remove(name);
	}
    }
    
    /**
     * Merges the information this video has with another video
     * 
     * @param video
     *            The other video
     */
    public void merge(Video video) {
	if (title == null) {
	    title = video.title;
	}
	if (releaseDate == -1) {
	    releaseDate = video.releaseDate;
	}
	if (type == Type.VIDEO) {
	    type = video.type;
	}
	if (posterURL == null) {
	    posterURL = video.posterURL;
	}
	if (rated == null) {
	    rated = video.rated;
	}
	if (rating == -1.0F) {
	    rating = video.rating;
	}
	if (runtime == -1) {
	    runtime = video.runtime;
	}
	if (plot == null || plot.isEmpty()) {
	    plot = video.plot;
	}
	plotKeywords.addAll(video.plotKeywords);
	genres.addAll(video.genres);
	languages.addAll(video.languages);
	if (cast.size() < video.cast.size()) {
	    cast.clear();
	    cast.putAll(video.cast);
	}
	if (crew.size() < video.crew.size()) {
	    crew.clear();
	    crew.putAll(video.crew);
	}
    }
    
    /**
     * Writes this video to the RAF
     * 
     * @param raf
     *            The RandomAccessFile
     * @throws IOException
     *             If an exception occurs during IO
     */
    public void write(RandomAccessFile raf) throws IOException {
	int dataSize = getDataSize();
	
	if (index == -1 || dataSize > capacity) {
	    if (index != -1) {
		delete(raf);
	    }
	    
	    Chunk chunk = Database.getVideoChunk(raf,
		    Math.round(dataSize * 1.2F));
	    index = chunk.getIndex();
	    capacity = chunk.getSize();
	    
	    raf.seek(index);
	    raf.writeBoolean(true); // Flag for valid chunk
	    raf.writeInt(capacity); // Reserved space for chunk
	} else {
	    raf.seek(index + 5); // Seek to the pointer after the header section
	}
	
	raf.writeByte(type.ordinal());
	raf.writeUTF(title);
	raf.writeInt(releaseDate);
	raf.writeUTF(posterURL);
	raf.writeUTF(rated);
	raf.writeByte(rating);
	raf.writeShort(runtime);
	raf.writeUTF(plot);
	IOUtils.writeSet(raf, plotKeywords);
	IOUtils.writeSet(raf, genres);
	IOUtils.writeSet(raf, languages);
	IOUtils.writeMap(raf, cast);
	IOUtils.writeMap(raf, crew);
    }
    
    /**
     * Reads the video from the RAF
     * 
     * @param raf
     *            The RandomAccessFile
     * @param index
     *            The index of this video chunk
     * @param capacity
     *            The capacity of this video chunk
     * @param type
     *            The type of this video
     * @throws IOException
     *             If an exception occurs during IO
     */
    public void read(RandomAccessFile raf, long index, int capacity, Type type)
	    throws IOException {
	this.index = index;
	this.capacity = capacity;
	this.type = type;
	title = raf.readUTF();
	releaseDate = raf.readInt();
	posterURL = raf.readUTF();
	rated = raf.readUTF();
	rating = raf.readByte();
	runtime = raf.readUnsignedShort();
	plot = raf.readUTF();
	plotKeywords = IOUtils.readSet(raf);
	genres = IOUtils.readSet(raf);
	languages = IOUtils.readSet(raf);
	cast = IOUtils.readMap(raf);
	crew = IOUtils.readMap(raf);
    }
    
    /**
     * Deletes this video from the RAF
     * 
     * @param raf
     *            The RandomAccessFile
     * @throws IOException
     *             If an exception occurs during IO
     */
    public final void delete(RandomAccessFile raf) throws IOException {
	// Flags the chunk as invalid
	Database.freeChunk(raf, index, capacity);
	index = -1;
	capacity = 0;
    }
    
    /**
     * Gets the size of the video record when written onto the file
     * 
     * @return The size of the video record
     */
    protected int getDataSize() {
	int size = 5; // header (flag + capacity)
	size += 1; // type
	size += IOUtils.getUTF8StringSize(title); // title
	size += 4; // releaseDate
	size += IOUtils.getUTF8StringSize(posterURL); // posterURL
	size += IOUtils.getUTF8StringSize(rated); // rated
	size += 1; // rating
	size += 2; // runtime
	size += IOUtils.getUTF8StringSize(plot); // plot
	size += IOUtils.getSetSize(plotKeywords); // plotKeywords
	size += IOUtils.getSetSize(genres); // genres
	size += IOUtils.getSetSize(languages); // languages
	size += IOUtils.getMapSize(cast); // cast
	size += IOUtils.getMapSize(crew); // crew
	return size;
    }
    
    @Override
    public String toString() {
	return title;
    }
    
    @Override
    public int compareTo(Video v) {
	return title.compareTo(v.title);
    }
}
