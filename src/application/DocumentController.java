package application;

import java.net.URL;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

public class DocumentController implements Initializable {

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {

        //setting up the initial GUI state

        imgBtnOpenPalette.setImage(new Image(this.getClass().getResourceAsStream("/images/open-palette.png")));
        imgBtnSavePalette.setImage(new Image(this.getClass().getResourceAsStream("/images/save-palette.png")));
        imgBtnOpen.setImage(new Image(this.getClass().getResourceAsStream("/images/open.png")));
        imgBtnRun.setImage(new Image(this.getClass().getResourceAsStream("/images/run.png")));
        imgBtnSave.setImage(new Image(this.getClass().getResourceAsStream("/images/save.png")));
        imgInfo.setImage(new Image(this.getClass().getResourceAsStream("/images/about.png")));
        imgResetIntensity.setImage(new Image(this.getClass().getResourceAsStream("/images/reset.png")));
        imgBtnViewPalette.setImage(new Image(this.getClass().getResourceAsStream("/images/view.png")));

        tgbExtraPalettesToggle.setText("Off");

        lblColorCount.setText("-");

        sppBasePane.setDividerPositions(1);

        prgProgress.setProgress(0.0);
        txtColorCount.setDisable(true);

        txtColorCount.setText("1");
        btnSaveImage.setDisable(true);

        txtHelpAbout.setDisable(true);
        txtHelpAbout.setVisible(false);

        txtIntensityRed.setText("0.299");
        txtIntensityGreen.setText("0.587");
        txtIntensityBlue.setText("0.114");
        lblIntensity.setText(String.format("%.3f", (0.299 + 0.587 + 0.114)));

        txtIntensityRed.textProperty().addListener((observable, oldValue, newValue) -> intensityValidateUpdate() );
        txtIntensityGreen.textProperty().addListener((observable, oldValue, newValue) -> intensityValidateUpdate() );
        txtIntensityBlue.textProperty().addListener((observable, oldValue, newValue) -> intensityValidateUpdate() );

        txaColorList.clear();
        txaColorList.setText("0,0,0");
        cmbPaletteSelect.setItems(paletteOptions);
        cmbPaletteSelect.getSelectionModel().select(0);
        cmbDitherSelect.setItems(ditherOptions);
        cmbDitherSelect.getSelectionModel().select(0);

        btnRun.setDisable(true);
        btnSaveImage.setDisable(true);

        //lambda expressions to add listeners to the scroll panes' scroll bar values
        //  these will sync the two scroll panes
        scpLeftPane.vvalueProperty().addListener(
                (observable, oldValue, newValue) -> scpRightPane.setVvalue((double) newValue));
        scpLeftPane.hvalueProperty().addListener(                       //the left pane's scroll bars are mirrored horizontally, so we need to reverse the horizontal value when applying it
                (observable, oldValue, newValue) -> scpRightPane.setHvalue(1 - (double) newValue));
        scpRightPane.vvalueProperty().addListener(
                (observable, oldValue, newValue) -> scpLeftPane.setVvalue((double) newValue));
        scpRightPane.hvalueProperty().addListener(
                (observable, oldValue, newValue) -> scpLeftPane.setHvalue(1 - (double) newValue));

        //pass references to controls in this class to the bridge class
        //  initializing the class here because only 'null' is passed prior to the javaFX document controller initializer being reached;
        bridgeClass = Bridge.getInstance();
        bridgeClass.initialize(progressInfo, txaColorList);

        logicController = new LogicController();    //pass the instance of the bridge class down through the class hierarchy

        imgBase.setImage(logicController.getNullImage());
        imgProcessed.setImage(logicController.getNullImage());
        imgPalettePreview.setImage(new Image(this.getClass().getResourceAsStream("/palette_images/blank.png")));

        progressWorker.setDaemon(true);        //thread will end with program close (if only daemon threads remain, JVM will close them all and terminate)
        progressWorker.start();

        paletteOptions.addAll(logicController.loadPalettes());  //load all of the built in palettes (converting them from text files)
        cmbPaletteSelect.setValue(paletteOptions.get(0));
        logicController.updateSelectedPalette(0);

        logicController.updateSelectedDither(cmbDitherSelect.getValue());

        txtHelpAbout.setText(logicController.getHelpText());
    }

    //======== event handlers for UI controls ========================================================================================================
    //================================================================================================================================================

    @FXML private void handlerButtons(ActionEvent e) {
        Object source = e.getSource();

        if( source == btnOpenImage ) {

            IdedImage image = logicController.getNewImage();

            imgBase.setImage(image);

            refreshImageLoaderTask(image);
            Thread imageLoaderThread = new Thread(imageLoaderTask);
            imageLoaderThread.setDaemon(true);
            imageLoaderThread.start();

            imgProcessed.setImage(logicController.getNullImage());

            refreshBugWorkaround();

        } else if( source == btnRun ) {

            //disable all the buttons so the user doesn't mess with them during the calculations
            //  theoretically only the run button should need to be disabled, but just disable everything to be safe
            btnRun.setDisable(true);
            btnSaveImage.setDisable(true);
            btnOpenImage.setDisable(true);
            cmbPaletteSelect.setDisable(true);
            cmbDitherSelect.setDisable(true);

            refreshImageProcessorTask();
            Thread imageProcessorThread = new Thread(imageProcessorTask);
            imageProcessorThread.setDaemon(true);
            imageProcessorThread.start();

        } else if( source == btnSaveImage )
            logicController.saveImage();

        else if( source == btnOpenPalette ) {

            cmbPaletteSelect.setValue("- User defined palette -");  //we are opening a palette from a text file, so set the selected palette to the appropriate value to use it
            logicController.loadUserPalette();

        } else if( source == btnSavePalette )
            logicController.saveUserColorList(txaColorList.getText());

        else if( source == btnViewPalette )
            logicController.showPaletteViewer(txaColorList.getText());

        else if( source == tgbExtraPalettesToggle ) {  //a number of palettes are considered 'extra' or more gimmicky, so we have a toggle to show and hide them - to keep the list readable

            paletteOptions.clear();

            if( tgbExtraPalettesToggle.isSelected() ) {
                tgbExtraPalettesToggle.setText("On");
                paletteOptions.addAll(logicController.toggleExtraPalettes(true));
            } else {
                tgbExtraPalettesToggle.setText("Off");
                paletteOptions.addAll(logicController.toggleExtraPalettes(false));
            }

            //changing the list messes with the index values, so just select the top value
            cmbPaletteSelect.setValue(paletteOptions.get(0));
            logicController.updateSelectedPalette(0);

        }
    }

    @FXML private void handlerImgButtons(MouseEvent e) {     //handler for pseudo-buttons - small images that act as buttons
        Object source = e.getSource();

        if( source == imgInfo ) {
            txtHelpAbout.setDisable(!txtHelpAbout.isDisable());
            txtHelpAbout.setVisible(!txtHelpAbout.isVisible());
        } else if( source == imgResetIntensity ) {
            txtIntensityRed.setText("0.299");
            txtIntensityGreen.setText("0.587");
            txtIntensityBlue.setText("0.114");
        }
    }

    @FXML private void handlerComboBoxes(ActionEvent e) {
        Object source = e.getSource();
        String paletteName;
        int paletteNumber;

        if( source == cmbPaletteSelect && cmbPaletteSelect.getValue() != null ) {

            paletteName = cmbPaletteSelect.getValue();
            paletteNumber = paletteOptions.indexOf(paletteName);

            //the color count text box normal just displays the number of colors in the palette, but for the adaptive palette
            //  it becomes editable to allow the user the define how many colors will be calculated
            if( paletteName.equals("Adaptive Palette") )
                txtColorCount.setDisable(false);
            else
                txtColorCount.setDisable(true);

            logicController.updateSelectedPalette(paletteNumber);

            if( !paletteName.equals("- User defined palette -") ) {
                txtColorCount.setText("" + logicController.getPaletteSize());
                logicController.updateColorListDisplay();
            }

            imgPalettePreview.setImage(logicController.getPaletteImage(paletteNumber));

        } else if( source == cmbDitherSelect )
            logicController.updateSelectedDither(cmbDitherSelect.getValue());
    }

    @FXML private void handlerRadioButtons(ActionEvent e) {
        Object source = e.getSource();

        if( source == rbtMatchDefault )
            logicController.matchingStyleOverride(0);
        else if( source == rbtMatchMap )
            logicController.matchingStyleOverride(2);
        else if( source == rbtMatchSearch )
            logicController.matchingStyleOverride(1);
    }

    //================================================================================================================================================
    //================================================================================================================================================

    //validate the values inside the intensity value text boxes
    //  if they are valid, send the values forward
    //  if not, send default values and let the user know current values are not acceptable
    private void intensityValidateUpdate() {
        double iR;
        double iG;
        double iB;

        try{
            iR = Double.parseDouble(txtIntensityRed.getText());
            iG = Double.parseDouble(txtIntensityGreen.getText());
            iB = Double.parseDouble(txtIntensityBlue.getText());
            lblIntensity.setText(String.format("%.3f", (iR + iG + iB)));
            if( (iR + iG + iB) <= 1.0 )
                lblIntensity.setStyle("-fx-text-fill: #A2A09E");
            else {
                iR = 0.299;
                iG = 0.587;
                iB = 0.114;
                lblIntensity.setStyle("-fx-text-fill: red");
            }
        } catch( NumberFormatException e ) {
            iR = 0.299;
            iG = 0.587;
            iB = 0.114;
            lblIntensity.setText("ERROR");
            lblIntensity.setStyle("-fx-text-fill: red");
        }

        logicController.setColorIntensityValues(iR, iG, iB);
    }

    private int validateColorCount() {  //makes sure the value in the txtColorCount textField is valid
        int colorCount;

        try {
            colorCount = Integer.parseInt(txtColorCount.getText());

            if( colorCount < 1 ) {
                txtColorCount.setText("1");
                return 1;
            } else if( colorCount > 9999 ) {        //the text box is currently only large enough to handle 4 digits
                txtColorCount.setText("9999");
                return 9999;
            } else
                return colorCount;
        } catch( NumberFormatException ex ) {
            txtColorCount.setText("1");
            return 1;
        }
    }

    //this is a work around for some unknown javaFX bug causing the right scrollPane to not properly update/refresh it's content
    //  all the user would have to do is click inside the scrollPane to show it's content properly; this seems to emulate the fix (usually)
    private void refreshBugWorkaround() {
        scpLeftPane.requestFocus();
        scpRightPane.requestFocus();
    }

    //these will 'reset' the tasks after they have been run, re-using a task without doing this causes the thread to hang
    //  this is not an ideal way of handling the threading if more threads for other tasks are planned in the future, but is convenient for now
    private void refreshImageProcessorTask() {
        imageProcessorTask = new Task<>() {
            @Override
            public Void call() {

                if( cmbPaletteSelect.getValue().equals("Adaptive Palette") ) {
                    logicController.generateAdaptivePalette(validateColorCount());
                    logicController.updateColorListDisplay();
                } else if( cmbPaletteSelect.getValue().equals("- User defined palette -") ) {
                    logicController.updateUserPalette(txaColorList.getText());
                    txtColorCount.setText("" + logicController.getPaletteSize());
                }

                imgProcessed.setImage(logicController.processImage());

                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();

                //enable all the disabled buttons now that it's safe to use them again
                btnRun.setDisable(false);
                btnSaveImage.setDisable(false);
                btnOpenImage.setDisable(false);
                cmbPaletteSelect.setDisable(false);
                cmbDitherSelect.setDisable(false);

                refreshBugWorkaround();

                progressInfo.offer(0);
            }
        };
    }

    private void refreshImageLoaderTask(IdedImage image) {
        imageLoaderTask = new Task<>() {
            @Override
            public Void call() {

                Platform.runLater(() -> {
                    lblColorCount.setText("-");
                    btnRun.setDisable(true);
                    btnSaveImage.setDisable(true);
                });

                image.initColors();

                return null;
            }
            @Override
            protected void succeeded() {
                super.succeeded();

                Platform.runLater(() -> {
                    lblColorCount.setText(NumberFormat.getNumberInstance(Locale.US).format(image.colorCount()) + " colors");
                    btnRun.setDisable(false);
                    btnSaveImage.setDisable(true);
                });
            }
        };
    }

    private Thread progressWorker = new Thread(new Runnable() {   //a thread that will be running in the background to update the progress bar on demand
        private int max_value = 1;                                //max value to use for the progress bar, JavaFX progress bars range from 0 to 1 so this will be used to find the proper fraction of the bar that needs to be filled
        private double current_value = 0;                         //current progress value

        @Override
        public void run() {

            while(true) {                                         //this thread will be continually running
                try {
                    int num = progressInfo.take();                //will attempt to take the next value from the queue, if nothing is present it will block

                    Platform.runLater(() -> {                     //if you want to change certain attributes of UI controls in javaFX from another thread, you must use a runLater runnable
                        if( num > 1 )                             //if the number is greater than 1 then use that number as the new max value
                            max_value = num;
                        else if( num == 0 || num == -1 ) {        //if the number is 0, set it to empty; if it is -1 set it to indeterminate
                            prgProgress.setProgress(num);
                            current_value = 0;
                        } else if( num == 1 ) {                   //if the number is 1, add one step to current progress
                            current_value++;
                            prgProgress.setProgress(current_value / max_value);
                        }
                    });

                } catch( InterruptedException e ) {
                    prgProgress.setProgress(-1);
                    break;
                }
            }
        }
    });

    //======== UI controls ===========================================================================================================================
    @FXML private SplitPane sppBasePane;                //the split pane that holds the left control section and the right image panes

    @FXML private Button btnRun;                        //button to run the current parameters on the current image
    @FXML private Button btnSaveImage;                  //button to open a file saver to save the processed image
    @FXML private Button btnOpenImage;                  //button to open file chooser to select the image you want to work with; menu options detailed below
    @FXML private ImageView imgInfo;                    //image for the pseudo-button for showing the help/about info

    @FXML private Label lblColorCount;                  //label showing the number of colors in the current image

    @FXML private ToggleButton tgbExtraPalettesToggle;  //toggle button to show or hide the extra palettes in the palette list

    @FXML private ComboBox<String> cmbPaletteSelect;    //ComboBox to select a palette
    @FXML private ImageView imgPalettePreview;          //small image showing a preview of the palette
    @FXML private TextField txtColorCount;              //TextField showing the number of colors in the current palette and allows the user to choose the number of colors in an optimized palette
    @FXML private Button btnOpenPalette;                //button to open a text file containing palette data
    @FXML private Button btnSavePalette;                //button to save the text in the txaColoList to a text file
    @FXML private Button btnViewPalette;                //button to open a pop-pup window to give a graphical representation of *almost* any palette

    @FXML private ComboBox<String> cmbDitherSelect;     //ComboBox to select a dither type

    @FXML private ImageView imgBase;                    //base image, shown on the left panel
    @FXML private ImageView imgProcessed;               //processed image, shown on the right panel

    @FXML private ImageView imgBtnViewPalette;          //image on btnViewPalette
    @FXML private ImageView imgBtnOpenPalette;          //image on btnOpenPalette
    @FXML private ImageView imgBtnSavePalette;          //image on btnSavePalette
    @FXML private ImageView imgBtnOpen;                 //image on btnOpenImage
    @FXML private ImageView imgBtnRun;                  //image on btnRun
    @FXML private ImageView imgBtnSave;                 //image on btnSaveImage

    @FXML private ImageView imgResetIntensity;          //image for the pseudo-button for resetting the intensity values in the advanced options

    @FXML private RadioButton rbtMatchDefault;          //radio button for selecting the use the of the default color matching methods
    @FXML private RadioButton rbtMatchSearch;           //radio button for selecting to always use matching algorithms
    @FXML private RadioButton rbtMatchMap;              //radio button for selecting to always map the color values

    @FXML private Label lblIntensity;                   //label showing the sum of the intensity values
    @FXML private TextField txtIntensityRed;            //text box for the red intensity value
    @FXML private TextField txtIntensityGreen;          //text box for the green intensity value
    @FXML private TextField txtIntensityBlue;           //text box for the blue intensity value

    @FXML private ProgressBar prgProgress;              //progress bar showing where the program is in terms of it's calculations

    @FXML private ScrollPane scpLeftPane;               //the left scroll pane where the base image is shown
    @FXML private ScrollPane scpRightPane;              //the right scroll pane where the processed image is shown

    @FXML private TextArea txaColorList;                //the text area under the cmbPaletteSelect that will list all the colors in the palette

    @FXML private TextArea txtHelpAbout;                //a text area positioned in front of the left scroll pane that can be toggled between hidden and visible
    //================================================================================================================================================

    //all the main logic will be going on in the LogicController class; doing things this way to avoid messing with javaFX's standard structure
    //  (the class actually called "Main" isn't a traditional main class with javafx and the document controller class (this one) is being dedicated to UI management)
    private LogicController logicController = null;

    //this thread is where are the heavy lifting will be taking place to avoid locking up the UI thread
    //  future versions may implement a service to manage multiple tasks for various other things (like opening images and reading user defined palettes/dithers)
    //  this change is currently a low priority issue as those tasks will only cause the UI thread to hang in exceptionally extreme circumstances
    private Task<Void> imageProcessorTask;

    //short live thread to load the image, due our image being an extension of a normal image with some added calculations there could be some hanging
    private Task<Void> imageLoaderTask;

    //basically a stack to be processed, items are added and a thread can pull the items out
    //  blocking queues are specifically made for cross thread communication
    //  if a thread is trying to take a value from it, and nothing is there to take, it will 'block' the thread, a.k.a. make it sleep
    //  look at more info on  'offer' vs 'put'    'take' vs 'poll'    'LinkedBlockingQueue' vs 'ArrayBlockingQueue' etc
    private BlockingQueue<Integer> progressInfo = new LinkedBlockingQueue<>();

    private Bridge bridgeClass = null;

    private final ObservableList<String> paletteOptions = FXCollections.observableArrayList();  //the options for the palette choicebox

    private final ObservableList<String> ditherOptions = FXCollections.observableArrayList(     //the options for the dither choicebox
            "- None -",
            "Floyd-Steinberg",
            "Jarvis, Judice, & Ninke",
            "Stucki",
            "Atkinson",
            "Burkes",
            "Sierra",
            "Two-Row Sierra",
            "Sierra Lite",
            "Ordered [2x2]",
            "Ordered [3x3]",
            "Ordered [4x4]",
            "Ordered [4x4] [Negative]",
            "Ordered [8x8]",
            "Ordered [8x8] [Darkened]"
    );

}