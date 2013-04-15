/**
 * Class: TVSeries.java
 * Author: Frederick Widjaja
 * School: Jakarta International School
 * Language: Java SE 7
 * IDE: Eclipse
 */
package com.frederickw.vdb.data;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Set;
import java.util.TreeSet;

import org.json.simple.JSONObject;

import com.frederickw.vdb.IOUtils;
import com.frederickw.vdb.search.SearchResult;

/**
 * A data structure which inherits the properties of Video but also contains the
 * Episodes in a TV Series
 * 
 * @author Frederick Widjaja
 * 
 */
public class TVSeries extends Video {
    
    private final Set<Episode> episodes = new TreeSet<>();
    
    public TVSeries() {
    }
    
    public TVSeries(SearchResult result) {
	super(result);
    }
    
    public Set<Episode> getEpisodes() {
	return episodes;
    }
    
    public void addEpisode(Episode episode) {
	episodes.add(episode);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void merge(Video video) {
	super.merge(video);
	if (video instanceof TVSeries) {
	    TVSeries series = (TVSeries) video;
	    episodes.addAll(series.episodes);
	}
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void write(RandomAccessFile raf) throws IOException {
	super.write(raf);
	
	raf.writeShort(episodes.size());
	for (Episode episode : episodes) {
	    episode.write(raf);
	}
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void read(RandomAccessFile raf, long index, int capacity, Type type)
	    throws IOException {
	super.read(raf, index, capacity, type);
	
	int numEpisodes = raf.readUnsignedShort();
	for (int i = 0; i < numEpisodes; i++) {
	    episodes.add(Episode.read(raf));
	}
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected int getDataSize() {
	int size = super.getDataSize();
	
	size += 2; // episodes.size()
	for (Episode episode : episodes) {
	    size += episode.getDataSize();
	}
	return size;
    }
    
    public static class Episode implements Comparable<Episode> {
	
	private final int season;
	private final int episode;
	private final String title;
	
	public Episode(int season, int episode, String title) {
	    this.season = season;
	    this.episode = episode;
	    this.title = title;
	}
	
	public static Episode parse(JSONObject o) {
	    int season = ((Number) o.get("season")).intValue();
	    int episode = ((Number) o.get("episode")).intValue();
	    String title = (String) o.get("title");
	    return new Episode(season, episode, title);
	}
	
	public int getSeason() {
	    return season;
	}
	
	public int getEpisode() {
	    return episode;
	}
	
	public String getTitle() {
	    return title;
	}
	
	@Override
	public boolean equals(Object o) {
	    if (o == null) {
		return false;
	    } else if (o == this) {
		return true;
	    } else if (o instanceof Episode) {
		Episode e = (Episode) o;
		return e.season == season && e.episode == episode
		        && e.title.equals(title);
	    }
	    return false;
	}
	
	@Override
	public int hashCode() {
	    return title.hashCode() ^ season << 16 + episode;
	}
	
	@Override
	public int compareTo(Episode e) {
	    if (season != e.season) {
		return season - e.season;
	    } else if (episode != e.episode) {
		return episode - e.episode;
	    } else {
		return title.compareTo(e.title);
	    }
	}
	
	public void write(RandomAccessFile raf) throws IOException {
	    raf.writeShort(season);
	    raf.writeShort(episode);
	    raf.writeUTF(title);
	}
	
	public static Episode read(RandomAccessFile raf) throws IOException {
	    return new Episode(raf.readUnsignedShort(),
		    raf.readUnsignedShort(), raf.readUTF());
	}
	
	private int getDataSize() {
	    // season + episode + title
	    return 4 + IOUtils.getUTF8StringSize(title);
	}
	
    }
    
}
