package welker.linkchecker.models;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import org.eclipse.jetty.client.HttpResponseException;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpHeaderValue;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;

import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import welker.linkchecker.views.GUI;

public class Link {
	private GUI gui;
	private URI uri;	
	
	private String m856;
	private String title;
	private String author;
	private String bibNumber;
	
	private String url;
	private String statusCode;
	private String statusDescription;

    public static final int timeoutTime = 10; //number of seconds the link checker should run before its thread times out
    private boolean isTimeout;

    private boolean isError;

    private boolean isIgnored;

    public static final String GENERIC_ERROR = "#Link Checker Error#";

	public Link(GUI gui, String title, String author, String m856, String bibNumber){
		this.gui = gui;
		
		this.m856 = m856;
		this.title = title;
		this.author = author;
		this.bibNumber = bibNumber;

        //set this.timeout to true by default and then make it false if it does not timeout after link is checked
        this.isTimeout = true;

        this.isError = false;
        this.isIgnored = false;
		
	}
	
	
	
	/*
	 * This function handles checking the link for this record, sending it through the whole process
	 * of regex, Http request, and creation of a LinkCheckerResult object.
	 */
	public void checkLink(String[] linksToIgnore){
		url = doRegex();

        checkToIgnore(linksToIgnore);
        if(!isIgnored){
            boolean isHTTPS = false;
            if(url.startsWith("https")){
                isHTTPS = true;
            }

            doHTTPRequest(isHTTPS);
        }
		this.isTimeout = false;
	}
	
	/*
	 * Use regex to determine whether this URL belongs to the ignored URL list
	 */
    private void checkToIgnore(String[] linksToIgnore){

        for(int i=0; i < linksToIgnore.length; i++){
            String regexPattern = linksToIgnore[i].replaceAll(" ","").replaceAll(System.getProperty("line.separator"),"");
            if(!regexPattern.isEmpty()){
                Pattern r = Pattern.compile(regexPattern);
                Matcher m = r.matcher(url);
                if(m.find()){
                    isIgnored = true;
                    System.out.println("ignored a link");
                }
            }
        }
    }

	
	/*
	 * Use regex to isolate the url from extraneous 856 data
	 */
	private String doRegex(){
		url = null;
		//The regex pattern starts with http or https and ends with the a MARC field terminator | or $ or a space
		//System.out.println("Setting regex pattern...");
		String regexPattern = "https?://[^\\s\\|\\$\"]+";
		//System.out.println("Compiling regex pattern...");
		Pattern r = Pattern.compile(regexPattern);
		//System.out.println("Searching for URL with regex pattern...");
		Matcher m = r.matcher(m856);
		try{
			if(m.find()){
				url = m.group();
				//System.out.println("Found url " + url);
			}
		}catch(IllegalStateException e){
			e.printStackTrace();
		}
		return url;
	}
	
	
	
	/*
	 * Make an HTTP or HTTPS request to the specified URL and return the response
	 */
	private void doHTTPRequest(boolean isHTTPS){

        //create a Jetty HttpClient
        HttpClient client = null;

        if(isHTTPS){
            //create an SslContextFactory for handling HTTPS
            SslContextFactory ssl = new SslContextFactory();
            ssl.setTrustAll(true);
            client = new HttpClient(ssl);
        }else{
            client = new HttpClient();
        }

        client.setFollowRedirects(true);
        try {
            client.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //System.out.println("Requesting " + url + "...");
        gui.writeToActivityLog("***********************************************************");
        gui.writeToActivityLog("Requesting " + url + "...");

        //Do an HttpPost request to the API to transfer the chat
        ContentResponse response = null;
        try {
            Request request = client.newRequest(url).method(HttpMethod.HEAD);
            request.getHeaders().put(HttpHeader.CONTENT_TYPE, "application/x-www-form-urlencoded");
            request.getHeaders().put(HttpHeader.ACCEPT, "application/x-www-form-urlencoded");
            request.getHeaders().put(HttpHeader.CONNECTION, HttpHeaderValue.KEEP_ALIVE);
            request.getHeaders().put(HttpHeader.USER_AGENT, "Mozilla/4.0");

            response = request.send();

            //System.out.println("Received response.");
            //check the results and save them to variables

            statusCode = String.valueOf(response.getStatus());
            statusDescription = response.getReason();
            //System.out.println("Request succeeded.");
            //System.out.println("Status Code: "+response.getStatus() + " - " + response.getReason());

        } catch (InterruptedException | TimeoutException | ExecutionException | HttpResponseException e ) {
            e.printStackTrace();
            statusCode = this.GENERIC_ERROR;
            statusDescription = this.GENERIC_ERROR;
            isError = true;
        }

        //close the HttpClient
        try {
            client.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }

	}


    public void writeToActivityLog(){
        if(isError){
            gui.writeToActivityLog("Request succeeded for "+ url +". Status code "+statusCode+": "+statusDescription);
        }else{
            gui.writeToActivityLog("Request failed for "+url+".");
        }
    }



	
	public String getStatusCode(){
		return this.statusCode;
	}

    public boolean getIsTimeout(){
        return this.isTimeout;
    }

    public void setStatusCode(String statusCode){
        this.statusCode = statusCode;
    }

    public void setStatusDescription(String statusDescription){
        this.statusDescription = statusDescription;
    }

    public String getM856() {
        return m856;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getBibNumber() {
        return bibNumber;
    }

    public String getUrl() {
        return url;
    }

    public String getStatusDescription() {
        return statusDescription;
    }

    public boolean getIsIgnored(){
        return isIgnored;
    }
}
