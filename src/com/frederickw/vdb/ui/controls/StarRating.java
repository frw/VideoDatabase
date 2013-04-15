package com.frederickw.vdb.ui.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

import com.frederickw.vdb.ui.resources.Resource;

public class StarRating extends HBox {
    public static int UNRATED = 0;
    
    private static final Image STAR_EMPTY = new Image(
	    Resource.getImageAsStream("star_empty"));
    private static final Image STAR_FILLED = new Image(
	    Resource.getImageAsStream("star_filled"));
    
    private final Star[] stars = new Star[10];
    private final Label label = new Label();
    
    private int hover;
    private IntegerProperty ratingProperty = new SimpleIntegerProperty();
    private BooleanProperty editableProperty = new SimpleBooleanProperty(true);
    
    public StarRating() {
	this(UNRATED);
    }
    
    public StarRating(int rating) {
	setSpacing(3);
	setAlignment(Pos.CENTER_LEFT);
	
	ObservableList<Node> children = getChildren();
	for (int i = 0; i < 10; i++) {
	    stars[i] = new Star(i);
	}
	children.addAll(stars);
	children.add(label);
	
	setRating(rating);
	
	setOnMouseExited(new EventHandler<MouseEvent>() {
	    @Override
	    public void handle(MouseEvent e) {
		hover = 0;
		if (isEditable()) {
		    updateRating();
		}
	    }
	});
	editableProperty.addListener(new ChangeListener<Boolean>() {
	    @Override
	    public void changed(ObservableValue<? extends Boolean> ov,
		    Boolean oldValue, Boolean newValue) {
		updateRating();
	    }
	});
    }
    
    public IntegerProperty ratingProperty() {
	return ratingProperty;
    }
    
    public int getRating() {
	return ratingProperty.get();
    }
    
    public void setRating(int rating) {
	ratingProperty.set(rating);
	updateRating();
    }
    
    public boolean isEditable() {
	return editableProperty.get();
    }
    
    public void setEditable(boolean editable) {
	editableProperty.set(editable);
    }
    
    private void updateRating() {
	for (Star star : stars) {
	    star.update();
	}
	int rating = hover != 0 && isEditable() ? hover : getRating();
	label.setText(rating == 0 ? "Unrated" : rating + "/10");
    }
    
    private class Star extends ImageView {
	
	private int index;
	
	Star(final int index) {
	    super(STAR_EMPTY);
	    this.index = index;
	    setFitWidth(24);
	    setFitHeight(24);
	    setOnMouseMoved(new EventHandler<MouseEvent>() {
		@Override
		public void handle(MouseEvent e) {
		    hover = index + 1;
		    if (isEditable()) {
			updateRating();
		    }
		}
	    });
	    setOnMouseClicked(new EventHandler<MouseEvent>() {
		@Override
		public void handle(MouseEvent e) {
		    if (isEditable()) {
			setRating(index + 1);
		    }
		}
	    });
	}
	
	void update() {
	    if (hover != 0 && isEditable()) {
		setImage(index < hover ? STAR_FILLED : STAR_EMPTY);
	    } else {
		setImage(index < getRating() ? STAR_FILLED : STAR_EMPTY);
	    }
	}
	
    }
    
}
