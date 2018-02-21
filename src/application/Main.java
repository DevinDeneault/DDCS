package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;

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
//        stage.setWidth(1280);
        stage.setMinHeight(400);
//        stage.setHeight(800);
        stage.setMaximized(true);
        stage.show();
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts.
     */
    public static void main(String[] args) {
        launch(args);
    }

}
