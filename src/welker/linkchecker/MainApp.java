package welker.linkchecker;

import java.io.IOException;

import welker.linkchecker.controllers.Operations;
import welker.linkchecker.views.GUI;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class MainApp extends Application {

	private Stage primaryStage;
	private AnchorPane rootLayout;
	
	private Operations controller;
	private GUI gui;
	
	public static void main(String[] args) {
		launch(args);
	}
	
	@Override
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;
		this.primaryStage.setTitle("Link Checker");
		this.primaryStage.setResizable(false);
		
		//load the FXML layout
		try{
			//load the root layout from the fxml file
			FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("views/MainMenu.fxml"));
			
			//create the controller class for handling interface events
			this.gui = new GUI(this);
			loader.setController(gui);
			
			//show the fxml
			rootLayout = (AnchorPane) loader.load();
			Scene scene = new Scene(rootLayout);
			primaryStage.setScene(scene);
			primaryStage.show();
			
		} catch(IOException e){
			//throw exception if load fails
			e.printStackTrace();
		}finally{
			//create the process controller class for handling the logic of processing the data
			this.controller = new Operations(this.gui);
		}
	}
	
	
	public Operations getMainController(){
		return this.controller;
	}
	

	
}
