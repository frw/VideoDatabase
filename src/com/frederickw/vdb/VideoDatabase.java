/**
 * Class: VideoDatabase.java
 * Author: Frederick Widjaja
 * School: Jakarta International School
 * Language: Java SE 7
 * IDE: Eclipse
 */

package com.frederickw.vdb;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Dialogs;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import com.frederickw.vdb.data.Database;
import com.frederickw.vdb.ui.Settings;
import com.frederickw.vdb.ui.resources.Resource;

/**
 * Application entry point
 * 
 * @author Frederick Widjaja
 * 
 */
public class VideoDatabase extends Application {
    
    private static Stage primaryStage;
    
    /**
     * Initializes the primary stage
     * 
     * @param primaryStage
     *            The primary stage provided by the FX Application
     * @throws IOException
     *             If an exception occurs when loading the FXML file
     */
    private void init(Stage primaryStage) throws IOException {
	VideoDatabase.primaryStage = primaryStage;
	primaryStage.getIcons().add(
	        new Image(Resource.getImageAsStream("icon")));
	primaryStage.setTitle("Video Database");
	Parent contentPane = FXMLLoader.load(Resource.getFXML("Workspace"));
	Scene scene = new Scene(contentPane);
	primaryStage.setScene(scene);
    }
    
    /**
     * @return The primary stage as provided by the FX Application
     */
    public static Stage getPrimaryStage() {
	return primaryStage;
    }
    
    /**
     * FX Application starting point
     */
    @Override
    public void start(Stage primaryStage) throws IOException {
	init(primaryStage);
	primaryStage.show();
    }
    
    /**
     * Launches the program
     * 
     * @param args
     *            Program parameters
     */
    public static void main(String[] args) {
	try {
	    Database.loadVideos();
	    Settings.loadSettings();
	} catch (IOException e) {
	    Dialogs.showErrorDialog(
		    VideoDatabase.getPrimaryStage(),
		    "Please restart the program, or contact support if the problem persists.",
		    "Error while loading videos and settings", "Error", e);
	}
	
	launch(args);
    }
    
}
