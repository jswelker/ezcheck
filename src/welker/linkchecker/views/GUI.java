package welker.linkchecker.views;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.scene.control.*;
import welker.linkchecker.MainApp;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;

public class GUI implements Initializable{
	
	private AnchorPane rootLayout;
	
	private MainApp app;
	
	private boolean isStopped;
	
	@FXML private Button start;
	@FXML private Button stop;
	@FXML private Button outputFileBrowse;
	@FXML private TextField outputFileName;
	@FXML private ChoiceBox outputDelimiter;
	@FXML private TextArea outputLog;
	@FXML private TextField sierraUsername;
	@FXML private TextField sierraPassword;
	@FXML private TextField sqlHost;
	@FXML private TextField sqlPort;
	@FXML private TextField itemLocationStart;
	@FXML private TextField itemLocationEnd;
	@FXML private TextField bibRecordStart;
	@FXML private TextField textInputFileName;
	@FXML private Button textInputFileBrowse;
	@FXML private TextField textRecordsToSkip;
	@FXML private ChoiceBox inputDelimiter;
	@FXML private TextField bibColNum;
	@FXML private TextField m856ColNum;
	@FXML private TextField titleColNum;
	@FXML private TextField authorColNum;
	@FXML private CheckBox headerRow;
	@FXML private TextField marcInputFileName;
	@FXML private Button marcInputFileBrowse;
	@FXML private TextField marcRecordsToSkip;
	@FXML private TabPane tabs;
	@FXML private Tab sqlTab;
	@FXML private Tab textTab;
	@FXML private Tab marcTab;
	@FXML private Label statusLabel;
	@FXML private ProgressBar progressBar;
	@FXML private ProgressIndicator workingIndicator;

    @FXML private TextArea ignoreLinks;

    @FXML private MenuItem saveSettings;
    @FXML private MenuItem openSettings;
    @FXML private MenuItem about;
    @FXML private MenuItem getHelp;


	public GUI(MainApp app){
		this.app = app;

        //uiUpdatePool = Executors.newFixedThreadPool(1);
	}
	
	
	/*
	 * This method is called automatically by @FXML after initializing objects designated above.
	 * All event handlers and startup methods related to FXML start here.
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		start.setOnAction(new EventHandler<ActionEvent>(){
			public void handle(ActionEvent arg0) {
				lockUI();
				outputLog.setText("");
				processUserRequest();
			}
		});
		
		stop.setOnAction(new EventHandler<ActionEvent>(){
			public void handle(ActionEvent arg0){
				isStopped = true;
			}
		});
		
		outputFileBrowse.setOnAction(new EventHandler<ActionEvent>(){
			public void handle(ActionEvent arg0){
				openTextFileChooser(outputFileName);
			}
		});
		
		textInputFileBrowse.setOnAction(new EventHandler<ActionEvent>(){
			public void handle(ActionEvent arg0){
				openTextFileChooser(textInputFileName);
			}
		});
		
		marcInputFileBrowse.setOnAction(new EventHandler<ActionEvent>(){
			public void handle(ActionEvent arg0){
				openMARCFileChooser(marcInputFileName);
			}
		});
	}
	
	/*
	 * Lock the UI, primarily for when the user has pressed Start and the application is running
	 */
	private void lockUI(){
		start.setDisable(true);
		stop.setDisable(false);
		
		tabs.setDisable(true);
		
		textInputFileName.setDisable(true);
		textInputFileBrowse.setDisable(true);
		textRecordsToSkip.setDisable(true);
		inputDelimiter.setDisable(true);
		m856ColNum.setDisable(true);
		bibColNum.setDisable(true);
		titleColNum.setDisable(true);
		authorColNum.setDisable(true);
		headerRow.setDisable(true);
		
		marcInputFileName.setDisable(true);
		marcInputFileBrowse.setDisable(true);
		marcRecordsToSkip.setDisable(true);
		
		sierraUsername.setDisable(true);
		sierraPassword.setDisable(true);
		sqlHost.setDisable(true);
		sqlPort.setDisable(true);
		itemLocationStart.setDisable(true);
		itemLocationEnd.setDisable(true);
		bibRecordStart.setDisable(true);
		
		outputDelimiter.setDisable(true);
		outputFileName.setDisable(true);
		outputFileBrowse.setDisable(true);
	}
	
	
	/*
	 * Select a file and change a textfield's text value to the name of the file
	 */
	@FXML
	private void openTextFileChooser(TextField field){
		
		try{
			//create file chooser
			FileChooser chooser = new FileChooser();
			
			//set extension filter
			FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Text files (*.txt)", "*.txt");
			chooser.getExtensionFilters().add(filter);
			
			//show open/save file dialog and get the file
			File file = null;
			String fileName = null;
			if(field == this.outputFileName){
				file = chooser.showSaveDialog(null);
				if(file.getPath().indexOf(".txt") == -1){
					fileName = file.getPath() + ".txt";
				}else{
					fileName = file.getPath();
				}
			}else{
				file = chooser.showOpenDialog(null);
				fileName = file.getPath();
			}
			
			//change the text field to display the file name and path
			field.setText(fileName);
		}catch(NullPointerException e){
			e.printStackTrace();
		}
	}
	
	/*
	 * Select a MARC file and change a textfield's text value to the name of the file
	 */
	@FXML
	private void openMARCFileChooser(TextField field){
		
		try{
			//create file chooser
			FileChooser chooser = new FileChooser();
			
			//set extension filter
			FileChooser.ExtensionFilter filter1 = new FileChooser.ExtensionFilter("MARC files (*.mrc)", "*.mrc");
			//FileChooser.ExtensionFilter filter2 = new FileChooser.ExtensionFilter("MARC files (*.mrk)", "*.mrk");
			chooser.getExtensionFilters().add(filter1);
			//chooser.getExtensionFilters().add(filter2);
			
			//show open/save file dialog and get the file
			File file = null;
			String fileName = null;
			if(field == this.outputFileName){
				file = chooser.showSaveDialog(null);
				fileName = file.getPath() + ".txt";
			}else{
				file = chooser.showOpenDialog(null);
				fileName = file.getPath();
			}
			
			//change the text field to display the file name and path
			field.setText(fileName);
		}catch(NullPointerException e){
			e.printStackTrace();
		}
	}
	
	/*
	 * Write to activity log for user to view
	 */
	public void writeToActivityLog(String message){
        //Create a new thread to update the UI so that it doesn't freeze due to concurrency issues
        class ThreadedTask implements Runnable {

            private String message;

            public ThreadedTask(String message){
                this.message = message;
            }

            @Override
            public void run() {
                outputLog.setText(message);
                outputLog.appendText("");

                //scroll to the bottom of the log
                outputLog.setScrollTop(Double.MIN_VALUE);
            }
        }

        String wholeMessage = new StringBuilder().append(outputLog.getText()).append(message).toString();
        //modify the text of the output log so that it is 300 lines or less
        StringBuilder newText = new StringBuilder();
        String[] lines = wholeMessage.split("\r\n");

        for (int i=Math.max(0, lines.length - 300); i<lines.length; i++) {
            newText.append(new StringBuilder().append(lines[i]).append("\r\n").toString());
        }

        ThreadedTask thread = new ThreadedTask(newText.toString());
        Platform.runLater(thread);
	}
	
	
	/*
	 * Write to activity log for user to view
	 */
	public void writeToStatusLabel(String message){
        //Create a new thread to update the UI so that it doesn't freeze due to concurrency issues
        class ThreadedTask implements Runnable {

            private String message;

            public ThreadedTask(String message){
                this.message = message;
            }

            @Override
            public void run() {
                statusLabel.setText(message);
            }
        }

        ThreadedTask thread = new ThreadedTask(message);
        Platform.runLater(thread);
	}
	
	/*
	 * Reset buttons and properties
	 */
	public void resetUI(){
        //Create a new thread to update the UI so that it doesn't freeze due to concurrency issues
        class ThreadedTask implements Runnable {
            @Override
            public void run() {
                isStopped = false;
                start.setDisable(false);
                stop.setDisable(true);

                tabs.setDisable(false);

                textInputFileName.setDisable(false);
                textInputFileBrowse.setDisable(false);
                textRecordsToSkip.setDisable(false);
                inputDelimiter.setDisable(false);
                m856ColNum.setDisable(false);
                bibColNum.setDisable(false);
                titleColNum.setDisable(false);
                authorColNum.setDisable(false);
                headerRow.setDisable(false);

                marcRecordsToSkip.setDisable(false);
                marcInputFileName.setDisable(false);
                marcInputFileBrowse.setDisable(false);

                sierraUsername.setDisable(false);
                sierraPassword.setDisable(false);
                sqlHost.setDisable(false);
                sqlPort.setDisable(false);
                itemLocationStart.setDisable(false);
                itemLocationEnd.setDisable(false);
                bibRecordStart.setDisable(false);

                outputDelimiter.setDisable(false);
                outputFileName.setDisable(false);
                outputFileBrowse.setDisable(false);

                workingIndicator.setVisible(false);
                progressBar.setProgress(0);
                statusLabel.setText("Inactive");
            }
        }

        ThreadedTask thread = new ThreadedTask();
        Platform.runLater(thread);

	}
	
	
	/*
	 * Update the progress bar
	 */
	public void updateProgressBar(double progress){
        //Create a new thread to update the UI so that it doesn't freeze due to concurrency issues
        class ThreadedTask implements Runnable {

            private double progress;

            public ThreadedTask(double progress){
                this.progress = progress;
            }

            @Override
            public void run() {
                progressBar.setProgress(progress);
            }
        }

        ThreadedTask thread = new ThreadedTask(progress);
        Platform.runLater(thread);
	}
	
	
	/*
	 * Toggle whether the Stop button is enabled (useful for disabling it during SQL execution)
	 */
	public void toggleStopDisabled(boolean isDisabled){
        //Create a new thread to update the UI so that it doesn't freeze due to concurrency issues
        class ThreadedTask implements Runnable {

            private boolean isDisabled;

            public ThreadedTask(boolean isDisabled){
                this.isDisabled = isDisabled;
            }

            @Override
            public void run() {
                stop.setDisable(isDisabled);
            }
        }

        ThreadedTask thread = new ThreadedTask(isDisabled);
        Platform.runLater(thread);
	}
	
	/*
	 * Validate user input and send off for processing
	 */
	@FXML
	private void processUserRequest(){
		boolean validated = validateInput();
		if(validated){
			//show the Working Indicator
			workingIndicator.setVisible(true);
			
			//proceed with executing the program in the background thread
			Tab selectedTab = tabs.getSelectionModel().getSelectedItem();
			String tabName = null;
			if(selectedTab.equals(sqlTab)){
				tabName = "sql";
			}else if(selectedTab.equals(textTab)){
				tabName = "text";
			}else if (selectedTab.equals(marcTab)){
				tabName = "marc";
			}
			this.app.getMainController().createTask(tabName);
		}
	}
	
	
	@FXML
	private boolean validateInput(){
		//get the selected tab
		Tab selectedTab = tabs.getSelectionModel().getSelectedItem();
		String tabName = null;
		ArrayList<String> validationErrors = new ArrayList();
		
		//apply validation rules based on the current selected tab
		if(selectedTab.equals(sqlTab)){
			
			tabName = "sql";
			
			if(sierraUsername.getText() == null || sierraUsername.getText().isEmpty()){
				validationErrors.add( "Sierra username must be provided." );
			}
			if(sierraPassword.getText() == null || sierraPassword.getText().isEmpty()){
				validationErrors.add( "Sierra password must be provided." );
			}
			if(sqlHost.getText() == null || sqlHost.getText().isEmpty()){
				validationErrors.add( "Sierra SQL server host must be provided." );
			}
			if(sqlPort.getText() == null || sqlPort.getText().isEmpty()){
				validationErrors.add( "Sierra SQL server port must be provided." );
			}
			try{
				double d = Double.parseDouble(sqlPort.getText());
			} catch(NumberFormatException e){
				validationErrors.add( "Sierra SQL server port must only contain numbers." );
			}
			if(itemLocationStart.getText() == null || itemLocationStart.getText().isEmpty()){
				validationErrors.add( "Item location code start must be provided." );
			}
			if(itemLocationEnd.getText() == null || itemLocationEnd.getText().isEmpty()){
				validationErrors.add( "Item location code end must be provided." );
			}
			if(bibRecordStart.getText() == null || bibRecordStart.getText().isEmpty()){
				validationErrors.add( "Bib record start number must be provided." );
			}
			try{
				double d = Double.parseDouble(bibRecordStart.getText());
			} catch(NumberFormatException e){
				validationErrors.add( "Bib record start number must only contain numbers. Do not include a beginning \"b\" or ending check-digit letter." );
			}
			
		}else if(selectedTab.equals(textTab)){
			
			tabName = "text";
			
			if(inputDelimiter.getSelectionModel().getSelectedIndex() == -1){
				validationErrors.add( "Input field delimiter must be selected." );
			}
			
			if(textInputFileName.getText() == null || textInputFileName.getText().isEmpty()){
				validationErrors.add( "Input file must be provided." );
			}
			
			if(bibColNum.getText() == null || bibColNum.getText().isEmpty()){
				validationErrors.add( "Bib record ID column number must be provided." );
			}
			try{
				double d = Double.parseDouble(bibColNum.getText());
			}catch(NumberFormatException e){
				validationErrors.add( "Bib record ID column number must only contain numbers." );
			}
			
			if(m856ColNum.getText() == null || m856ColNum.getText().isEmpty()){
				validationErrors.add( "856 or hyperlink column number must be provided." );
			}
			try{
				double d = Double.parseDouble(m856ColNum.getText());
			}catch(NumberFormatException e){
				validationErrors.add( "856 or hyperlink column number must only contain numbers." );
			}
			
			if(titleColNum.getText() == null || titleColNum.getText().isEmpty()){
				validationErrors.add( "Title column number must be provided." );
			}
			try{
				double d = Double.parseDouble(titleColNum.getText());
			}catch(NumberFormatException e){
				validationErrors.add( "Title column number must only contain numbers." );
			}
			
			if(authorColNum.getText() == null || authorColNum.getText().isEmpty()){
				validationErrors.add( "Author column number must be provided." );
			}
			try{
				double d = Double.parseDouble(authorColNum.getText());
			}catch(NumberFormatException e){
				validationErrors.add( "Author column number must only contain numbers." );
			}
			
		}else if (selectedTab.equals(marcTab)){
			
			tabName = "marc";
			
			if(marcInputFileName.getText() == null || marcInputFileName.getText().isEmpty()){
				validationErrors.add( "Input file must be provided." );
			}
			
		}
		
		
		if(outputFileName.getText() == null || outputFileName.getText().isEmpty()){
			validationErrors.add( "Output file must be selected." );
		}
		
		//check if any validation errors were detected
		if(!validationErrors.isEmpty()){
			
			String validationMessage = "Please fix the following errors: ";
			for(String s : validationErrors){
				validationMessage += "\r\n" + s;
			}
			
			String outputText = 
				"===============================\r\n"+
				validationMessage;
			
			this.writeToActivityLog(outputText);
			resetUI();
			return false;
			
		}else{
			return true;
		}
	}
	
	

	public AnchorPane getRootLayout() {
		return rootLayout;
	}


	public Button getStart() {
		return start;
	}


	public Button getStop() {
		return stop;
	}


	public Button getOutputFileBrowse() {
		return outputFileBrowse;
	}


	public TextField getOutputFileName() {
		return outputFileName;
	}


	public ChoiceBox getOutputDelimiter() {
		return outputDelimiter;
	}


	public TextArea getOutputLog() {
		return outputLog;
	}


	public TextField getSierraUsername() {
		return sierraUsername;
	}


	public TextField getSierraPassword() {
		return sierraPassword;
	}


	public TextField getSqlHost() {
		return sqlHost;
	}


	public TextField getSqlPort() {
		return sqlPort;
	}


	public TextField getItemLocationStart() {
		return itemLocationStart;
	}


	public TextField getItemLocationEnd() {
		return itemLocationEnd;
	}


	public TextField getBibRecordStart() {
		return bibRecordStart;
	}
	

	public TextField getTextInputFileName(){
		return textInputFileName;
	}
	

	public TextField getTextRecordsToSkip(){
		return textRecordsToSkip;
	}


	public ChoiceBox getInputDelimiter() {
		return inputDelimiter;
	}


	public TextField getBibColNum() {
		return bibColNum;
	}


	public TextField getM856ColNum() {
		return m856ColNum;
	}


	public TextField getTitleColNum() {
		return titleColNum;
	}


	public TextField getAuthorColNum() {
		return authorColNum;
	}


	public CheckBox getHeaderRow(){
		return headerRow;
	}
	

	public TextField getMarcInputFileName(){
		return marcInputFileName;
	}
	

	public TextField getMarcRecordsToSkip(){
		return marcRecordsToSkip;
	}
	

	public TabPane getTabs() {
		return tabs;
	}


	public Tab getSqlTab() {
		return sqlTab;
	}


	public Tab getTextTab() {
		return textTab;
	}


	public Tab getMarcTab() {
		return marcTab;
	}
	
	public Label getStatusLabel(){
		return statusLabel;
	}
	
	public ProgressBar getProgressBar(){
		return progressBar;
	}
	
	public ProgressIndicator getWorkingIndicator(){
		return workingIndicator;
	}

    public TextArea getIgnoreLinks(){
        return ignoreLinks;
    }

	public boolean getIsStopped(){
		return isStopped;
	}


	
}
