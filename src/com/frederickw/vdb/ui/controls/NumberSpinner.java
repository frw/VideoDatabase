package com.frederickw.vdb.ui.controls;

import java.text.NumberFormat;

import javafx.beans.binding.NumberBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

import javax.swing.JSpinner;


/**
 * JavaFX Control that behaves like a {@link JSpinner} known in Swing. The
 * number in the textfield can be incremented or decremented by a configurable
 * stepSize using the arrow buttons in the control or the up and down arrow
 * keys.
 * 
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class NumberSpinner extends HBox {
    public static final String ARROW = "NumberSpinnerArrow";
    public static final String NUMBER_FIELD = "NumberField";
    public static final String NUMBER_SPINNER = "NumberSpinner";
    public static final String SPINNER_BUTTON_UP = "SpinnerButtonUp";
    public static final String SPINNER_BUTTON_DOWN = "SpinnerButtonDown";
    public static final String BUTTONS_BOX = "ButtonsBox";
    
    private static final double ARROW_SIZE = 4;
    
    private final NumberTextField numberField;
    private final Button incrementButton;
    private final Button decrementButton;
    
    private final ObjectProperty<Number> stepSizeProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<Comparable> minimumProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<Comparable> maximumProperty = new SimpleObjectProperty<>();
    
    public NumberSpinner() {
	this(Integer.valueOf(0), Integer.valueOf(1), null, null);
    }
    
    public NumberSpinner(int value, int stepSize, int minimum, int maximum) {
	this(Integer.valueOf(value), Integer.valueOf(stepSize), Integer
	        .valueOf(minimum), Integer.valueOf(maximum));
    }
    
    public NumberSpinner(double value, double stepSize, double minimum,
	    double maximum) {
	this(Double.valueOf(value), Double.valueOf(stepSize), Double
	        .valueOf(minimum), Double.valueOf(maximum));
    }
    
    public NumberSpinner(Number value, Number stepSize, Comparable minimum,
	    Comparable maximum) {
	this(value, stepSize, minimum, maximum, NumberFormat.getInstance());
    }
    
    public NumberSpinner(Number value, Number stepSize, Comparable minimum,
	    Comparable maximum, NumberFormat nf) {
	if (value == null || stepSize == null) {
	    throw new IllegalArgumentException(
		    "value and stepSize must be non-null");
	}
	if (minimum != null && minimum.compareTo(value) > 0 || maximum != null
	        && maximum.compareTo(value) < 0) {
	    throw new IllegalArgumentException(
		    "(minimum <= value <= maximum) is false");
	}
	setId(NUMBER_SPINNER);
	stepSizeProperty.set(stepSize);
	minimumProperty.set(minimum);
	maximumProperty.set(maximum);
	
	// TextField
	numberField = new NumberTextField(value, nf);
	numberField.setId(NUMBER_FIELD);
	
	// Enable arrow keys for dec/inc
	numberField.addEventFilter(KeyEvent.KEY_PRESSED,
	        new EventHandler<KeyEvent>() {
		    @Override
		    public void handle(KeyEvent e) {
		        switch (e.getCode()) {
			case DOWN:
			    decrement();
			    e.consume();
			    break;
			case UP:
			    increment();
			    e.consume();
			    break;
			default:
			    break;
			}
		    }
	        });
	
	valueProperty().addListener(new ChangeListener<Number>() {
	    @Override
	    public void changed(ObservableValue<? extends Number> ov,
		    Number oldValue, Number newValue) {
		incrementButton.setDisable(getNextValue() == null);
		decrementButton.setDisable(getPreviousValue() == null);
	    }
	});
	
	// Painting the up and down arrows
	Path arrowUp = new Path();
	arrowUp.setId(ARROW);
	arrowUp.getElements().addAll(new MoveTo(-ARROW_SIZE, 0),
	        new LineTo(ARROW_SIZE, 0), new LineTo(0, -ARROW_SIZE),
	        new LineTo(-ARROW_SIZE, 0));
	arrowUp.setFill(Color.BLACK);
	
	Path arrowDown = new Path();
	arrowDown.setId(ARROW);
	arrowDown.getElements().addAll(new MoveTo(-ARROW_SIZE, 0),
	        new LineTo(ARROW_SIZE, 0), new LineTo(0, ARROW_SIZE),
	        new LineTo(-ARROW_SIZE, 0));
	arrowDown.setFill(Color.BLACK);
	
	// inc/dec buttons
	incrementButton = new Button();
	incrementButton.setId(SPINNER_BUTTON_UP);
	incrementButton.setStyle("-fx-background-radius: 0;");
	incrementButton.setGraphic(arrowUp);
	incrementButton.setDisable(getNextValue() == null);
	incrementButton.prefWidthProperty().bind(numberField.heightProperty());
	incrementButton.minWidthProperty().bind(numberField.heightProperty());
	NumberBinding incrementButtonHeight = numberField.heightProperty()
	        .subtract(3).divide(2);
	incrementButton.maxHeightProperty().bind(incrementButtonHeight);
	incrementButton.prefHeightProperty().bind(incrementButtonHeight);
	incrementButton.minHeightProperty().bind(incrementButtonHeight);
	incrementButton.setFocusTraversable(false);
	incrementButton.setOnAction(new EventHandler<ActionEvent>() {
	    @Override
	    public void handle(ActionEvent ae) {
		increment();
		ae.consume();
	    }
	});
	
	decrementButton = new Button();
	decrementButton.setId(SPINNER_BUTTON_DOWN);
	decrementButton.setStyle("-fx-background-radius: 0;");
	decrementButton.setGraphic(arrowDown);
	decrementButton.setDisable(getPreviousValue() == null);
	decrementButton.prefWidthProperty().bind(numberField.heightProperty());
	decrementButton.minWidthProperty().bind(numberField.heightProperty());
	NumberBinding decrementButtonHeight = incrementButtonHeight.add(1);
	decrementButton.maxHeightProperty().bind(decrementButtonHeight);
	decrementButton.prefHeightProperty().bind(decrementButtonHeight);
	decrementButton.minHeightProperty().bind(decrementButtonHeight);
	decrementButton.setFocusTraversable(false);
	decrementButton.setOnAction(new EventHandler<ActionEvent>() {
	    @Override
	    public void handle(ActionEvent ae) {
		decrement();
		ae.consume();
	    }
	});
	
	VBox buttons = new VBox();
	buttons.setId(BUTTONS_BOX);
	buttons.getChildren().addAll(incrementButton, decrementButton);
	getChildren().addAll(numberField, buttons);
    }
    
    /**
     * increment number value by stepSize
     */
    private void increment() {
	Number value = getNextValue();
	if (value != null) {
	    setValue(value);
	}
    }
    
    /**
     * decrement number value by stepSize
     */
    private void decrement() {
	Number value = getPreviousValue();
	if (value != null) {
	    setValue(value);
	}
    }
    
    private Number getNextValue() {
	return stepValue(+1);
    }
    
    private Number getPreviousValue() {
	return stepValue(-1);
    }
    
    private Number stepValue(int direction) {
	Number oldValue = getValue();
	Number stepSize = getStepSize();
	Comparable minimum = getMinimum();
	Comparable maximum = getMaximum();
	
	Number newValue;
	if (oldValue instanceof Double) {
	    double v = oldValue.doubleValue() + direction
		    * stepSize.doubleValue();
	    newValue = Double.valueOf(v);
	} else if (oldValue instanceof Long) {
	    long v = oldValue.longValue() + direction * stepSize.longValue();
	    newValue = Long.valueOf(v);
	} else if (oldValue instanceof Float) {
	    float v = oldValue.floatValue() + direction * stepSize.floatValue();
	    newValue = Float.valueOf(v);
	} else {
	    int v = oldValue.intValue() + direction * stepSize.intValue();
	    if (oldValue instanceof Integer) {
		newValue = Integer.valueOf(v);
	    } else if (oldValue instanceof Short) {
		newValue = Short.valueOf((short) v);
	    } else {
		newValue = Byte.valueOf((byte) v);
	    }
	}
	
	if (minimum != null && minimum.compareTo(newValue) > 0
	        || maximum != null && maximum.compareTo(newValue) < 0) {
	    return null;
	} else {
	    return newValue;
	}
    }
    
    public ObjectProperty<Number> valueProperty() {
	return numberField.valueProperty();
    }
    
    public Number getValue() {
	return numberField.getValue();
    }
    
    public void setValue(Number value) {
	numberField.setValue(value);
    }
    
    public ObjectProperty<Number> stepSizeProperty() {
	return stepSizeProperty;
    }
    
    public Number getStepSize() {
	return stepSizeProperty.get();
    }
    
    public void setStepSize(Number stepSize) {
	stepSizeProperty.set(stepSize);
    }
    
    public ReadOnlyObjectProperty<Comparable> minimumProperty() {
	return minimumProperty;
    }
    
    public Comparable getMinimum() {
	return minimumProperty.get();
    }
    
    public void setMinimum(Comparable minimum) {
	if (minimum != null && minimum.compareTo(getValue()) > 0) {
	    throw new IllegalArgumentException("minimum > value");
	}
	minimumProperty.set(minimum);
	decrementButton.setDisable(getPreviousValue() == null);
    }
    
    public ReadOnlyObjectProperty<Comparable> maximumProperty() {
	return maximumProperty;
    }
    
    public Comparable getMaximum() {
	return maximumProperty.get();
    }
    
    public void setMaximum(Comparable maximum) {
	if (maximum != null && maximum.compareTo(getValue()) < 0) {
	    throw new IllegalArgumentException("maximum < value");
	}
	maximumProperty.set(maximum);
	incrementButton.setDisable(getNextValue() == null);
    }
    
}
