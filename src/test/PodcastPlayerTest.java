package test;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.List;
import org.junit.jupiter.api.Test;
import controller.PodCastPlayerController;
import javafx.embed.swing.JFXPanel;
import model.PodcastPlayerModel;
import model.PodcastPlayerModel.Episode;
import model.PodcastPlayerModel.Podcast;

public class PodcastPlayerTest {

	@Test
	void testLoadMany() throws URISyntaxException, IOException {
		PodcastPlayerModel testMdl = new PodcastPlayerModel();
		System.out.println();
		String lilLine = "**************************************************";
		// How Stuff Works
		// This podcast RSS feed contains no usable podcast links. Still parsing correctly.
		testMdl.addPodcast("https://syndication.howstuffworks.com/rss/HSW");
		// Smartless
		testMdl.addPodcast("https://rss.art19.com/smartless");
		// Crime Junkie
		testMdl.addPodcast("https://feeds.megaphone.fm/ADL9840290619");
		// Lex Friedman
		testMdl.addPodcast("https://lexfridman.com/feed/podcast/");
		// 99% PI
		testMdl.addPodcast("https://feeds.simplecast.com/BqbsxVfO");
		// A bad cast e.g. an invalid address.
		// DateLine Podcast.
		//Wonky Source... works fine...
		testMdl.addPodcast("http://feeds.feedburner.com/TEDTalks_audio");
		// DateLine Podcast.
		testMdl.addPodcast("https://podcastfeeds.nbcnews.com/HL4TzgYC");
		//Bad Source
		testMdl.addPodcast("https://audioboom.com/channels/00000.rss");



		List<Podcast> myList = testMdl.getLibrary();
		for (Podcast item : myList) {
			System.out.println("\n" + lilLine + "*****************" + item.getTitle().toUpperCase()
					+ "******************" + lilLine);
			System.out.println(lilLine + lilLine + lilLine);
			System.out.println(lilLine + lilLine + lilLine + "\n\n");
			System.out.println(item.getTitle());
			System.out.println(item.getDescription());
			System.out.println(item.getLink());
			System.out.println(item.getImage().toString());

			int index = 0;
			for (Episode ep : item.getEpisodes()) {
				System.out.println(
						"\n" + lilLine + "***********" + "*****Each Episode**********" + "************" + lilLine);
				System.out.println(lilLine + lilLine + lilLine + "\n");
				System.out.println(ep.getTitle());
				System.out.println(ep.getDescription());
				System.out.println(ep.getLink());
				System.out.println();
				index++;
				if (index == 2) {
					break;
				}

			}
			System.out.println();
		}
	}
	
	@Test
	void testLoadDiffSet() throws URISyntaxException, IOException {
		PodcastPlayerModel testMdl = new PodcastPlayerModel();
		System.out.println();
		String lilLine = "**************************************************";
		// How Stuff Works
		// This podcast RSS feed contains no podcasts. Still parsing correctly.
		testMdl.addPodcast("https://syndication.howstuffworks.com/rss/HSW");
		// Smartless
		testMdl.addPodcast("https://rss.art19.com/smartless");
		// Planet Money
		testMdl.addPodcast("https://feeds.npr.org/510289/podcast.xml");
		// No Such Thing as a Fish
		testMdl.addPodcast("https://audioboom.com/channels/2399216.rss");
		// Revisionist History
		testMdl.addPodcast("https://feeds.megaphone.fm/revisionisthistory");
		// Conan O'Brian Needs a Friend.
		testMdl.addPodcast("https://feeds.simplecast.com/dHoohVNH");
		// DateLine Podcast.
		testMdl.addPodcast("https://podcastfeeds.nbcnews.com/HL4TzgYC");
		// A bad cast e.g. an invalid address.
		testMdl.addPodcast("NOTHTML.xml");


		List<Podcast> myList = testMdl.getLibrary();
		for (Podcast item : myList) {
			System.out.println("\n" + lilLine + "*****************" + item.getTitle().toUpperCase()
					+ "******************" + lilLine);
			System.out.println(lilLine + lilLine + lilLine);
			System.out.println(lilLine + lilLine + lilLine + "\n\n");
			System.out.println(item.getTitle());
			System.out.println(item.getDescription());
			System.out.println(item.getLink());
			System.out.println(item.getImage().toString());

			int index = 0;
			for (Episode ep : item.getEpisodes()) {
				System.out.println(
						"\n" + lilLine + "***********" + "*****Each Episode**********" + "************" + lilLine);
				System.out.println(lilLine + lilLine + lilLine + "\n");
				System.out.println(ep.getTitle());
				System.out.println(ep.getDescription());
				System.out.println(ep.getLink());
				System.out.println();
				index++;
				if (index == 2) {
					break;
				}

			}
			System.out.println();
		}
	}

	@Test
	void testgetOnesInfo() throws URISyntaxException, IOException, ClassNotFoundException {
		// Dummy Panel to create media Objects needed for test.
		JFXPanel dummyPan = new JFXPanel();
		PodcastPlayerModel testMdl = new PodcastPlayerModel();
		System.out.println(); // lex Friedman
		testMdl.getPodcastRSSContent();
		testMdl.getLocalFile();
		assertThrows(IllegalArgumentException.class, () -> {
			testMdl.getEpisode(null, null);
		});
		assertThrows(IllegalArgumentException.class, () -> {
			testMdl.getEpisode("Lex Fridman Podcast", null);
		});
		testMdl.getEpisode("Lex Fridman Podcast", "Eric Schmidt: Google");
		testMdl.addPodcast(null);
		Episode thisEpi = testMdl.getLibrary().get(0).getEpisodes().get(0);
		System.out.println(thisEpi.getTitle());
		System.out.println(thisEpi.getDescription());
		System.out.println(thisEpi.getLink().toString());
		System.out.println(thisEpi.getPodcast().getTitle());
    
		testMdl.storeModel(new ObjectOutputStream(new FileOutputStream("podcastSaveState.txt")));
		testMdl.loadModel("podcastSaveState.txt");
	}

	@Test
	void testController() throws MalformedURLException, IOException, URISyntaxException {
		// Dummy Panel to create media Objects needed for test.
		JFXPanel dummyPan = new JFXPanel();
		PodcastPlayerModel model = new PodcastPlayerModel();
		PodCastPlayerController control = new PodCastPlayerController(model);
		control.changeEp("Lex Fridman Podcast", "Eric Schmidt: Google");
		control.changeEp("Planet Money", "Day of the Debt");
		control.getMediaPlayer();
		control.fastForwardPodcast();
		control.rewindPodcast();
		control.getPodcasts();
		control.pausePodcast();
		control.startPlayPodcast();

	}
}
	 
