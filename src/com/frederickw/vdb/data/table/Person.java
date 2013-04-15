package com.frederickw.vdb.data.table;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Person {
    private final StringProperty name;
    private final StringProperty description;
    
    public Person() {
	this("", "");
    }
    
    public Person(String name, String description) {
	this.name = new SimpleStringProperty(name);
	this.description = new SimpleStringProperty(description);
    }
    
    public String getName() {
	return name.get();
    }
    
    public void setName(String name) {
	this.name.set(name);
    }
    
    public String getDescription() {
	return description.get();
    }
    
    public void setDescription(String description) {
	this.description.set(description);
    }
    
    @Override
    public String toString() {
	return getName();
    }
}