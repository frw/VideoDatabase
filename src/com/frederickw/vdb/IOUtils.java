/**
 * Class: IOUtils.java
 * Author: Frederick Widjaja
 * School: Jakarta International School
 * Language: Java SE 7
 * IDE: Eclipse
 */
package com.frederickw.vdb;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Dialogs;
import javafx.scene.image.Image;

import org.json.simple.JSONValue;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * A Utilities class that provides methods for IO operations
 * 
 * @author Frederick Widjaja
 * 
 */
public class IOUtils {
    
    public static final File HOME_DIRECTORY = new File(
	    System.getProperty("user.home"));
    public static final File DATABASE_DIRECTORY = new File(HOME_DIRECTORY,
	    "Video Database");
    
    private static final String USER_AGENT = "Mozilla/5.0 (Windows; U; Windows NT 6.1; rv:2.2) Gecko/20110201";
    
    private static final Cache<String, Image> IMAGE_CACHE = CacheBuilder.newBuilder().maximumSize(
	    100).expireAfterAccess(5, TimeUnit.MINUTES).build();
    
    static {
	if (!DATABASE_DIRECTORY.exists() && !DATABASE_DIRECTORY.mkdirs()) {
	    Dialogs.showErrorDialog(
		    null,
		    "Please restart the program, or contact support if the problem persists.",
		    "Unable to create directory "
		            + DATABASE_DIRECTORY.getAbsolutePath());
	}
    }
    
    /**
     * Retrieves a response in JSON format
     * 
     * @param url
     *            The url to retrieve a response from
     * @param kv
     *            Key-value pair for a get request
     * @return The JSON response
     * @throws Exception
     *             If an exception was thrown
     */
    public static Object getJSON(String url, Object... kv) throws Exception {
	return getJSON(createURL(url, kv));
    }
    
    /**
     * Retrieves a response in JSON format
     * 
     * @param The
     *            url to retrieve a response from
     * @return The JSON response
     * @throws Exception
     *             If an exception was thrown
     */
    public static Object getJSON(URL url) throws Exception {
	IOException ioe = null;
	for (int i = 0; i < 3; i++) {
	    try {
		InputStreamReader reader = new InputStreamReader(
		        openStream(url));
		return JSONValue.parseWithException(reader);
	    } catch (IOException e) {
		ioe = e;
	    }
	}
	throw ioe;
    }
    
    /**
     * Generates a url for a get response
     * 
     * @param url
     *            The url to retrieve a response from
     * @param kv
     *            Key-value pair for a get request
     * @return The URL
     * @throws MalformedURLException
     *             If the url generated is malformed
     */
    public static URL createURL(String url, Object... kv)
	    throws MalformedURLException {
	if (kv.length > 0) {
	    StringBuffer sb = new StringBuffer(url);
	    try {
		for (int i = 0; i < kv.length; i += 2) {
		    sb.append(i == 0 ? '?' : '&');
		    sb.append(URLEncoder.encode(kv[i].toString(), "UTF-8"));
		    sb.append('=');
		    sb.append(URLEncoder.encode(kv[i + 1].toString(), "UTF-8"));
		}
	    } catch (UnsupportedEncodingException e) {
	    }
	    return new URL(sb.toString());
	} else {
	    return new URL(url);
	}
    }
    
    /**
     * Opens the url stream after setting the User-Agent of the request
     * 
     * @param url
     *            The url to retrieve a response from
     * @return The InputStream
     * @throws IOException
     *             If an exception occurs during IO
     */
    public static InputStream openStream(URL url) throws IOException {
	URLConnection conn = url.openConnection();
	conn.setRequestProperty("User-Agent", USER_AGENT);
	return conn.getInputStream();
    }
    
    /**
     * Retrieves an image from the cache, or generates a new image if it doesn't
     * already exist
     * 
     * @param url
     *            The image url
     * @return An Image
     */
    public static Image getImage(final String url) {
	try {
	    return IMAGE_CACHE.get(url, new Callable<Image>() {
		@Override
		public Image call() {
		    Image img = new Image(url, true);
		    img.errorProperty().addListener(
			    new ChangeListener<Boolean>() {
			        @Override
			        public void changed(
			                ObservableValue<? extends Boolean> ov,
			                Boolean oldValue, Boolean newValue) {
				    if (newValue) {
				        IMAGE_CACHE.invalidate(url);
				    }
			        }
			    });
		    return img;
		}
	    });
	} catch (ExecutionException e) {
	    return null;
	}
    }
    
    /**
     * Retrieves the size of the UTF-8 String
     * 
     * @param s
     *            The UTF-8 String
     * @return The size of the String
     */
    public static int getUTF8StringSize(String s) {
	int strlen = s.length(), utflen = 2, c;
	for (int i = 0; i < strlen; i++) {
	    c = s.charAt(i);
	    if (c >= 0x0001 && c <= 0x007F) {
		utflen++;
	    } else if (c > 0x07FF) {
		utflen += 3;
	    } else {
		utflen += 2;
	    }
	}
	return utflen;
    }
    
    /**
     * Writes a map to a RandomAccessFile
     * 
     * @param raf
     *            The RandomAccessFile
     * @param map
     *            the Map
     * @throws IOException
     *             If an exception occurs during IO
     */
    public static void writeMap(RandomAccessFile raf,
	    Map<String, Set<String>> map) throws IOException {
	raf.writeShort(map.size());
	for (Entry<String, Set<String>> entry : map.entrySet()) {
	    raf.writeUTF(entry.getKey());
	    writeSet(raf, entry.getValue());
	}
    }
    
    /**
     * Reads a map from a RandomAccessFile
     * 
     * @param raf
     *            The RandomAccessFile
     * @return The Map
     * @throws IOException
     *             If an exception occurs during IO
     */
    public static Map<String, Set<String>> readMap(RandomAccessFile raf)
	    throws IOException {
	int mapSize = raf.readUnsignedShort();
	Map<String, Set<String>> map = new LinkedHashMap<>(mapSize);
	for (int i = 0; i < mapSize; i++) {
	    String key = raf.readUTF();
	    int setSize = raf.readUnsignedShort();
	    Set<String> set = new LinkedHashSet<>(setSize);
	    for (int n = 0; n < setSize; n++) {
		set.add(raf.readUTF());
	    }
	    map.put(key, set);
	}
	return map;
    }
    
    /**
     * Gets the size of the map when written onto a file
     * 
     * @param map
     *            The map
     * @return The size of the map
     */
    public static int getMapSize(Map<String, Set<String>> map) {
	int size = 2; // map.size()
	for (Entry<String, Set<String>> entry : map.entrySet()) {
	    size += getUTF8StringSize(entry.getKey());
	    size += getSetSize(entry.getValue());
	}
	return size;
    }
    
    /**
     * Writes a set to a RandomAccessFile
     * 
     * @param raf
     *            The RandomAccessFile
     * @param set
     *            The Set
     * @throws IOException
     *             If an exception occurs during IO
     */
    public static void writeSet(RandomAccessFile raf, Set<String> set)
	    throws IOException {
	raf.writeShort(set.size());
	for (String s : set) {
	    raf.writeUTF(s);
	}
    }
    
    /**
     * Reads a set from a RandomAccessFile
     * 
     * @param raf
     *            The RandomAccessFile
     * @return The Set
     * @throws IOException
     *             If an exception occurs during IO
     */
    public static Set<String> readSet(RandomAccessFile raf) throws IOException {
	int setSize = raf.readUnsignedShort();
	Set<String> set = new TreeSet<>();
	for (int n = 0; n < setSize; n++) {
	    set.add(raf.readUTF());
	}
	return set;
    }
    
    /**
     * Gets the size of the set when written to a file
     * 
     * @param set
     *            The Set
     * @return The size of the Set
     */
    public static int getSetSize(Set<String> set) {
	int size = 2; // collection.size()
	for (String s : set) {
	    size += getUTF8StringSize(s);
	}
	return size;
    }
    
}
