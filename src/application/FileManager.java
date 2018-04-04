package application;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FileManager {

    private FileOpener imageChooser = new FileOpener("Open Image File", "images");
    private FileOpener paletteChooser = new FileOpener("Open Palette Text File", "text");
    private PngSaver imageSaver = new PngSaver("Save Image File");
    private PaletteSaver paletteSaver = new PaletteSaver("Save Palette Text File");


    //============= working with images ==============================================================================================================
    //================================================================================================================================================

    public IdedImage loadBaseImage() {      //get an image file from a FileChooser and return it

        String imageLocation = imageChooser.getFileLocation();

        if( imageLocation.equals("error") ) //if the error message was received display a pre-packaged image showing something something is wrong
            return new IdedImage(this.getClass().getResourceAsStream("/images/null.png"));

        return new IdedImage("file:" + imageLocation);
    }

    public void saveImage(Palette palette, IdedImage image) {
        imageSaver.saveImage(palette, image);
    }

    //============= working with palette files =======================================================================================================
    //================================================================================================================================================

    public void savePalette(String colorString) {
        paletteSaver.savePalette(colorString);
    }

    public int[][] loadUserPalette() {          //open the FileChooser for the user to select their custom palette and then validate it

        String paletteLocation = paletteChooser.getFileLocation();

        if( paletteLocation.equals("error") )   //if the error message was received return the fall-back value
            return new int[][]{{0, 0, 0}};
        else
            return validatePalette(paletteLocation, true);
    }

    public Map<String, String> getMetaData(String fileLocation, Boolean externalFile) { //grab the metadata from a palette file - only relevant to the built in palettes right now

        Map<String, String> meta = new HashMap<>();
        String[] info;
        InputStream inputStream = null;
        BufferedReader bufferedReader = null;

        try {
            if( externalFile )				//if the file being loaded is from an external source
                inputStream = new FileInputStream(fileLocation);
            else	                        //if the file being loaded is packaged with the program (inside the project)
                inputStream = getClass().getResourceAsStream(fileLocation);

            assert inputStream != null;
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            String line = bufferedReader.readLine();

            while( line != null && line.contains("meta") ) {

                info = line.split(":");
                meta.put(info[1], info[2]);

                line = bufferedReader.readLine();
            }

        } catch (Exception err) {
            return null;	//something went wrong, return the fall-back value
        } finally {
            try {
                Objects.requireNonNull(bufferedReader).close();
                inputStream.close();
            } catch (IOException e) { e.printStackTrace(); }
        }

        if( meta.size() == 0 )
            return null;
        else
            return meta;
    }

    public String[] getBuiltInPaletteList() {   //load the built in palettes, a list of the palettes is kept in a dedicated file inside of the project folder
        ArrayList<String> paletteInfo = new ArrayList<>();
        InputStream inputStream = null;
        BufferedReader bufferedReader = null;

        try {
            inputStream = getClass().getResourceAsStream("/palette_txt/_palette_list.txt");

            assert inputStream != null;
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            String line = bufferedReader.readLine();

            while( line != null ) {
                paletteInfo.add(line);
                line = bufferedReader.readLine();
            }

        } catch (Exception ignored) {
        } finally {
            try {
                Objects.requireNonNull(bufferedReader).close();
                inputStream.close();
            } catch (IOException e) { e.printStackTrace();}
        }

        return paletteInfo.toArray(new String[paletteInfo.size()]);
    }

    public int[][] validatePalette(String fileLocation, Boolean externalFile) {	//read and validate all the color values from a palette file

        ArrayList<int[]> colorList = new ArrayList<>();		//values will be stored here while being read from the file
        int[][] colorArray;                     			//values will be stored here for further use
        InputStream inputStream = null;
        BufferedReader bufferedReader = null;

        try {
            if (externalFile)				//if the file being loaded is from an external source
                inputStream = new FileInputStream(fileLocation);
            else	                        //if the file being loaded is packaged with the program (inside the project)
                inputStream = getClass().getResourceAsStream(fileLocation);

            assert inputStream != null;
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            String line = bufferedReader.readLine();

            while( line != null ) {

                if( line.contains("meta") ) {   //if it's a metadata line, just skip it
                    line = bufferedReader.readLine();
                    continue;
                }

                colorStringToArray(colorList, line);
                line = bufferedReader.readLine();
            }

            colorArray = colorList.toArray(new int[colorList.size()][3]);

        } catch (Exception err) {
            return new int[][]{{0, 0, 0}};	//something went wrong, return the fall-back value
        } finally {
            try {
                Objects.requireNonNull(bufferedReader).close();
                inputStream.close();
            } catch (IOException e) { e.printStackTrace(); }
        }

        if (colorArray.length == 0)         //if the text file was empty or had no valid colors, return fall-back value
            return new int[][]{{0, 0, 0}};
        else
            return colorArray;
    }

    public static void colorStringToArray(ArrayList<int[]> colorList, String line) {
        String[] colorString;
        int[] colorInt;
        if(line.matches("\\d?\\d?\\d,\\d?\\d?\\d,\\d?\\d?\\d")) {	//lines must obey the following format => <color>,<color>,<color>

            colorString = line.split(",");						//separate the 3 color values

            colorInt = new int[3];

            int red = Integer.parseInt(colorString[0]);
            int green = Integer.parseInt(colorString[1]);
            int blue = Integer.parseInt(colorString[2]);

            //the regex already guarantees that there won't be any negative numbers, but we also need to make sure that no value is above the maximum of 255
            if(red < 256 && green < 256 && blue < 256) {
                colorInt[0] = red;			//red value
                colorInt[1] = green;		//green value
                colorInt[2] = blue;			//blue value

                colorList.add(colorInt);
            }
        }

        //if the line was formatted improperly, ignore it and move on
    }

    //============= working with other files =========================================================================================================
    //================================================================================================================================================

    public String loadHelpText() {

        StringBuilder completeText = new StringBuilder();	//we'll be holding all the text in a single string

        InputStream inputStream = getClass().getResourceAsStream("/txt/help.txt");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        try {

            String line = bufferedReader.readLine();

            while( line != null ) {
                completeText.append("\n").append(line);
                line = bufferedReader.readLine();
            }

        } catch (Exception err) {
            return "";	//something went wrong, return a fall-back value
        } finally {
            try {
                bufferedReader.close();
                inputStream.close();
            } catch (IOException e) { e.printStackTrace(); }
        }

        return completeText.toString();
    }

}
