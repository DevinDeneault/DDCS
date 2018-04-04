package application;

import java.util.Arrays;
import java.util.concurrent.BlockingQueue;

import javafx.application.Platform;

import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class Bridge {

	//---------------------------------------------------singleton
    private static volatile Bridge instance = null;

    private Bridge() { }

    public static Bridge getInstance() {
        if( instance == null ) {
            synchronized( Bridge.class ) {
                if( instance == null )
                    instance = new Bridge();
            }
        }
        return instance;
    }
    //---------------------------------------------------



    private Stage stage;
    private int[][] colorArray = new int[][]{{0,0,0}};

    public void setStage(Stage popupStage) {
        stage = popupStage;
    }

    public void showStage() {
        stage.showAndWait();
    }   //show the pop-up window

    public int[][] getColors() {
        return colorArray;
    }

    public void setColors(int[][] colors) {
        colorArray = colors;
    }




    //this class should be given references to a few things so it can create a line of communication between classes
    private BlockingQueue<Integer> progressQueue;			//a blocking queue for working with the progress bar
    private TextArea colorList;
//    private TextArea errorOutput;
//    private ImageView errorImage;

    public void initialize(BlockingQueue<Integer> blockingQueue, TextArea textArea2) {	//NOTE: 'null' values will be accepted, but this class won't do much of anything if that is the case
        progressQueue = blockingQueue;
        colorList = textArea2;
//        errorOutput = textArea1;
//        errorImage = imageView;
    }



    public void updateColorList (int[][] palette) {

        StringBuilder sb = new StringBuilder();

        for( int[] color: palette )
            sb.append(color[0]).append(",").append(color[1]).append(",").append(color[2]).append("\n");

        String colorsString = String.valueOf(sb);

        Platform.runLater(() -> {		//if you want to change certain attributes of UI controls in javaFX from another thread, you must use a runLater runnable
            colorList.clear();
            colorList.appendText(colorsString);
        });
    }

	public void updateProgress(int value) {
		progressQueue.offer(value);
	}

	//better exception handling needs to be implemented, retiring this for now
//	public void handleError(String errorInfo, Exception type) {
//		try {
//			Platform.runLater(() -> {
//
//                errorImage.setImage(new Image(this.getClass().getResourceAsStream("/images/error.png")));
//                errorOutput.setText(type + "\n" + errorInfo + "\n\n" + errorOutput.getText());
//                System.out.println(type + "\n" + errorInfo + "\n");
//
//			});
//		} catch( Exception e ) {  //let's hope this never happens...
//			System.out.println("error in error handler\n" + Arrays.toString(e.getStackTrace()));
//		}
//	}
}
