/**
 * Class: Resource.java
 * Author: Frederick Widjaja
 * School: Jakarta International School
 * Language: Java SE 7
 * IDE: Eclipse
 */
package com.frederickw.vdb.ui.resources;

import java.io.InputStream;
import java.net.URL;

/**
 * A class used to load resources
 * 
 * @author Frederick Widjaja
 * 
 */
public class Resource {
    
    public static URL get(String name) {
	return Resource.class.getResource(name);
    }
    
    public static InputStream getAsStream(String name) {
	return Resource.class.getResourceAsStream(name);
    }
    
    public static URL getImage(String name) {
	return get("image/" + name + ".png");
    }
    
    public static InputStream getImageAsStream(String name) {
	return getAsStream("image/" + name + ".png");
    }
    
    public static URL getCSS(String name) {
	return get("css/" + name + ".css");
    }
    
    public static URL getFXML(String name) {
	return get("fxml/" + name + ".fxml");
    }
    
}
