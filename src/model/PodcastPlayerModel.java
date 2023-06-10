package model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.UnsupportedEncodingException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

/**
 * Acts as the model for the PodcastPlayer program.
 * 
 * Contains methods to load entire podcasts from
 *  XML files or individual audio files from the
 *  hard drive. Organizes all of the metadata
 *  associated with all of the podcasts and episodes.
 * 
 * @author seanomeara02, Todd Noecker, Garrett Scott
 * @version 2.0
 *
 */
@SuppressWarnings("deprecation")
public class PodcastPlayerModel extends Observable implements Serializable {
	
	/**
	 * ID for serialVersionUID
	 */
	private static final long serialVersionUID = 4770164474717564717L;
	
	private static final String LOCAL_FILE = "./localFiles/Ep1_Jefe.mp3";
	private static final String TEST_MP3 = "https://feeds.npr.org/510289/podcast.xml";
	private Episode curEp;
	
	/**
	 * Stores Podcasts as indexed by their titles.
	 *  Although the Podcasts have their titles
	 *  already associated with them, this mapping
	 *  makes it easier to fetch an individual Podcast.
	 */
	private Map<String, Podcast> library;
	
	/**
	 * Initializes the library as an empty HashMap.
	 * 
	 * @author seanomeara02
	 */
	public PodcastPlayerModel() {
		this.library = new HashMap<String, Podcast>();
		
	}
	
	/* ------------------------------------------------------------------------ 
	 * ---------------------------PUBLIC METHODS-------------------------------
	 * ------------------------------------------------------------------------
	 */
	
	/**
	 * Returns a List of all of the currently loaded
	 *  Podcasts in alphabetical order by title.
	 * 
	 * @return a sorted List of Podcasts.
	 * @author seanomeara02
	 */
	public List<Podcast> getLibrary() {
		List<Podcast> list = new ArrayList<Podcast>(library.values());
		Collections.sort(list);
		return list;
	}
	
	/**
	 * Loads a new Podcast from an XML file, and
	 *  stores it in the library.
	 * 
	 * @param filePath is a path to an XML file
	 *  storing data for a podcast.
	 * @author Todd Noecker
	 * @throws IOException 
	 * @throws URISyntaxException 
	 */
	public void addPodcast(String filePath) throws IOException, URISyntaxException {
		try {
			URI check = new URI(filePath);
			String HTML = getSiteContent(check);
			if(HTML != null) {
			Podcast podcast = new Podcast(filePath, HTML);
			library.put(podcast.getTitle(), podcast);
			}
		} catch (Exception ex) {
			System.out.println("Site " + ex.getMessage() + " did not contain an RSS feed or did not exist. Skipping\n");
		}

	}
	
	/**
	 * Loads a local audio file into a Media object.
	 * 
	 * Neither the file path or the Media object is
	 *  stored by the model.
	 * 
	 * @return a Media object representing the
	 *  audio file in a playable format.
	 * @author Garrett Scott
	 * @throws IOException 
	 * @throws MalformedURLException 
	 * @throws URISyntaxException 
	 */
	public Media getLocalFile() throws MalformedURLException, IOException, URISyntaxException {
		
		return new Media(new File(LOCAL_FILE).toURI().toString());
	}
	
	/**
	 * Loads a set of podcasts into the podcast Player. An RSS feed is passed to the
	 * podcast constructor to be parsed into a podcasts Object with episodes.
	 * 
	 * @author Garrett Scott, Todd Noecker, Sara Grimes, seanomeara02
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public void getPodcastRSSContent() throws IOException, URISyntaxException {
		// Planet Money
		this.addPodcast(TEST_MP3);
		// Planet Money
		this.addPodcast("https://feeds.npr.org/510289/podcast.xml");
		// No Such Thing as a Fish
		this.addPodcast("https://audioboom.com/channels/2399216.rss");
		// Lex Friedman
		this.addPodcast("https://lexfridman.com/feed/podcast/");
		// 99% PI
		this.addPodcast("https://feeds.simplecast.com/BqbsxVfO");
		// DateLine Podcast.
		this.addPodcast("https://podcastfeeds.nbcnews.com/HL4TzgYC");
		// Ted Daily Podcast.
		this.addPodcast("http://feeds.feedburner.com/TEDTalks_audio");
	}
	
	/**
	 * Returns a MediaPlayer created from one of the loaded
	 *  Podcast Episodes.
	 * 
	 * The Episode is specified by the name of the
	 *  Podcast which contains it and the episode
	 *  name.
	 * 
	 * @param podcastName is the title of the Podcast
	 *  of which this Episode is a part.
	 * @param episodeTitle is the name of the specified Episode.
	 * @return a MediaPlayer which plays the specified Episode.
	 * @author seanomeara02, Garrett Scott
	 */
	public MediaPlayer getEpisode(String podcastName, String episodeTitle) {
		// get podcast from library
		Podcast podcast = library.get(podcastName);
		
		// if it returns null throw exception
		if (podcast == null)
			throw new IllegalArgumentException("Podcast "+podcastName+" not found");
		
		// get the episode from the podcast
		this.curEp = podcast.getEpisode(episodeTitle);
		
		// if episode isn't there throw exception
		if (this.curEp == null)
			throw new IllegalArgumentException("Podcast "+podcastName+" has no episode "+episodeTitle);
		
		// Get startTime
		Duration startTime = this.curEp.getCurTime();
		
		Media newM = new Media(this.curEp.getLink().toString());
		MediaPlayer newMP = new MediaPlayer(newM);
		if (startTime != null) {
			newMP.setStartTime(startTime);
		}
		
		setChanged();
		notifyObservers(this.curEp);
		return newMP;
	}
	
	/**
	 * This saves the Episodes current play position for future reference.
	 * 
	 * @param curTime Duration object.
	 * @author Garrett Scott
	 */
	public void savePlayPos(Duration curTime) {
		this.curEp.setCurTime(curTime);
	}
	
	/**
	 * This method writes its self to file to file. 
	 * 
	 * @param out output stream associated with a file file to write to. 
	 * @throws IOException
	 * @author Garrett Scott
	 */
	public void storeModel (ObjectOutputStream out) throws IOException {
		out.writeObject(this);
		out.close();
	}
	
	/**
	 * This will load a model from the save file.
	 * 
	 * @param fileName is the name of the file in which the serialized model is stored.
	 * @return Model derived from the save file.
	 * @throws IOException caused by file not existing. 
	 * @throws ClassNotFoundException thrown if the input object isn't a PodcastPlayerModel
	 */
	public PodcastPlayerModel loadModel(String fileName) throws IOException, ClassNotFoundException {
		// Create the file input stream
		FileInputStream fis = new FileInputStream(fileName);
		// Create the input stream from the file
		ObjectInputStream ois = new ObjectInputStream(fis);
		// read in the saved model
		PodcastPlayerModel outputModel = (PodcastPlayerModel) ois.readObject();
		ois.close();
		
		return outputModel;
	}
	
	/* ------------------------------------------------------------------------ 
	 * --------------------------PRIVATE METHODS-------------------------------
	 * ------------------------------------------------------------------------
	 */
	
	/**
	 * This method will scrape a passed RSS site's content and store it into a
	 * String to be parsed.
	 * 
	 * 
	 * @author Todd Noecker
	 * 
	 * @param url the passed url
	 * 
	 * @return the full parsed text of the RSS feed site.
	 */
	private String getSiteContent(URI url) {
		StringBuffer buff = null;

		try {
			// Basic input stream from passed URI.
			InputStream content = url.toURL().openStream();
			int len = 0;
			buff = new StringBuffer();
			while ((len = content.read()) != -1) {
				buff.append((char) len);
			}
		} catch (Exception ex) {
			// Seemed useful for now, might be best to remove the print or adapt it
			// for the final build.
			System.out.println(
					"Site " + ex.getMessage() + " did not contain an RSS feed or did not exist. Skipping\n");
			return null;
		}
		return buff.toString();
	}
	
	/* ------------------------------------------------------------------------
	 * --------------------------PUBLIC INNERCLASSES---------------------------
	 * ------------------------------------------------------------------------
	 */
	
	/**
	 * Stores all of the metadata for a specific podcast, including a list of
	 * Episodes in the podcast.
	 * 
	 * @author seanomeara02, Todd Noecker
	 */
	public class Podcast implements Comparable<Podcast>, Serializable {
		
		/**
		 * ID for serialVersionUID
		 */
		private static final long serialVersionUID = -5190644900968741510L;
		
		private String title; // the title of the podcast
		private String description; // a description of the podcast
		private URI link; // a link to the podcast
		private URI image; // a link to the podcast's artwork
		private Map<String,Episode> episodes; // the list of episodes in the podcast

		/**
		 * Will parse the data of a given XML file to fill all of the above fields.
		 * 
		 * @param filePath is the path to an XML file representing the podcast.
		 * @param HTML is the content of the podcast's RSS feed.
		 * @author seanomeara02, Todd Noecker
		 * @throws IOException
		 * @throws URISyntaxException
		 */
		public Podcast(String filePath, String HTML) throws IOException, URISyntaxException {
			// Convert String address to URI.
			URI myURI = new URI(filePath);

			if (HTML != null) {
				// Parse all needed fields for a podcast object.
				this.title = parseTitle(HTML);
				this.title = convertSpecialChars(this.title);
				this.link = myURI;
				String image = parseImageLoc(HTML);
				if (image != null) {
					this.image = new URI(image);
				} else {
					// Default Image inbound...
				}
				this.description = parseDescription(HTML);
				this.description = convertSpecialChars(this.description);

				// Called to parse all available episodes of a podcast.
				this.parseEpisodes(this, HTML);
			} else {
				return;
			}
		}
		
		/* ------------------------------------------------------------------------ 
		 * ---------------------------PUBLIC METHODS-------------------------------
		 * ------------------------------------------------------------------------
		 */

		/**
		 * Fetches the title of the Podcast.
		 * 
		 * @return the title of the Podcast.
		 * @author seanomeara02
		 */
		public String getTitle() {
			return title;
		}

		/**
		 * Fetches the description of the Podcast.
		 * 
		 * @return the description of the Podcast.
		 * @author seanomeara02
		 */
		public String getDescription() {
			return description;
		}

		/**
		 * Fetches the link to the Podcast.
		 * 
		 * @return the link to the Podcast.
		 * @author seanomeara02
		 */
		public URI getLink() {
			return link;
		}

		/**
		 * Fetches the link to the artwork for the Podcast.
		 * 
		 * @return the link to the artwork of the Podcast.
		 * @author seanomeara02
		 */
		public URI getImage() {
			return image;
		}

		/**
		 * Fetches the List of Episodes in the Podcast.
		 * 
		 * @return the Episodes of the Podcast.
		 * @author seanomeara02
		 */
		public List<Episode> getEpisodes() {
			List<Episode> list = new ArrayList<Episode>(episodes.values());
			Collections.sort(list);
			return list;
		}
		
		/**
		 * Returns the Episode object that has the title matching the parameter title.
		 * 
		 * @param title to search for
		 * @return Episode object with title
		 * @author Garrett Scott
		 */
		public Episode getEpisode(String title) {
			return episodes.get(title);
		}

		/**
		 * Specifies how Podcasts should be sorted.
		 * 
		 * Used when returning the model's library to sort the Podcasts by their titles.
		 * 
		 * @author seanomeara02
		 */
		@Override
		public int compareTo(Podcast that) {
			return this.title.compareTo(that.title);
		}
		
		/* ------------------------------------------------------------------------ 
		 * --------------------------PRIVATE METHODS-------------------------------
		 * ------------------------------------------------------------------------
		 */

		/**
		 * This method will compare segments of characters for matches to trigger words
		 * like <author> or <title>. The content after the passed matched words will be
		 * parsed out of the associated String and assigned as a field value in a
		 * podcast or episode object. This method specifically finds the starting word.
		 * 
		 * @author Todd Noecker
		 * @param HTML     the parsed RSS feed taken as an HTML
		 * @param seachFor the passed search word.
		 * @return the parsed String with extraneous chars removed.
		 */
		private String parseXML(String HTML, String searchFor) {
			String triggerStr = searchFor;
			String endStr = searchFor.substring(0, 1) + '/' + searchFor.substring(1);
			int triggerLen = triggerStr.length();
			String searchStr = "";

			if (HTML == "") {
				System.out.println("No Site Content");
				return null;
			}

			for (int index = 0; index < HTML.length() - triggerLen; index++) {

				searchStr = HTML.substring(index, index + triggerLen);

				if (searchStr.equals(triggerStr)) {
					return (terminatorSearch(HTML, index, searchStr, triggerLen, triggerStr, endStr));
				}
			}
			return null;
		}

		/**
		 * This method will compare segments of characters for matches to triggers like
		 * </ or />. The content iss then stripped of extraneous characters and passed
		 * to the clean method.
		 * 
		 * @author Todd Noecker
		 * @param HTML     the parsed RSS feed taken as an HTML
		 * @param index    the passed state of the index and thus position in the file.
		 * @param seachStr the passed String to use as a comparison.
		 * @param the      passed length of the trigger String.
		 * @param endStr   the modified start String passed to indicate end of the
		 *                 desired text.
		 * @return the parsed String with extraneous chars removed. null is returned if
		 *         no match is found.
		 */
		private String terminatorSearch(String HTML, int index, String searchStr, int triggerLen, String triggerStr,
				String endStr) {

			int countIndex = index + triggerLen + 1;
			searchStr = HTML.substring(index, countIndex);
			String compStr = HTML.substring(index, countIndex);
			int compIndex = index;

			// Find the terminator '</'
			while (!compStr.equals(endStr) && countIndex - 1 <= HTML.length()) {
				if (compStr.charAt(compStr.length() - 2) == '<' && compStr.charAt(compStr.length() - 1) == '/') {
					String addStr = searchStr.substring(triggerLen, searchStr.length() - 1);
					return cleanString(addStr);
				}
				// Find the terminator '/>'
				if (compStr.charAt(compStr.length() - 2) == '/' && compStr.charAt(compStr.length() - 1) == '>') {
					String addStr = searchStr.substring(triggerLen, searchStr.length() - 1);
					return cleanString(addStr);
				}
				// Update the searched String by moving one character.
				searchStr = HTML.substring(index, countIndex);
				countIndex++;
				compIndex++;
				// Updates compStr i.e. moves it a char down the line
				if (countIndex < HTML.length() - triggerStr.length()) {
					compStr = HTML.substring(compIndex, countIndex);
				}
			}
			return null;
		}

		/**
		 * This method will scrape a passed RSS site's content and store it into a
		 * String to be parsed.
		 * 
		 * @author Todd Noecker
		 * 
		 * @param url the passed url
		 * 
		 * @return the full parsed text of the RSS feed site.
		 */
		private String parseImageLoc(String HTML) {
			String retString = null;
			retString = parseXML(HTML, "<image><url>");

			if (retString == null) {
				retString = parseXML(HTML, "<itunes:image");
			}
			if (retString == null) {
				retString = parseXML(HTML, "<image>");
				retString = retString.substring(4, retString.length());
			}

			return retString;
		}

		/**
		 * This method will use the parse XML method to get the title from the HTML
		 * string.
		 * 
		 * @author Todd Noecker
		 * 
		 * @param HTML the passed HTML
		 * 
		 * @return the found title or null;
		 */
		private String parseTitle(String HTML) {
			String retString = null;
			retString = parseXML(HTML, "<title>");

			if (retString == null) {
				retString = parseXML(HTML, "<itunes:summary>");
			}
			return retString;
		}

		/**
		 * This method will use the parse XML method to get the description from the
		 * HTML string.
		 * 
		 * @author Todd Noecker
		 * 
		 * @param HTML the passed HTML
		 * 
		 * @return the found author or null;
		 */
		private String parseDescription(String HTML) {
			String retString = null;
			retString = parseXML(HTML, "<itunes:summary>");

			if (retString == null) {
				retString = parseXML(HTML, "<p>");
			}
			if (retString == null) {
				retString = parseXML(HTML, "<description><![CDATA[");
			}

			return retString;
		}

		/**
		 * Gets the link to the mp3 file for the currently explored item. A link is
		 * prefaced with url" and ends with " links always seem to be in an enclosure
		 * tag.
		 * 
		 * @author Todd Noecker
		 * 
		 * @param linkHTML The passed HTML used for parsing from <item> to </item>
		 * 
		 * @return retString the parsed link.
		 */
		private String getLink(String linkHTML) {
			String enclose = "<enclosure";
			String url = "url=" + '"';
			int index = 0;

			// identify "<enclosure"
			if (linkHTML.indexOf(enclose) != -1) {
				while (!linkHTML.substring(index, index + enclose.length()).equals(enclose)
						&& index < linkHTML.length() - 1) {
					index++;
				}
				linkHTML = linkHTML.substring(index, linkHTML.length());
				index = 0;

				// identify "url="""
				while (!linkHTML.substring(index, index + url.length()).equals(url) && index < linkHTML.length() - 1) {
					index++;
				}
				linkHTML = linkHTML.substring(index, linkHTML.length());

				index = url.length();
				// identify the end '"' "
				while (linkHTML.charAt(index) != '"' && index < linkHTML.length() - 1) {
					index++;
				}
			}
			// Return everything found between url="-start and "-end after
			if (index != 0) {
				return linkHTML.substring(url.length(), index);
			} else {
				return getLinkAlt(linkHTML);
			}
		}

		/**
		 * Gets the link to the mp3 file for the currently explored item. This acts as
		 * an alternate method if the most standard XML style is not used in the RSS
		 * feed.
		 * 
		 * @author Todd Noecker
		 * 
		 * @param linkHTML The passed HTML used for parsing from <link> to </link>
		 * 
		 * @return retString the parsed link.
		 */
		private String getLinkAlt(String linkHTML) {
			String link = "<link>";
			String endLink = "</link>";
			int index = 0;

			// identify "<link>"
			if (linkHTML.indexOf(link) != -1) {
				while (!linkHTML.substring(index, index + link.length()).equals(link)
						&& index < linkHTML.length() - 1) {
					index++;
				}
				linkHTML = linkHTML.substring(index, linkHTML.length());
				index = 0;

				// identify the end '</link>' "
				while (!linkHTML.substring(index, index + endLink.length()).equals(endLink)
						&& index < linkHTML.length() - 1) {
					index++;
				}
			}
			// Return the contents between <link></link>
			return linkHTML.substring(link.length(), index);

		}

		/**
		 * This method will remove all extraneous characters found in passed strings
		 * resulting from varied RSS styles. This differs from the convert special chars
		 * method as it will remove characters within the normal ASCII range.
		 * 
		 * @return retString the cleaned String.
		 * 
		 * @author Todd Noecker
		 */
		private String cleanString(String retString) {
			// Guards null
			if (retString == null) {
				return retString;
			}
			retString = retString.trim();

			// Guards empty file.
			if (retString.length() > 0) {

				// Checks for junk chars at the end of the String.
				while (retString.charAt(retString.length() - 1) == ' '
						|| retString.charAt(retString.length() - 1) == '/'
						|| retString.charAt(retString.length() - 1) == '<'
						|| retString.charAt(retString.length() - 1) == '"') {
					retString = retString.substring(0, retString.length() - 1);

				}

				// Checks for junk chars at the beginning of the String.
				while (retString.charAt(0) == '<' || retString.charAt(0) == '/' || retString.charAt(0) == '>'
						|| retString.charAt(0) == ' ' || retString.charAt(0) == '"') {
					retString = retString.substring(1, retString.length());

				}
				// Checks for the specific case of a dangling href=
				if (retString.length() > 5) {
					if (retString.substring(0, 5).equals("href=")) {
						retString = retString.substring(6, retString.length());
					}
				}
				// Checks for the specific case of a dangling ![CDATA[
				if (retString.length() > 8) {
					if (retString.substring(0, 8).equals("![CDATA[")) {
						retString = retString.substring(8, retString.length());
					}
				}
				// Checks for the specific case of a dangling ![CDATA[<p>
				if (retString.length() > 11) {
					if (retString.substring(0, 11).equals("![CDATA[<p>")) {
						retString = retString.substring(11, retString.length());
					}
				}
			}
			return retString;
		}

		/**
		 * This method will convert special signifier in the passed String with a more
		 * human friendly output. The replaceAll with [^\u0000-\u007F] is regex to
		 * remove all characters beyond the normal ASCII range. Needed conversions are
		 * made prior.
		 * 
		 * @return the modified String with extraneous characters removed.
		 * 
		 * @param the passed String to be converted.
		 * 
		 * @author Todd Noecker
		 */
		private String convertSpecialChars(String baseStr) throws UnsupportedEncodingException {

			if (baseStr != null) {
				baseStr = baseStr.replaceAll("©", "");
				baseStr = baseStr.replaceAll("&amp;", "&");
				baseStr = baseStr.replaceAll("&#8211;", "--");
				baseStr = baseStr.replaceAll("&#", "#");
				baseStr = baseStr.replaceAll("³", "");
				baseStr = baseStr.replaceAll("&apos;", "'");
				baseStr = baseStr.replaceAll("#039;", "'");
				baseStr = baseStr.replaceAll("&quot;", "'");
				// This regex sequence replaces all special chars outside the normal ASCII
				// range with "".
				baseStr = baseStr.replaceAll("[^\u0000-\u007F]", "");
				baseStr = baseStr.replaceAll("<br>", "");
				baseStr = baseStr.replaceAll("<em>", "");
				baseStr = baseStr.replaceAll("<p>", "");
				baseStr = baseStr.replaceAll("<div>", "");
				baseStr = baseStr.replaceAll("]]>", "");
				baseStr = baseStr.replaceAll("div>", "");

				String compStr = "<a href=" + '"';
				if (baseStr.contains(compStr)) {
					baseStr = removeHTMLLinks(baseStr);
				}
			}
			return baseStr;
		}

		/**
		 * This method will look for all embedded links in a passed description, and
		 * remove all <a href=" tags and its contents.
		 * 
		 * @author Todd Noecker
		 * 
		 * @param the passed String to have href tags removed.
		 * 
		 * @return the String with all hrefs removed.
		 */
		private String removeHTMLLinks(String baseStr) {
			String link = "<a href=" + '"';
			String endLink = '"' + ">";
			int index = 0;
			int index2 = 0;

			// identify "<a href=""
			if (baseStr.indexOf(link) != -1) {
				while (!baseStr.substring(index, index + link.length()).equals(link) && index < baseStr.length() - 1) {
					index++;
				}
				index2 = index;

				// identify the end "">"
				while (!baseStr.substring(index2, index2 + endLink.length()).equals(endLink)
						&& index2 < baseStr.length() - 1) {
					index2++;
				}
			}
			// Return the contents between <link></link>
			if (index2 < baseStr.length()) {
				return baseStr.substring(0, index) + baseStr.substring(index2 + 2, baseStr.length());
			}
			return baseStr;

		}

		/**
		 * This method is a modified version of the ParseXML method it will search for
		 * <item> tags which indicate an episode in the RSS feed. If and for each tag is
		 * found that just the item tag text is passed to the XML parser to remove just
		 * the episode description.
		 * 
		 * @param HTML    the passed HTML
		 * 
		 * @param thisPod The self instance of the current podcast to have episodes
		 *                added to.
		 * 
		 * @author Todd Noecker
		 */
		private void parseEpisodes(Podcast thisPod, String HTML)
				throws MalformedURLException, URISyntaxException, UnsupportedEncodingException {

			String triggerStr = "<item>";
			String endStr = "</item>";
			int triggerLen = triggerStr.length();
			episodes = new HashMap<String, Episode>();
			String searchStr = "";
			int podIndex = 0;

			if (HTML == "") {
				return;
			}

			// Loop to visit each char in the HTML String.
			for (int index = 0; index < HTML.length() - triggerLen; index++) {

				searchStr = HTML.substring(index, index + triggerLen);

				// Find the start of the episode block
				if (searchStr.equals(triggerStr)) {
					int foundIndex = index;
					int endIndex = index;
					// Find the end of the episode block.
					while (!searchStr.equals(endStr)) {
						searchStr = HTML.substring(endIndex, endIndex + triggerLen + 1);
						endIndex++;
					}

					// Break off just the item block.
					searchStr = HTML.substring(foundIndex, endIndex);

					// Generate each field value.
					String title = parseTitle(searchStr);
					String epDescription = parseDescription(searchStr);
					String link = getLink(searchStr);
					URI linkURL = new URI(link);

					// Replace char identifiers with correct characters.
					title = convertSpecialChars(title);
					epDescription = convertSpecialChars(epDescription);

					// We are currently not using author in for specific podcast episodes.
					// author = convertSpecialChars(author);

					// Add episode to podcast.
					Episode addEp = new Episode(title, epDescription, linkURL, this, podIndex);
					this.episodes.put(title, addEp);
					podIndex++;
				}
			}
		}
	}

	/**
	 * Stores all of the metadata for a specific Episode of a Podcast.
	 * 
	 * @author seanomeara02
	 */
	public static class Episode implements Serializable, Comparable<Episode> {

		/**
		 * ID for serialVersionUID
		 */
		private static final long serialVersionUID = -8761764102871496427L;
		
		private String title; // the title of the episode
		private String desc; // a description of the episode
		private URI link; // a link to the episode
		private Podcast parent; // the podcast of which this podcast is a part
		private int epNum; // the index of this episode in its parent's episode list
		private Duration currTime; // the current time in the podcast
		
		/**
		 * Initializes all of the metadata for the
		 *  Episode. All of the below data is required,
		 *  but any of it can be set to null if not
		 *  applicable.
		 * 
		 * @param title is the name of the Episode.
		 * @param desc is a description of the Episode.
		 * @param link is a link to the Episode.
		 * @param parent is the Podcast in which this
		 *  Episode is contained.
		 * @param epNum is the number of the Episode in
		 *  its parent Podcast.
		 * @author seanomeara02, Todd Noecker
		 */
		public Episode(String title, String desc, URI link, Podcast parent, int epNum) {
			this.title = title;
			this.desc = desc;
			this.link = link;
			this.parent = parent;
			this.epNum = epNum;
			this.currTime = null;
		}
		
		/* ------------------------------------------------------------------------ 
		 * ---------------------------PUBLIC METHODS-------------------------------
		 * ------------------------------------------------------------------------
		 */

		/**
		 * Fetches the title of this Episode.
		 * 
		 * @return the title of this Episode.
		 * @author seanomeara02
		 */
		public String getTitle() {
			return title;
		}
		
		/**
		 * Fetches the description of this Episode.
		 * 
		 * @return the description of this Episode.
		 * @author seanomeara02
		 */
		public String getDescription() {
			return desc;
		}
		
		/**
		 * Fetches the link to this Episode.
		 * 
		 * @return the link to this Episode.
		 * @author seanomeara02
		 */
		public URI getLink() {
			return link;
		}

		/**
		 * Fetches the parent Podcast of this
		 *  Episode.
		 * 
		 * @return this Episodes' parent Podcast.
		 * @author seanomeara02
		 */
		public Podcast getPodcast() {
			return parent;
		}
		
		/**
		 * Sets the current time the podcast is at. Used for reference when restarting 
		 * the episode. 
		 * 
		 * @param curTime Duration object used to seek to the last point the podcast was played.
		 * @author Garrett Scott
		 */
		public void setCurTime(Duration curTime) {
			this.currTime = curTime;
		}
		
		/**
		 * This returns the current time stamp the episode is at.
		 * 
		 * @return Duration object representing the current time in the podcast.
		 * @author Garrett Scott
		 */
		public Duration getCurTime() {
			return this.currTime;
		}
		
		/**
		 * Specifies how Episodes should be sorted.
		 * 
		 * Used when returning the model's library to sort the Episodes by their indices.
		 * 
		 * @author seanomeara02
		 */
		@Override
		public int compareTo(Episode that) {
			if (this.epNum < that.epNum) {
				return -1;
			} else if (this.epNum > that.epNum) {
				return 1;
			} else {
				return 0;
			}
		}
	}

}