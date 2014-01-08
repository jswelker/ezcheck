package welker.linkchecker.models;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by welker on 1/8/14.
 */
public class SettingsFile {

    private File file;

    public SettingsFile(String fileLocation){
        this.file = new File(fileLocation);
    }


    /*
     * Open the settings file, read settings, and return a HashMap of the settings
     */
    public HashMap<String, String> readSettings(){
        HashMap<String, String> settings = new HashMap<String, String>();

        FileReader fr = null;
        BufferedReader br = null;
        try {
            fr = new FileReader(file);
            br = new BufferedReader(fr);

            //loop through each line and save its value to the hashmap
            String currentLine = null;
            while((currentLine = br.readLine()) != null){
                String[] setting = currentLine.split("=");
                settings.put(setting[0], setting[1]);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            if(br != null){
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


        return settings;
    }

    public void writeSettings(  HashMap<String, String> settings){

        try{
            FileWriter fw = new FileWriter(file, true);
            BufferedWriter bw = new BufferedWriter(fw);

            //write the data
            for(Map.Entry<String, String> cursor : settings.entrySet()){
                bw.write( new StringBuilder().append(cursor.getKey()).append("=").append(cursor.getValue()).append("\r\n").toString() );
            }

            //close the file
            bw.close();
            fw.close();

        }catch(IOException e){
            e.printStackTrace();
        }

    }

}
