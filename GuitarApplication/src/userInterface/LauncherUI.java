package userInterface;

import java.sql.*;
import javafx.concurrent.*;
import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;
import be.tarsos.dsp.pitch.PitchProcessor.PitchEstimationAlgorithm;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import client.DBConnect;
import client.Sound;

/**
 * This class is in charge of running all of the application level code. This includes sound processing,
 * javafx stage work, Thread task managements, and all other levels of function. Imports for this class
 * include java.sql classes, javafx concurrency classes, TarsosDSP library classes, javafx stage/scene controls,
 * and the DBConnect and Sound classes from the client package.
 * @author Jonathan Novins
 * @version 1.3.4
 */
public class LauncherUI extends Application{
	
/**
 * The general display Stage that will be utilized by the tuning tool
 */
	public Stage tunerStage;
	
/**
 * 	The Text that will display the letter note according to the frequency in Sound
 */
	public Text noteText;
	
/**
 * The Text that will display a symbol in regards to how far over the closest whole note the read freq is (1/3)
 */
	public Text noteOver;
	
/**
 * The Text that will display a symbol in regards to how far over the closest whole note the read freq is (2/3)
 */
	public Text noteOver2;
	
/**
 * The Text that will display a symbol in regards to how far over the closest whole note the read freq is (3/3)
 */
	public Text noteOver3;
	
/**
 * The Text that will display a symbol in regards to how far under the closest whole note the read freq is (1/3)
 */
	public Text noteUnder;
	
/**
 * The Text that will display a symbol in regards to how far under the closest whole note the read freq is (2/3)
 */
	public Text noteUnder2;
	
/**
 * The Text that will display a symbol in regards to how far under the closest whole note the read freq is (3/3)
 */
	public Text noteUnder3;
	
/**
 * The instantiated Sound Object that acts for all sound manipulation/detection actions
 */
	public Sound sound = new Sound();
	
/**
 * The instantiated DBConnect Object that acts for all database related actions
 */
	public DBConnect ghDBConnect = new DBConnect();
	
/**
 * The 'running' String that will continually hold the value of the currently logged in user (DEFAULT = Guest)
 */
	public static String loggedName = "Guest";
	
	//All code here is used to instantiate variables that are needed in a wider scope than the enclosed can provide
/**
 * A boolean int that serves to tell whether or not a successful microphone connection has taken place yet
 */
	public int micConnect = 0; // An int that represents a binary value relating to whether or not a microphone connect attempt has been successfull or not
	
/**
 * 	A boolean int that serves to tell whether or not the full sound detection loop should execute at a given time
 */
	public int soundRun = 0; // when 1, fully execute sound analysis loop; when 0, executes minimal part of the block 
	
/**
 * @deprecated
 * 	A boolean int that serves to tell whether or not the intonation waiting cycle UI should be updating or not
 */
	public int waitingCycleRun = 0; // when 1, proceed to rotate waiting symbols in intonation tool
	
	// An array that will represent the note String that run parallel with the noteFreqArray
/**
 * The array that is responsible for holding all required letter-notes in the correct order (parallels to noteFreqArray)
 */
	public final static String[] noteArray = new String[] {"D#","E","F","F#","G","G#","A","A#","B","C","C#","D",
									   "D#","E","F","F#","G","G#","A","A#","B","C","C#","D",
									   "D#","E","F","F#","G","G#","A","A#","B","C","C#","D",
									   "D#","E","F","F#","G","G#","A","A#","B","C","C#","D",};
	                           
	// An array that represents note frequencies picked up by the AudioDispatcherFactory; Runs parallel to the noteArray array above
/**
 * The array that is responsible for holding all required letter-associated-frequencies in the correft order (parallels 
 * to noteArray)
 */
	public final static Double[] noteFreqArray = new Double[] {77.78, 82.41, 87.31, 92.64, 97.88, 103.46, 110.5, 117.0, 123.25, 131.8, 138.45, 147.0, 
										   155.45, 165.5, 174.25, 187.0, 196.4, 208.0, 220.6, 234.8, 246.0, 263.0, 277.5, 294.3,
										   312.0, 328.0, 350.0, 368.0, 392.0, 417.0, 440.0, 467.0, 494.0, 524.0, 556.0, 589.0,
										   662.0, 659.0, 698.0, 738.6, 787.0, 834.0, 886.5, 938.0, 991.3, 1046.5, 1108.73, 1174.66};
	
	// A double that gives the class global access to the current value of noteFreq (the current frequency being picked up by the listening device)
	// Defaults to -1 because AudioDispacherFactory will return -1 anyway if mic action is not loud enough to be considered a proper note
	
	// An AudioDispatcher that will be used for the sound loop. Declared up here so all scopes have access to its current value and state
/**
 * Null instantiation of an AudioDispatcher dispatcher will later be passed to the Sound Class for population
 */
	public static AudioDispatcher dispatcher = null;
	
	// The thread that will continually run the dispatcher when requested. Declared outside of start(Stage) so all scopes have access to its current value and state
/**
 * Thread that will continually run the dispatcher when requested. Although soundLoop will always run once called, boolean
 * int soundRun can be used to prevent the full Thread from executing every time
 */
	public Thread soundLoop;
	
/**
 * 	Java Concurrency Task that will be embedded in the soundLoop Thread that will be in charge of making UI related updates
 * throughout the applications lifetime
 */
	public Task<Void> updateTuner;

/**
 * Standard javafx start method that will begin loading and executing the user interface level of the application
 */
	public void start(Stage primaryStage) throws Exception{
		
		//General UI layout dedicated to necessary windows to run the application.
		//Although some of the logic will be embedded within the UI for functionality purposes, most logic will be in a separaet package
		//This UI will be dependent on javafx and will use mostly custom designs for javafx utilities.
		//Each new window will be coded within an event in order to use the resources only when necessary
		
		// Set soundRun equal to 0 to prevent any note calculations/output from taking place
		soundRun = 0;
		
		// Loop that attempts to connect to the computer's default sound device only if it has not already done so
		
		dispatcher = sound.micConnect(dispatcher);
		
		PitchDetectionHandler handler = new PitchDetectionHandler() { // Custom Object from TarsosDSP that picks up when a pitch is available from the microphone
	        /**
	         * handlePitch is the core of the applications sound processing. Every frequency that is picked up and deemed
	         * acceptable will run through the intended portions of this method
	         */
			@Override
	        public void handlePitch(PitchDetectionResult pitchDetectionResult,
            		AudioEvent audioEvent) { // handlePitch method that will run as long as the dispatcher is set to run (called by the run(), start(), etc, methods from the Thread soundLoop
	            		
	            if(sound.getSoundRun() == 1) { // This loop will only run if executed in a space where soundRun is set to 0 such as the tuner, scale practice, or intonation checker
	            		
	            	String nearestNote = null; // String that will continually update based on the equivalent note of the most recently picked up frequency
	           		int nearestIndex = sound.getNearestIndex();
	                	
	            		
	           		if(pitchDetectionResult.getPitch() > 50 && (pitchDetectionResult.getProbability() > .9)) { // this block will only execute if there is a proper pitch picked up from the dispatcher
	               		System.out.println("CURRENT DETECTED MIC INPUT[]\t" + audioEvent.getTimeStamp() + "\t" + pitchDetectionResult.getPitch()); // prints out current time stamp and exact frequency being picked up
	               		System.out.println(pitchDetectionResult.getProbability());
	               		float noteFreq = pitchDetectionResult.getPitch(); // sets the pitch detection result to a variable for later use
	               		sound.setNoteFreq(noteFreq);
	               		float noteProb = pitchDetectionResult.getProbability();
	               		sound.setNoteProb(noteProb);
	               		double distance = Math.abs(noteFreqArray[0] - noteFreq); // finds the distance between the first array value and the frequency
	                	
	               		for(int i = 1; i < noteFreqArray.length; i++) { // block that finds the closest whole note to the noteFreq
	               			double testDist = Math.abs(noteFreqArray[i] - noteFreq);
	              			if(testDist < distance) {
	               				nearestIndex = i;
	               				distance = testDist;
	               			}
	               		}
	               		sound.setNearestIndex(nearestIndex);
                		nearestNote = noteArray[nearestIndex]; // sets the nearest note to a variable for future use
                		System.out.println("Nearest note is: " + nearestNote); // prints to console for development purposes
	                	
	               		if(tunerStage.isShowing()) {
	               			updateTuner = new Task<Void>() {
	            				/**
	            				 * Call is a standard Task related method that is embedded within the handlePitch method.
	            				 * It acts as the primary execution method for the updateTuner Task.
	            				 */
	               				@Override
	            				protected Void call() {
	            					
	            					double noteDiff = 0;
	            					double noteFreq = sound.getNoteFreq();
	            					double nearestFreq = noteFreqArray[sound.getNearestIndex()];
	            					double maxUpperBound = (noteFreqArray[sound.getNearestIndex()+1] - nearestFreq) /2;
	            					double maxLowerBound = (nearestFreq - noteFreqArray[sound.getNearestIndex()-1]) /2;
	            					
	            					if(noteFreq > nearestFreq) {
	            						noteDiff = noteFreq - nearestFreq;
	            					}
	            					else {
	            						noteDiff = nearestFreq - noteFreq;
	            					}
	            	
	            					noteOver.setText("");
	            					noteOver2.setText("");
	            					noteOver3.setText("");
	            					noteUnder.setText("");
	            					noteUnder2.setText("");
	            					noteUnder3.setText("");
	            					System.out.println("");
	            					System.out.println(noteDiff);
	            					System.out.println("");
	            					
	            					
	            					
	            					if(noteDiff > (maxUpperBound * .05) && noteDiff < (maxUpperBound * .33)) {
	            						noteOver.setText("+");
	            					}
	            					else if(noteDiff > (maxUpperBound * .33) && noteDiff < (maxUpperBound *.66)) {
	            						noteOver.setText("+");
	            						noteOver2.setText("+");
	            					}
	            					else if(noteDiff > (maxUpperBound * .66) && noteDiff < (maxUpperBound * .999)) {
	            						noteOver.setText("+");
	            						noteOver2.setText("+");
	            						noteOver3.setText("+");
	            					}
	            					else if(noteDiff > (maxLowerBound * .05) && noteDiff < (maxLowerBound * .33)){
	            						noteUnder.setText("-");
	            					}
	            					else if (noteDiff > (maxLowerBound * .33) && noteDiff < (maxLowerBound * .66)) {
	            						noteUnder.setText("-");
	            						noteUnder2.setText("-");
	            					}
	            					else if(noteDiff > (maxLowerBound * .66) && noteDiff < (maxLowerBound * .999)) {
	            						noteUnder.setText("-");
	            						noteUnder2.setText("-");
	            						noteUnder3.setText("-");
	            					}
	            					
	            					noteText.setText(noteArray[sound.getNearestIndex()]);					
	            	                return null ;
	            				}
	            	        }; 
	               			updateTuner.run();
	               		}
	               		
	               		
	               	}
	           	}
	           }  
	       }; 
	        dispatcher.addAudioProcessor(new PitchProcessor(PitchEstimationAlgorithm.YIN, 44100, 1024, handler)); // adds an audio processor to the mic connection along with the desired pitch detection algorithm
	        micConnect = 1; // toggle the value of micConnect to 1
	        
		soundLoop = new Thread(dispatcher); // declares a new thread that will run continuously in the background for dispatcher
		soundLoop.start(); // starts the aforementioned thread
		
		//This pane will serve as the general layout for the launcher window
		Pane generalDisplay = new Pane();
		generalDisplay.setPrefSize(626,626);
		
		//Sets up the background image for the launcher
		Image launcherBackground = new Image("file:resources/images/environment/launcherBackground.png");
		ImageView launcherBgView = new ImageView(launcherBackground);
		
		//Tuner button serves to open the functional tuner window
		Button tunerMenuButton = new Button("Tuner");
		tunerMenuButton.setLayoutX(260);
		tunerMenuButton.setLayoutY(220);
		tunerMenuButton.setStyle("-fx-background-color: \r\n" + 
				"        #090a0c,\r\n" + 
				"        linear-gradient(#38424b 0%, #1f2429 20%, #191d22 100%),\r\n" + 
				"        linear-gradient(#20262b, #191d22),\r\n" + 
				"        radial-gradient(center 50% 0%, radius 100%, rgba(163,163,163,0.9), rgba(255,255,255,0));\r\n" + 
				"    -fx-background-radius: 5,4,3,5;\r\n" + 
				"    -fx-background-insets: 0,1,2,0;\r\n" + 
				"    -fx-text-fill: white;\r\n" + 
				"    -fx-effect: dropshadow( three-pass-box , rgba(0,0,0,0.6) , 5, 0.0 , 0 , 1 );\r\n" + 
				"    -fx-font-family: \"Impact\";\r\n" + 
				"    -fx-text-fill: linear-gradient(white, #d0d0d0);\r\n" + 
				"    -fx-font-size: 32px;\r\n" + 
				"    -fx-padding: 10 20 10 20;");
		
		//Block that changes formatting of button when mouse is hovered over it
		tunerMenuButton.setOnMouseEntered( e -> {
			tunerMenuButton.setStyle("-fx-background-color: \r\n" + 
					"        #090a0c,\r\n" + 
					"        linear-gradient(#38424b 0%, #1f2429 20%, #191d22 100%),\r\n" + 
					"        linear-gradient(#20262b, #191d22),\r\n" + 
					"        radial-gradient(center 50% 0%, radius 100%, rgba(73,73,73,0.9), rgba(255,255,255,0));\r\n" + 
					"    -fx-background-radius: 5,4,3,5;\r\n" + 
					"    -fx-background-insets: 0,1,2,0;\r\n" + 
					"    -fx-text-fill: white;\r\n" + 
					"    -fx-effect: dropshadow( three-pass-box , rgba(0,0,0,0.6) , 5, 0.0 , 0 , 1 );\r\n" + 
					"    -fx-font-family: \"Impact\";\r\n" + 
					"    -fx-text-fill: linear-gradient(white, #d0d0d0);\r\n" + 
					"    -fx-font-size: 32px;\r\n" + 
					"    -fx-padding: 10 20 10 20;");
		});
		
		//Block that sets the format of a button back to normal when the mouse leaves its hover space
		tunerMenuButton.setOnMouseExited( e -> {
			tunerMenuButton.setStyle("-fx-background-color: \r\n" + 
					"        #090a0c,\r\n" + 
					"        linear-gradient(#38424b 0%, #1f2429 20%, #191d22 100%),\r\n" + 
					"        linear-gradient(#20262b, #191d22),\r\n" + 
					"        radial-gradient(center 50% 0%, radius 100%, rgba(163,163,163,0.9), rgba(255,255,255,0));\r\n" + 
					"    -fx-background-radius: 5,4,3,5;\r\n" + 
					"    -fx-background-insets: 0,1,2,0;\r\n" + 
					"    -fx-text-fill: white;\r\n" + 
					"    -fx-effect: dropshadow( three-pass-box , rgba(0,0,0,0.6) , 5, 0.0 , 0 , 1 );\r\n" + 
					"    -fx-font-family: \"Impact\";\r\n" + 
					"    -fx-text-fill: linear-gradient(white, #d0d0d0);\r\n" + 
					"    -fx-font-size: 32px;\r\n" + 
					"    -fx-padding: 10 20 10 20;");
		});
		
		//Scale practice button serves to open the functional scale practice window
		Button scalePracticeButton = new Button("Scale Practice");
		scalePracticeButton.setLayoutX(203);
		scalePracticeButton.setLayoutY(300);
		scalePracticeButton.setStyle("-fx-background-color: \r\n" + 
				"        #090a0c,\r\n" + 
				"        linear-gradient(#38424b 0%, #1f2429 20%, #191d22 100%),\r\n" + 
				"        linear-gradient(#20262b, #191d22),\r\n" + 
				"        radial-gradient(center 50% 0%, radius 100%, rgba(163,163,163,0.9), rgba(255,255,255,0));\r\n" + 
				"    -fx-background-radius: 5,4,3,5;\r\n" + 
				"    -fx-background-insets: 0,1,2,0;\r\n" + 
				"    -fx-text-fill: white;\r\n" + 
				"    -fx-effect: dropshadow( three-pass-box , rgba(0,0,0,0.6) , 5, 0.0 , 0 , 1 );\r\n" + 
				"    -fx-font-family: \"Impact\";\r\n" + 
				"    -fx-text-fill: linear-gradient(white, #d0d0d0);\r\n" + 
				"    -fx-font-size: 32px;\r\n" + 
				"    -fx-padding: 10 20 10 20;");
		
		//Block that changes formatting of button when mouse is hovered over it
		scalePracticeButton.setOnMouseEntered( e -> {
			scalePracticeButton.setStyle("-fx-background-color: \r\n" + 
					"        #090a0c,\r\n" + 
					"        linear-gradient(#38424b 0%, #1f2429 20%, #191d22 100%),\r\n" + 
					"        linear-gradient(#20262b, #191d22),\r\n" + 
					"        radial-gradient(center 50% 0%, radius 100%, rgba(73,73,73,0.9), rgba(255,255,255,0));\r\n" + 
					"    -fx-background-radius: 5,4,3,5;\r\n" + 
					"    -fx-background-insets: 0,1,2,0;\r\n" + 
					"    -fx-text-fill: white;\r\n" + 
					"    -fx-effect: dropshadow( three-pass-box , rgba(0,0,0,0.6) , 5, 0.0 , 0 , 1 );\r\n" + 
					"    -fx-font-family: \"Impact\";\r\n" + 
					"    -fx-text-fill: linear-gradient(white, #d0d0d0);\r\n" + 
					"    -fx-font-size: 32px;\r\n" + 
					"    -fx-padding: 10 20 10 20;");
		});
		
		//Block that sets the format of a button back to normal when the mouse leaves its hover space
		scalePracticeButton.setOnMouseExited( e -> {
			scalePracticeButton.setStyle("-fx-background-color: \r\n" + 
					"        #090a0c,\r\n" + 
					"        linear-gradient(#38424b 0%, #1f2429 20%, #191d22 100%),\r\n" + 
					"        linear-gradient(#20262b, #191d22),\r\n" + 
					"        radial-gradient(center 50% 0%, radius 100%, rgba(163,163,163,0.9), rgba(255,255,255,0));\r\n" + 
					"    -fx-background-radius: 5,4,3,5;\r\n" + 
					"    -fx-background-insets: 0,1,2,0;\r\n" + 
					"    -fx-text-fill: white;\r\n" + 
					"    -fx-effect: dropshadow( three-pass-box , rgba(0,0,0,0.6) , 5, 0.0 , 0 , 1 );\r\n" + 
					"    -fx-font-family: \"Impact\";\r\n" + 
					"    -fx-text-fill: linear-gradient(white, #d0d0d0);\r\n" + 
					"    -fx-font-size: 32px;\r\n" + 
					"    -fx-padding: 10 20 10 20;");
		});
		
		//Intonation button serves to open the functional intonation checker window
		Button intonationButton = new Button("Intonation Setup");
		intonationButton.setLayoutX(235);
		intonationButton.setLayoutY(580);
		
		//Hyperlink setup to allow user to login to a pre-existing account
		Hyperlink accountChangeScreen = new Hyperlink(loggedName);
		Text accountStatus = new Text("Logged in as: ");
		accountStatus.setFill(Color.WHITE);
		accountStatus.setLayoutX(235);
		accountStatus.setLayoutY(620);
		accountChangeScreen.setLayoutX(305);
		accountChangeScreen.setLayoutY(604);
		
		//Adds all existing nodes to the general display window
		generalDisplay.getChildren().addAll(launcherBgView, tunerMenuButton, scalePracticeButton, intonationButton, accountChangeScreen, accountStatus);
		
		//Sets up the scene and shows the initial launcher window
		Scene scene = new Scene(generalDisplay);
		primaryStage.setScene(scene);
		primaryStage.setTitle("Guitar Helper");
		primaryStage.show();
		
		//
		//
		//
		//
		tunerMenuButton.setOnAction(e -> {
			primaryStage.hide();
			sound.setSoundRun(1);
			
			Text tunerTitleText = new Text("Primary Tuner");
			tunerTitleText.setFill(Color.WHITE);
			tunerTitleText.setLayoutX(50);
			tunerTitleText.setLayoutY(50);
			tunerTitleText.setFont(Font.font("calibri", 50));
			
			Ellipse divider = new Ellipse(3, 700);
			divider.setFill(Color.WHITE);
			divider.setLayoutX(395);
			divider.setLayoutY(-100);
			
			Text tunerInfoText = new Text("Related Info");
			tunerInfoText.setFill(Color.WHITE);
			tunerInfoText.setLayoutX(415);
			tunerInfoText.setLayoutY(50);
			tunerInfoText.setFont(Font.font("Calibri", 35));
			
			Text exactFreq = new Text("Exact Frequency:");
					
			Text timeRet = new Text("Timestamp Retrieved");
			
			tunerStage = new Stage();
			Pane tunerDisplay = new Pane();
			tunerDisplay.setPrefSize(600, 600);
			
			Image tunerBkImg = new Image("file:resources/images/environment/tunerback.jpg");
			ImageView tunerImgView = new ImageView(tunerBkImg);
			
			noteText = new Text("E");
			noteText.setFill(Color.WHITE);
			noteText.setLayoutX(170);
			noteText.setLayoutY(315);
			noteText.setFont(Font.loadFont("file:resources/fonts/digital-7.ttf", 120));
			
			noteOver = new Text("+");
			noteOver.setFill(Color.WHITE);
			noteOver.setFont(Font.loadFont("file:resources/fonts/digital-7.ttf", 60));
			noteOver.setLayoutX(223);
			noteOver.setLayoutY(295);
			
			noteOver2 = new Text("+");
			noteOver2.setFill(Color.WHITE);
			noteOver2.setFont(Font.loadFont("file:resources/fonts/digital-7.ttf", 60));
			noteOver2.setLayoutX(246);
			noteOver2.setLayoutY(295);
			
			noteOver3 = new Text("+");
			noteOver3.setFont(Font.loadFont("file:resources/fonts/digital-7.ttf",60));
			noteOver3.setFill(Color.WHITE);
			noteOver3.setLayoutX(269);
			noteOver3.setLayoutY(295);
			
			noteUnder = new Text("-");
			noteUnder.setFill(Color.WHITE);
			noteUnder.setFont(Font.loadFont("file:resources/fonts/digital-7.ttf", 60));
			noteUnder.setLayoutX(145);
			noteUnder.setLayoutY(295);
			
			noteUnder2 = new Text("-");
			noteUnder2.setFill(Color.WHITE);
			noteUnder2.setFont(Font.loadFont("file:resources/fonts/digital-7.ttf", 60));
			noteUnder2.setLayoutX(122);
			noteUnder2.setLayoutY(295);
			
			noteUnder3 = new Text("-");
			noteUnder3.setFill(Color.WHITE);
			noteUnder3.setFont(Font.loadFont("file:resources/fonts/digital-7.ttf", 60));
			noteUnder3.setLayoutX(96);
			noteUnder3.setLayoutY(295);
			
			Image tunerImage = new Image("file:resources/images/environment/tunerPic.png");
			ImageView tunerImageView = new ImageView(tunerImage);
			tunerImageView.setLayoutX(-95);
			tunerImageView.setLayoutY(25);
			tunerDisplay.getChildren().addAll(tunerImgView, tunerImageView, noteText, noteOver, noteOver2, noteOver3, noteUnder, noteUnder2,
					noteUnder3, tunerTitleText, tunerInfoText,divider);
			
			Scene tunerScene = new Scene(tunerDisplay);
			tunerStage.setScene(tunerScene);
			tunerStage.setTitle("Guitar Helper - Tuner");
			tunerStage.show();
			
			tunerStage.setOnHidden(e2 -> {
					sound.setSoundRun(0);
					primaryStage.show();
			});
			
		});
		
		//
		//CRUDE DISPLAY JUST TO GET GENERAL LAYOUT DOWN
		//NOT SET UP TO FUNCTION YET
		//
		scalePracticeButton.setOnAction(e -> {
			soundRun = 1;
			primaryStage.hide();
			
			Stage scaleStage = new Stage();
			Pane scaleDisplay = new Pane();
			scaleDisplay.setPrefSize(400,200);
			
			Image scaleChoiceBkImg = new Image("file:resources/images/environment/tunerback.jpg");
			ImageView scaleChoiceBkImgView = new ImageView(scaleChoiceBkImg);
			
			Text scaleChoiceTxt = new Text("Select an option below.");
			scaleChoiceTxt.setFill(Color.WHITE);
			scaleChoiceTxt.setFont(Font.font(20));
			scaleChoiceTxt.setLayoutX(15);
			scaleChoiceTxt.setLayoutY(30);
			
			Button customScaleButton = new Button("Load Custom Scale");
			customScaleButton.setStyle("-fx-background-color: \r\n" + 
					"        #090a0c,\r\n" + 
					"        linear-gradient(#38424b 0%, #1f2429 20%, #191d22 100%),\r\n" + 
					"        linear-gradient(#20262b, #191d22),\r\n" + 
					"        radial-gradient(center 50% 0%, radius 100%, rgba(163,163,163,0.9), rgba(255,255,255,0));\r\n" + 
					"    -fx-background-radius: 5,4,3,5;\r\n" + 
					"    -fx-background-insets: 0,1,2,0;\r\n" + 
					"    -fx-text-fill: white;\r\n" + 
					"    -fx-effect: dropshadow( three-pass-box , rgba(0,0,0,0.6) , 5, 0.0 , 0 , 1 );\r\n" + 
					"    -fx-font-family: \"Impact\";\r\n" + 
					"    -fx-text-fill: linear-gradient(white, #d0d0d0);\r\n" + 
					"    -fx-font-size: 20px;\r\n" + 
					"    -fx-padding: 10 20 10 20;");
			customScaleButton.setOnMouseEntered( e2 -> {
				customScaleButton.setStyle("-fx-background-color: \r\n" + 
						"        #090a0c,\r\n" + 
						"        linear-gradient(#38424b 0%, #1f2429 20%, #191d22 100%),\r\n" + 
						"        linear-gradient(#20262b, #191d22),\r\n" + 
						"        radial-gradient(center 50% 0%, radius 100%, rgba(73,73,73,0.9), rgba(255,255,255,0));\r\n" + 
						"    -fx-background-radius: 5,4,3,5;\r\n" + 
						"    -fx-background-insets: 0,1,2,0;\r\n" + 
						"    -fx-text-fill: white;\r\n" + 
						"    -fx-effect: dropshadow( three-pass-box , rgba(0,0,0,0.6) , 5, 0.0 , 0 , 1 );\r\n" + 
						"    -fx-font-family: \"Impact\";\r\n" + 
						"    -fx-text-fill: linear-gradient(white, #d0d0d0);\r\n" + 
						"    -fx-font-size: 20px;\r\n" + 
						"    -fx-padding: 10 20 10 20;");
			});
			
			
			customScaleButton.setOnMouseExited( e2 -> {
				customScaleButton.setStyle("-fx-background-color: \r\n" + 
						"        #090a0c,\r\n" + 
						"        linear-gradient(#38424b 0%, #1f2429 20%, #191d22 100%),\r\n" + 
						"        linear-gradient(#20262b, #191d22),\r\n" + 
						"        radial-gradient(center 50% 0%, radius 100%, rgba(163,163,163,0.9), rgba(255,255,255,0));\r\n" + 
						"    -fx-background-radius: 5,4,3,5;\r\n" + 
						"    -fx-background-insets: 0,1,2,0;\r\n" + 
						"    -fx-text-fill: white;\r\n" + 
						"    -fx-effect: dropshadow( three-pass-box , rgba(0,0,0,0.6) , 5, 0.0 , 0 , 1 );\r\n" + 
						"    -fx-font-family: \"Impact\";\r\n" + 
						"    -fx-text-fill: linear-gradient(white, #d0d0d0);\r\n" + 
						"    -fx-font-size: 20px;\r\n" + 
						"    -fx-padding: 10 20 10 20;");
			});
			customScaleButton.setLayoutX(15);
			customScaleButton.setLayoutY(140);
			
			
			Button existingScaleButton = new Button("Use Scale Presets");
			existingScaleButton.setStyle("-fx-background-color: \r\n" + 
					"        #090a0c,\r\n" + 
					"        linear-gradient(#38424b 0%, #1f2429 20%, #191d22 100%),\r\n" + 
					"        linear-gradient(#20262b, #191d22),\r\n" + 
					"        radial-gradient(center 50% 0%, radius 100%, rgba(163,163,163,0.9), rgba(255,255,255,0));\r\n" + 
					"    -fx-background-radius: 5,4,3,5;\r\n" + 
					"    -fx-background-insets: 0,1,2,0;\r\n" + 
					"    -fx-text-fill: white;\r\n" + 
					"    -fx-effect: dropshadow( three-pass-box , rgba(0,0,0,0.6) , 5, 0.0 , 0 , 1 );\r\n" + 
					"    -fx-font-family: \"Impact\";\r\n" + 
					"    -fx-text-fill: linear-gradient(white, #d0d0d0);\r\n" + 
					"    -fx-font-size: 20px;\r\n" + 
					"    -fx-padding: 10 20 10 20;");
			existingScaleButton.setOnMouseEntered( e2 -> {
				existingScaleButton.setStyle("-fx-background-color: \r\n" + 
						"        #090a0c,\r\n" + 
						"        linear-gradient(#38424b 0%, #1f2429 20%, #191d22 100%),\r\n" + 
						"        linear-gradient(#20262b, #191d22),\r\n" + 
						"        radial-gradient(center 50% 0%, radius 100%, rgba(73,73,73,0.9), rgba(255,255,255,0));\r\n" + 
						"    -fx-background-radius: 5,4,3,5;\r\n" + 
						"    -fx-background-insets: 0,1,2,0;\r\n" + 
						"    -fx-text-fill: white;\r\n" + 
						"    -fx-effect: dropshadow( three-pass-box , rgba(0,0,0,0.6) , 5, 0.0 , 0 , 1 );\r\n" + 
						"    -fx-font-family: \"Impact\";\r\n" + 
						"    -fx-text-fill: linear-gradient(white, #d0d0d0);\r\n" + 
						"    -fx-font-size: 20px;\r\n" + 
						"    -fx-padding: 10 20 10 20;");
			});
		
			existingScaleButton.setOnMouseExited( e2 -> {
				existingScaleButton.setStyle("-fx-background-color: \r\n" + 
						"        #090a0c,\r\n" + 
						"        linear-gradient(#38424b 0%, #1f2429 20%, #191d22 100%),\r\n" + 
						"        linear-gradient(#20262b, #191d22),\r\n" + 
						"        radial-gradient(center 50% 0%, radius 100%, rgba(163,163,163,0.9), rgba(255,255,255,0));\r\n" + 
						"    -fx-background-radius: 5,4,3,5;\r\n" + 
						"    -fx-background-insets: 0,1,2,0;\r\n" + 
						"    -fx-text-fill: white;\r\n" + 
						"    -fx-effect: dropshadow( three-pass-box , rgba(0,0,0,0.6) , 5, 0.0 , 0 , 1 );\r\n" + 
						"    -fx-font-family: \"Impact\";\r\n" + 
						"    -fx-text-fill: linear-gradient(white, #d0d0d0);\r\n" + 
						"    -fx-font-size: 20px;\r\n" + 
						"    -fx-padding: 10 20 10 20;");
			});
			existingScaleButton.setLayoutX(15);
			existingScaleButton.setLayoutY(70);
			
			
			scaleDisplay.getChildren().addAll(scaleChoiceBkImgView, scaleChoiceTxt, customScaleButton, existingScaleButton
					);
			
			Scene scaleScene = new Scene(scaleDisplay);
			scaleStage.setScene(scaleScene);
			scaleStage.setTitle("Guitar Helper - Scale Practice");
			scaleStage.show();
			
			scaleStage.setOnHidden(e2 -> {
				soundRun = 0;
				primaryStage.show();
			});
		});
		
		intonationButton.setOnAction(e -> {
			primaryStage.hide();
			
			float noteFreq = -1;
			float noteProb = 0;
			float openNote = -1;
			
			int stringStatus1 = 0;
			int stringStatus2 = 0;
			int stringStatus3 = 0;
			int stringStatus4 = 0;
			int stringStatus5 = 0;
			int stringStatus6 = 0;
			
			waitingCycleRun = 1;
			soundRun = 1;
			
			Stage intStage = new Stage();
			Pane intDisplay = new Pane();
			intDisplay.setPrefSize(700, 410);
			
			Image intBackground = new Image("file:resources/images/environment/tunerback.jpg");
			ImageView intBkgView = new ImageView(intBackground);
			
			Text intTitleText = new Text("Intonation Setup");
			intTitleText.setFill(Color.WHITE);
			intTitleText.setFont(Font.font(40));
			intTitleText.setLayoutX(10);
			intTitleText.setLayoutY(40);
			
			
			Text string1 = new Text("First String (Low E Standard)");
			string1.setFill(Color.RED);
			string1.setFont(Font.font(20));
			string1.setLayoutX(50);
			string1.setLayoutY(95);
			
			Text string2 = new Text("Second String (A Standard)");
			string2.setFill(Color.RED);
			string2.setFont(Font.font(20));
			string2.setLayoutX(50);
			string2.setLayoutY(150);
			
			Text string3 = new Text("Third String (D Standard)");
			string3.setFill(Color.RED);
			string3.setFont(Font.font(20));
			string3.setLayoutX(50);
			string3.setLayoutY(205);
			
			Text string4 = new Text("Fourth String (G Standard)");
			string4.setFill(Color.RED);
			string4.setFont(Font.font(20));
			string4.setLayoutX(430);
			string4.setLayoutY(95);
			
			Text string5 = new Text("Fifth String (B Standard)");
			string5.setFill(Color.RED);
			string5.setFont(Font.font(20));
			string5.setLayoutX(430);
			string5.setLayoutY(150);
			
			Text string6 = new Text("Sixth String (High E Standard)");
			string6.setFill(Color.RED);
			string6.setFont(Font.font(20));
			string6.setLayoutX(430);
			string6.setLayoutY(205);
			
			Image redCheck = new Image("file:resources/images/status/notfinished.png");
			ImageView redCheckView1 = new ImageView(redCheck);
			redCheckView1.setLayoutX(5);
			redCheckView1.setLayoutY(68);
			ImageView redCheckView2 = new ImageView(redCheck);
			redCheckView2.setLayoutX(5);
			redCheckView2.setLayoutY(123);
			ImageView redCheckView3 = new ImageView(redCheck);
			redCheckView3.setLayoutX(5);
			redCheckView3.setLayoutY(178);
			ImageView redCheckView4 = new ImageView(redCheck);
			redCheckView4.setLayoutX(385);
			redCheckView4.setLayoutY(68);
			ImageView redCheckView5 = new ImageView(redCheck);	
			redCheckView5.setLayoutX(385);
			redCheckView5.setLayoutY(123);
			ImageView redCheckView6 = new ImageView(redCheck);
			redCheckView6.setLayoutX(385);
			redCheckView6.setLayoutY(178);
			
			Image waiting = new Image("file:resources/images/status/waiting.png");
			ImageView waitingView1 = new ImageView(waiting);
			waitingView1.setLayoutX(5);
			waitingView1.setLayoutY(68);
			ImageView waitingView2 = new ImageView(waiting);
			waitingView2.setLayoutX(5);
			waitingView2.setLayoutY(123);
			ImageView waitingView3 = new ImageView(waiting);
			waitingView3.setLayoutX(5);
			waitingView3.setLayoutY(178);
			ImageView waitingView4 = new ImageView(waiting);
			waitingView4.setLayoutX(385);
			waitingView4.setLayoutY(68);
			ImageView waitingView5 = new ImageView(waiting);
			waitingView5.setLayoutX(385);
			waitingView5.setLayoutY(123);
			ImageView waitingView6 = new ImageView(waiting);
			waitingView6.setLayoutX(385);
			waitingView6.setLayoutY(178);
			
			Image greenCheck = new Image("file:resources/images/status/finished.png");
			ImageView greenCheckView1 = new ImageView(greenCheck);
			greenCheckView1.setLayoutX(5);
			greenCheckView1.setLayoutY(68);
			ImageView greenCheckView2 = new ImageView(greenCheck);
			greenCheckView2.setLayoutX(5);
			greenCheckView2.setLayoutY(123);
			ImageView greenCheckView3 = new ImageView(greenCheck);
			greenCheckView3.setLayoutX(5);
			greenCheckView3.setLayoutY(178);
			ImageView greenCheckView4 = new ImageView(greenCheck);
			greenCheckView4.setLayoutX(385);
			greenCheckView4.setLayoutY(68);
			ImageView greenCheckView5 = new ImageView(greenCheck);
			greenCheckView5.setLayoutX(385);
			greenCheckView5.setLayoutY(123);
			ImageView greenCheckView6 = new ImageView(greenCheck);
			greenCheckView6.setLayoutX(385);
			greenCheckView6.setLayoutY(178);
			
			intDisplay.getChildren().addAll(intBkgView, intTitleText, string1, string2, string3, string4,
					string5, string6, redCheckView1, redCheckView2, redCheckView3, redCheckView4, redCheckView5,
					redCheckView6);
			
			Scene intScene = new Scene(intDisplay);
			intStage.setScene(intScene);
			intStage.setTitle("Guitar Helper - Intonation Checking Tool");
			intStage.show();
			
			//THREAD NOT FUNCTIONING PROPERLY; REIMPLEMENT AT A LATER DATE
			/* 
			Thread waitingCycle = new Thread() {
				@Override
				public void run() {
					if(waitingCycleRun == 1) {
						waitingView1.setRotate(waitingView1.getRotate()+5);
						waitingView2.setRotate(waitingView1.getRotate()+1);
						waitingView3.setRotate(waitingView1.getRotate()+1);
						waitingView4.setRotate(waitingView1.getRotate()+1);
						waitingView5.setRotate(waitingView1.getRotate()+1);
						waitingView6.setRotate(waitingView1.getRotate()+1);
						
					}
				}
			};
			waitingCycle.start();
			*/
			 
			Thread intUpdater = new Thread() {
				/**
				 * Standard run method in charge for execution of the intUpdater Thread. Involves handling of all pseudo-
				 * trigger based execution particularly within the intonation tool
				 */
				@Override
				public void run() {
					float noteFreq = sound.getNoteFreq();
					float noteProb = sound.getNoteProb();
					if(noteFreq != -1 && noteProb > .98) {
						sound.setOpenNote(noteFreq);
					}
					
					if(sound.getStringStatus1() == 0 && sound.getOpenNote() != -1) {
						noteFreq = -1;
						string1.setFill(Color.ORANGE);
						Platform.runLater(new Runnable() {
							public void run() {
								intDisplay.getChildren().removeAll(redCheckView1);
								intDisplay.getChildren().add(waitingView1);
							}
							
						});
						
						sound.setStringStatus1(1);
					}
					else if(stringStatus2 == 0) {
						
						
						
						sound.setStringStatus2(1);
					}
					else if(stringStatus3 == 0) {
						
						
						
						sound.setStringStatus3(1);
					}
					else if(stringStatus4 == 0) {
						
						
						
						sound.setStringStatus4(1);
					}
					else if(stringStatus5 == 0) {
	
						
						
						sound.setStringStatus5(1);
					}
					else if(stringStatus6 == 0) {
						
						
						
						sound.setStringStatus6(1);
					}
					
				}
			};
			
			intUpdater.start();
			
			intStage.setOnHidden(e2 -> {
				soundRun = 0;
				waitingCycleRun = 0;
				primaryStage.show();
			});
			
		});
		
		
		//This is an event from a hyperlink on the launcher window that will open a window for
		//a user who wants to login to a pre-existing account. This window is closed within an
		//event to prevent unneeded use of resources. This new window is meant to run over the 
		//launcher window meaning both windows will be open at once.
		accountChangeScreen.setOnAction( e -> {
			accountChangeScreen.setVisited(false);
			//Sets up the stage for the login screen
			Stage loginStage = new Stage();
			
			//The equivalent of generalDisplay pane but for the login screen. All components will be laid here
			Pane loginPane = new Pane();
			loginPane.setPrefSize(500, 300);
			
			//Sets up a background image for the login screen, this image will be the same as the createAccount screen
			Image loginBackground = new Image("file:resources/images/environment/loginBackground.jpg");
			ImageView loginBackgroundView = new ImageView(loginBackground);
			loginBackgroundView.setLayoutX(-400);
			loginBackgroundView.setLayoutY(-200);
			
			//Sets up a text to provide general user directions
			Text loginPrompt = new Text("Enter the credentials below to login.");
			loginPrompt.setFill(Color.WHITE);
			loginPrompt.setFont(Font.font(20));
			loginPrompt.setLayoutX(20);
			loginPrompt.setLayoutY(38);
			
			//Provides a label for the username text field
			Label usernameLabel = new Label("Username: ");
			usernameLabel.setTextFill(Color.WHITE);
			usernameLabel.setFont(Font.font(15));
			usernameLabel.setLayoutX(20);
			usernameLabel.setLayoutY(60);
			
			//Provides a place for the user to enter their existing username
			TextField usernameField = new TextField("usernameField");
			usernameField.setPromptText("Enter your username.");
			usernameField.setText("");
			usernameField.setLayoutX(99);
			usernameField.setLayoutY(53);
			
			//Provides a label for the password text field
			Label passwordLabel = new Label("Password: ");
			passwordLabel.setTextFill(Color.WHITE);
			passwordLabel.setFont(Font.font(15));
			passwordLabel.setLayoutX(20);
			passwordLabel.setLayoutY(89);
			
			//Provides a place for the user to enter the password to an existing account
			PasswordField passwordField = new PasswordField();
			passwordField.setPromptText("Enter your password.");
			passwordField.setText("");
			passwordField.setLayoutX(99);
			passwordField.setLayoutY(83);
			
			//Text setup to guide the user to the "Create Account" hyperlink (below)
			Text makeAccountPrompt = new Text("Don't have an account?");
			makeAccountPrompt.setFill(Color.WHITE);
			makeAccountPrompt.setFont(Font.font(10));
			makeAccountPrompt.setLayoutX(20);
			makeAccountPrompt.setLayoutY(125);
			
			//Hyperlink to allow the user to create an account if they do not have a pre-existing one
			Hyperlink makeAccountLink = new Hyperlink("Click here.");
			makeAccountLink.setLayoutX(128);
			makeAccountLink.setLayoutY(110);
			
			//Button that allows user to confirm that all credentials are entered and a login attempt should take place
			Button loginConfirmButton = new Button("Login");
			loginConfirmButton.setFont(Font.font(14));
			loginConfirmButton.setLayoutX(20);
			loginConfirmButton.setLayoutY(135);
			
			//Adds all created components to the login screen window
			loginPane.getChildren().addAll(loginBackgroundView, loginPrompt,
					makeAccountPrompt, usernameLabel, passwordLabel,
					usernameField, passwordField, makeAccountLink, loginConfirmButton);
			
			//Sets up a scene to display the newly created login window
			Scene loginScene = new Scene(loginPane);
			loginStage.setTitle("Guitar Helper - Login");
			loginStage.setScene(loginScene);
			loginStage.show();
			
			loginConfirmButton.setOnAction(e3 -> {
				try {
					if(ghDBConnect.login(usernameField.getText(), passwordField.getText())) {
						accountChangeScreen.setText(loggedName);
						loginStage.hide();
					}
					else {
						
					}
				} 
				catch (SQLException e1) {
					e1.printStackTrace();
				}
			});
			
			//This event will serve as a way for the user to open up the create account window.
			//It is nested within the login window which is nested within the launcher window.
			//The window will be created if and only if the user presses the required hyperlink.
			//This window will be an end-window seeing as no other windows can be opened from it.
			makeAccountLink.setOnAction( e2 -> {
				//Sets up the stage for the create account window
				Stage createAccountStage = new Stage();
				
				//Equivalent of generalDisplay from the launcher window. All components will be laid here.
				Pane createAccountPane = new Pane();
				createAccountPane.setPrefSize(500, 300);
				
				//Code to setup and include an image background within the window (same picture as login screen).
				Image createAccountBackground = new Image("file:resources/images/environment/loginBackground.jpg");
				ImageView createAccountBackgroundView = new ImageView(createAccountBackground);
				createAccountBackgroundView.setLayoutX(-400);
				createAccountBackgroundView.setLayoutY(-200);
				
				//Text prompt to provide general direction to the user
				Text createAccountPrompt = new Text("Enter the required information below.");
				createAccountPrompt.setFill(Color.WHITE);
				createAccountPrompt.setFont(Font.font(20));
				createAccountPrompt.setLayoutX(20);
				createAccountPrompt.setLayoutY(38);
				
				//Label for the desired username field
				Label createAccountUsernameLabel = new Label("Username: ");
				createAccountUsernameLabel.setTextFill(Color.WHITE);
				createAccountUsernameLabel.setFont(Font.font(15));
				createAccountUsernameLabel.setLayoutX(20);
				createAccountUsernameLabel.setLayoutY(60);
				
				//TextField for the user to enter the desired username while making an account
				TextField createAccountUsernameField = new TextField("usernameField");
				createAccountUsernameField.setPromptText("Enter desired username.");
				createAccountUsernameField.setText("");
				createAccountUsernameField.setLayoutX(99);
				createAccountUsernameField.setLayoutY(53);
				
				//Label for the account password field
				Label createAccountPasswordLabel = new Label("Password : ");
				createAccountPasswordLabel.setTextFill(Color.WHITE);
				createAccountPasswordLabel.setFont(Font.font(15));
				createAccountPasswordLabel.setLayoutX(20);
				createAccountPasswordLabel.setLayoutY(89);
				
				//TextField for the user to enter the password for their new account
				PasswordField createAccountPasswordField = new PasswordField();
				createAccountPasswordField.setPromptText("Enter desired password.");
				createAccountPasswordField.setText("");
				createAccountPasswordField.setLayoutX(99);
				createAccountPasswordField.setLayoutY(83);
				
				//Label for the password confirmation field below
				Label passwordConfirmLabel = new Label("Confirm password: ");
				passwordConfirmLabel.setTextFill(Color.WHITE);
				passwordConfirmLabel.setFont(Font.font(15));
				passwordConfirmLabel.setLayoutX(20);
				passwordConfirmLabel.setLayoutY(118);
				
				//TextField for the user to confirm their previously entered password
				PasswordField passwordConfirmField = new PasswordField();
				passwordConfirmField.setPromptText("Confirm password.");
				passwordConfirmField.setText("");
				passwordConfirmField.setLayoutX(150);
				passwordConfirmField.setLayoutY(112);
				passwordConfirmField.setPrefSize(98, 10);
				
				//Button that allows user to confirm that all credentials are entered and a create account attempt should take place
				Button createAccountConfirmButton = new Button("Create Account");
				createAccountConfirmButton.setFont(Font.font(14));
				createAccountConfirmButton.setLayoutX(20);
				createAccountConfirmButton.setLayoutY(145);
				
				Text usernameTakenText = new Text("Username is already taken.");
				usernameTakenText.setFill(Color.RED);
				usernameTakenText.setFont(Font.font(15));
				usernameTakenText.setLayoutX(43);
				usernameTakenText.setLayoutY(255);
				
				Image redCheck = new Image("file:resources/images/status/notfinished.png");
				ImageView usernameTakenPrompt = new ImageView(redCheck);
				usernameTakenPrompt.setLayoutX(245);
				usernameTakenPrompt.setLayoutY(48);
				usernameTakenPrompt.setScaleX(.5);
				usernameTakenPrompt.setScaleY(.5);
				
				ImageView usernameTakenPrompt2 = new ImageView(redCheck);
				usernameTakenPrompt2.setLayoutX(5);
				usernameTakenPrompt2.setLayoutY(230);
				usernameTakenPrompt2.setScaleX(.5);
				usernameTakenPrompt2.setScaleY(.5);
				
				Text passwordMismatchText = new Text("Passwords do not match.");
				passwordMismatchText.setFill(Color.RED);
				passwordMismatchText.setFont(Font.font(15));
				passwordMismatchText.setLayoutX(43);
				passwordMismatchText.setLayoutY(285);
				
				ImageView passwordTakenPrompt = new ImageView(redCheck);
				passwordTakenPrompt.setLayoutX(5);
				passwordTakenPrompt.setLayoutY(260);
				passwordTakenPrompt.setScaleX(.5);
				passwordTakenPrompt.setScaleY(.5);
				
				ImageView passwordTakenPrompt2 = new ImageView(redCheck);
				passwordTakenPrompt2.setLayoutX(245);
				passwordTakenPrompt2.setLayoutY(75);
				passwordTakenPrompt2.setScaleX(.5);
				passwordTakenPrompt2.setScaleY(.5);
				
				//Adds all created nodes to the create account window
				createAccountPane.getChildren().addAll(createAccountBackgroundView, createAccountPrompt,
						createAccountUsernameLabel, createAccountPasswordLabel,
						createAccountUsernameField, createAccountPasswordField, passwordConfirmLabel, passwordConfirmField,
						createAccountConfirmButton);
				
				//Sets up the scene to show the create account window and hide the login screen window
				Scene createAccountScene = new Scene(createAccountPane);
				createAccountStage.setTitle("Guitar Helper - Create Account");
				createAccountStage.setScene(createAccountScene);
				createAccountStage.show();
				loginStage.hide();
				
				createAccountConfirmButton.setOnAction( e3 -> {
					createAccountPane.getChildren().removeAll(usernameTakenText, usernameTakenPrompt, usernameTakenPrompt2,
														passwordMismatchText, passwordTakenPrompt, passwordTakenPrompt2);
					int usernameError = 0;
					int passwordError = 0;
					try {
						if(ghDBConnect.isTaken(createAccountUsernameField.getText())){
							usernameError = 1;
						}
						if(!createAccountPasswordField.getText().equals(passwordConfirmField.getText())){
							passwordError = 1;
						}
						
						if(usernameError == 0 && passwordError == 0) {
							ghDBConnect.createAccount(createAccountUsernameField.getText(), createAccountPasswordField.getText());
							createAccountConfirmButton.setDisable(true);
							createAccountConfirmButton.setText("Account Created");
						}
						else if(usernameError == 1 && passwordError == 0) {
							createAccountPane.getChildren().addAll(usernameTakenText, usernameTakenPrompt, usernameTakenPrompt2);
						}
						else if(usernameError == 0 && passwordError == 1) {
							createAccountPane.getChildren().addAll(passwordMismatchText, passwordTakenPrompt, passwordTakenPrompt2);
						}
						else {
							createAccountPane.getChildren().addAll(usernameTakenText, usernameTakenPrompt, usernameTakenPrompt2,passwordMismatchText, passwordTakenPrompt, passwordTakenPrompt2);
						}
					} catch (SQLException e1) {
						e1.printStackTrace();
					}
				});
			});
		});
		
	}

	/**
	 * Standard java main class, used solely for the javafx application to launch successfully.
	 * @param args
	 */
	public static void main(String[] args) {
		launch(args);
	}
}

	
	
