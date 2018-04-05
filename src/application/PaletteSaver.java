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

    private FileChooser saver = new FileChooser();
    private String savedFile = null;        //the full directory to the previously saved file
    private String fileMatcher = ".*err";   //a regex-ready string used to verify a file has the proper extension - initial value should never be seen

    private void setExtensionTxt() {
        saver.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("txt", "*.txt"));
        fileMatcher = ".*txt";
    }


    public void savePalette(String colorString) {
        BufferedWriter bufferedWriter = null;
        FileWriter fileWriter = null;
        try {

            File previousDirectory = null;

            if( savedFile != null )     //check if there is a previously saved file that can be used
                previousDirectory = new File(savedFile.substring(0, savedFile.lastIndexOf("\\") + 1));

            saver.setInitialDirectory(previousDirectory);

            File file = saver.showSaveDialog(null);

            if( file == null || !file.getPath().matches(fileMatcher) )
                return;
            else {
                fileWriter = new FileWriter(file);
                bufferedWriter = new BufferedWriter(fileWriter);
                bufferedWriter.write(colorString);
            }

            savedFile = file.toString();

        } catch (IOException e) { e.printStackTrace();
        } finally {
            try {
                bufferedWriter.close();
                fileWriter.close();
            } catch (IOException e) { e.printStackTrace(); }
        }
    }

}
