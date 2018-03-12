package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;

import java.awt.*;

public class Main extends Application {

	@Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("ddcs_fxml.fxml"));

        Scene scene = new Scene(root);

        scene.getStylesheets().add(getClass().getResource("ddcs_styles.css").toExternalForm());

        stage.setScene(scene);
        stage.setTitle("DDCS v" + "0.4.0");
        stage.getIcons().add(new Image(this.getClass().getResourceAsStream("/images/icon.png")));
        stage.setMinWidth(1280);
        stage.setMinHeight(400);
        stage.setMaximized(true);
        stage.show();










        Canvas popupCanvas = new Canvas();
        Group popupGroup = new Group();
        popupGroup.getChildren().add(popupCanvas);

        Scene popupScene = new Scene(popupGroup);
        Stage popupStage = new Stage();

        popupStage.setScene(popupScene);
        popupStage.initModality(Modality.APPLICATION_MODAL);

        popupStage.setMinHeight(96);

        popupCanvas.setWidth(323);
        popupCanvas.setHeight(227);

        popupStage.setResizable(false);

        popupStage.getIcons().add(new Image(this.getClass().getResourceAsStream("/images/palette.png")));
        popupStage.setTitle("Palette Viewer");

        popupScene.getStylesheets().add(getClass().getResource("ddcs_popup_styles.css").toExternalForm());

        GraphicsContext gc = popupCanvas.getGraphicsContext2D();
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1.0);


        popupStage.setOnShowing(event -> {

            Point p = MouseInfo.getPointerInfo().getLocation();

            popupStage.setX(p.x + 12);
            popupStage.setY(p.y - 64);

            gc.setFill(Color.rgb(160, 160, 160));
            gc.fillRect(0, 0, popupCanvas.getWidth(), popupCanvas.getHeight());

            int[][] colors = bridgeClass.getColors();
            int numColors = colors.length;

            int squareWidth;
            int squareHeight;

            int spacingX;
            int spacingY;

            int neededRows;
            double numColumns;  //double to retain double type in division below

            if( numColors < 11767 ) {
                if( numColors > 1024 ) {
                    numColumns = 106;
                    squareWidth = 3;
                    squareHeight = 2;
                    spacingX = 3;
                    spacingY = 2;
                    neededRows = (int) Math.ceil(colors.length / numColumns);

                    for( int y = 0; y < neededRows; y++ ) {
                        for( int x = 0, count = y * (int) numColumns; x < numColumns && count < colors.length; x++, count++ ) {
                            gc.setFill(Color.rgb(colors[count][0],colors[count][1],colors[count][2]));
                            gc.fillRect(2 + (spacingX * x),2 + (spacingY * y), squareWidth, squareHeight);
                        }
                    }

                    int incompleteRowSize = colors.length % 106;
                    if( incompleteRowSize != 0 ) {
                        gc.strokeLine( (incompleteRowSize * 3 + 3.5), (neededRows * 2 + 1.5), (incompleteRowSize * 3 + 4.5), (neededRows * 2 + 1.5));
                    }

                } else {
                    if( numColors < 257 ) {
                        numColumns = 16;
                        squareWidth = 18;
                        squareHeight = 12;
                        spacingX = 20;
                        spacingY = 14;
                    } else if( numColors < 513 ) {
                        numColumns = 16;
                        squareWidth = 18;
                        squareHeight = 5;
                        spacingX = 20;
                        spacingY = 7;
                    } else {
                        numColumns = 32;
                        squareWidth = 8;
                        squareHeight = 5;
                        spacingX = 10;
                        spacingY = 7;
                    }

                    neededRows = (int) Math.ceil(colors.length / numColumns);

                    for( int y = 0; y < neededRows; y++ ) {
                        for( int x = 0, count = y * (int) numColumns; x < numColumns && count < colors.length; x++, count++ ) {
                            gc.setFill(Color.rgb(colors[count][0],colors[count][1],colors[count][2]));
                            gc.strokeRect(2.5 + (spacingX * x),2.5 + (spacingY* y), squareWidth, squareHeight);
                            gc.fillRect(3 + (spacingX * x),3 + (spacingY * y), squareWidth - 1, squareHeight - 1);
                        }
                    }
                }
            } else {
                gc.setFill(Color.BLACK);
                gc.fillText("Palette is too large to be displayed.", 16, 16);
            }
        });

        bridgeClass.setStage(popupStage);

    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts.
     */
    public static void main(String[] args) {
        launch(args);
    }

    private DdcsBridge bridgeClass = DdcsBridge.getInstance();

}
