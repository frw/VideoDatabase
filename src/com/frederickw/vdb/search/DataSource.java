/**
 * Class: DataSource.java
 * Author: Frederick Widjaja
 * School: Jakarta International School
 * Language: Java SE 7
 * IDE: Eclipse
 */
package com.frederickw.vdb.search;

import com.frederickw.vdb.data.Video;

/**
 * A source of data for search results or video information
 * 
 * @author Frederick Widjaja
 * 
 */
public interface DataSource {
    
    SearchResult[] search(String search);
    
    Video grab(SearchResult result);
    
}
