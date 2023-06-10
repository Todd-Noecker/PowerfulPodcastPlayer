package view;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import controller.PodCastPlayerController;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import model.PodcastPlayerModel;
import model.PodcastPlayerModel.Episode;
import model.PodcastPlayerModel.Podcast;

/**
 * This Class functions as the view for the podcast Player Application. Its
 * primary function is to create the GUI and handle events.
 * 
 * @author Sara Grimes, Sean O'Meara, Garrett Scott, Todd Noecker
 */

@SuppressWarnings("deprecation")
public class PodCastPlayerView extends Application implements Observer {
	/**
	 * Field for the controller
	 */
	private PodCastPlayerController controller;
	/**
	 * Field for the model
	 */
	private PodcastPlayerModel model;
	/**
	 * Field for the scene to display the gui
	 */
	private Scene scene;
	/**
	 * Field for the media player
	 */
	private MediaPlayer mp;
	/**
	 * Field for the media view
	 */
	private MediaView mv;
	/**
	 * Field for the borderpane
	 */
	private BorderPane border;
	/**
	 * Field for the file for serialization
	 */
	private static final String FILENAME = "podcastSaveState.txt";

	/**
	 * Starts the GUI and makes the layout.
	 * 
	 * @param mainStage The stage where the GUI will be displayed
	 * @author Sara Grimes, seanomeara02
	 */
	@Override
	public void start(Stage mainStage) throws Exception {
		this.model = new PodcastPlayerModel();
		// Check if file exists to load model from.
		File loadFile = new File(FILENAME);
		if (loadFile.exists()) {
			this.model.loadModel(FILENAME);
		}
		this.model.addObserver(this);
		this.controller = new PodCastPlayerController(this.model);

		Group root = new Group();
		this.scene = new Scene(root);
		border = new BorderPane();

		scene.setFill(Color.BLACK);
		String css = this.getClass().getResource("style.css").toExternalForm();
		scene.getStylesheets().add(css);

		border.setTop(makeLabel("Powerful Podcast Player", null, 40.0, null, false, 0, 0));
		border.setBottom(makePlayBar());
		border.setLeft(displayPodcasts());
		border.setRight(makeImageBox());
		border.setPrefHeight(650);
		border.setPrefWidth(1200);
		border.autosize();

		root.getChildren().add(border);

		mainStage.setTitle("Powerful Podcast Player");
		mainStage.setScene(scene);
		mainStage.show();

	}

	/**
	 * On program exit this method will save the state of the current model instance
	 * to be loaded on next runtime.
	 * 
	 * @author Garrett Scott
	 */
	public void stop() throws IOException {
		// Create a save file to store if one doesn't exist
		File saveFile = new File(FILENAME);
		saveFile.createNewFile();

		// Create a file output stream that will write to the file.
		FileOutputStream fos = new FileOutputStream(FILENAME);

		// Create the Object Stream that will write the model through the file stream to
		// file.
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		// Write the model's state to file.
		this.model.storeModel(oos);

	}

	/**
	 * Creates the layout to display the available podcasts
	 * 
	 * @return flow The flowpane that the podcasts are stored in
	 * @author Sara Grimes, seanomeara02
	 */
	private VBox displayPodcasts() {
		List<Podcast> podcasts = controller.getPodcasts();
		VBox podMenu = new VBox();
		Label podHeader = makeLabel("Available Podcasts", null, 20.0, null, false, 0, 0);
		podHeader.getStyleClass().add("border");
		VBox.setMargin(podHeader, new Insets(10, 10, 5, 10));
		VBox podList = new VBox();
		VBox.setMargin(podList, new Insets(5, 10, 10, 10));
		podMenu.getChildren().addAll(podHeader, podList);
		podList.setPrefWidth(200);
		for (int i = 0; i < podcasts.size(); i++) {
			Podcast thisPod = podcasts.get(i);
			Label name = makeLabel(thisPod.getTitle(), null, 0, null, false, 0, 0);
			List<Episode> episodes = thisPod.getEpisodes();
			name.setOnMouseClicked((event) -> {
				deselect(podList, null);
				name.getStyleClass().clear();
				name.getStyleClass().add("selected");
				displayEpisodes(name.getText(), episodes);
				VBox imageBox = (VBox) border.getRight();
				ImageView podImage = (ImageView) imageBox.getChildren().get(0);
				podImage.setImage(new Image(thisPod.getImage().toString()));
				Label podLabel = (Label) imageBox.getChildren().get(1);
				podLabel.setText(thisPod.getDescription());
				podLabel.getStyleClass().addAll("border");
			});
			podList.getChildren().add(name);
			name.setTextFill(Color.WHITE);
		}
		return podMenu;
	}

	/**
	 * Deselects the podcast or episodes after a new one has been selected
	 * 
	 * @param list  A list of available podcasts
	 * @param list2 A list of available episodes
	 * @author Sara Grimes
	 */
	private void deselect(VBox list, FlowPane list2) {
		if (list != null) {
			for (int i = 0; i < list.getChildren().size(); i++) {
				list.getChildren().get(i).getStyleClass().clear();
				list.getChildren().get(i).getStyleClass().add("deselected");
			}
		} else {
			for (int i = 0; i < list2.getChildren().size(); i++) {
				HBox box = (HBox) list2.getChildren().get(i);
				box.getChildren().get(0).getStyleClass().clear();
				box.getChildren().get(0).getStyleClass().add("deselected");
			}
		}
	}

	/**
	 * Displays the available episodes for the specified podcast
	 * 
	 * @param podcastName The name of the selected podcast
	 * @param episodes    The list of episodes for the selected podcast
	 * @author Sara Grimes, Garrett Scott, seanomeara02
	 */
	private void displayEpisodes(String podcastName, List<Episode> episodes) {
		ScrollPane scroll = new ScrollPane();
		scroll.setPrefSize(1000, 500);
		scroll.getStyleClass().add("scroll-pane");
		scroll.autosize();
		BorderPane.setMargin(scroll, new Insets(10, 10, 10, 10));
		VBox epMenu = new VBox();
		Label epHeader = makeLabel("Available Episodes for " + podcastName, null, 20.0, null, false, 0, 0);
		epHeader.getStyleClass().add("border");
		VBox.setMargin(epHeader, new Insets(10, 10, 5, 10));
		FlowPane flow = new FlowPane(Orientation.HORIZONTAL);
		VBox.setMargin(flow, new Insets(5, 10, 10, 10));
		epMenu.getChildren().addAll(epHeader, scroll);
		scroll.setContent(flow);
		for (int i = 0; i < episodes.size(); i++) {
			HBox epInfo = new HBox();
			Label name = makeLabel(episodes.get(i).getTitle(), null, 0, null, true, 200, 30);
			HBox.setMargin(name, new Insets(5, 10, 5, 5));
			name.setOnMouseClicked((event) -> {
				deselect(null, flow);
				name.getStyleClass().clear();
				name.getStyleClass().add("selected");
				controller.changeEp(podcastName, name.getText());
				if (this.mp != null) {
					this.mp.dispose();
				}
//				this.mp.dispose();
				this.mp = controller.getMediaPlayer();
				this.mv = new MediaView(this.mp);
				((Group) this.scene.getRoot()).getChildren().add(this.mv);
			});
			Label desc = makeLabel(episodes.get(i).getDescription(), null, 0, null, true, 390, 50);
			HBox.setMargin(desc, new Insets(5, 5, 5, 10));
			epInfo.getChildren().addAll(name, desc);
			flow.getChildren().add(epInfo);
		}
		border.setCenter(epMenu);
	}

	/**
	 * Creates the container for the Podcast Image and description.
	 * 
	 * @return the VBox containing the Podcast Image and description.
	 * @author seanomeara02
	 */
	private VBox makeImageBox() {
		VBox imageBox = new VBox();

		File imgFile = new File("localFiles/Powerful_Podcast_Logo.jpg");
		ImageView podImage = new ImageView(new Image(imgFile.toURI().toString()));
		podImage.setFitHeight(300);
		podImage.setFitWidth(300);
		BorderPane.setMargin(imageBox, new Insets(0, 20, 20, 20));

		Label podDesc = makeLabel(
				"Welcome to the Powerful Podcast Player brought to you by The Powerful"
				+ " CSC335 Experience! Select a podcast to get started!",
				null, 0, null, true, 300, 150);
		podDesc.getStyleClass().add("border");
		VBox.setMargin(podDesc, new Insets(10, 0, 0, 0));

		imageBox.getChildren().addAll(podImage, podDesc);

		return imageBox;
	}

	/**
	 * Creates the BorderPane containing the play, pause, fast forward, and rewind
	 * buttons and the labels containing the information about the current Episode.
	 * 
	 * @return the constructed BorderPane.
	 * @author seanomeara02
	 */
	private BorderPane makePlayBar() {
		BorderPane playBar = new BorderPane();

		Group buttons = new Group();
		buttons.getChildren().addAll(getPlayPause().get(0), getPlayPause().get(1), getFastForward(), getRewind());

		Label podTitle = new Label();
		podTitle.setTextFill(Color.WHITE);

		Label epTitle = new Label();
		epTitle.setTextFill(Color.WHITE);

		VBox podLabel = new VBox();
		podLabel.getChildren().addAll(podTitle, epTitle);

		BorderPane.setAlignment(podLabel, Pos.CENTER_LEFT);
		BorderPane.setMargin(podLabel, new Insets(10, 10, 10, 10));
		BorderPane.setMargin(buttons, new Insets(10, 10, 10, 10));

		podLabel.setPrefWidth(400);

		Label padding = new Label();
		padding.setPrefWidth(400);

		playBar.setLeft(podLabel);
		playBar.setCenter(buttons);
		playBar.setRight(padding);

		BorderPane.setMargin(playBar, new Insets(75, 25, 25, 25));

		return playBar;
	}

	/**
	 * Creates the fast forward button and returns it
	 * 
	 * @return fastForward A group containing polygons that make up the fast forward
	 *         button
	 * @author Sara Grimes
	 */
	private Node getFastForward() {
		Group fastForward = new Group();
		Polygon fast1 = new Polygon();
		Polygon fast2 = new Polygon();
		fast1.getPoints().setAll(new Double[] { 0.0, 0.0, 25.0, 15.0, 0.0, 30.0 });
		fast2.getPoints().setAll(new Double[] { 0.0, 0.0, 25.0, 15.0, 0.0, 30.0 });
		fast1.setTranslateX(380.0);
		fast1.setTranslateY(37.0);
		fast1.setFill(Color.WHITE);
		fast2.setTranslateX(360.0);
		fast2.setTranslateY(37.0);
		fast2.setFill(Color.WHITE);
		fast1.setOnMouseClicked((event) -> {
			controller.fastForwardPodcast();
		});
		fast2.setOnMouseClicked((event) -> {
			controller.fastForwardPodcast();
		});
		fastForward.getChildren().add(fast1);
		fastForward.getChildren().add(fast2);
		return fastForward;
	}

	/**
	 * Creates the rewind button and returns it
	 * 
	 * @return rewind A group containing polygons that make up the rewind button
	 * @author Sara Grimes
	 */
	private Node getRewind() {
		Group rewind = new Group();
		Polygon re1 = new Polygon();
		Polygon re2 = new Polygon();
		re1.getPoints().setAll(new Double[] { 25.0, 0.0, 0.0, 15.0, 25.0, 30.0 });
		re2.getPoints().setAll(new Double[] { 25.0, 0.0, 0.0, 15.0, 25.0, 30.0 });
		re1.setTranslateX(140.0);
		re1.setTranslateY(37.0);
		re1.setFill(Color.WHITE);
		re2.setTranslateX(160.0);
		re2.setTranslateY(37.0);
		re2.setFill(Color.WHITE);
		re1.setOnMouseClicked((event) -> {
			controller.rewindPodcast();
		});
		re2.setOnMouseClicked((event) -> {
			controller.rewindPodcast();
		});
		rewind.getChildren().add(re1);
		rewind.getChildren().add(re2);
		return rewind;
	}

	/**
	 * Creates the play and pause buttons and returns them
	 * 
	 * @return nodes A list container the play and pause buttons
	 * @author Sara Grimes
	 */
	private List<Node> getPlayPause() {
		Rectangle stop = new Rectangle();
		Polygon play = new Polygon();
		play.getPoints().setAll(new Double[] { 0.0, 0.0, 45.0, 25.0, 0.0, 50.0 });
		play.setTranslateX(220.0);
		play.setTranslateY(30.0);
		play.setFill(Color.WHITE);
		play.setOnMouseClicked((event) -> {
			controller.startPlayPodcast();
		});
		stop.setX(0);
		stop.setY(0);
		stop.setWidth(45);
		stop.setHeight(45);
		stop.setTranslateX(280.0);
		stop.setTranslateY(30.0);
		stop.setFill(Color.WHITE);
		stop.setOnMouseClicked((event) -> {
			controller.pausePodcast();
		});
		List<Node> nodes = new ArrayList<Node>();
		nodes.add(play);
		nodes.add(stop);
		return nodes;
	}

	/**
	 * Creates a Label with the specified text, font, and size, with font color
	 * white.
	 * 
	 * @param text  The text to be made into a label
	 * @param font  The font to be used
	 * @param size  The size of the font
	 * @param color The color of the text
	 * @param wrap  If the texts needs to wrap
	 * @param w     The width for the label
	 * @param h     The height for the label
	 * 
	 * @return the constructed Label.
	 * @author Sara Grimes, seanomeara02
	 */
	private Label makeLabel(String text, String font, double size,
			Color color, boolean wrap, double w, double h) {
		if (font == null || font == "")
			font = "Verdana";
		if (size == 0)
			size = 12.0;
		if (color == null)
			color = Color.WHITE;
		Label title = new Label(text);
		title.setFont(new Font(font, size));
		title.setTextFill(color);
		title.setWrapText(wrap);
		if (w != 0 && h != 0)
			title.setPrefSize(w, h);
		return title;
	}

	/**
	 * Updates the labels indicating which Podcast and Episode is being played
	 * currently in response to the model.
	 * 
	 * @param o  is a reference to the model, which is ignored.
	 * @param ep is the Episode chosen by the user.
	 * @author seanomeara02
	 */
	@Override
	public void update(Observable o, Object ep) {
		Episode episode = (Episode) ep;
		BorderPane playBar = (BorderPane) border.getBottom();
		VBox podLabel = (VBox) playBar.getLeft();

		Label podTitle = (Label) podLabel.getChildren().get(0);
		podTitle.setText(episode.getPodcast().getTitle());

		Label epTitle = (Label) podLabel.getChildren().get(1);
		epTitle.setText(episode.getTitle());
		podLabel.getStyleClass().add("border");
		;
	}

}