package application;

import java.util.concurrent.BlockingQueue;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

//provides a basic line of communication between classes
//  primarily used to allow classes deeper in the class hierarchy to send information up to the GUI

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
    private BlockingQueue<Integer> progressQueue;    //a blocking queue for working with the progress bar
    private TextArea colorList;
    private Canvas canvas;

    public void initialize(BlockingQueue<Integer> blockingQueue, TextArea textArea, Canvas _canvas) {    //NOTE: 'null' values will be accepted, but this class won't do much of anything if that is the case
        progressQueue = blockingQueue;
        colorList = textArea;
        canvas = _canvas;
    }






    public void updateColorDisplay(int[][] palette, String displayInfoString) { //update both the text area listing out the colors and the small preview window above it to show the current palette

        StringBuilder sB = new StringBuilder();

        for( int[] color: palette )
            sB.append(color[0]).append(",").append(color[1]).append(",").append(color[2]).append("\n");

        String colorsString = String.valueOf(sB);

        //if you want to change certain attributes of UI controls in javaFX from another thread, you must use a runLater runnable
        Platform.runLater(() -> {   //update the textarea
            colorList.clear();
            colorList.appendText(colorsString);
        });

        boolean tmpDrawImage = false;
        String[] displayInfoStrings;
        int[] tmpDisplayInfo = null;

        if( !displayInfoString.contains("!") )
            tmpDrawImage = true;
        else{
            displayInfoStrings = displayInfoString.split(",");                  //first value is just an exclamation mark being used as a flag, discarded
            tmpDisplayInfo = new int[]{ Integer.parseInt(displayInfoStrings[1]),    //width of the color squares that will be drawn
                                        Integer.parseInt(displayInfoStrings[2]),    //height of the color squares that will be drawn
                                        Integer.parseInt(displayInfoStrings[3]),    //number of columns to be drawn
                                        Integer.parseInt(displayInfoStrings[4]),    //number of rows to be drawn
                                        Integer.parseInt(displayInfoStrings[5])};   //'step' size; i.e. a value of 3 means only every third color from the palette will be drawn
        }

        boolean drawImage = tmpDrawImage;  //in order to move a variable into a lambda it must be effectively final; somewhat odd looking, but no way around this
        int[] displayInfo = tmpDisplayInfo;
        Platform.runLater(() -> {
            GraphicsContext gc = canvas.getGraphicsContext2D();

            if( drawImage ) //some options don't have a consistent set of colors (adaptive, user defined) and some have an awkward number of colors (NES, 3-level rgb), so just use a pre-built image
                gc.drawImage(new Image(this.getClass().getResourceAsStream("/palette_images/" + displayInfoString + ".png")), 0, 0);
            else {

                int width = displayInfo[0];
                int height = displayInfo[1];
                int columns = displayInfo[2];
                int rows = displayInfo[3];
                int step = displayInfo[4];

                gc.setFill(Color.BLACK);
                gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

                int index = 0;
                for( int row = 0; row < rows; row++) {
                    for( int column = 0; column < columns; column++) {
                        if( index == palette.length )
                            break;
                        gc.setFill(Color.rgb(palette[index][0], palette[index][1], palette[index][2]));
                        gc.fillRect((column * width), (row * height), width, height);

                        index += step;
                    }
                }
            }
        });
    }





    public void updateProgress(int value) {
    progressQueue.offer(value);
    }

    //better exception handling needs to be implemented, retiring this for now
//    public void handleError(String errorInfo, Exception type) {
//    try {
//    Platform.runLater(() -> {
//
//                errorImage.setImage(new Image(this.getClass().getResourceAsStream("/images/error.png")));
//                errorOutput.setText(type + "\n" + errorInfo + "\n\n" + errorOutput.getText());
//                System.out.println(type + "\n" + errorInfo + "\n");
//
//    });
//    } catch( Exception e ) {  //let's hope this never happens...
//    System.out.println("error in error handler\n" + Arrays.toString(e.getStackTrace()));
//    }
//    }
}
