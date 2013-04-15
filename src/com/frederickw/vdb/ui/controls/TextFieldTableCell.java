package com.frederickw.vdb.ui.controls;

import java.text.Format;
import java.text.ParseException;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;

public class TextFieldTableCell<S, T> extends TableCell<S, T> {
    
    private ObjectProperty<Format> format = new SimpleObjectProperty<>();
    private TextField textField = new TextField();
    
    public TextFieldTableCell() {
	this(null);
    }
    
    public TextFieldTableCell(Format format) {
	setFormat(format);
	textField.setOnKeyReleased(new EventHandler<KeyEvent>() {
	    @Override
	    public void handle(KeyEvent t) {
		if (t.getCode() == KeyCode.ENTER) {
		    commitEdit();
		} else if (t.getCode() == KeyCode.ESCAPE) {
		    cancelEdit();
		}
	    }
	});
	textField.focusedProperty().addListener(new ChangeListener<Boolean>() {
	    @Override
	    public void changed(ObservableValue<? extends Boolean> ov,
		    Boolean oldValue, Boolean newValue) {
		if (!newValue) {
		    commitEdit();
		}
	    }
	});
    }
    
    @SuppressWarnings("unchecked")
    private T getTextFieldItem() {
	Format format = getFormat();
	String input = textField.getText();
	if (format == null) {
	    return (T) input;
	} else {
	    try {
		return (T) getFormat().parseObject(textField.getText());
	    } catch (ParseException e) {
		return null;
	    }
	}
    }
    
    private void commitEdit() {
	T item = getTextFieldItem();
	commitEdit(item == null ? getItem() : item);
    }
    
    public static <S, T> Callback<TableColumn<S, T>, TableCell<S, T>> forTableColumn() {
	return new Callback<TableColumn<S, T>, TableCell<S, T>>() {
	    @Override
	    public TableCell<S, T> call(TableColumn<S, T> tc) {
		return new TextFieldTableCell<S, T>();
	    }
	};
    }
    
    public static <S, T> Callback<TableColumn<S, T>, TableCell<S, T>> forTableColumn(
	    final Format format) {
	return new Callback<TableColumn<S, T>, TableCell<S, T>>() {
	    @Override
	    public TableCell<S, T> call(TableColumn<S, T> tc) {
		return new TextFieldTableCell<S, T>(format);
	    }
	};
    }
    
    public ObjectProperty<Format> formatProperty() {
	return format;
    }
    
    public Format getFormat() {
	return format.get();
    }
    
    public void setFormat(Format format) {
	this.format.set(format);
    }
    
    @Override
    public void startEdit() {
	if (!isEditable() || !getTableView().isEditable()
	        || !getTableColumn().isEditable()) {
	    return;
	}
	super.startEdit();
	textField.setText(getItemAsText());
	setText(null);
	setGraphic(textField);
	textField.selectAll();
    }
    
    @Override
    public void cancelEdit() {
	super.cancelEdit();
	setText(getItemAsText());
	setGraphic(null);
    }
    
    @Override
    public void updateItem(T item, boolean empty) {
	super.updateItem(item, empty);
	if (isEmpty()) {
	    setText(null);
	    setGraphic(null);
	} else if (isEditing()) {
	    textField.setText(getItemAsText());
	    setText(null);
	    setGraphic(textField);
	} else {
	    setText(getItemAsText());
	    setGraphic(null);
	}
    }
    
    private String getItemAsText() {
	T item = getItem();
	return item == null ? "" : item.toString();
    }
    
}