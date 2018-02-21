package application;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Objects;

import javafx.scene.image.Image;

public class DdcsFileManager {

	private DdcsBridge bridgeClass = DdcsBridge.getInstance();
	private DdcsImage image = DdcsImage.getInstance();

	private DdcsFileOpener imageChooser = new DdcsFileOpener("Open Image File", "images");
	private DdcsFileOpener paletteChooser = new DdcsFileOpener("Open Palette Text File", "text");
	private DdcsPngSaver imageSaver = new DdcsPngSaver("Save Image File");
    private DdcsPaletteSaver paletteSaver = new DdcsPaletteSaver("Save Palette Text File");


	//============= working with images ==============================================================================================================
	//================================================================================================================================================

	public void loadBaseImage() {	//get an image file from a FileChooser and return it
		try {

			String imageLocation = imageChooser.getFileLocation();

			if(imageLocation.equals("error")) {	//if the error message was received display a pre-packaged image showing something something is wrong
				image.setImage(image.nullImage());
			} else {
				image.setImage(new Image("file:" + imageLocation));
			}

        } catch(Exception e) { bridgeClass.handleError(classID, "03", e); }
	}



	public void saveImage() {
		imageSaver.saveImage();
	}

	public void savePalette(String colorString) { paletteSaver.saveText(colorString); }



	//============= working with palette files ================================================================================================
	//================================================================================================================================================

	public int[][] loadUserPalette() {		//open the FileChooser for the user to select their custom palette and then validate it

		String paletteLocation = paletteChooser.getFileLocation();

		if(paletteLocation.equals("error")) {	//if the error message was received return the fall-back value
			return new int[][]{{0, 0, 0}};
		} else {
			return validatePalette(paletteLocation, "external");
		}
	}

	public int[][] validatePalette(String fileLocation, String inputType) {	//this will read and validate a text file containing palette data
		try {

			ArrayList<int[]> colorList = new ArrayList<>();		//values will be stored here while being read from the file
			int[][] colorArray;                     			//values will be stored here for further use                    // = new int[][]{{0,0,0}};
//			String[] colorString;								//stores the color as 3 strings
//			int[] colorInt;										//stores the color as 3 integers

			InputStream inputStream = null;
			BufferedReader bufferedReader = null;

			try {
				if (inputType.equals("external")) {				//if the file being loaded is from an external source
					inputStream = new FileInputStream(fileLocation);
				} else if (inputType.equals("internal")) {		//if the file being loaded is packaged with the program (inside the project)
					inputStream = getClass().getResourceAsStream(fileLocation);
				}

				assert inputStream != null;
				bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

				String line = bufferedReader.readLine();

				while(line != null) {

                    colorStringToArray(colorList, line);

                    line = bufferedReader.readLine();
				}

//			    colorArray = new int[colorList.size()][3];
			    colorArray = colorList.toArray(new int[colorList.size()][3]);

			} catch (Exception err) {
				return new int[][]{{0, 0, 0}};	//something went wrong, return the fall-back value
			} finally {
				try {
					Objects.requireNonNull(bufferedReader).close();
					inputStream.close();
				} catch (Exception err) {
					//-------------------------
				}
			}

			if (colorArray.length == 0) {       //if the text file was empty or had no valid colors, return fall-back value
                return new int[][]{{0, 0, 0}};
            } else {
                return colorArray;
            }

        } catch(Exception e) { bridgeClass.handleError(classID, "02", e); } return null;
	}



    public static void colorStringToArray(ArrayList<int[]> colorList, String line) {    //array arguments are shallow copies, will change originals, even in other classes?
        String[] colorString;
        int[] colorInt;
        if(line.matches("\\d?\\d?\\d,\\d?\\d?\\d,\\d?\\d?\\d")) {	//lines must obey the following format => <color>,<color>,<color>

            colorString = line.split(",");							//separate the 3 color values

            colorInt = new int[3];

            colorInt[0] = Integer.parseInt(colorString[0]);			//red value
            colorInt[1] = Integer.parseInt(colorString[1]);			//green value
            colorInt[2] = Integer.parseInt(colorString[2]);			//blue value

            colorList.add(colorInt);

        } else {
            //line was formatted improperly, ignore it and move on
        }
    }


    private final String classID = "03";	//used as a reference when displaying errors
}
