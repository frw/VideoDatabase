/**
 * Class: Database.java
 * Author: Frederick Widjaja
 * School: Jakarta International School
 * Language: Java SE 7
 * IDE: Eclipse
 */
package com.frederickw.vdb.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.scene.control.Dialogs;
import javafx.scene.control.TreeItem;

import com.frederickw.vdb.IOUtils;
import com.frederickw.vdb.VideoDatabase;
import com.frederickw.vdb.ui.Settings;
import com.frederickw.vdb.ui.Settings.Grouping;

/**
 * A class used to store and organize the videos
 * 
 * @author Frederick Widjaja
 * 
 */
public class Database {
    
    private static RandomAccessFile DATABASE_FILE;
    
    static {
	try {
	    DATABASE_FILE = new RandomAccessFile(new File(
		    IOUtils.DATABASE_DIRECTORY, "videos.dat"), "rw");
	} catch (FileNotFoundException e) {
	    Dialogs.showErrorDialog(
		    null,
		    "Please restart the program, or contact support if the problem persists.",
		    "Unable to access videos.dat", "Error", e);
	}
    }
    
    private static final TreeSet<Video> VIDEOS = new TreeSet<Video>();
    private static final ObservableSet<Video> OBSERVABLE_VIDEOS = FXCollections.observableSet(VIDEOS);
    
    private static final Map<Integer, Set<Video>> YEAR_GROUP = new TreeMap<>();
    private static final Map<Type, Set<Video>> TYPE_GROUP = new TreeMap<>();
    private static final Map<String, Set<Video>> GENRE_GROUP = new TreeMap<>();
    private static final Map<String, Set<Video>> LANGUAGE_GROUP = new TreeMap<>();
    private static final Map<String, Set<Video>> ACTOR_GROUP = new TreeMap<>();
    private static final Map<String, Set<Video>> DIRECTOR_GROUP = new TreeMap<>();
    
    private static final Queue<Chunk> unusedChunks = new PriorityQueue<>();
    
    /**
     * 
     * @return Returns an ObservableSet of all the videos
     */
    public static ObservableSet<Video> getVideos() {
	return OBSERVABLE_VIDEOS;
    }
    
    /**
     * Loads all the videos from the database file
     * 
     * @throws IOException
     *             If an exception occurs during IO
     */
    public static void loadVideos() throws IOException {
	VIDEOS.clear();
	unusedChunks.clear();
	DATABASE_FILE.seek(0);
	Video video = null;
	while (DATABASE_FILE.getFilePointer() != DATABASE_FILE.length()) {
	    long index = DATABASE_FILE.getFilePointer();
	    boolean valid = DATABASE_FILE.readBoolean();
	    int size = DATABASE_FILE.readInt();
	    if (valid) {
		if (video != null) {
		    addVideoToGroups(video, true);
		}
		Type type = Type.values()[DATABASE_FILE.readUnsignedByte()];
		switch (type) {
		case TV_SERIES:
		    video = new TVSeries();
		    break;
		default:
		    video = new Video();
		    break;
		}
		video.read(DATABASE_FILE, index, size, type);
	    } else {
		unusedChunks.add(new Chunk(index, size));
	    }
	    DATABASE_FILE.seek(index + size);
	}
	if (video != null) {
	    addVideoToGroups(video, false);
	}
    }
    
    /**
     * Called when a video is added
     * 
     * @param video
     *            The video
     */
    public static void onVideoAdded(Video video) {
	addVideoToGroups(video, false);
	try {
	    video.write(DATABASE_FILE);
	} catch (IOException e) {
	    Dialogs.showErrorDialog(
		    VideoDatabase.getPrimaryStage(),
		    "Please try again, or contact support if the problem persists.",
		    "Error while writing video", "Error", e);
	}
    }
    
    /**
     * Called when a video is being edited
     * 
     * @param video
     *            The video
     */
    public static void onVideoEditing(Video video) {
	// Ensure that previous groupings are cleared
	removeVideoFromGroups(video, true);
    }
    
    /**
     * Called when a video has finished being edited
     * 
     * @param video
     *            The video
     */
    public static void onVideoEdited(Video video) {
	// Re-add video to groupings
	addVideoToGroups(video, false);
	try {
	    video.write(DATABASE_FILE);
	} catch (IOException e) {
	    Dialogs.showErrorDialog(
		    VideoDatabase.getPrimaryStage(),
		    "Please try again, or contact support if the problem persists.",
		    "Oops!", "Error", e);
	}
    }
    
    /**
     * Called when a video has been deleted
     * 
     * @param video
     *            The video
     */
    public static void onVideoDeleted(Video video) {
	removeVideoFromGroups(video, false);
	try {
	    video.delete(DATABASE_FILE);
	} catch (IOException e) {
	    Dialogs.showErrorDialog(
		    VideoDatabase.getPrimaryStage(),
		    "Please try again, or contact support if the problem persists.",
		    "Oops!", "Error", e);
	}
    }
    
    /**
     * Adds a video to its respective groups
     * 
     * @param video
     *            The video
     * @param silent
     *            If this change should notify all listeners
     */
    private static void addVideoToGroups(Video video, boolean silent) {
	addToMap(YEAR_GROUP, video.getYear(), video);
	
	addToMap(TYPE_GROUP, video.getType(), video);
	
	Set<String> genres = video.getGenres();
	if (!genres.isEmpty()) {
	    for (String genre : genres) {
		addToMap(GENRE_GROUP, genre, video);
	    }
	} else {
	    addToMap(GENRE_GROUP, "Unspecified", video);
	}
	
	Set<String> languages = video.getLanguages();
	if (!languages.isEmpty()) {
	    for (String language : languages) {
		addToMap(LANGUAGE_GROUP, language, video);
	    }
	} else {
	    addToMap(LANGUAGE_GROUP, "Unspecified", video);
	}
	
	Set<String> cast = video.getCast().keySet();
	if (!cast.isEmpty()) {
	    for (String actor : cast) {
		addToMap(ACTOR_GROUP, actor, video);
	    }
	} else {
	    addToMap(ACTOR_GROUP, "No Cast", video);
	}
	
	boolean hasDirector = false;
	for (Entry<String, Set<String>> entry : video.getCrew().entrySet()) {
	    if (entry.getValue().contains("Director")) {
		addToMap(DIRECTOR_GROUP, entry.getKey(), video);
		hasDirector = true;
	    }
	}
	if (!hasDirector) {
	    addToMap(DIRECTOR_GROUP, "No Director", video);
	}
	
	if (silent) {
	    VIDEOS.add(video);
	} else {
	    OBSERVABLE_VIDEOS.add(video);
	}
    }
    
    private static <E> void addToMap(Map<E, Set<Video>> map, E e, Video video) {
	Set<Video> videos = map.get(e);
	if (videos == null) {
	    videos = new TreeSet<>();
	    map.put(e, videos);
	}
	videos.add(video);
    }
    
    /**
     * Removes a video from all the groups
     * 
     * @param video
     *            The video
     * @param silent
     *            If this change should notify all listeners
     */
    private static void removeVideoFromGroups(Video video, boolean silent) {
	removeFromMap(YEAR_GROUP, video);
	removeFromMap(TYPE_GROUP, video);
	removeFromMap(GENRE_GROUP, video);
	removeFromMap(LANGUAGE_GROUP, video);
	removeFromMap(ACTOR_GROUP, video);
	removeFromMap(DIRECTOR_GROUP, video);
	if (silent) {
	    VIDEOS.remove(video);
	} else {
	    OBSERVABLE_VIDEOS.remove(video);
	}
    }
    
    private static void removeFromMap(Map<?, Set<Video>> map, Video video) {
	Iterator<Set<Video>> itr = map.values().iterator();
	while (itr.hasNext()) {
	    Set<Video> videos = itr.next();
	    videos.remove(video);
	    if (videos.isEmpty()) {
		itr.remove();
	    }
	}
    }
    
    /**
     * Searches for a video based on the input
     * 
     * @param input
     *            The input
     * @return A Set containing all the videos that matches the input
     */
    public static Set<Video> search(String input) {
	String[] searchKeywords = input.toLowerCase().trim().split("\\W+");
	// First add all existing videos in database to results
	Set<Video> results = new TreeSet<Video>(VIDEOS);
	for (String keyword : searchKeywords) {
	    if (keyword.isEmpty()) {
		continue;
	    }
	    Iterator<Video> itr = results.iterator();
	    outer: while (itr.hasNext()) {
		Video video = itr.next();
		
		// Then attempt to match video to keyword
		if (Settings.isFilterEnabled(Settings.TITLE_FILTER_KEY)
		        && video.getTitle().toLowerCase().contains(keyword)) {
		    continue;
		}
		if (Settings.isFilterEnabled(Settings.PLOT_KEYWORD_FILTER_KEY)) {
		    for (String plotKeyword : video.getPlotKeywords()) {
			if (plotKeyword.toLowerCase().contains(keyword)) {
			    continue outer;
			}
		    }
		}
		if (Settings.isFilterEnabled(Settings.GENRE_FILTER_KEY)) {
		    for (String genre : video.getGenres()) {
			if (genre.toLowerCase().contains(keyword)) {
			    continue outer;
			}
		    }
		}
		if (Settings.isFilterEnabled(Settings.LANGUAGE_FILTER_KEY)) {
		    for (String language : video.getLanguages()) {
			if (language.toLowerCase().contains(keyword)) {
			    continue outer;
			}
		    }
		}
		if (Settings.isFilterEnabled(Settings.ACTOR_FILTER_KEY)) {
		    for (String actor : video.getCast().keySet()) {
			if (actor.toLowerCase().contains(keyword)) {
			    continue outer;
			}
		    }
		}
		if (Settings.isFilterEnabled(Settings.CREW_MEMBER_FILTER_KEY)) {
		    for (String crewMember : video.getCrew().keySet()) {
			if (crewMember.toLowerCase().contains(keyword)) {
			    continue outer;
			}
		    }
		}
		
		// If it keyword does not match video at all, remove video
		itr.remove();
	    }
	}
	return results;
    }
    
    /**
     * Organizes a video tree based on the groupings of the video
     * 
     * @param videos
     *            The Set of videos to organize
     * @return The root node of the TreeView
     */
    public static TreeItem<Object> getVideoTree(Set<Video> videos) {
	TreeItem<Object> root = new TreeItem<Object>("All Videos");
	addChildren(root, Settings.getGroups(), 0, videos);
	return root;
    }
    
    private static void addChildren(TreeItem<Object> node,
	    List<Grouping> groups, int idx, Set<Video> videos) {
	List<TreeItem<Object>> children = node.getChildren();
	
	if (idx == groups.size()) {
	    for (Video video : videos) {
		children.add(new TreeItem<Object>(video));
	    }
	} else {
	    Map<?, Set<Video>> map;
	    switch (groups.get(idx++)) {
	    case YEAR:
		map = YEAR_GROUP;
		break;
	    case TYPE:
		map = TYPE_GROUP;
		break;
	    case GENRE:
		map = GENRE_GROUP;
		break;
	    case LANGUAGE:
		map = LANGUAGE_GROUP;
		break;
	    case ACTOR:
		map = ACTOR_GROUP;
		break;
	    case DIRECTOR:
		map = DIRECTOR_GROUP;
		break;
	    default:
		throw new IllegalStateException();
	    }
	    for (Entry<?, Set<Video>> entry : map.entrySet()) {
		Set<Video> groupVideos = entry.getValue();
		Set<Video> merged = new TreeSet<>();
		for (Video video : videos) {
		    if (groupVideos.contains(video)) {
			merged.add(video);
		    }
		}
		if (!merged.isEmpty()) {
		    TreeItem<Object> child = new TreeItem<Object>(
			    entry.getKey());
		    addChildren(child, groups, idx, merged);
		    children.add(child);
		}
	    }
	}
    }
    
    /**
     * Gets the next available chunk that can fit the video
     * 
     * @param raf
     *            The RandomAccessFile
     * @param size
     *            Minimum size of the video chunk
     * @return The Chunk that the video should be written in
     * @throws IOException
     *             If an exception occurs during IO
     */
    public static Chunk getVideoChunk(RandomAccessFile raf, int size)
	    throws IOException {
	Iterator<Chunk> itr = unusedChunks.iterator();
	while (itr.hasNext()) {
	    Chunk chunk = itr.next();
	    if (chunk.size > size) {
		itr.remove();
		
		if (chunk.size > size + 50) {
		    Chunk c = new Chunk(chunk.index, size);
		    chunk.index += size;
		    chunk.size -= size;
		    unusedChunks.add(chunk);
		    
		    raf.seek(chunk.index);
		    raf.writeBoolean(false);
		    raf.writeInt(chunk.size);
		    return c;
		} else {
		    return chunk;
		}
	    }
	}
	long index = raf.length();
	raf.setLength(index + size);
	return new Chunk(index, size);
    }
    
    /**
     * Frees the chunk after a video has been deleted
     * 
     * @param raf
     *            The RandomAccessFile
     * @param index
     *            Index of the video chunk that has been deleted
     * @param size
     *            Size of the video chunk that has been deleted
     * @throws IOException
     *             If an exception occurs during IO
     */
    public static void freeChunk(RandomAccessFile raf, long index, int size)
	    throws IOException {
	long length = raf.length();
	
	Iterator<Chunk> itr = unusedChunks.iterator();
	while (itr.hasNext()) {
	    Chunk chunk = itr.next();
	    if (index + size == chunk.index) {
		itr.remove();
		chunk.index = index;
		chunk.size += size;
		
		unusedChunks.add(chunk);
		
		raf.seek(chunk.index);
		raf.writeBoolean(false);
		raf.writeInt(chunk.size);
		return;
	    } else if (chunk.index + chunk.size == index) {
		itr.remove();
		chunk.size += size;
		if (chunk.index + chunk.size == length) {
		    raf.setLength(length - chunk.size);
		} else {
		    unusedChunks.add(chunk);
		    
		    raf.seek(chunk.index + 1);
		    raf.writeInt(chunk.size);
		}
		return;
	    }
	}
	if (index + size == length) {
	    raf.setLength(length - size);
	} else {
	    unusedChunks.add(new Chunk(index, size));
	    
	    raf.seek(index);
	    raf.writeBoolean(false);
	    raf.writeInt(size);
	}
    }
    
    public static class Chunk implements Comparable<Chunk> {
	private long index;
	private int size;
	
	private Chunk(long index, int size) {
	    this.index = index;
	    this.size = size;
	}
	
	public long getIndex() {
	    return index;
	}
	
	public int getSize() {
	    return size;
	}
	
	@Override
	public int compareTo(Chunk o) {
	    return size - o.size;
	}
    }
}
