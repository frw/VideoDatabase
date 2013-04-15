/**
 * Class: Settings.java
 * Author: Frederick Widjaja
 * School: Jakarta International School
 * Language: Java SE 7
 * IDE: Eclipse
 */
package com.frederickw.vdb.ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialogs;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;

import com.frederickw.vdb.IOUtils;
import com.frederickw.vdb.VideoDatabase;
import com.frederickw.vdb.ui.resources.Resource;

/**
 * A class used to store program settings, as well as to control the gui which
 * edits them
 * 
 * @author Frederick Widjaja
 * 
 */
public class Settings extends Stage implements Initializable {
    
    public enum Grouping {
	YEAR, TYPE, GENRE, LANGUAGE, ACTOR, DIRECTOR
    }
    
    public static final File SETTINGS_FILE = new File(
	    IOUtils.DATABASE_DIRECTORY, "settings.ini");
    
    public static final String TITLE_FILTER_KEY = "title";
    public static final String PLOT_KEYWORD_FILTER_KEY = "plotKeyword";
    public static final String GENRE_FILTER_KEY = "genre";
    public static final String LANGUAGE_FILTER_KEY = "language";
    public static final String ACTOR_FILTER_KEY = "actor";
    public static final String CREW_MEMBER_FILTER_KEY = "crewMember";
    
    private static final Map<String, Boolean> SEARCH_FILTERS = new HashMap<>();
    private static final ObservableList<Grouping> GROUPS = FXCollections.observableArrayList();
    
    static {
	SEARCH_FILTERS.put(TITLE_FILTER_KEY, true);
	SEARCH_FILTERS.put(PLOT_KEYWORD_FILTER_KEY, true);
	SEARCH_FILTERS.put(GENRE_FILTER_KEY, true);
	SEARCH_FILTERS.put(LANGUAGE_FILTER_KEY, true);
	SEARCH_FILTERS.put(ACTOR_FILTER_KEY, true);
	SEARCH_FILTERS.put(CREW_MEMBER_FILTER_KEY, true);
    }
    
    public static boolean isFilterEnabled(String key) {
	Boolean b = SEARCH_FILTERS.get(key);
	if (b == null) {
	    throw new IllegalArgumentException("Filter key " + key
		    + " is not valid.");
	}
	return b;
    }
    
    public static void setFilterEnabled(String key, boolean value) {
	if (key == null || !SEARCH_FILTERS.containsKey(key)) {
	    throw new IllegalArgumentException("Filter key " + key
		    + " is not valid.");
	}
	SEARCH_FILTERS.put(key, value);
    }
    
    public static ObservableList<Grouping> getGroups() {
	return GROUPS;
    }
    
    /**
     * Saves settings onto a text file
     * 
     * @throws IOException
     *             If an exception occurs during IO
     */
    public static void saveSettings() throws IOException {
	PrintStream out = new PrintStream(new FileOutputStream(SETTINGS_FILE));
	out.println("########################## Settings for Video Database ##########################");
	out.println();
	out.println("# Filter Settings");
	for (Entry<String, Boolean> entry : SEARCH_FILTERS.entrySet()) {
	    out.println(entry.getKey() + "=" + entry.getValue());
	}
	out.println();
	out.println("# Grouping Settings");
	for (Grouping group : GROUPS) {
	    out.println("group=" + group);
	}
	out.close();
    }
    
    /**
     * Loads all of the settings in the text file onto memory
     * 
     * @throws IOException
     *             If an exception occurs during IO
     */
    public static void loadSettings() throws IOException {
	if (SETTINGS_FILE.exists()) {
	    BufferedReader in = new BufferedReader(
		    new FileReader(SETTINGS_FILE));
	    GROUPS.clear();
	    String line;
	    while ((line = in.readLine()) != null) {
		if (!line.isEmpty() && line.charAt(0) != '#') {
		    int idx = line.indexOf('=');
		    String key = line.substring(0, idx).trim();
		    String value = line.substring(idx + 1).trim();
		    switch (key) {
		    case "group":
			Grouping group = Grouping.valueOf(value);
			if (group != null) {
			    GROUPS.add(group);
			}
			break;
		    default:
			if (SEARCH_FILTERS.containsKey(key)) {
			    SEARCH_FILTERS.put(key, Boolean.parseBoolean(value));
			}
			break;
		    }
		}
	    }
	    in.close();
	}
    }
    
    @FXML
    private CheckBox actor;
    @FXML
    private CheckBox crewMember;
    @FXML
    private CheckBox genre;
    @FXML
    private CheckBox language;
    @FXML
    private ListView<Grouping> lstAvailableGroupings;
    @FXML
    private ListView<Grouping> lstSelectedGroupings;
    @FXML
    private CheckBox plotKeyword;
    @FXML
    private CheckBox title;
    
    public Settings() {
	getIcons().add(new Image(Resource.getImageAsStream("icon")));
	setTitle("Settings");
	Parent parent;
	try {
	    parent = FXMLLoader.load(Resource.getFXML("Settings"), null,
		    new JavaFXBuilderFactory(),
		    new Callback<Class<?>, Object>() {
		        @Override
		        public Object call(Class<?> c) {
			    if (c.equals(Settings.class)) {
			        return Settings.this;
			    } else {
			        try {
				    return c.newInstance();
			        } catch (InstantiationException
			                | IllegalAccessException e) {
				    throw new RuntimeException(e);
			        }
			    }
		        }
		        
		    });
	} catch (IOException e) {
	    return;
	}
	setScene(new Scene(parent));
    }
    
    @Override
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
	assert actor != null : "fx:id=\"actor\" was not injected: check your FXML file 'Settings.fxml'.";
	assert crewMember != null : "fx:id=\"crewMember\" was not injected: check your FXML file 'Settings.fxml'.";
	assert genre != null : "fx:id=\"genre\" was not injected: check your FXML file 'Settings.fxml'.";
	assert language != null : "fx:id=\"language\" was not injected: check your FXML file 'Settings.fxml'.";
	assert lstAvailableGroupings != null : "fx:id=\"lstAvailableGroupings\" was not injected: check your FXML file 'Settings.fxml'.";
	assert lstSelectedGroupings != null : "fx:id=\"lstSelectedGroupings\" was not injected: check your FXML file 'Settings.fxml'.";
	assert plotKeyword != null : "fx:id=\"plotKeyword\" was not injected: check your FXML file 'Settings.fxml'.";
	assert title != null : "fx:id=\"title\" was not injected: check your FXML file 'Settings.fxml'.";
	
	for (CheckBox cb : Arrays.asList(title, plotKeyword, genre, language,
	        actor, crewMember)) {
	    cb.setSelected(SEARCH_FILTERS.get(cb.getId()));
	}
	
	ObservableList<Grouping> available = lstAvailableGroupings.getItems();
	for (Grouping grouping : Grouping.values()) {
	    if (!GROUPS.contains(grouping)) {
		available.add(grouping);
	    }
	}
	
	lstSelectedGroupings.setItems(FXCollections.observableList(GROUPS));
	
	setOnHiding(new EventHandler<WindowEvent>() {
	    @Override
	    public void handle(WindowEvent e) {
		try {
		    saveSettings();
		} catch (IOException e1) {
		    Dialogs.showErrorDialog(
			    VideoDatabase.getPrimaryStage(),
			    "Please try again, or contact support if the problem persists.",
			    "Oops!", "Error", e1);
		}
	    }
	});
    }
    
    @FXML
    private void filterSelected(ActionEvent event) {
	CheckBox checkBox = (CheckBox) event.getSource();
	SEARCH_FILTERS.put(checkBox.getId(), checkBox.isSelected());
    }
    
    @FXML
    private void onAddGrouping(ActionEvent event) {
	Grouping grouping = lstAvailableGroupings.getSelectionModel().getSelectedItem();
	if (grouping != null) {
	    lstAvailableGroupings.getItems().remove(grouping);
	    lstSelectedGroupings.getItems().add(grouping);
	}
    }
    
    @FXML
    private void onMoveGroupingDown(ActionEvent event) {
	MultipleSelectionModel<Grouping> selectionModel = lstSelectedGroupings.getSelectionModel();
	int index = selectionModel.getSelectedIndex();
	if (index >= 0 && index < GROUPS.size() - 1) {
	    swap(lstSelectedGroupings.getItems(), index++, index);
	    selectionModel.clearAndSelect(index);
	}
    }
    
    @FXML
    private void onMoveGroupingUp(ActionEvent event) {
	MultipleSelectionModel<Grouping> selectionModel = lstSelectedGroupings.getSelectionModel();
	int index = selectionModel.getSelectedIndex();
	if (index > 0 && index < GROUPS.size()) {
	    swap(lstSelectedGroupings.getItems(), index--, index);
	    selectionModel.clearAndSelect(index);
	}
    }
    
    private <E> void swap(List<E> list, int i1, int i2) {
	E temp = list.get(i1);
	list.set(i1, list.get(i2));
	list.set(i2, temp);
    }
    
    @FXML
    private void onRemoveGrouping(ActionEvent event) {
	Grouping grouping = lstSelectedGroupings.getSelectionModel().getSelectedItem();
	if (grouping != null) {
	    lstSelectedGroupings.getItems().remove(grouping);
	    lstAvailableGroupings.getItems().add(grouping);
	}
    }
    
}
