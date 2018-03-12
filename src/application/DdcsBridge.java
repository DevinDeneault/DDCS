package application;

import java.util.Arrays;
import java.util.concurrent.BlockingQueue;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class DdcsBridge {

	//---------------------------------------------------singleton
    private static volatile DdcsBridge instance = null;

    private DdcsBridge() { }

    public static DdcsBridge getInstance() {
        if (instance == null) {
            synchronized (DdcsBridge.class) {
                if (instance == null) {
                    instance = new DdcsBridge();
                }
            }
        }
        return instance;
    }
    //---------------------------------------------------



    Stage stage;
    private int[][] colorArray = new int[][]{{0,0,0}};

    public void setStage(Stage derp) {
        stage = derp;
    }

    public void showStage() {
        stage.showAndWait();
    }

    public int[][] getColors() {
        return colorArray;
    }

    public void setColors(int[][] colors) {
        colorArray = colors;
    }







    //this class should be given references to a few things so it can create a line of communication between classes
    private Label lblOutput;							    //javaFX label to output information to
    private BlockingQueue<Integer> progressQueue;			//a blocking queue for working with the progress bar
    private TextArea colorList;

    public void initialize(Label label, BlockingQueue<Integer> blockingQueue, TextArea textArea) {	//NOTE: 'null' values will be accepted, but this class won't do much of anything if that is the case
        lblOutput = label;
        progressQueue = blockingQueue;
        colorList = textArea;
    }



    public void updateColorList (int[][] palette) {
        try {

            StringBuilder sb = new StringBuilder();

            for (int[] color: palette) {
                sb.append(color[0]).append(",").append(color[1]).append(",").append(color[2]).append("\n");
            }

            String colorsString = String.valueOf(sb);

            Platform.runLater(() -> {		//if you want to change certain attributes of UI controls in javaFX from another thread, you must use a runLater runnable
                colorList.clear();
                colorList.appendText(colorsString);
            });
        } catch(Exception e) {
            handleError(classID, "01", e);
        }
    }



	public void updateProgress(int value) {
		progressQueue.offer(value);
	}

	public void updateProgressInfo(String value) {

		try {
			Platform.runLater(() -> {		//if you want to change certain attributes of UI controls in javaFX from another thread, you must use a runLater runnable

				if(lblOutput != null && !lblOutput.getText().contains("ERR:")) {	//if we have a label to output to and that label doesn't already contain an error message (which takes priority)
					lblOutput.setText(value);
				}
			});
		} catch(Exception e) {
			handleError(classID, "00", e);
		}
	}

	public void handleError(String classID, String methodID, Exception type) {
		try {
			Platform.runLater(() -> {

				String exceptionType = type.toString();								//get the type of exception, cutting off the extra fluff at the beginning to save space
				exceptionType = exceptionType.substring(exceptionType.lastIndexOf(".") + 1, exceptionType.length());

				if(lblOutput == null && !lblOutput.getText().contains("ERR:")) {	//if no label was given to output to or there is already an error, just output to the console
					System.out.println(classID + "." + methodID + "." + exceptionType);
				} else {
					lblOutput.setOpacity(1.0);
					lblOutput.setStyle("-fx-text-fill: red");
					lblOutput.setText("ERR:" + classID + "." + methodID + "." + exceptionType);
				}
			});
		} catch(Exception e) {							//let's hope this never happens...
			System.out.println("error in error handler\n" + Arrays.toString(e.getStackTrace()));
		}
	}

	private final String classID = "00";	//used as a reference when displaying errors
}
