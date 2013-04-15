package com.frederickw.vdb.ui.controls;

import java.text.NumberFormat;
import java.text.ParseException;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.TextField;

/**
 * Textfield implementation that accepts formatted number and stores them in a
 * BigDecimal property The user input is formatted when the focus is lost or the
 * user hits RETURN.
 * 
 */
public class NumberTextField extends TextField {
    
    private final NumberFormat format;
    private final ObjectProperty<Number> valueProperty = new SimpleObjectProperty<>();
    
    public NumberTextField() {
	this(Integer.valueOf(0));
    }
    
    public NumberTextField(Number value) {
	this(value, NumberFormat.getInstance());
    }
    
    public NumberTextField(Number value, NumberFormat nf) {
	super();
	format = nf;
	
	// try to parse when focus is lost or RETURN is hit
	setOnAction(new EventHandler<ActionEvent>() {
	    
	    @Override
	    public void handle(ActionEvent arg0) {
		parseAndFormatInput();
	    }
	});
	
	textProperty().addListener(new ChangeListener<String>() {
	    @Override
	    public void changed(ObservableValue<? extends String> ov,
		    String oldValue, String newValue) {
		if (!isFocused()) {
		    parseAndFormatInput();
		}
	    }
	});
	
	focusedProperty().addListener(new ChangeListener<Boolean>() {
	    @Override
	    public void changed(ObservableValue<? extends Boolean> ov,
		    Boolean oldValue, Boolean newValue) {
		if (!newValue) {
		    parseAndFormatInput();
		}
	    }
	});
	
	// Set text in field if BigDecimal property is changed from outside.
	valueProperty().addListener(new ChangeListener<Number>() {
	    @Override
	    public void changed(ObservableValue<? extends Number> ov,
		    Number oldValue, Number newValue) {
		setText(format.format(newValue));
	    }
	});
	
	setValue(value);
    }
    
    public final Number getValue() {
	return valueProperty.get();
    }
    
    public final void setValue(Number value) {
	valueProperty.set(value);
    }
    
    public ObjectProperty<Number> valueProperty() {
	return valueProperty;
    }
    
    /**
     * Tries to parse the user input to a number according to the provided
     * NumberFormat
     */
    private void parseAndFormatInput() {
	String input = getText();
	if (input != null && !input.isEmpty()) {
	    try {
		Number newValue = format.parse(input);
		Number oldValue = getValue();
		if (!oldValue.getClass().equals(newValue.getClass())) {
		    if (oldValue instanceof Double) {
			newValue = Double.valueOf(newValue.doubleValue());
		    } else if (oldValue instanceof Long) {
			newValue = Long.valueOf(newValue.longValue());
		    } else if (oldValue instanceof Float) {
			newValue = Float.valueOf(newValue.floatValue());
		    } else if (oldValue instanceof Integer) {
			newValue = Integer.valueOf(newValue.intValue());
		    } else if (oldValue instanceof Short) {
			newValue = Short.valueOf(newValue.shortValue());
		    } else if (oldValue instanceof Byte) {
			newValue = Byte.valueOf(newValue.byteValue());
		    }
		}
		setValue(newValue);
		selectAll();
		return;
	    } catch (ParseException ex) {
	    }
	}
	// If parsing fails keep old number
	super.setText(format.format(valueProperty.get()));
    }
}
