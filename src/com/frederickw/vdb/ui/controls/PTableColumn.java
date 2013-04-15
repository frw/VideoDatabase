package com.frederickw.vdb.ui.controls;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class PTableColumn<S, T> extends TableColumn<S, T> {
    
    private final DoubleProperty percentageWidth;
    private ChangeListener<Number> widthListener;
    
    public PTableColumn() {
	this(1.0);
    }
    
    public PTableColumn(double percentageWidth) {
	this.percentageWidth = new SimpleDoubleProperty(percentageWidth);
	tableViewProperty().addListener(new ChangeListener<TableView<S>>() {
	    @Override
	    public void changed(ObservableValue<? extends TableView<S>> ov,
		    TableView<S> oldTable, final TableView<S> newTable) {
		prefWidthProperty().bind(
		        newTable.widthProperty().multiply(
		                percentageWidthProperty()));
		if (widthListener != null) {
		    widthProperty().removeListener(widthListener);
		}
		widthListener = new ChangeListener<Number>() {
		    @Override
		    public void changed(ObservableValue<? extends Number> ov,
			    Number oldValue, Number newValue) {
			TableView<S> table = getTableView();
			if (table != null && table.isPressed()) {
			    setPercentageWidth(newValue.doubleValue()
				    / newTable.getWidth());
			}
		    }
		};
		widthProperty().addListener(widthListener);
	    }
	});
    }
    
    public final DoubleProperty percentageWidthProperty() {
	return this.percentageWidth;
    }
    
    public final double getPercentageWidth() {
	return this.percentageWidthProperty().get();
    }
    
    public final void setPercentageWidth(double value)
	    throws IllegalArgumentException {
	if (value >= 0 && value <= 1) {
	    this.percentageWidthProperty().set(value);
	} else {
	    throw new IllegalArgumentException(
		    String.format(
		            "The provided percentage width is not between 0.0 and 1.0. Value is: %1$s",
		            value));
	}
    }
}