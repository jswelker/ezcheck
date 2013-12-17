package welker.linkchecker.models;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import welker.linkchecker.controllers.Operations;
import welker.linkchecker.views.GUI;

public class TextFileDataset {

	private Operations controller;
	private GUI gui;
	
	private File file;
	
	private int col856;
	private int colTitle;
	private int colAuthor;
	private int colBibNumber;
	
	private String delimiter;
	
	public TextFileDataset(GUI gui){
		this.gui = gui;
	}
	
	public boolean loadFile(){
		
		//try opening the file
		//System.out.println("Sending update to UI..");
		gui.writeToStatusLabel("Locating text file...");
		//System.out.println("Creating File object...");
		this.file = new File(gui.getTextInputFileName().getText());
		BufferedReader reader = null;
		
		try{
			//System.out.println("Creating BufferedReader object...");
			reader = new BufferedReader(new FileReader(file));
			reader.close();
		} catch(FileNotFoundException e){
			gui.writeToActivityLog("Input file not found.\r\n" + e.getMessage());
			return false;
		} catch(IOException e){
			gui.writeToActivityLog("Input file failed to load.\r\n" + e.getMessage());
			return false;
		}
		
		return true;
		
	}
	
	/*
	 * Loop through the text file and create LinkCheckerRecords for each text row
	 */
	public ArrayList<Link> returnParsedRecords(){
		
		this.getColumnNumbers();
		this.getDelimiter();
		
		try{
			//open the file
			gui.writeToStatusLabel("Reading data from text file...");
			BufferedReader reader = null;
			reader = new BufferedReader(new FileReader(file));
			String text = null;
			
			//create an array for holding the output records
			ArrayList<Link> records = new ArrayList();
			
			//decide how many rows to skip due to header and the user-specified skip values
			int skip = 0;
			if(gui.getHeaderRow().isSelected()){
				skip ++;
			}
			if(gui.getTextRecordsToSkip().getText().isEmpty() == false){
				skip += Integer.valueOf(gui.getTextRecordsToSkip().getText());
			}
			
			//loop through lines of the text file
			int rowCount = 1;
			while((text = reader.readLine()) != null){
				
				//skip the first X rows as defined above
				if(rowCount > skip){
					//get new LinkCheckerRecords and merge them with the master array
					ArrayList<Link> newRecords = lineToLinkCheckerRecords(text);
					records.addAll(newRecords);
				}
				rowCount ++;
			}
			
			return records;
			
		} catch(FileNotFoundException e){
			gui.writeToActivityLog("Input file not found.\r\n" + e.getMessage());
			return null;
		} catch(IOException e){
			gui.writeToActivityLog("Input file failed to load.");
			return null;
		}
	}
	
	private ArrayList<Link> lineToLinkCheckerRecords(String text){
		ArrayList<Link> records = new ArrayList();
		
		//split the line by the delimiter
		String[] fields = text.split(delimiter);
		
		//get the various data elements
		String bibNumber = fields[colBibNumber - 1];
		String m856 = fields[col856 - 1];
		String author = fields[colAuthor - 1];
		String title = fields[colTitle - 1];
		
		//get all the 856 data present in the text file's field
		String[] m856s = m856.split(";");
		
		//create a new Link for each 856 data point
		for(String thism856 : m856s){
			//only create it if this 856 data point has a URL, identified by the substring http
			if(thism856.indexOf("http") != -1){
				Link record = new Link(gui, title, author, thism856, bibNumber);
				//push this record to the array
				records.add(record);
			}
		}
		
		return records;
	}
	
	
	/*
	 * Get the column locations from the user interface
	 */
	private void getColumnNumbers(){
		col856 = Integer.valueOf(gui.getM856ColNum().getText());
		colTitle = Integer.valueOf(gui.getTitleColNum().getText());
		colAuthor = Integer.valueOf(gui.getAuthorColNum().getText());
		colBibNumber = Integer.valueOf(gui.getBibColNum().getText());
	}
	
	
	/*
	 * Get the delimiter from the user interface
	 */
	private void getDelimiter(){
		delimiter = null;
		switch(gui.getInputDelimiter().getSelectionModel().getSelectedIndex()){
			case 0:
				delimiter = "\t";
				break;
				
			case 1:
				delimiter = "~";
				break;
		
			default: 
				delimiter = "\t";
				break;
		}
	}
	
}
