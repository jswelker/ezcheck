package welker.linkchecker.views;

import welker.linkchecker.models.Link;
import welker.linkchecker.views.GUI;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by welker on 12/13/13.
 */
public class ResultsFile {

    private GUI gui;
    private String delimiter;

    public ResultsFile(GUI gui, String delimiter){
        this.gui = gui;
        this.delimiter = delimiter;

    }


    /*
	 * Writes data about a record to a text file.
	 * If the text file already exists, it appends text rather than overwriting.
	 */
    public synchronized void writeResults(Link link){
        //update the status code and description if the link checker timed out
        if(link.getIsTimeout()){
            link.setStatusCode(Link.GENERIC_ERROR);
            link.setStatusDescription("Request to URL timed out.");
        }




        try{
            File file = new File(gui.getOutputFileName().getText());
            boolean isNew = false;
            if(!file.exists()){
                file.createNewFile();
                isNew = true;
            }

            FileWriter fw = new FileWriter(file, true);
            BufferedWriter bw = new BufferedWriter(fw);

            //write a header row if one does not yet exist
            if(isNew){
                bw.write( "URL" + delimiter +
                        "HTTP Status Code" + delimiter +
                        "HTTP Status Description" + delimiter +
                        "Record Number" + delimiter +
                        "MARC 856" + delimiter +
                        "Title" + delimiter +
                        "Author" + "\r\n" );
            }

            //write the data to a new row
            //System.out.println("Writing to file...");
            bw.write( link.getUrl() + delimiter +
                    link.getStatusCode() + delimiter +
                    link.getStatusDescription() + delimiter +
                    String.valueOf(link.getBibNumber()) + delimiter +
                    link.getM856() + delimiter +
                    link.getTitle() + delimiter +
                    link.getAuthor() + "\r\n" );

            //System.out.println("Closing file...");
            bw.close();
            fw.close();

        }catch(IOException e){
            e.printStackTrace();
        }
    }


}
