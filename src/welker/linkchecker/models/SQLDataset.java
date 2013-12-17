package welker.linkchecker.models;

import java.sql.*;
import java.util.ArrayList;

import welker.linkchecker.views.GUI;


public class SQLDataset {
	
	private GUI gui;
	
	private Connection con;
	private ResultSet rs;
	private PreparedStatement ps;
	
	private String query;
	private String sierraUsername;
	private String sierraPassword;
	private String sqlHost;
	private String sqlPort;
	private String itemLocationStart;
	private String itemLocationEnd;
	private String bibNumberStart;
	
	
	public SQLDataset(GUI gui){
		
		this.gui = gui;
		
		this.sierraUsername = gui.getSierraUsername().getText();
		this.sierraPassword = gui.getSierraPassword().getText();
		this.sqlHost = gui.getSqlHost().getText();
		this.sqlPort = gui.getSqlPort().getText();
		this.itemLocationStart = gui.getItemLocationStart().getText();
		this.itemLocationEnd = gui.getItemLocationEnd().getText();
		this.bibNumberStart = gui.getBibRecordStart().getText();	
		
		//create the query
		this.query = 
			"select distinct " +
			"rm_bib.record_num as \"bib_id\", " +
			"m856.field_content as \"m856\", " +
			"brp.best_title, " +
			"brp.best_author " +
			
			"from sierra_view.bib_record b " +
			"left join sierra_view.bib_record_property brp on brp.bib_record_id=b.id " +
			"left join sierra_view.record_metadata rm_bib on b.record_id=rm_bib.id " +
			"inner join ( " +
			" select * from sierra_view.varfield v where v.marc_tag='856' " +
			") m856 on b.id=m856.record_id " +
			
			"left join sierra_view.bib_record_item_record_link bil on b.id=bil.bib_record_id " +
			"inner join ( " +
			" select * from sierra_view.item_record i where i.location_code between ? and ? " +
			") i on i.id=bil.item_record_id "+
			
			"WHERE rm_bib.record_num >= ? " +
			
			"order by rm_bib.record_num ASC";
	}
	
	public ArrayList<Link> processQuery(){

		//execute the query and get a resultset
		rs = executeQuery();
		
		//get an array of LinkCheckerRecords from the resultset
		ArrayList<Link> records = new ArrayList();
		records = makeRecords();
		
		//close connections and return records
		boolean closed = closeConnections();
		if(closed){
			return records;
		}else{
			return null;
		}
		
	}
	
	private ResultSet executeQuery(){
		
		//create connection objects
		con = null;
		rs = null;
		ps = null;
		
		//make the connection
		try{
			gui.writeToStatusLabel("Connecting to SQL database...");
			Class.forName("org.postgresql.Driver");
			con = DriverManager.getConnection("jdbc:postgresql://" + sqlHost + ":" + sqlPort + "/iii?ssl=true&sslfactory=org.postgresql.ssl.NonValidatingFactory", sierraUsername, sierraPassword);
			
			//create a prepared statement for a parameterized query
			ps = con.prepareStatement(this.query);
			ps.setString(1, this.itemLocationStart);
			ps.setString(2, this.itemLocationEnd);
			ps.setInt(3, Integer.valueOf(this.bibNumberStart));
			
			//execute query
			gui.writeToStatusLabel("Executing SQL query...");
			gui.writeToActivityLog("Querying the database. In a large database with millions of records, this could take several minutes. Please be patient. If you exit this program while the query is executing, it will continue to execute on the server, and new queries might be blocked until it is finished.");
			gui.toggleStopDisabled(true);
			rs = ps.executeQuery();
			//System.out.println("Query executed successfully.");
			
		} catch(SQLException e) {
			e.printStackTrace();
			gui.writeToActivityLog("SQL query failed.\r\n" + e.getMessage());
            gui.resetUI();
			return null;
		} catch(ClassNotFoundException e){
			e.printStackTrace();
			gui.writeToActivityLog("SQL query failed.\r\n" + e.getMessage());
            gui.resetUI();
			return null;
		}
		
		return rs;
	}
	
	private ArrayList<Link> makeRecords(){

		ArrayList<Link> records = new ArrayList();
		
		try {
			//loop through the results and create a new Link for each one
			//System.out.println("Processing SQL result...");
			while(rs.next()){
				
				String author =  rs.getString("best_author");
				String title =  rs.getString("best_title");
				String m856 = rs.getString("m856");
				String bibNumber = String.valueOf(rs.getInt("bib_id"));
				
				Link record = new Link(gui, title, author, m856, bibNumber);
				//push the Link to the holder array
				records.add(record);
				
			}
		} catch (SQLException e) {
			gui.writeToActivityLog("SQL query failed.\r\n" + e.getMessage());
            gui.resetUI();
		}
		
		return records;
	}
	
	private boolean closeConnections(){
		//re-enable the stop button
		gui.toggleStopDisabled(false);
		
		//close the connection
		try{
			 rs.close();
			 ps.close();
			 con.close();
			 return true;
		 } catch (SQLException e){
			 gui.writeToActivityLog("SQL query failed.\r\n" + e.getMessage());
             gui.resetUI();
			 return false;
		 } 
		
	}
	
	
}
