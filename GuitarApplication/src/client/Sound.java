package client;

import javax.sound.sampled.LineUnavailableException;
import userInterface.LauncherUI;
import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;

/**
 * This class acts to handle all storage and retrieval of information relating to the functioning sound loop.
 * This allows for global manipulation of any variables related to sound, including variables exclusively
 * used in the tuner, scale practice, and intonation checker. The class imports the javax.sound class,
 * the LauncherUI class from the userInterface package, and several necessary classes from the TarsosDSP library.
 * @author Jonathan Novins
 * @version 1.2
 */
public class Sound {
/**
 * Serves as a variable that will hold the nearest index based on the currently read frequency. Designed
 * for use in conjunction with the noteFreqArray and noteArray arrays from the LauncherUI class.	
 */
	int nearestIndex;
	
/**
 * Serves as storage for the most recently read frequency in hz from the TarsosDSP PitchDetectionResult
 */
	float noteFreq;

/**
 * Serves as storage for the probability of the most recently read frequency based on how much the loudest
 * sound stands out from its surroundings
 */
	float noteProb;

/**
 * Serves as storage for the most recently read open note frequency. Intended for use within the intonation
 * checking tool.
 */	
	float openNote;

/**
 * Serves as storage for the most recently read harmonic note frequency. Intended for use within the intonation
 * checking tool in conjunction with the openNote value 
 */
	float harmonicNote;
	
/**
 * Serves as storage for the intonation-check status relating to the first string that the user tests. Intended
 * for use within the intonation checking tool.	
 */
	int stringStatus1;

/**
 * Serves as storage for the intonation-check status relating to the second string that the user tests. Intended
 * for use within the intonation checking tool.	
 */
	int stringStatus2;
	
/**
 * Serves as storage for the intonation-check status relating to the third string that the user tests. Intended
 * for use within the intonation checking tool.	
 */
	int stringStatus3;
	
/**
 * Serves as storage for the intonation-check status relating to the fourth string that the user tests. Intended
 * for use within the intonation checking tool.	
 */
	int stringStatus4;
	
/**
 * Serves as storage for the intonation-check status relating to the fifth string that the user tests. Intended
 * for use within the intonation checking tool.	
 */
	int stringStatus5;

/**
 * Serves as storage for the intonation-check status relating to the sixth string that the user tests. Intended
 * for use within the intonation checking tool.		
 */
	int stringStatus6;

/**
 * Serves as an int toggle relating to whether or notthe sound loop should be running at a given moment. This
 * value should be set to 1 for open microphone simulation or 0 for closed microphone behavior.
 */
	int soundRun;

/**
 * Constructor that will create a new Sound Object and set all values to their defaults. Sound Object will
 * generally serve as a global access point for certain variables.
 */
	public Sound() {
		soundRun = 0;
		nearestIndex = 0;
		noteFreq = -1;
		noteProb = 0;
		openNote = -1;
		harmonicNote = -1;
		stringStatus1 = 0;
		stringStatus2 = 0;
		stringStatus3 = 0;
		stringStatus4 = 0;
		stringStatus5 = 0;
		stringStatus6 = 0;
	}

/**
 * Serves to set up a microphone connection to the host system's default recording device.
 * @param dispatcher - receives a dispatcher device from the initial microphone connection attempt
 * in the LauncherUI class. When the dispatcher is received, it is unfinished and does not hold the required
 * connections.
 * @return The initially received AudioDispatcher Object that has since been modified to function to its
 * fullest extend.
 */
	public AudioDispatcher micConnect(AudioDispatcher dispatcher) {
		try {
			dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(2048, 0); // attempts to make a connection to the computers default recording device
			System.out.println("***MICROPHONE CONNECTION SUCCESSFUL***\n");
			return dispatcher;
		} catch (LineUnavailableException e1) {
			System.out.println("***MICROPHONE CONNECTION FAILED***");
			System.out.println("[PLEASE MAKE SURE THE DESIRED DEVICE IS SET TO DEFAULT IN WINDOWS]");
			e1.printStackTrace();
			return null;
		}
	}
	
/**
 * Basic get method for the int nearestIndex
 * @return Most recently stored value in the nearestIndex variable
 */
	public int getNearestIndex() {
		return nearestIndex;
	}

/**
 * Basic set method for the int nearestIndex
 * @param nearestIndex The desired value that nearestIndex should be set to
 */
	public void setNearestIndex(int nearestIndex) {
		this.nearestIndex = nearestIndex;
	}

/**
 * Basic get method for the float noteFreq
 * @return Most recently stored value in the noteFreq variable
 */
	public float getNoteFreq() {
		return noteFreq;
	}

/**
 * Basic set method for the float noteFreq
 * @param noteFreq The desired value that nearestIndex should be set to.
 */
	public void setNoteFreq(float noteFreq) {
		this.noteFreq = noteFreq;
	}

/**
 * Basic get method for the float noteProb
 * @return Most recently stored value in the noteProb variable
 */
	public float getNoteProb() {
		return noteProb;
	}

/**
 * Basic set method for the float noteProb
 * @param noteProb The desired value that noteProb should be set to
 */
	public void setNoteProb(float noteProb) {
		this.noteProb = noteProb;
	}

/**
 * Basic get method for the float openNote
 * @return Most recently stored value in the openNote variable
 */
	public float getOpenNote() {
		return openNote;
	}

/**
 * Basic set method for the float openNote
 * @param openNote The desired value that openNote should be set to
 */
	public void setOpenNote(float openNote) {
		this.openNote = openNote;
	}

/**
 * Basic get method for the float harmonicNote
 * @return Most recently stored value in the harmonicNote variable
 */
	public float getHarmonicNote() {
		return harmonicNote;
	}

/**
 * Basic set method for the float harmonicNote
 * @param harmonicNote The desired value that harmonicNote should be set to
 */
	public void setHarmonicNote(float harmonicNote) {
		this.harmonicNote = harmonicNote;
	}

/**
 * Basic get method for the int stringStatus1
 * @return Most recently stored value in the stringStatus1 variable
 */
	public int getStringStatus1() {
		return stringStatus1;
	}

/**
 * Basic set method for the int stringStatus1
 * @param stringStatus1 The desired value that stringStatus1 should be set to
 */
	public void setStringStatus1(int stringStatus1) {
		this.stringStatus1 = stringStatus1;
	}

/**
 * Basic get method for the int stringStatus2
 * @return Most recently stored value in the stringStatus2 variable
 */
	public int getStringStatus2() {
		return stringStatus2;
	}

/**
 * Basic set method for the int stringStatus2
 * @param stringStatus2 The desired value that stringStatus2 should be set to
 */
	public void setStringStatus2(int stringStatus2) {
		this.stringStatus2 = stringStatus2;
	}

/**
 * Basic get method for the int stringStatus3
 * @return Most recently stored value in the stringStatus3 variable
 */
	public int getStringStatus3() {
		return stringStatus3;
	}

/**
 * Basic set method for the int stringStatus3
 * @param stringStatus3 The desired value that stringStatus3 should be set to
 */
	public void setStringStatus3(int stringStatus3) {
		this.stringStatus3 = stringStatus3;
	}

/**
 * Basic get method for the int stringStatus4
 * @return Most recently stored value in the stringStatus4 variable
 */
	public int getStringStatus4() {
		return stringStatus4;
	}

/**
 * Basic set method for the int stringStatus4
 * @param stringStatus4 The desired value that stringStatus4 should be set to
 */
	public void setStringStatus4(int stringStatus4) {
		this.stringStatus4 = stringStatus4;
	}

/**
 * Basic get method for the int stringStatus5
 * @return Most recently stored value in the stringStatus4 variable
 */
	public int getStringStatus5() {
		return stringStatus5;
	}

/**
 * Basic set method for the int stringStatus5
 * @param stringStatus5 The desired value that stringStatus5 should be set to
 */
	public void setStringStatus5(int stringStatus5) {
		this.stringStatus5 = stringStatus5;
	}

/**
 * Basic get method for the int stringStatus6
 * @return Most recently stored value in the stringStatus6 variable
 */
	public int getStringStatus6() {
		return stringStatus6;
	}

/**
 * Basic set method for the int stringStatus6
 * @param stringStatus6 The desired value that stringStatus6 should be set to
 */
	public void setStringStatus6(int stringStatus6) {
		this.stringStatus6 = stringStatus6;
	}
	
/**
 * Basic get method for the int soundRun
 * @return Most recently stored valye in the soundRun variable
 */
	public int getSoundRun() {
		return soundRun;
	}

/**
 * Basic set method for the int sound
 * @param soundRun The desired value that soundRun should be set to
 */
	public void setSoundRun(int soundRun) {
		this.soundRun = soundRun;
	}
}
