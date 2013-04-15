/**
 * Class: Workspace.java
 * Author: Frederick Widjaja
 * School: Jakarta International School
 * Language: Java SE 7
 * IDE: Eclipse
 */
package com.frederickw.vdb.ui;

import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.SetChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.Dialogs;
import javafx.scene.control.Dialogs.DialogOptions;
import javafx.scene.control.Dialogs.DialogResponse;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.util.Callback;

import com.frederickw.vdb.IOUtils;
import com.frederickw.vdb.VideoDatabase;
import com.frederickw.vdb.data.Database;
import com.frederickw.vdb.data.TVSeries;
import com.frederickw.vdb.data.TVSeries.Episode;
import com.frederickw.vdb.data.Type;
import com.frederickw.vdb.data.Video;
import com.frederickw.vdb.ui.Settings.Grouping;
import com.frederickw.vdb.ui.controls.PTableColumn;
import com.frederickw.vdb.ui.controls.StarRating;

/**
 * A class used to control the behavior of the main GUI
 * 
 * @author Frederick Widjaja
 * 
 */
public class Workspace implements Initializable {
    
    private static final Font NAME_FONT = Font.font("Arial", FontWeight.BOLD,
	    FontPosture.ITALIC, 13.0);
    private static final Font DESCRIPTION_FONT = Font.font("Arial", 13.0);
    
    @FXML
    private Accordion accVideoDetails;
    @FXML
    private Button btnDelete;
    @FXML
    private Button btnEdit;
    @FXML
    private PTableColumn<Episode, Number> colEpisode;
    @FXML
    private PTableColumn<Episode, Number> colSeason;
    @FXML
    private PTableColumn<Episode, String> colTitle;
    @FXML
    private ImageView imgPoster;
    @FXML
    private Label lblGenres;
    @FXML
    private Label lblLanguages;
    @FXML
    private Label lblPlot;
    @FXML
    private Label lblPlotKeywords;
    @FXML
    private Label lblRated;
    @FXML
    private Label lblReleaseDate;
    @FXML
    private Label lblRuntime;
    @FXML
    private Label lblTitle;
    @FXML
    private Label lblType;
    @FXML
    private GridPane paneCast;
    @FXML
    private GridPane paneCrew;
    @FXML
    private TitledPane paneEpisodes;
    @FXML
    private GridPane paneInfo;
    @FXML
    private VBox paneVideo;
    @FXML
    private StarRating srRating;
    @FXML
    private TableView<Episode> tblEpisodes;
    @FXML
    private TreeView<Object> treeVideos;
    @FXML
    private TextField txtSearch;
    
    @FXML
    private void addVideo() {
	VideoDialog vd = new VideoDialog();
	vd.initOwner(VideoDatabase.getPrimaryStage());
	vd.show();
    }
    
    @FXML
    private void deleteVideo() {
	Video video = (Video) treeVideos.getSelectionModel().getSelectedItem().getValue();
	if (video != null
	        && Dialogs.showConfirmDialog(
	                VideoDatabase.getPrimaryStage(),
	                "Are you sure you want to delete this video permanently?",
	                "Confirm delete", "Warning", DialogOptions.YES_NO) == DialogResponse.YES) {
	    Database.onVideoDeleted(video);
	}
    }
    
    @FXML
    private void editVideo() {
	Video video = (Video) treeVideos.getSelectionModel().getSelectedItem().getValue();
	if (video != null) {
	    VideoDialog vd = new VideoDialog(video);
	    vd.initOwner(VideoDatabase.getPrimaryStage());
	    vd.show();
	}
    }
    
    @FXML
    private void searchVideo() {
	Set<Video> results = Database.search(txtSearch.getText());
	treeVideos.setRoot(Database.getVideoTree(results));
    }
    
    @FXML
    private void showSettings() {
	Settings settings = new Settings();
	settings.initOwner(VideoDatabase.getPrimaryStage());
	settings.show();
    }
    
    private void displayVideo(Video video) {
	if (video != null) {
	    String poster = video.getPosterURL();
	    if (!poster.isEmpty()) {
		imgPoster.setImage(IOUtils.getImage(poster));
		imgPoster.setVisible(true);
	    } else {
		imgPoster.setVisible(false);
	    }
	    lblTitle.setText(video.getTitle());
	    srRating.setRating(video.getRating());
	    
	    lblReleaseDate.setText(String.format("%1$d-%2$02d-%3$02d",
		    video.getYear(), video.getMonth(), video.getDate()));
	    Type type = video.getType();
	    lblType.setText(type.toString());
	    lblRated.setText(video.getRated());
	    int runtime = video.getRuntime();
	    lblRuntime.setText(runtime > 0 ? runtime + " mins" : "");
	    lblPlot.setText(video.getPlot());
	    lblPlotKeywords.setText(join(video.getPlotKeywords()));
	    lblGenres.setText(join(video.getGenres()));
	    lblLanguages.setText(join(video.getLanguages()));
	    
	    layoutLabels(paneCast, getTable(video.getCast()));
	    layoutLabels(paneCrew, getTable(video.getCrew()));
	    
	    ObservableList<Episode> items = tblEpisodes.getItems();
	    items.clear();
	    if (type == Type.TV_SERIES) {
		items.addAll(((TVSeries) video).getEpisodes());
		paneEpisodes.setVisible(true);
	    } else {
		paneEpisodes.setVisible(false);
	    }
	    
	    paneVideo.setVisible(true);
	} else {
	    paneVideo.setVisible(false);
	}
    }
    
    private void layoutLabels(GridPane pane, String[][] table) {
	ObservableList<Node> children = pane.getChildren();
	children.clear();
	for (int r = 0; r < table.length; r++) {
	    for (int c = 0; c < 2; c++) {
		String s = table[r][c];
		if (s != null) {
		    Label label = new Label(s);
		    label.setFont(c == 0 ? NAME_FONT : DESCRIPTION_FONT);
		    GridPane.setConstraints(label, c, r);
		    children.add(label);
		}
	    }
	}
    }
    
    private String[][] getTable(Map<String, Set<String>> map) {
	int numRows = 0;
	for (Set<String> value : map.values()) {
	    numRows += Math.max(1, value.size());
	}
	String[][] table = new String[numRows][2];
	int row = 0;
	for (Entry<String, Set<String>> actor : map.entrySet()) {
	    table[row][0] = actor.getKey();
	    Set<String> value = actor.getValue();
	    if (value.isEmpty()) {
		row++;
	    } else {
		for (String s : value) {
		    table[row++][1] = s;
		}
	    }
	}
	return table;
    }
    
    private String join(Collection<String> c) {
	StringBuffer sb = new StringBuffer();
	for (String s : c) {
	    if (sb.length() > 0) {
		sb.append(", ");
	    }
	    sb.append(s);
	}
	return sb.toString();
    }
    
    private void refreshVideoTree() {
	txtSearch.setText("");
	treeVideos.setRoot(Database.getVideoTree(Database.getVideos()));
    }
    
    @Override
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
	assert accVideoDetails != null : "fx:id=\"accVideoDetails\" was not injected: check your FXML file 'Workspace.fxml'.";
	assert btnDelete != null : "fx:id=\"btnDelete\" was not injected: check your FXML file 'Workspace.fxml'.";
	assert btnEdit != null : "fx:id=\"btnEdit\" was not injected: check your FXML file 'Workspace.fxml'.";
	assert colEpisode != null : "fx:id=\"colEpisode\" was not injected: check your FXML file 'Workspace.fxml'.";
	assert colSeason != null : "fx:id=\"colSeason\" was not injected: check your FXML file 'Workspace.fxml'.";
	assert colTitle != null : "fx:id=\"colTitle\" was not injected: check your FXML file 'Workspace.fxml'.";
	assert imgPoster != null : "fx:id=\"imgPoster\" was not injected: check your FXML file 'Workspace.fxml'.";
	assert lblGenres != null : "fx:id=\"lblGenres\" was not injected: check your FXML file 'Workspace.fxml'.";
	assert lblLanguages != null : "fx:id=\"lblLanguages\" was not injected: check your FXML file 'Workspace.fxml'.";
	assert lblPlot != null : "fx:id=\"lblPlot\" was not injected: check your FXML file 'Workspace.fxml'.";
	assert lblPlotKeywords != null : "fx:id=\"lblPlotKeywords\" was not injected: check your FXML file 'Workspace.fxml'.";
	assert lblRated != null : "fx:id=\"lblRated\" was not injected: check your FXML file 'Workspace.fxml'.";
	assert lblReleaseDate != null : "fx:id=\"lblReleaseDate\" was not injected: check your FXML file 'Workspace.fxml'.";
	assert lblRuntime != null : "fx:id=\"lblRuntime\" was not injected: check your FXML file 'Workspace.fxml'.";
	assert lblTitle != null : "fx:id=\"lblTitle\" was not injected: check your FXML file 'Workspace.fxml'.";
	assert lblType != null : "fx:id=\"lblType\" was not injected: check your FXML file 'Workspace.fxml'.";
	assert paneCast != null : "fx:id=\"paneCast\" was not injected: check your FXML file 'Workspace.fxml'.";
	assert paneCrew != null : "fx:id=\"paneCrew\" was not injected: check your FXML file 'Workspace.fxml'.";
	assert paneEpisodes != null : "fx:id=\"paneEpisodes\" was not injected: check your FXML file 'Workspace.fxml'.";
	assert paneInfo != null : "fx:id=\"paneInfo\" was not injected: check your FXML file 'Workspace.fxml'.";
	assert paneVideo != null : "fx:id=\"paneVideo\" was not injected: check your FXML file 'Workspace.fxml'.";
	assert srRating != null : "fx:id=\"srRating\" was not injected: check your FXML file 'Workspace.fxml'.";
	assert tblEpisodes != null : "fx:id=\"tblEpisodes\" was not injected: check your FXML file 'Workspace.fxml'.";
	assert treeVideos != null : "fx:id=\"treeVideos\" was not injected: check your FXML file 'Workspace.fxml'.";
	assert txtSearch != null : "fx:id=\"txtSearch\" was not injected: check your FXML file 'Workspace.fxml'.";
	
	treeVideos.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
	treeVideos.getSelectionModel().selectedItemProperty().addListener(
	        new ChangeListener<TreeItem<Object>>() {
		    @Override
		    public void changed(
		            ObservableValue<? extends TreeItem<Object>> ov,
		            TreeItem<Object> oldValue, TreeItem<Object> newItem) {
		        Video selected;
		        if (newItem == null
		                || !(newItem.getValue() instanceof Video)) {
			    selected = null;
		        } else {
			    selected = (Video) newItem.getValue();
		        }
		        displayVideo(selected);
		    }
	        });
	
	colSeason.setCellValueFactory(new Callback<CellDataFeatures<Episode, Number>, ObservableValue<Number>>() {
	    @Override
	    public ObservableValue<Number> call(
		    CellDataFeatures<Episode, Number> data) {
		return new SimpleIntegerProperty(data.getValue().getSeason());
	    }
	});
	colEpisode.setCellValueFactory(new Callback<CellDataFeatures<Episode, Number>, ObservableValue<Number>>() {
	    @Override
	    public ObservableValue<Number> call(
		    CellDataFeatures<Episode, Number> data) {
		return new SimpleIntegerProperty(data.getValue().getEpisode());
	    }
	});
	colTitle.setCellValueFactory(new Callback<CellDataFeatures<Episode, String>, ObservableValue<String>>() {
	    @Override
	    public ObservableValue<String> call(
		    CellDataFeatures<Episode, String> data) {
		return new SimpleStringProperty(data.getValue().getTitle());
	    }
	});
	
	refreshVideoTree();
	Database.getVideos().addListener(new SetChangeListener<Video>() {
	    @Override
	    public void onChanged(SetChangeListener.Change<? extends Video> c) {
		refreshVideoTree();
	    }
	});
	Settings.getGroups().addListener(new ListChangeListener<Grouping>() {
	    @Override
	    public void onChanged(
		    ListChangeListener.Change<? extends Grouping> c) {
		refreshVideoTree();
	    }
	});
    }
}