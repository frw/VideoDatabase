package com.frederickw.vdb.ui.controls;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Popup;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import javafx.util.Callback;

import com.frederickw.vdb.IOUtils;
import com.frederickw.vdb.data.Video;
import com.frederickw.vdb.search.SearchEngine;
import com.frederickw.vdb.search.SearchResult;

public class SearchResultPopup extends Popup {
    private static final double ARROW_SIZE = 5;
    
    private final int pageLimit;
    
    private final BorderPane contentPane;
    
    private final VBox resultsBox;
    private final SearchResultItem[] items;
    private final Button previousPage;
    private final Button nextPage;
    private final Label pageNumber;
    
    private final VBox progressBox;
    private final ProgressIndicator progressIndicator;
    
    private final VBox noResultsBox;
    private final Label noResultsLabel;
    
    private int totalPages;
    private int page = 0;
    private SearchResult[] results;
    private Callback<Video, ?> onSelect;
    
    public SearchResultPopup() {
	this(3);
    }
    
    public SearchResultPopup(int pageLimit) {
	this.pageLimit = pageLimit;
	
	contentPane = new BorderPane();
	contentPane.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8); -fx-background-radius: 10;");
	getContent().add(contentPane);
	
	resultsBox = new VBox();
	resultsBox.setSpacing(10);
	resultsBox.setAlignment(Pos.CENTER);
	resultsBox.setPadding(new Insets(5, 5, 5, 5));
	
	items = new SearchResultItem[pageLimit];
	for (int i = 0; i < pageLimit; i++) {
	    SearchResultItem item = new SearchResultItem();
	    items[i] = item;
	}
	
	HBox navigation = new HBox();
	navigation.setSpacing(5);
	navigation.setAlignment(Pos.CENTER);
	
	Path arrowLeft = new Path();
	arrowLeft.getElements().addAll(new MoveTo(0, -ARROW_SIZE),
	        new LineTo(0, ARROW_SIZE), new LineTo(-ARROW_SIZE, 0),
	        new LineTo(0, -ARROW_SIZE));
	arrowLeft.setFill(Color.BLACK);
	
	previousPage = new Button();
	previousPage.setPrefWidth(21);
	previousPage.setPrefHeight(21);
	previousPage.setMinWidth(Region.USE_PREF_SIZE);
	previousPage.setMinHeight(Region.USE_PREF_SIZE);
	previousPage.setMaxWidth(Region.USE_PREF_SIZE);
	previousPage.setMaxHeight(Region.USE_PREF_SIZE);
	previousPage.setGraphic(arrowLeft);
	previousPage.setOnAction(new EventHandler<ActionEvent>() {
	    @Override
	    public void handle(ActionEvent e) {
		setPage(page - 1);
	    }
	});
	
	pageNumber = new Label();
	pageNumber.setStyle("-fx-text-fill: white");
	
	Path arrowRight = new Path();
	arrowRight.getElements().addAll(new MoveTo(0, -ARROW_SIZE),
	        new LineTo(0, ARROW_SIZE), new LineTo(ARROW_SIZE, 0),
	        new LineTo(0, -ARROW_SIZE));
	arrowRight.setFill(Color.BLACK);
	
	nextPage = new Button();
	nextPage.setPrefWidth(21);
	nextPage.setPrefHeight(21);
	nextPage.setMinWidth(Region.USE_PREF_SIZE);
	nextPage.setMinHeight(Region.USE_PREF_SIZE);
	nextPage.setMaxWidth(Region.USE_PREF_SIZE);
	nextPage.setMaxHeight(Region.USE_PREF_SIZE);
	nextPage.setGraphic(arrowRight);
	nextPage.setOnAction(new EventHandler<ActionEvent>() {
	    @Override
	    public void handle(ActionEvent e) {
		setPage(page + 1);
	    }
	});
	
	navigation.getChildren().addAll(previousPage, pageNumber, nextPage);
	
	resultsBox.getChildren().addAll(items);
	resultsBox.getChildren().add(navigation);
	
	progressBox = new VBox();
	progressBox.setAlignment(Pos.CENTER);
	progressBox.setPadding(new Insets(5, 5, 5, 5));
	
	progressIndicator = new ProgressIndicator(-1.0);
	
	progressBox.getChildren().add(progressIndicator);
	
	noResultsBox = new VBox();
	noResultsBox.setAlignment(Pos.CENTER);
	noResultsBox.setPadding(new Insets(5, 5, 5, 5));
	
	noResultsLabel = new Label(
	        "No results found. Please try again using other keywords.");
	noResultsLabel.setStyle("-fx-text-fill: white");
	
	noResultsBox.getChildren().add(noResultsLabel);
	
	setOnHidden(new EventHandler<WindowEvent>() {
	    @Override
	    public void handle(WindowEvent e) {
		results = null;
		onSelect = null;
		for (SearchResultItem item : items) {
		    item.setSearchResult(null);
		}
	    }
	});
    }
    
    public void show(TextField textField, Button btnSearch,
	    Callback<Video, ?> onSelect) {
	this.onSelect = onSelect;
	
	btnSearch.setDisable(true);
	showProgress();
	
	setWidth(textField.getWidth());
	Point2D p = textField.localToScene(0.0, textField.getHeight() + 5.0);
	Scene scene = textField.getScene();
	Window window = scene.getWindow();
	double screenX = window.getX() + scene.getX() + p.getX();
	double screenY = window.getY() + scene.getY() + p.getY();
	show(textField, screenX, screenY);
	
	startSearch(textField, btnSearch);
    }
    
    private void startSearch(final TextField textField, final Button btnSearch) {
	new Thread() {
	    @Override
	    public void run() {
		final SearchResult[] results = SearchEngine.search(textField.getText());
		if (results.length == 0) {
		    Platform.runLater(new Runnable() {
			@Override
			public void run() {
			    showNoResults();
			    btnSearch.setDisable(false);
			}
		    });
		} else {
		    Platform.runLater(new Runnable() {
			@Override
			public void run() {
			    setSearchResults(results);
			    showResults();
			    btnSearch.setDisable(false);
			}
		    });
		}
	    }
	}.start();
    }
    
    private void showResults() {
	setAutoHide(true);
	BorderPane.setAlignment(resultsBox, Pos.CENTER);
	contentPane.setCenter(resultsBox);
    }
    
    private void showProgress() {
	setAutoHide(false);
	BorderPane.setAlignment(progressBox, Pos.CENTER);
	contentPane.setCenter(progressBox);
    }
    
    private void showNoResults() {
	setAutoHide(true);
	BorderPane.setAlignment(noResultsBox, Pos.CENTER);
	contentPane.setCenter(noResultsBox);
    }
    
    private void setSearchResults(SearchResult[] results) {
	this.results = results;
	totalPages = results.length / pageLimit;
	if (results.length % pageLimit != 0) {
	    totalPages++;
	}
	setPage(0);
    }
    
    private void setPage(int page) {
	this.page = page;
	
	int offset = page * pageLimit;
	for (int i = 0; i < pageLimit; i++) {
	    SearchResult result = null;
	    int idx = offset + i;
	    if (idx < results.length) {
		result = results[idx];
	    }
	    items[i].setSearchResult(result);
	}
	previousPage.setDisable(page == 0);
	pageNumber.setText(page + 1 + "/" + totalPages);
	nextPage.setDisable(page == totalPages - 1);
    }
    
    private class SearchResultItem extends BorderPane {
	private static final String STANDARD_STYLE = "-fx-background-color: transparent; -fx-background-radius: 10;";
	private static final String HOVERED_STYLE = "-fx-background-color: rgba(100, 100, 100, 0.8); -fx-background-radius: 10;";
	
	private final ImageView poster;
	private final Label title;
	private final Label type;
	
	private SearchResult result;
	
	public SearchResultItem() {
	    setPadding(new Insets(10, 10, 10, 10));
	    styleProperty().bind(
		    Bindings.when(hoverProperty()).then(
		            new SimpleStringProperty(HOVERED_STYLE)).otherwise(
		            new SimpleStringProperty(STANDARD_STYLE)));
	    
	    poster = new ImageView();
	    poster.setFitWidth(100);
	    poster.setFitHeight(100);
	    poster.setPreserveRatio(true);
	    poster.setMouseTransparent(true);
	    BorderPane.setAlignment(poster, Pos.CENTER_RIGHT);
	    BorderPane.setMargin(poster, new Insets(0, 5, 0, 0));
	    setLeft(poster);
	    
	    VBox vbox = new VBox();
	    vbox.setSpacing(5.0);
	    vbox.setMouseTransparent(true);
	    BorderPane.setAlignment(vbox, Pos.CENTER_LEFT);
	    
	    title = new Label();
	    title.setWrapText(true);
	    title.setFont(Font.font("Arial", FontWeight.BOLD, 25.0));
	    title.setStyle("-fx-text-fill: white");
	    title.setMouseTransparent(true);
	    
	    type = new Label();
	    type.setFont(Font.font("Arial", 15.0));
	    type.setStyle("-fx-text-fill: white");
	    type.setMouseTransparent(true);
	    
	    vbox.getChildren().addAll(title, type);
	    
	    setCenter(vbox);
	    
	    setOnMouseClicked(new EventHandler<MouseEvent>() {
		@Override
		public void handle(MouseEvent e) {
		    Video video = result.grab();
		    if (video != null && onSelect != null) {
			onSelect.call(video);
		    }
		    hide();
		}
	    });
	}
	
	public void setSearchResult(SearchResult result) {
	    this.result = result;
	    if (result == null) {
		poster.setImage(null);
		title.setText("");
		type.setText("");
	    } else {
		String posterURL = null;
		for (int i = 0; i < 4 && posterURL == null; i++) {
		    posterURL = result.getPosterURL(i);
		}
		if (posterURL != null) {
		    poster.setImage(IOUtils.getImage(posterURL));
		    poster.setVisible(true);
		} else {
		    poster.setImage(null);
		    poster.setVisible(false);
		}
		title.setText(result.getTitle() + " (" + result.getYear() + ")");
		type.setText(result.getType().toString());
	    }
	}
    }
}
