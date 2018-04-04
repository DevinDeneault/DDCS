package application;

import javafx.stage.FileChooser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class PaletteSaver {

    PaletteSaver(String title) {
        saver.setTitle(title);
        setExtensionTxt();
    }

    private Bridge bridgeClass = Bridge.getInstance();

    private FileChooser saver = new FileChooser();
    private String savedFile = null;		//the full directory to the previously saved file
    private String fileMatcher = ".*err";

    private void setExtensionTxt() {

        saver.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("txt", "*.txt"));
        fileMatcher = ".*txt";

    }




    public void saveText(String colorString) {
        BufferedWriter bw = null;
        FileWriter fw = null;
        try {

            File previousDirectory = null;

            if(savedFile != null) {															//make sure there was a previously saved file that can be used
                previousDirectory = new File(savedFile.substring(0, savedFile.lastIndexOf("\\") + 1));	//cut off the file name, leaving just the directory
            }

            saver.setInitialDirectory(previousDirectory);

            File file = saver.showSaveDialog(null);											//open the file saver and set the file name/directory to be saved

            if (file == null || !file.getPath().matches(fileMatcher)) {						//make sure something is defined
                return;
            } else {

                fw = new FileWriter(file);
                bw = new BufferedWriter(fw);
                bw.write(colorString);

            }

            savedFile = file.toString();

        } catch (IOException e) { e.printStackTrace();
        } finally {
            try {
                bw.close(); // Close the writer regardless of what happens...
                fw.close();
            } catch (Exception ignored) { }
        }
    }

}
