package com.frederickw.vdb.data.table;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import com.frederickw.vdb.data.TVSeries;

public class Episode {
    private final IntegerProperty season;
    private final IntegerProperty episode;
    private final StringProperty title;
    
    public Episode() {
	this(0, 0, "");
    }
    
    public Episode(TVSeries.Episode episode) {
	this(episode.getSeason(), episode.getEpisode(), episode.getTitle());
    }
    
    public Episode(int season, int episode, String title) {
	this.season = new SimpleIntegerProperty(season);
	this.episode = new SimpleIntegerProperty(episode);
	this.title = new SimpleStringProperty(title);
    }
    
    public int getSeason() {
	return season.get();
    }
    
    public void setSeason(int season) {
	this.season.set(season);
    }
    
    public int getEpisode() {
	return episode.get();
    }
    
    public void setEpisode(int episode) {
	this.episode.set(episode);
    }
    
    public String getTitle() {
	return title.get();
    }
    
    public void setTitle(String title) {
	this.title.set(title);
    }
    
    public TVSeries.Episode getTVSeriesEpisode() {
	return new TVSeries.Episode(getSeason(), getEpisode(), getTitle());
    }
}