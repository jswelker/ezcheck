package welker.linkchecker.controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

import javafx.concurrent.Task;
import welker.linkchecker.models.*;
import welker.linkchecker.views.GUI;

public class Operations {
	
	private GUI gui;
	
	private ArrayList<Link> links;
	
	private String outputDelimiter;

    private String[] linksToIgnore;

    private int errors;
    private int finished;
    private int skipped;
    private boolean isProcessing;

    private Thread currentOperation;

	public Operations(GUI gui){
		this.gui = gui;
        this.isProcessing = false;
	}
	
	public synchronized void createTask(String tabName){
		
		//create a thread for this activity and then start running it
		class ThreadedTask extends Task{
			private String tabName;
			public ThreadedTask(String tabName){
				this.tabName = tabName;
			}

			@Override
			protected Object call() throws Exception {
                processData(tabName);
                return null;
			}
		}
		//create the thread and set it as a daemon so that it closes when the main thread closes
		currentOperation = new Thread(new ThreadedTask(tabName));
		currentOperation.setDaemon(true);
		currentOperation.start();

        this.isProcessing = true;

	}

	
	private void processData(String tabName){

		//Determine which type of process was selected by the user and do special logic for 
		//each of those to get a normalized ArrayList of Link objects

		switch(tabName){
		case "sql":
			System.out.println("Running processSQL...");
			this.processSQL();
			break;
		
		case "text":
			System.out.println("Running processText...");
			this.processText();
			break;
			
		case "marc":
			System.out.println("Running processMARC...");
			this.processMARC();
			break;
		}
		
		//get the output delimiter string
		System.out.println("Running determineOutputDelimiter...");
		this.determineOutputDelimiter();
		
		//loop through the links and do the actual checking and writing to file
		System.out.println("Running loopThroughLinks...");
		this.loopThroughLinks();
		
	}
	
	
	/*
	 * Create the SQLDataset object and get an ArrayList of links for this.links
	 */
	private void processSQL(){
		SQLDataset sql = new SQLDataset(gui);
		links = sql.processQuery();
		if(links == null){
			gui.resetUI();
		}
	}
	
	
	/*
	 * Create the TextFileDataset object and get an ArrayList of links for this.links
	 */
	private void processText(){
		TextFileDataset text = new TextFileDataset(gui);
		boolean fileLoaded = text.loadFile();
		if(fileLoaded){
			this.links = text.returnParsedRecords();
		}else{
			this.links = null;
			gui.resetUI();
		}
	}
	
	/*
	 * Create the MARCDataset object and get an ArrayList of links for this.links
	 */
	private void processMARC(){
		MARCDataset marc = new MARCDataset(gui);
		links = marc.parseFile();
		if(links == null){
			gui.resetUI();
		}
	}
	
	private void determineOutputDelimiter(){
		//determine which delimiter to use in the output file
		outputDelimiter = null;
		switch(gui.getOutputDelimiter().getSelectionModel().getSelectedIndex()){
			case 0:
				outputDelimiter = "\t";
				break;
				
			case 1:
				outputDelimiter = "~";
				break;
			default: 
				outputDelimiter = "\t";
				break;
		}
	}

	/*
	 * Loop through links, check links, write to file, and report on results
	 */
	private void loopThroughLinks(){
		//Loop through Links and check links
        //Each link runs in a separate thread, and the thread times out after 30 seconds

        linksToIgnore = gui.getIgnoreLinks().getText().split(";");

		errors = 0;
        skipped = 0;
		finished = 0;
        ResultsFile file = new ResultsFile(gui, outputDelimiter);
        ExecutorService executor = Executors.newFixedThreadPool(10);

        //create a list of threads and add them to the pool to be executed
        for(int i = 0; i < links.size(); i++){

            //define a new class that extends Task to include i and errors properties
            class ThreadedTask implements Runnable{
                private Link link;
                private ResultsFile file;

                public ThreadedTask(Link link, ResultsFile file){
                    this.link = link;
                    this.file = file;
                }

                @Override
                public void run() {

                    //define a new Callable thread class to be used within this thread
                    class ThreadedSubTask implements Runnable{
                        private Link link;
                        private ResultsFile file;
                        public ThreadedSubTask(Link link, ResultsFile file){
                            this.link = link;
                            this.file = file;
                        }

                        @Override
                        public void run() {
                            //check the link and get a response
                            link.checkLink(linksToIgnore);
                        }
                    }

                    //create the thread from the newly defined class
                    ThreadedSubTask subtask = new ThreadedSubTask(link, file);

                    //create an ExecutorService to execute the thread
                    ExecutorService subExecutor = Executors.newFixedThreadPool(1);
                    subExecutor.execute(subtask);
                    subExecutor.shutdown();

                    //tell the executor not to proceed until either the thread completes or the timeout time has passed
                    try{
                        subExecutor.awaitTermination(link.timeoutTime, TimeUnit.SECONDS);
                    }catch(InterruptedException e){
                        System.out.println("SubExecutor was interrupted before terminating.");
                    }

                    //write results to the activity log and output file, but only if the isProcessing flag is still set to true
                    //write output to file
                    if(isProcessing){
                        link.writeToActivityLog();
                        file.queueResultsToWrite(link);
                    }


                    //increment the finished records counter
                    if(link.getIsIgnored()){
                        skipped++;
                    }else{
                        finished++;
                    }
                    //increment the error count if this link resulted in an error  (timeout or http problem)
                    if(link.getStatusCode() == link.GENERIC_ERROR){
                        errors++;
                    }

                    //update the progress bar
                    gui.updateProgressBar(((double)finished + (double)skipped) / (double) links.size());
                    //update the status label
                    gui.writeToStatusLabel("Checking links... "+(finished+skipped)+" out of "+links.size());
                }
            }

            ThreadedTask thread = new ThreadedTask(links.get(i), file);
            executor.execute(thread);
        }

        //execute the threads and close the thread pool
        executor.shutdown(); //does not terminate threads but just closes the pool and sets it to expire when empty

        //create a loop to wait for either the links to all be finished or for a stop command to be issued
        while(gui.getIsStopped() == false && (finished + skipped) < links.size()){
            try {
                currentOperation.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println("wait error");
                currentOperation.interrupt();
            }
        }


        System.out.println("Stopping...");
        //tell the thread pool to terminate if it hasn't already
        try {
            executor.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        executor.shutdownNow();


		//display the results message
        System.out.println("Finished.");
		String resultsMessage = null;
		if(gui.getIsStopped() == true){
			gui.writeToStatusLabel("Stopped");
			resultsMessage = "***********************************************************\r\n"+
							"Stopped by user.\r\n"+
							finished + " links checked\r\n"+
                            skipped + " links skipped\r\n"+
							errors + " errors\r\n"+
							"***********************************************************";
		}else if((finished + skipped) != links.size()){
			gui.writeToStatusLabel("Inactive");
			resultsMessage = "***********************************************************\r\n"+
							"Link checking did not finish.\r\n"+
							finished + " links checked\r\n"+
                            skipped + " links skipped\r\n"+
							errors + " errors\r\n"+
							"***********************************************************";
		}else{
			gui.writeToStatusLabel("Finished");
			resultsMessage = "***********************************************************\r\n"+
							"Finished checking links.\r\n"+
							finished + " links checked\r\n"+
                            skipped + " links skipped\r\n"+
							errors + " errors\r\n"+
							"***********************************************************";
		}
		
		gui.writeToActivityLog(resultsMessage);
		gui.resetUI();
        this.isProcessing = false;
	}
	
}
