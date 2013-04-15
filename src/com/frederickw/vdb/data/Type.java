/**
 * Class: Type.java
 * Author: Frederick Widjaja
 * School: Jakarta International School
 * Language: Java SE 7
 * IDE: Eclipse
 */
package com.frederickw.vdb.data;

/**
 * An enum which represent the video type
 * 
 * @author Frederick Widjaja
 * 
 */
public enum Type implements Comparable<Type> {
    // @formatter:off
    VIDEO("Video", "V"),
    MOVIE("Movie", "M"),
    TV_SERIES("TV Series", "TVS"),
    TV_MOVIE("TV Movie", "TV");
    // @formatter:on
    
    public final String name;
    public final String code;
    
    Type(String name, String code) {
	this.name = name;
	this.code = code;
    }
    
    @Override
    public String toString() {
	return name;
    }
    
    /**
     * Parses a Type based on the code given
     * 
     * @param s
     *            The code
     * @return A Type
     */
    public static Type parse(String s) {
	for (Type type : Type.values()) {
	    if (type.code.equalsIgnoreCase(s)) {
		return type;
	    }
	}
	return null;
    }
}