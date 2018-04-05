package application;

import java.io.File;
import java.util.Objects;

import javafx.stage.FileChooser;

public class FileOpener {

	FileOpener(String title, String type) {
		chooser.setTitle(title);

		if(type.equals("images"))
			setExtensionImages();
		else if(type.equals("text"))
			setExtensionText();
	}

	private String selectedFile = null;		//the full directory to the previously selected file
	private FileChooser chooser = new FileChooser();
	private String fileMatcher = ".*err";	//a regex-ready string used to verify a file has the proper extension - initial value should never be seen


    public String getFileLocation() {		//this method will return a string representation of a directory selected by the user

		File previousDirectory = null;

		if(selectedFile != null) {			//make sure there was a previously selected file that can be used
			previousDirectory = new File(selectedFile.substring(0, selectedFile.lastIndexOf("\\") + 1));	//cut off the file name, leaving just the directory
		}

		chooser.setInitialDirectory(previousDirectory);				//a initial directory of "null" is acceptable and goes to your system's default directory - whatever that may be

		File file = chooser.showOpenDialog(null);		//this actually shows the chooser window and returns a file object when closed

		if (file == null || !file.getPath().matches(fileMatcher)) {	//if the user closes the FileChooser without selecting anything OR somehow selects an invalid file
			//the value of selectedFile hasn't been updated yet, so this will simply return the previously selected file
			//when all else fails send an error message instead
			return Objects.requireNonNullElse(selectedFile, "error");   //the value of selectedFile hasn't been updated yet, so this will simply return the previously selected file
		}

		selectedFile = file.toString();

		return selectedFile;
	}

	private void setExtensionImages() {	//valid image options
		chooser.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter("Images", "*.jpg", "*.JPG", "*.jpeg", "*.JPEG", "*.png", "*.PNG", "*.bmp", "*.BMP"),
				new FileChooser.ExtensionFilter("JPG", "*.jpg", "*.JPG", "*.jpeg", "*.JPEG"),
				new FileChooser.ExtensionFilter("PNG", "*.png", "*.PNG"),
				new FileChooser.ExtensionFilter("BMP", "*.bmp", "*.BMP")
			);
		fileMatcher = ".*(jpg|JPG|jpeg|JPEG|png|PNG|bmp|BMP)";
	}

	private void setExtensionText() {	//valid text file options
		chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
		fileMatcher = ".*txt";
	}
}
