package controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.List;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import model.PodcastPlayerModel;
import model.PodcastPlayerModel.Podcast;

/**
 * This Class functions as the controller for the podcast Player Application.
 * It's primary function is to create and interact with the MediaPlayer Object.
 * This class retrieves Podcast objects from the podcast model Class.
 * 
 * @author Sara Grimes, Sean O'Meara, Garrett Scott, Todd Noecker
 */
public class PodCastPlayerController {

	//The MediaPlayer Object acts as a primary point of interaction for
	//the loaded Media file.
	private MediaPlayer podPlayer;
	private PodcastPlayerModel model;

	/**
	 * Instantiates a model for this class to interact with.
	 * 
	 * @param passedModel The PodcastPlayer Model object required.
	 * @throws URISyntaxException 
	 * @throws IOException 
	 * @throws MalformedURLException 
	 * @author Garrett Scott, Todd Noecker 
	 */
	public PodCastPlayerController(PodcastPlayerModel passedModel) throws MalformedURLException, 
																		IOException, URISyntaxException {
		this.model = passedModel;
		try {
			model.getPodcastRSSContent();
		} catch (FileNotFoundException e) {
			System.err.println("Oops: No File!");
		}
		
		this.podPlayer = null;
	}
	
	/* ------------------------------------------------------------------------ 
	 * ---------------------------PUBLIC METHODS-------------------------------
	 * ------------------------------------------------------------------------
	 */
	
	/**
	 * Get the MediaPlayer with the current podcast loaded into it.
	 * 
	 * @return MediaPlayer object with a podcast in it.
	 * @author Garrett Scott
	 */
	public MediaPlayer getMediaPlayer() {
		return this.podPlayer;
	}
	
	/**
	 * This Changes the current episode being played. It will first check if there is a current
	 * episode loaded. If there is, it will save its play time for future reference then get the 
	 * new episode from the model. 
	 *  
	 * @param podcastName string name of the podcast
	 * @param epTitle string name of the podcast episode
	 * @author Garrett Scott
	 */
	public void changeEp(String podcastName, String epTitle) {
		if (this.podPlayer != null) {
			// Save the current playing episode time stamp
			this.model.savePlayPos(this.podPlayer.getCurrentTime());
		}
		this.podPlayer = model.getEpisode(podcastName, epTitle);
	}
	
	/**
	 * This method will start a podcast episode playing.
	 * 
	 * @author Garrett Scott
	 * 
	 */
	public void startPlayPodcast() {
		this.podPlayer.play();
	}
	
	/**
	 * This method will pause the currently playing podcast episode.
	 * 
	 * @author Garrett Scott
	 * 
	 */
	public void pausePodcast() {
		this.podPlayer.pause();
	}
	
	/**
	 * This method will fast forward the current podcast episode.
	 * 
	 * @author Garrett Scott
	 * 
	 */
	public void fastForwardPodcast() {
		// Duration object = to 30 s.
		Duration forward = Duration.seconds(30);
		// Adds 30 seconds to the current time of the podcast and seeks to that time.
		this.podPlayer.seek(this.podPlayer.getCurrentTime().add(forward));
	}
	
	/**
	 * This method will rewind the current podcast episode.
	 * 
	 * @author Garrett Scott
	 * 
	 */
	public void rewindPodcast() {
		// Duration object = to 15 s
		Duration back = Duration.seconds(15);
		// subtracts 15 seconds from current time of podcast and seeks to that time. 
		this.podPlayer.seek(this.podPlayer.getCurrentTime().subtract(back));
	}
	
	/**
	 * Returns a sorted list of all Podcasts currently stored in the model.
	 * 
	 * @return the current list of Podcasts.
	 * 
	 * @author Garrett Scott
	 */
	public List<Podcast> getPodcasts(){
		return model.getLibrary();
	}
	
}