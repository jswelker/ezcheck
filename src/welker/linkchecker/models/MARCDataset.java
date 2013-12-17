package welker.linkchecker.models;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.marc4j.MarcStreamReader;
import org.marc4j.marc.VariableField;

import welker.linkchecker.views.GUI;

public class MARCDataset {

	private GUI gui;
	
	private ArrayList<Link> records;
	
	public MARCDataset(GUI gui){
		this.gui = gui;
		records = new ArrayList();
	}
	
	/*
	 * Open the MARC file and convert it to Link objects
	 */
	public ArrayList<Link> parseFile(){

        gui.writeToStatusLabel("Reading MARC records...");

		//open the file and get a MarcStreamReader object
		MarcStreamReader reader = openFile();
		
		//loop through all marc records and create LinkCheckerRecords, adding them to the array
		ArrayList<Link> records = new ArrayList();
		while(reader.hasNext()){
			ArrayList<Link> newRecords = marcToLinkCheckerRecords(reader);
			records.addAll(newRecords);
		}
		
		return records;
	}
	
	private MarcStreamReader openFile(){
		MarcStreamReader reader = null;
		try{
			//open the file
			FileInputStream input = new FileInputStream(gui.getMarcInputFileName().getText());
			reader = new MarcStreamReader(input);
		}catch(IOException e){
			e.printStackTrace();
			gui.writeToActivityLog("MARC file failed to load.\r\n" + e.getMessage());
            gui.resetUI();
		}
		
		return reader;
	}
	
	 
	 
	 private ArrayList<Link> marcToLinkCheckerRecords(MarcStreamReader reader){
		ArrayList<Link> records = new ArrayList();
	 	//System.out.println("Reading MARC record...");
		org.marc4j.marc.Record record = reader.next();
		
		//get record data
		String idNumber = record.getControlNumber();
		//System.out.println(idNumber);
		
		VariableField authorField = record.getVariableField("100");
		String author;
		if(authorField != null){
			author = record.getVariableField("100").toString();
		}else{
			author = "";
		}
		//System.out.println(author);
		
		String title;
		VariableField titleField = record.getVariableField("245");
		if(titleField != null){
			title = record.getVariableField("245").toString();
		}else{
			title = "";
		}
				
		//System.out.println(title);
		
		//get all the 856 fields attached to this record
		List m856s = record.find("856", "");
		//loop through 856 fields and save the data so that each 856 field generates a separate Link in the program
		for(int i = 0; i < m856s.size(); i++){
			
			VariableField m856Field = (VariableField)m856s.get(i);
			String m856;
			if(m856Field != null){
				m856 = m856Field.toString();
			}else{
				m856 = "";
			}
			//System.out.println(m856);
			
			Link data = new Link(gui, title, author, m856, idNumber);
			records.add(data);
		}
		
		return records;
		
	 }
	 
	 
	 private static String getStringBufferFromFileUTF(File file) throws FileNotFoundException,IOException {
         String sb = new String();
         FileInputStream fis = new FileInputStream(file);
         int n;
         while ((n = fis.available()) > 0) {
             byte[] b = new byte[n];
             int res = fis.read(b);
             if (res == -1)
                 break;
             String s = new String(b, "UTF8");
             sb = sb.concat(s);
         }

         return sb;
     }
	
}
