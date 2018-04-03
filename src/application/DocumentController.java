package application;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
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

		System.out.println(Runtime.getRuntime().availableProcessors()); //--------------------------------------------------------------------------------------------------------------

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

        sppBasePane.setDividerPositions(1);

		prgProgress.setProgress(0.0);
		txtColorCount.setDisable(true);
		lblProgress.setText("");

		txtColorCount.setText("1");
		btnSaveImage.setDisable(true);

		txtHelpAbout.setText(getHelpText());
		txtHelpAbout.setDisable(true);
		txtHelpAbout.setVisible(false);

//		cbSortPalette.setDisable(true);

		txtIntensityRed.setText("0.2989");
		txtIntensityGreen.setText("0.5870");
		txtIntensityBlue.setText("0.1140");
        lblIntensity.setText(String.format("%.4f", (0.2989 + 0.5870 + 0.1140)));

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
		//these will sync the two scroll panes
		scpLeftPane.vvalueProperty().addListener(
				(observable, oldValue, newValue) -> scpRightPane.setVvalue((double) newValue));
		scpLeftPane.hvalueProperty().addListener(                       //the left pane's scroll bars are mirrored horizontally, so we need to reverse the horizontal value when applying it
				(observable, oldValue, newValue) -> scpRightPane.setHvalue(1 - (double) newValue));
		scpRightPane.vvalueProperty().addListener(
				(observable, oldValue, newValue) -> scpLeftPane.setVvalue((double) newValue));
		scpRightPane.hvalueProperty().addListener(
				(observable, oldValue, newValue) -> scpLeftPane.setHvalue(1 - (double) newValue));

		//pass references to controls in this class to the bridge class
		//initializing the class here because only 'null' is passed prior to the javaFX document controller initializer being reached;
		bridgeClass = Bridge.getInstance();
		bridgeClass.initialize(lblProgress, progressInfo, txaColorList);

		logicController = new LogicController();	//pass the instance of the bridge class down through the class hierarchy

		imgBase.setImage(logicController.getNullImage());
		imgProcessed.setImage(logicController.getNullImage());
		imgPalettePreview.setImage(new Image(this.getClass().getResourceAsStream("/palette_images/blank.png")));

		progressWorker.setDaemon(true);									//thread will end with program close (if only daemon threads remain, JVM will close them all and terminate)
		progressWorker.start();											//start the thread that handles the updating of the progress bar

		paletteOptions.addAll(logicController.loadPalettes());          //load all of the built in palettes (converting them from text files
		cmbPaletteSelect.setValue(paletteOptions.get(0));
		logicController.updateSelectedPalette(0);

	}

	//======== event handlers for UI controls ========================================================================================================
	//================================================================================================================================================

	@FXML private void handlerButtons(ActionEvent e) {              //handler for standard button controls
		Object source = e.getSource();

		if (source == btnOpenImage) {

			imgBase.setImage(logicController.getNewImage());		//get an selected image from a FileChooser then display it in the left pane
			btnRun.setDisable(false);								//since we should now have an image to work with, enable the run button
			btnSaveImage.setDisable(true);							//we likely have loaded a new image, disable save until it's been processed
			lblProgress.setText("");								//also clear the progress text
			imgProcessed.setImage(logicController.getNullImage());	//clear the right image

			refreshBugWorkaround();

		} else if (source == btnRun) {

			bridgeClass.updateProgressInfo("");

			//disable all the buttons so the user doesn't mess with them during the calculations
			//theoretically only the run button should need to be disabled, but just disable everything to be safe
			btnRun.setDisable(true);
	        btnSaveImage.setDisable(true);
	        btnOpenImage.setDisable(true);
	        cmbPaletteSelect.setDisable(true);
	        cmbDitherSelect.setDisable(true);

			refreshTask();
			Thread imageProcessorThread = new Thread(imageProcessorTask);
			imageProcessorThread.setDaemon(true);				    //a daemon thread closes when the main program does
			imageProcessorThread.start();

		} else if (source == btnSaveImage) {

			logicController.saveImage();

		} else if (source == btnOpenPalette) {

		    cmbPaletteSelect.setValue("- User defined palette -");
            logicController.loadUserPalette();

        } else if (source == btnSavePalette) {

		    logicController.saveUserColorList(txaColorList.getText());

        } else if (source == btnViewPalette) {

            logicController.showPaletteViewer(txaColorList.getText());

        } else if (source == tgbExtraPalettesToggle) {

            paletteOptions.clear();

		    if(tgbExtraPalettesToggle.isSelected()) {
		        tgbExtraPalettesToggle.setText("On");
                paletteOptions.addAll(logicController.toggleExtraPalettes(true));
            } else {
                tgbExtraPalettesToggle.setText("Off");
                paletteOptions.addAll(logicController.toggleExtraPalettes(false));
            }

            cmbPaletteSelect.setValue(paletteOptions.get(0));
            logicController.updateSelectedPalette(0);

		}
	}

    @FXML private void handlerImgButton(MouseEvent e) {
        Object source = e.getSource();

        if (source == imgInfo) {
            txtHelpAbout.setDisable(!txtHelpAbout.isDisable());
            txtHelpAbout.setVisible(!txtHelpAbout.isVisible());
        } else if (source == imgResetIntensity) {
            txtIntensityRed.setText("0.2989");
            txtIntensityGreen.setText("0.5870");
            txtIntensityBlue.setText("0.1140");
        }
    }

	@FXML private void handlerComboSelect(ActionEvent e) {          //when a ComboBox selection is changed
		Object source = e.getSource();
		String paletteName;
		int paletteNumber;

		if(source == cmbPaletteSelect && cmbPaletteSelect.getValue() != null) {

			paletteName = cmbPaletteSelect.getValue();
			paletteNumber = paletteOptions.indexOf(paletteName);

			if(paletteName.equals("Adaptive Palette")) {		    //if the user selects the optimized palette option, enable txtColorCount so they can define their own palette size
				txtColorCount.setDisable(false);
			} else {
				txtColorCount.setDisable(true);
			}

            logicController.updateSelectedPalette(paletteNumber);

			if(!paletteName.equals("- User defined palette -")) {
                txtColorCount.setText("" + logicController.getPaletteSize());
                logicController.updateColorListDisplay();
            }

			imgPalettePreview.setImage(logicController.getPaletteImage(paletteNumber));

		} else if(source == cmbDitherSelect) {
			logicController.updateSelectedDither(cmbDitherSelect.getValue());
		}
    }

    @FXML private void handlerSearchOverrideOptions(ActionEvent e) {
        Object source = e.getSource();

//        switch(source.toString()) {
//            case rbtMatchDefault:
//
//        }
        if (source == rbtMatchDefault){
            logicController.matchingStyleOverride(0);

//            cbSortPalette.setDisable(true);
//            logicController.sortPalette(false);
        } else if (source == rbtMatchMap) {
            logicController.matchingStyleOverride(2);
//            cbSortPalette.setDisable(false);
//            logicController.sortPalette(cbSortPalette.isSelected());
        } else if (source == rbtMatchSearch) {
            logicController.matchingStyleOverride(1);
//            cbSortPalette.setDisable(true);
//            logicController.sortPalette(false);
        }
//        else if (source == cbSortPalette) {
//            logicController.sortPalette(cbSortPalette.isSelected());
//        }


    }

	//================================================================================================================================================
	//================================================================================================================================================

    private void intensityValidateUpdate() {
        double iR;
        double iG;
        double iB;

        try{
            iR = Double.parseDouble(txtIntensityRed.getText());
            iG = Double.parseDouble(txtIntensityGreen.getText());
            iB = Double.parseDouble(txtIntensityBlue.getText());
            lblIntensity.setText(String.format("%.4f", (iR + iG + iB)));
            if ((iR + iG + iB) <= 1.0) {
                lblIntensity.setStyle("-fx-text-fill: #A2A09E");
            } else {
                iR = 0.2989;
                iG = 0.5870;
                iB = 0.1140;
                lblIntensity.setStyle("-fx-text-fill: red");
            }
        } catch(NumberFormatException e) {
            iR = 0.2989;
            iG = 0.5870;
            iB = 0.1140;
            lblIntensity.setText("ERROR");
            lblIntensity.setStyle("-fx-text-fill: red");
        }

        logicController.setColorIntensityValues(iR, iG, iB);

    }

	private int validateColorCount() {                              //makes sure the value in the txtColorCount textField is valid
		int colorCount;

		try {
			colorCount = Integer.parseInt(txtColorCount.getText());	//check if the textField is actually a number

			if(colorCount < 1) {
				txtColorCount.setText("1");	                        //make sure the number falls within an acceptable range
				return 1;
			} else if(colorCount > 9999) {
				txtColorCount.setText("9999");
				return 9999;
			} else {
				return colorCount;
			}
        } catch (NumberFormatException ex) {
        	txtColorCount.setText("1");                             //if its not a number, or otherwise invalid, default to 1
            return 1;
        }
	}

	private void refreshBugWorkaround() {
		//this is a work around for some unknown javaFX bug causing the right scrollPane to not properly update/refresh it's content
        //all the user would have to do is click inside the scrollPane to show it's content properly; this seems to emulate the fix (usually)
        scpLeftPane.requestFocus();
        scpRightPane.requestFocus();
	}



	//this will 'reset' the task after it has been run, re-using the task without this causes the thread to hang
	//this is not an ideal way of handling the threading if more threads for other tasks are planned in the future, but is simple for now
	private void refreshTask() {
		imageProcessorTask = new Task<>() {
			@Override
			public Void call() {

				if (cmbPaletteSelect.getValue().equals("Adaptive Palette")) {        //if the user wants an optimized palette we'll need to send the up-to-date color count
					logicController.generateAdaptivePalette(validateColorCount());
					logicController.updateColorListDisplay();
				} else if (cmbPaletteSelect.getValue().equals("- User defined palette -")) {
					logicController.validateUserColorList(txaColorList.getText());
					txtColorCount.setText("" + logicController.getPaletteSize());
				}

				imgProcessed.setImage(logicController.processImage());

				return null;
			}

			@Override
			protected void succeeded() {                                            //things to do when the task has completed successfully
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

	private Thread progressWorker = new Thread(new Runnable() {	//a thread that will be running in the background to update the progress bar on demand
    	private int max_value = 1;								//max value to use for the progress bar, JavaFX progress bars range from 0 to 1 so this will be used to find the proper fraction of the bar that needs to be filled
    	private double current_value = 0;   					//current progress value

    	@Override
    	public void run() {

    	    while(true) { 										//this thread will be continually running
    	    	try {
        	    	int num = progressInfo.take();				//will attempt to take the next value from the queue, if nothing is present it will 'block', essentially making the thread sleep

        	    	Platform.runLater(() -> {	                //if you want to change certain attributes of UI controls in javaFX from another thread, you must use a runLater runnable
        	    		if (num > 1) {							//if the number is greater than 1 then use that number as the new max value
            	    		max_value = num;
            	    	} else if (num == 0 || num == -1) {		//if the number is 0, set it to empty; if it is -1 set it to indeterminate
            	    		prgProgress.setProgress(num);
            	    		current_value = 0;
            	    	} else if (num == 1) {					//if the number is 1, add one step to current progress
            	    		current_value++;
            	    		prgProgress.setProgress(current_value / max_value);
            	    	}
        			});
    	    	} catch(InterruptedException e)  { bridgeClass.handleError(classID, "00", e); }
    	    }
    	}
	});

	//======== UI controls ===========================================================================================================================
    @FXML private SplitPane sppBasePane;

	@FXML private Button btnOpenImage;	//button to open file chooser to select the image you want to work with; menu options detailed below

    @FXML private Button btnOpenPalette;
    @FXML private Button btnSavePalette;
	@FXML private Button btnViewPalette;
    @FXML private ToggleButton tgbExtraPalettesToggle;

	@FXML private ComboBox<String> cmbPaletteSelect;	//ComboBox to select a palette
	@FXML private ImageView imgPalettePreview;			//small image showing a preview of the palette
	@FXML private TextField txtColorCount;				//TextField showing the number of colors in the current palette and allows the user to choose the number of colors in an optimized palette
	@FXML private ComboBox<String> cmbDitherSelect;		//ComboBox to select a dither type
	@FXML private Button btnRun;						//button to run the current parameters on the current image
	@FXML private Label lblProgress;					//label to the right in the progress area, used to give info on what the program is currently working on, and to show error details if needed
	@FXML private Button btnSaveImage;					//button to open a file saver to save the processed image

	@FXML private ImageView imgBase;		//pre-processed image, shown on the left panel
	@FXML private ImageView imgProcessed;	//post-processed image, shown on the right panel

	@FXML private ImageView imgBtnViewPalette;
    @FXML private ImageView imgBtnOpenPalette;
    @FXML private ImageView imgBtnSavePalette;
    @FXML private ImageView imgBtnOpen;
    @FXML private ImageView imgBtnRun;
    @FXML private ImageView imgBtnSave;
    @FXML private ImageView imgInfo;
    @FXML private ImageView imgResetIntensity;

    @FXML private RadioButton rbtMatchDefault;
    @FXML private RadioButton rbtMatchSearch;
    @FXML private RadioButton rbtMatchMap;
//    @FXML private CheckBox cbSortPalette;

    @FXML private Label lblIntensity;
    @FXML private TextField txtIntensityRed;
    @FXML private TextField txtIntensityGreen;
    @FXML private TextField txtIntensityBlue;

	@FXML private ProgressBar prgProgress;

	@FXML private ScrollPane scpLeftPane;
	@FXML private ScrollPane scpRightPane;

	@FXML private TextArea txaColorList;

	@FXML private TextArea txtHelpAbout;	//a text area positioned in front of the left scroll pane that can be toggled between hidden and visible
	//================================================================================================================================================

	//all the main logic will be going on in the LogicController class; doing things this way to avoid messing with javaFX's standard structure
	//(the class actually called "Main" isn't a traditional main class with javafx and the document controller class (this one) is being dedicated to UI management)
	private LogicController logicController = null;

	//this thread is where are the heavy lifting will be taking place to avoid locking up the UI thread
	//future versions may implement a service to manage multiple tasks for various other things (like opening images and reading user defined palettes/dithers)
	//this change is currently a low priority issue as those tasks will only cause the UI thread to hang in exceptionally extreme circumstances
	private Task<Void> imageProcessorTask;

	//basically a stack to be processed, items are added and a thread can pull the items out
	//blocking queues are specifically made for cross thread communication
	//if a thread is trying to take a value from it, and nothing is there to take, it will 'block' the thread, a.k.a. make it sleep
    //look at more info on  'offer' vs 'put'	'take' vs 'poll'	'LinkedBlockingQueue' vs 'ArrayBlockingQueue' etc
    private BlockingQueue<Integer> progressInfo = new LinkedBlockingQueue<>();

	private Bridge bridgeClass = null;

    private final ObservableList<String> paletteOptions = FXCollections.observableArrayList();   //the options for the palette choicebox

    private final ObservableList<String> ditherOptions = FXCollections.observableArrayList(  //the options for the dither choicebox
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

    private String getHelpText() {		//get the text from the help file
		try {

			StringBuilder completeText = new StringBuilder();	//we'll be holding all the text in a single string

			InputStream inputStream = getClass().getResourceAsStream("/txt/help.txt");
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

			try {

				String line = bufferedReader.readLine();

				while(line != null) {

					completeText.append("\n").append(line);

					line = bufferedReader.readLine();
				}

			} catch (Exception err) {
				return "";	//something went wrong, return the fall-back value
			} finally {
				try {
					bufferedReader.close();
					inputStream.close();
				} catch (Exception err) {
					//-------------------------
				}
			}

			return completeText.toString();

        } catch(Exception e) { bridgeClass.handleError(classID, "02", e); } return "";
	}


    private final String classID = "01";	//used as a reference when displaying errors
}