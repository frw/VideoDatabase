/**
 * Class: VideoDialog.java
 * Author: Frederick Widjaja
 * School: Jakarta International School
 * Language: Java SE 7
 * IDE: Eclipse
 */
package com.frederickw.vdb.ui;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialogs;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;

import com.frederickw.vdb.IOUtils;
import com.frederickw.vdb.data.Database;
import com.frederickw.vdb.data.TVSeries;
import com.frederickw.vdb.data.Type;
import com.frederickw.vdb.data.Video;
import com.frederickw.vdb.data.table.Episode;
import com.frederickw.vdb.data.table.Person;
import com.frederickw.vdb.ui.controls.NumberSpinner;
import com.frederickw.vdb.ui.controls.SearchResultPopup;
import com.frederickw.vdb.ui.controls.StarRating;
import com.frederickw.vdb.ui.controls.TextFieldTableCell;
import com.frederickw.vdb.ui.resources.Resource;

import eu.schudt.javafx.controls.calendar.DatePicker;

/**
 * A class used to control the behavior of the video dialog
 * 
 * @author Frederick Widjaja
 * 
 */
public class VideoDialog extends Stage implements Initializable {
    
    private static final FileChooser FILE_CHOOSER = new FileChooser();
    static {
	FILE_CHOOSER.setInitialDirectory(IOUtils.HOME_DIRECTORY);
    }
    
    @FXML
    private Button btnCancel;
    @FXML
    private Button btnCastAdd;
    @FXML
    private Button btnCastDown;
    @FXML
    private Button btnCastRemove;
    @FXML
    private Button btnCastUp;
    @FXML
    private Button btnCrewAdd;
    @FXML
    private Button btnCrewDown;
    @FXML
    private Button btnCrewRemove;
    @FXML
    private Button btnCrewUp;
    @FXML
    private Button btnEpisodesAdd;
    @FXML
    private Button btnEpisodesRemove;
    @FXML
    private Button btnOK;
    @FXML
    private Button btnOpen;
    @FXML
    private Button btnSearch;
    @FXML
    private ComboBox<Type> cmbType;
    @FXML
    private TableColumn<Person, String> colActor;
    @FXML
    private TableColumn<Episode, Integer> colEpisode;
    @FXML
    private TableColumn<Person, String> colJob;
    @FXML
    private TableColumn<Person, String> colMember;
    @FXML
    private TableColumn<Person, String> colRole;
    @FXML
    private TableColumn<Episode, Integer> colSeason;
    @FXML
    private TableColumn<Episode, String> colTitle;
    @FXML
    private DatePicker dpReleaseDate;
    @FXML
    private ImageView imgPreview;
    @FXML
    private NumberSpinner spnrRuntime;
    @FXML
    private StarRating srRating;
    @FXML
    private Tab tabEpisodes;
    @FXML
    private TableView<Person> tblCast;
    @FXML
    private TableView<Person> tblCrew;
    @FXML
    private TableView<Episode> tblEpisodes;
    @FXML
    private TextField txtGenres;
    @FXML
    private TextField txtLanguages;
    @FXML
    private TextArea txtPlot;
    @FXML
    private TextField txtPlotKeywords;
    @FXML
    private TextField txtPoster;
    @FXML
    private TextField txtRated;
    @FXML
    private TextField txtTitle;
    
    private final SearchResultPopup popup = new SearchResultPopup();
    
    private Video video;
    
    public VideoDialog() {
	this(null);
    }
    
    public VideoDialog(Video video) {
	getIcons().add(new Image(Resource.getImageAsStream("icon")));
	setTitle((video == null ? "Add" : "Edit") + " Video");
	Parent parent;
	try {
	    parent = FXMLLoader.load(Resource.getFXML("VideoDialog"), null,
		    new JavaFXBuilderFactory(),
		    new Callback<Class<?>, Object>() {
		        @Override
		        public Object call(Class<?> c) {
			    if (c.equals(VideoDialog.class)) {
			        return VideoDialog.this;
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
	
	this.video = video;
	if (video != null) {
	    fillFields(video);
	}
    }
    
    @FXML
    private void searchOnline() {
	popup.show(txtTitle, btnSearch, new Callback<Video, Object>() {
	    @Override
	    public Object call(Video v) {
		fillFields(v);
		return null;
	    }
	});
    }
    
    @FXML
    private void updatePreview() {
	String url = txtPoster.getText();
	if (!url.isEmpty()) {
	    Image poster = IOUtils.getImage(url);
	    imgPreview.setImage(poster);
	}
    }
    
    @FXML
    private void selectFile() {
	File file = FILE_CHOOSER.showOpenDialog(this);
	if (file != null) {
	    try {
		txtPoster.setText(file.toURI().toURL().toExternalForm());
		updatePreview();
	    } catch (MalformedURLException mue) {
	    }
	}
    }
    
    @FXML
    private void addActor() {
	addElement(tblCast, new Person());
    }
    
    @FXML
    private void removeActors() {
	removeSelected(tblCast);
    }
    
    @FXML
    private void moveActorsUp() {
	moveSelectedUp(tblCast);
    }
    
    @FXML
    private void moveActorsDown() {
	moveSelectedDown(tblCast);
    }
    
    @FXML
    private void addCrewMember() {
	addElement(tblCrew, new Person());
    }
    
    @FXML
    private void removeCrewMembers() {
	removeSelected(tblCrew);
    }
    
    @FXML
    private void moveCrewMembersUp() {
	moveSelectedUp(tblCrew);
    }
    
    @FXML
    private void moveCrewMembersDown() {
	moveSelectedDown(tblCrew);
    }
    
    @FXML
    private void addEpisode() {
	addElement(tblEpisodes, new Episode());
    }
    
    @FXML
    private void removeEpisodes() {
	removeSelected(tblEpisodes);
    }
    
    @FXML
    private void onOK() {
	if (isInputValid()) {
	    boolean editing = video != null;
	    if (editing) {
		Database.onVideoEditing(video);
	    }
	    
	    parseFields();
	    
	    if (editing) {
		Database.onVideoEdited(video);
	    } else {
		Database.onVideoAdded(video);
	    }
	    
	    hide();
	}
    }
    
    @FXML
    private void onCancel() {
	hide();
    }
    
    private void fillFields(Video video) {
	setText(txtTitle, video.getTitle());
	try {
	    dpReleaseDate.setSelectedDate(dpReleaseDate.getDateFormat().parse(
		    video.getYear() + "-" + video.getMonth() + "-"
		            + video.getDate()));
	} catch (ParseException e) {
	}
	cmbType.getSelectionModel().select(video.getType());
	setText(txtPoster, video.getPosterURL());
	updatePreview();
	setText(txtRated, video.getRated());
	srRating.setRating(video.getRating());
	spnrRuntime.setValue(video.getRuntime());
	setText(txtPlot, video.getPlot());
	setText(txtPlotKeywords, join(video.getPlotKeywords()));
	setText(txtGenres, join(video.getGenres()));
	setText(txtLanguages, join(video.getLanguages()));
	
	List<Person> cast = tblCast.getItems();
	cast.clear();
	for (Entry<String, Set<String>> actor : video.getCast().entrySet()) {
	    String name = actor.getKey();
	    Set<String> roles = actor.getValue();
	    if (!roles.isEmpty()) {
		for (String role : actor.getValue()) {
		    cast.add(new Person(name, role));
		}
	    } else {
		cast.add(new Person(name, ""));
	    }
	}
	
	List<Person> crew = tblCrew.getItems();
	crew.clear();
	for (Entry<String, Set<String>> crewMember : video.getCrew().entrySet()) {
	    String name = crewMember.getKey();
	    Set<String> jobs = crewMember.getValue();
	    if (!jobs.isEmpty()) {
		for (String job : crewMember.getValue()) {
		    crew.add(new Person(name, job));
		}
	    } else {
		crew.add(new Person(name, ""));
	    }
	}
	
	if (video instanceof TVSeries) {
	    TVSeries series = (TVSeries) video;
	    List<Episode> episodes = tblEpisodes.getItems();
	    episodes.clear();
	    for (TVSeries.Episode episode : series.getEpisodes()) {
		episodes.add(new Episode(episode));
	    }
	}
    }
    
    private void parseFields() {
	Type type = cmbType.getSelectionModel().getSelectedItem();
	if (video == null) {
	    video = type == Type.TV_SERIES ? new TVSeries() : new Video();
	}
	video.setTitle(txtTitle.getText());
	video.setReleaseDate(Integer.parseInt(dpReleaseDate.getDateFormat().format(
	        dpReleaseDate.getSelectedDate()).replaceAll("-", "")));
	video.setType(type);
	video.setPosterURL(getText(txtPoster));
	video.setRated(getText(txtRated));
	video.setRating(srRating.getRating());
	video.setRuntime(spnrRuntime.getValue().intValue());
	video.setPlot(getText(txtPlot));
	video.addPlotKeywords(split(txtPlotKeywords.getText()));
	video.addGenres(split(txtGenres.getText()));
	video.addLanguages(split(txtLanguages.getText()));
	
	video.getCast().clear();
	for (Person person : tblCast.getItems()) {
	    video.addActor(person.getName(), person.getDescription());
	}
	
	video.getCrew().clear();
	for (Person person : tblCrew.getItems()) {
	    video.addCrewMember(person.getName(), person.getDescription());
	}
	
	if (type == Type.TV_SERIES) {
	    TVSeries series = (TVSeries) video;
	    series.getEpisodes().clear();
	    for (Episode episode : tblEpisodes.getItems()) {
		series.addEpisode(episode.getTVSeriesEpisode());
	    }
	}
    }
    
    private boolean isInputValid() {
	StringBuffer error = new StringBuffer();
	if (txtTitle.getText().isEmpty()) {
	    error.append("Title field is not filled!\n");
	}
	if (dpReleaseDate.getSelectedDate() == null) {
	    error.append("Release date field is not filled!\n");
	}
	for (Person person : tblCast.getItems()) {
	    if (person.getName().isEmpty()) {
		error.append("Cast table is not properly filled!");
		break;
	    }
	}
	for (Person person : tblCrew.getItems()) {
	    if (person.getName().isEmpty()) {
		error.append("Crew table is not properly filled!");
		break;
	    }
	}
	for (Episode episode : tblEpisodes.getItems()) {
	    if (episode.getTitle().isEmpty()) {
		error.append("Episode table is not properly filled!");
		break;
	    }
	}
	boolean valid = error.length() == 0;
	if (!valid) {
	    Dialogs.showErrorDialog(this, error.toString(),
		    "Invalid Input. Please fix before trying again.", "Error");
	}
	return valid;
    }
    
    private String getText(TextInputControl field) {
	String text = field.getText();
	return text;
    }
    
    private void setText(TextInputControl field, String text) {
	field.setText(text == null ? "" : text);
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
    
    private String[] split(String input) {
	return input.isEmpty() ? new String[0] : input.trim().split("( *, *)+");
    }
    
    private <T> void addElement(TableView<T> table, T element) {
	table.getItems().add(element);
	TableViewSelectionModel<T> model = table.getSelectionModel();
	model.clearSelection();
	model.selectLast();
    }
    
    private <T> void removeSelected(TableView<T> table) {
	ObservableList<T> items = table.getItems();
	ObservableList<T> selectedItems = table.getSelectionModel().getSelectedItems();
	for (T t : selectedItems) {
	    items.remove(t);
	}
    }
    
    private <T> void moveSelectedUp(TableView<T> table) {
	List<T> items = table.getItems();
	TableViewSelectionModel<T> model = table.getSelectionModel();
	int[] indices = convert(model.getSelectedIndices());
	for (int i = 0; i < indices.length; i++) {
	    int index = indices[i];
	    int previous = index - 1;
	    indices[i] = previous;
	    if (index > 0) {
		T temp = items.get(previous);
		items.set(previous, items.get(index));
		items.set(index, temp);
	    }
	}
	model.clearSelection();
	for (int index : indices) {
	    model.select(index);
	}
    }
    
    private <T> void moveSelectedDown(TableView<T> table) {
	List<T> items = table.getItems();
	TableViewSelectionModel<T> model = table.getSelectionModel();
	int[] indices = convert(model.getSelectedIndices());
	for (int i = indices.length - 1; i >= 0; i--) {
	    int index = indices[i];
	    int next = index + 1;
	    indices[i] = next;
	    if (index < items.size() - 1) {
		T temp = items.get(next);
		items.set(next, items.get(index));
		items.set(index, temp);
	    }
	}
	model.clearSelection();
	for (int index : indices) {
	    model.select(index);
	}
    }
    
    private int[] convert(Collection<Integer> c) {
	int[] arr = new int[c.size()];
	int i = 0;
	for (Integer e : c) {
	    arr[i++] = e;
	}
	return arr;
    }
    
    @Override
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
	assert btnCancel != null : "fx:id=\"btnCancel\" was not injected: check your FXML file 'VideoDialog.fxml'.";
	assert btnCastAdd != null : "fx:id=\"btnCastAdd\" was not injected: check your FXML file 'VideoDialog.fxml'.";
	assert btnCastDown != null : "fx:id=\"btnCastDown\" was not injected: check your FXML file 'VideoDialog.fxml'.";
	assert btnCastRemove != null : "fx:id=\"btnCastRemove\" was not injected: check your FXML file 'VideoDialog.fxml'.";
	assert btnCastUp != null : "fx:id=\"btnCastUp\" was not injected: check your FXML file 'VideoDialog.fxml'.";
	assert btnCrewAdd != null : "fx:id=\"btnCrewAdd\" was not injected: check your FXML file 'VideoDialog.fxml'.";
	assert btnCrewDown != null : "fx:id=\"btnCrewDown\" was not injected: check your FXML file 'VideoDialog.fxml'.";
	assert btnCrewRemove != null : "fx:id=\"btnCrewRemove\" was not injected: check your FXML file 'VideoDialog.fxml'.";
	assert btnCrewUp != null : "fx:id=\"btnCrewUp\" was not injected: check your FXML file 'VideoDialog.fxml'.";
	assert btnEpisodesAdd != null : "fx:id=\"btnEpisodesAdd\" was not injected: check your FXML file 'VideoDialog.fxml'.";
	assert btnEpisodesRemove != null : "fx:id=\"btnEpisodesRemove\" was not injected: check your FXML file 'VideoDialog.fxml'.";
	assert btnOK != null : "fx:id=\"btnOK\" was not injected: check your FXML file 'VideoDialog.fxml'.";
	assert btnOpen != null : "fx:id=\"btnOpen\" was not injected: check your FXML file 'VideoDialog.fxml'.";
	assert btnSearch != null : "fx:id=\"btnSearch\" was not injected: check your FXML file 'VideoDialog.fxml'.";
	assert cmbType != null : "fx:id=\"cmbType\" was not injected: check your FXML file 'VideoDialog.fxml'.";
	assert colActor != null : "fx:id=\"colActor\" was not injected: check your FXML file 'VideoDialog.fxml'.";
	assert colEpisode != null : "fx:id=\"colEpisode\" was not injected: check your FXML file 'VideoDialog.fxml'.";
	assert colJob != null : "fx:id=\"colJob\" was not injected: check your FXML file 'VideoDialog.fxml'.";
	assert colMember != null : "fx:id=\"colMember\" was not injected: check your FXML file 'VideoDialog.fxml'.";
	assert colRole != null : "fx:id=\"colRole\" was not injected: check your FXML file 'VideoDialog.fxml'.";
	assert colSeason != null : "fx:id=\"colSeason\" was not injected: check your FXML file 'VideoDialog.fxml'.";
	assert colTitle != null : "fx:id=\"colTitle\" was not injected: check your FXML file 'VideoDialog.fxml'.";
	assert dpReleaseDate != null : "fx:id=\"dpReleaseDate\" was not injected: check your FXML file 'VideoDialog.fxml'.";
	assert imgPreview != null : "fx:id=\"imgPreview\" was not injected: check your FXML file 'VideoDialog.fxml'.";
	assert spnrRuntime != null : "fx:id=\"spnrRuntime\" was not injected: check your FXML file 'VideoDialog.fxml'.";
	assert srRating != null : "fx:id=\"srRating\" was not injected: check your FXML file 'VideoDialog.fxml'.";
	assert tabEpisodes != null : "fx:id=\"tabEpisodes\" was not injected: check your FXML file 'VideoDialog.fxml'.";
	assert tblCast != null : "fx:id=\"tblCast\" was not injected: check your FXML file 'VideoDialog.fxml'.";
	assert tblCrew != null : "fx:id=\"tblCrew\" was not injected: check your FXML file 'VideoDialog.fxml'.";
	assert tblEpisodes != null : "fx:id=\"tblEpisodes\" was not injected: check your FXML file 'VideoDialog.fxml'.";
	assert txtGenres != null : "fx:id=\"txtGenres\" was not injected: check your FXML file 'VideoDialog.fxml'.";
	assert txtLanguages != null : "fx:id=\"txtLanguages\" was not injected: check your FXML file 'VideoDialog.fxml'.";
	assert txtPlot != null : "fx:id=\"txtPlot\" was not injected: check your FXML file 'VideoDialog.fxml'.";
	assert txtPlotKeywords != null : "fx:id=\"txtPlotKeywords\" was not injected: check your FXML file 'VideoDialog.fxml'.";
	assert txtPoster != null : "fx:id=\"txtPoster\" was not injected: check your FXML file 'VideoDialog.fxml'.";
	assert txtRated != null : "fx:id=\"txtRated\" was not injected: check your FXML file 'VideoDialog.fxml'.";
	assert txtTitle != null : "fx:id=\"txtTitle\" was not injected: check your FXML file 'VideoDialog.fxml'.";
	
	dpReleaseDate.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
	dpReleaseDate.setPromptText("YYYY-MM-DD");
	
	cmbType.getItems().addAll(Type.values());
	cmbType.getSelectionModel().select(Type.MOVIE);
	cmbType.getSelectionModel().selectedItemProperty().addListener(
	        new ChangeListener<Type>() {
		    @Override
		    public void changed(ObservableValue<? extends Type> ov,
		            Type oldValue, Type newValue) {
		        tabEpisodes.setDisable(!newValue.equals(Type.TV_SERIES));
		    }
	        });
	
	txtPoster.focusedProperty().addListener(new ChangeListener<Boolean>() {
	    @Override
	    public void changed(ObservableValue<? extends Boolean> observable,
		    Boolean oldValue, Boolean newValue) {
		if (!newValue) {
		    updatePreview();
		}
	    }
	});
	
	spnrRuntime.setMinimum(Integer.valueOf(0));
	
	tblCast.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
	tblCrew.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
	tblEpisodes.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
	
	Callback<TableColumn<Person, String>, TableCell<Person, String>> personStringCellFactory = TextFieldTableCell.forTableColumn();
	EventHandler<CellEditEvent<Person, String>> nameEditEventHandler = new EventHandler<CellEditEvent<Person, String>>() {
	    @Override
	    public void handle(CellEditEvent<Person, String> t) {
		t.getTableView().getItems().get(t.getTablePosition().getRow()).setName(
		        t.getNewValue());
	    }
	};
	EventHandler<CellEditEvent<Person, String>> descriptionEditEventHandler = new EventHandler<CellEditEvent<Person, String>>() {
	    @Override
	    public void handle(CellEditEvent<Person, String> t) {
		t.getTableView().getItems().get(t.getTablePosition().getRow()).setDescription(
		        t.getNewValue());
	    }
	};
	
	colActor.setCellValueFactory(new PropertyValueFactory<Person, String>(
	        "name"));
	colActor.setCellFactory(personStringCellFactory);
	colActor.setOnEditCommit(nameEditEventHandler);
	
	colRole.setCellValueFactory(new PropertyValueFactory<Person, String>(
	        "description"));
	colRole.setCellFactory(personStringCellFactory);
	colRole.setOnEditCommit(descriptionEditEventHandler);
	
	colMember.setCellValueFactory(new PropertyValueFactory<Person, String>(
	        "name"));
	colMember.setCellFactory(personStringCellFactory);
	colMember.setOnEditCommit(nameEditEventHandler);
	
	colJob.setCellValueFactory(new PropertyValueFactory<Person, String>(
	        "description"));
	colJob.setCellFactory(personStringCellFactory);
	colJob.setOnEditCommit(descriptionEditEventHandler);
	
	Callback<TableColumn<Episode, Integer>, TableCell<Episode, Integer>> episodeIntegerCellFactory = TextFieldTableCell.forTableColumn(NumberFormat.getIntegerInstance());
	Callback<TableColumn<Episode, String>, TableCell<Episode, String>> episodeStringCellFactory = TextFieldTableCell.forTableColumn();
	EventHandler<CellEditEvent<Episode, Integer>> seasonEditEventHandler = new EventHandler<CellEditEvent<Episode, Integer>>() {
	    @Override
	    public void handle(CellEditEvent<Episode, Integer> t) {
		Number value = t.getNewValue();
		t.getTableView().getItems().get(t.getTablePosition().getRow()).setSeason(
		        value.intValue());
	    }
	};
	EventHandler<CellEditEvent<Episode, Integer>> episodeEditEventHandler = new EventHandler<CellEditEvent<Episode, Integer>>() {
	    @Override
	    public void handle(CellEditEvent<Episode, Integer> t) {
		Number value = t.getNewValue();
		t.getTableView().getItems().get(t.getTablePosition().getRow()).setEpisode(
		        value.intValue());
	    }
	};
	EventHandler<CellEditEvent<Episode, String>> titleEditEventHandler = new EventHandler<CellEditEvent<Episode, String>>() {
	    @Override
	    public void handle(CellEditEvent<Episode, String> t) {
		t.getTableView().getItems().get(t.getTablePosition().getRow()).setTitle(
		        t.getNewValue());
	    }
	};
	colSeason.setCellValueFactory(new PropertyValueFactory<Episode, Integer>(
	        "season"));
	colSeason.setCellFactory(episodeIntegerCellFactory);
	colSeason.setOnEditCommit(seasonEditEventHandler);
	
	colEpisode.setCellValueFactory(new PropertyValueFactory<Episode, Integer>(
	        "episode"));
	colEpisode.setCellFactory(episodeIntegerCellFactory);
	colEpisode.setOnEditCommit(episodeEditEventHandler);
	
	colTitle.setCellValueFactory(new PropertyValueFactory<Episode, String>(
	        "title"));
	colTitle.setCellFactory(episodeStringCellFactory);
	colTitle.setOnEditCommit(titleEditEventHandler);
    }
}