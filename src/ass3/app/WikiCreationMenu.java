package ass3.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import ass3.app.tasks.CreateAudioFileTask;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;

import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class WikiCreationMenu {
	
	private static MediaPlayer currentAudioPreview = null;
	
	private static VBox scrollContentPane;
	
	public static void createWindow(Stage parentStage) {
				
		VBox rootLayout = new VBox(10);
		rootLayout.setPadding(new Insets(10));
				
		HBox menuLayout = new HBox(10);
		VBox.setVgrow(menuLayout, Priority.ALWAYS);
		
		// EDITOR LAYOUT //
		
		VBox editorLayout = new VBox(10);
		HBox.setHgrow(editorLayout, Priority.ALWAYS);
		
		HBox utilityBar = new HBox(10);
		
		ObservableList<String> synthesiserOptions = FXCollections.observableArrayList(
			"Festival",
			"eSpeak",
			"TTS",
			"yeet"
		);
		ComboBox synthesiserDropdown = new ComboBox(synthesiserOptions);
		synthesiserDropdown.getSelectionModel().selectFirst();
		synthesiserDropdown.setMinWidth(Control.USE_PREF_SIZE);
		
		ObservableList<String> voiceOptions = FXCollections.observableArrayList(
			"Brian",
			"Britney",
			"Sarah",
			"greg"
		);
		ComboBox voiceDropdown = new ComboBox(voiceOptions);
		voiceDropdown.getSelectionModel().selectFirst();
		voiceDropdown.setMinWidth(Control.USE_PREF_SIZE);
		
		Button previewButton = new Button("Preview selection");
		previewButton.setMinWidth(Control.USE_PREF_SIZE);
		previewButton.setDisable(true);
		
		TextField audioNameField = new TextField();
		audioNameField.setPromptText("Audio file name..");
		
		Button saveButton = new Button("Create audio file");
		saveButton.setMinWidth(Control.USE_PREF_SIZE);
		saveButton.setDisable(true);
		
		saveButton.disableProperty().addListener((observableValue, oldValue, newValue) -> {
			
			if (newValue == false && audioNameField.getText().length() == 0) {
				// can only change to true if audio name field is nonempty
				saveButton.setDisable(true);
			}
			
		});
		
		
				
		Pane spacer = new Pane();
		HBox.setHgrow(spacer, Priority.ALWAYS);
		
		utilityBar.getChildren().setAll(synthesiserDropdown, voiceDropdown, spacer, previewButton, audioNameField, saveButton);
		
		TextArea wikiTextArea = new TextArea();
		wikiTextArea.setText(dummyText);
		wikiTextArea.setWrapText(true);
		wikiTextArea.setMinHeight(400);
		VBox.setVgrow(wikiTextArea, Priority.ALWAYS);
		
		wikiTextArea.selectedTextProperty().addListener((textProperty, oldValue, newValue) -> {
			if (newValue.length() == 0) {
				saveButton.setDisable(true);
				previewButton.setDisable(true);
			} else {
				saveButton.setDisable(false);
				previewButton.setDisable(false);
			}
		});
		
		audioNameField.textProperty().addListener((observable, oldValue, newValue) -> {
			
			if (newValue.length() == 0) {
				saveButton.setDisable(true);
			} else if (wikiTextArea.getSelectedText().length() != 0) {
				saveButton.setDisable(false);
			}
			
		});
		
		previewButton.setOnAction( (e) -> {
			
			if (currentAudioPreview != null) {
				// prevent overlapping audio
				currentAudioPreview.stop();
			}
			
			previewButton.setDisable(true);
			
			String text = wikiTextArea.getSelectedText();
			Task<String> createAudioTask = new CreateAudioFileTask(text, null);
			
			createAudioTask.setOnSucceeded((e_) -> {
				
				previewButton.setDisable(false);
				
				// preview the audio with embedded player
				
				String filePath = (String) createAudioTask.getValue();
				Media audio = new Media(new File(filePath).toURI().toString());
				currentAudioPreview = new MediaPlayer(audio);
				currentAudioPreview.play();
				
			});
			
			createAudioTask.setOnFailed((e_) -> {
				previewButton.setDisable(false);
			});
			
			Service<String> service = new Service<String>() {
				
				@Override
				protected Task<String> createTask() {
					
					return createAudioTask;
					
				}
				
			};
			service.start();
			
		});
		
		saveButton.setOnAction((e) -> {
			
			// check if file with specified name exists
			try {
				
				ProcessBuilder builder = new ProcessBuilder("test", "-e", "audio/" + audioNameField.getText());
				int fileExists = builder.start().waitFor();
				
				if (fileExists == 0) {
					Alert alert = new Alert(AlertType.CONFIRMATION, "Would you like to overwrite it?", 
																	ButtonType.YES, ButtonType.NO);
					alert.setHeaderText("File with that name already exists");
					alert.showAndWait();
					if (alert.getResult() == ButtonType.YES) {
						
						saveAudioFile(wikiTextArea.getSelectedText(), audioNameField.getText());
						
					}
				}
				
			} catch (InterruptedException | IOException ex) {
				ex.printStackTrace();
			}
		
			
		});
		
		editorLayout.getChildren().setAll(utilityBar, wikiTextArea);
		
		// END EDITOR LAYOUT //
		
		Separator horizSeparator = new Separator(Orientation.VERTICAL);
		horizSeparator.setPadding(new Insets(0, 5, 0, 5));
			
		// CREATION MENU LAYOUT //
		
		VBox creationLayout = new VBox(10);
		
		creationLayout.setMinWidth(350);
		creationLayout.setPrefWidth(350);
		creationLayout.setMaxWidth(350);
		VBox.setVgrow(creationLayout, Priority.ALWAYS);
		
		ScrollPane audioScrollPane = new ScrollPane();
		VBox.setVgrow(audioScrollPane, Priority.ALWAYS);
		
		scrollContentPane = new VBox(10);
		scrollContentPane.setPadding(new Insets(10));
		
		audioScrollPane.setContent(scrollContentPane);
		audioScrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);
		VBox.setVgrow(audioScrollPane, Priority.ALWAYS);
		
		TextField creationNameField = new TextField();
		creationNameField.setPromptText("Creation name..");
		HBox.setHgrow(creationNameField, Priority.ALWAYS);
		
		Button saveCreationButton = new Button("Save Creation");
		
		HBox saveLayout = new HBox(10);
		saveLayout.getChildren().setAll(creationNameField, saveCreationButton);
		
		creationLayout.getChildren().setAll(audioScrollPane, saveLayout);
				
		// END CREATION MENU LAYOUT //
		
		menuLayout.getChildren().setAll(editorLayout, horizSeparator, creationLayout);
		rootLayout.getChildren().setAll(menuLayout);
		
		Scene scene = new Scene(rootLayout);	
		
		Stage window = new Stage();
		window.initOwner(parentStage);
		window.initModality(Modality.APPLICATION_MODAL);
		window.setScene(scene);
		window.sizeToScene();
		window.show();
		window.setMinWidth(window.getWidth());
		window.setMinHeight(window.getHeight());
		
	}
	
	private static void saveAudioFile(String text, String name) {
		
		Task<String> audioTask = new CreateAudioFileTask(text, name);
		audioTask.setOnSucceeded((e) -> {

			updateAudioFileList("");

		});
		
		Service<String> service = new Service<String>() {
			
			@Override
			protected Task<String> createTask() {
				return audioTask;
			}
			
		};
		service.start();
		
	}
	
	private static void updateAudioFileList(String searchTerm) {
		
		String cmd = "ls audio/ | grep \"" + searchTerm + "\"";
		try {
			
			ProcessBuilder builder = new ProcessBuilder("bash", "-c", cmd);
			Process process = builder.start();
			
			InputStream stdout = process.getInputStream();
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stdout));
			
			String fileName;
			while ((fileName = bufferedReader.readLine()) != null) {
				
				fileName = fileName.split(".")[0];  // remove extension
				System.out.println(fileName);
				
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private static HBox createAudioMenuItem(String name, String dateModified) {
		
		Label nameLabel = new Label(name),
			  dateLabel = new Label(dateModified);
		
		VBox labelContainer = new VBox(5);
		labelContainer.getChildren().setAll(nameLabel, dateLabel);
		
		Button playButton = new Button("Play"),
			   deleteButton = new Button("Delete");
		
		Pane spacer = new Pane();
		HBox.setHgrow(spacer, Priority.ALWAYS);
		
		HBox menuItem = new HBox(10);
		menuItem.getChildren().setAll(labelContainer, spacer, playButton, deleteButton);
		
		return menuItem;
		
	}
	
	private static String dummyText = 
			"Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nam finibus placerat nulla, ac pretium est efficitur a. Aliquam ultricies rutrum dignissim. Fusce non purus et dolor tristique rutrum. Aliquam convallis ornare est vitae condimentum. Pellentesque maximus vel urna ut auctor. Ut et lorem eu diam mattis posuere. Donec a aliquam magna, ac tempus orci. Nam a justo sit amet lectus iaculis facilisis. Aliquam enim orci, ultricies vitae nisi vel, tempor maximus urna. Quisque est risus, mattis in lacus ut, tempor faucibus ligula. Maecenas non accumsan nisl, id tincidunt nisl. Donec varius auctor lacus a semper. Vivamus dolor est, volutpat at accumsan vitae, semper a leo. Fusce eget commodo neque. Vivamus efficitur tempor fringilla. Fusce at mattis purus.\n" + 
			"\n" + 
			"Sed consequat lacinia ex nec consequat. Vestibulum a condimentum ligula, quis finibus nulla. Ut sit amet ante nec massa rhoncus fermentum et a tortor. Fusce suscipit justo sed nunc malesuada, nec placerat justo ullamcorper. Nulla rhoncus leo nec ultricies vulputate. Ut bibendum, sem non placerat congue, orci neque ullamcorper neque, in maximus nunc enim in est. Cras non pulvinar arcu. Etiam interdum tempor tristique. Vestibulum ornare iaculis erat a scelerisque. Ut varius mi tellus, sit amet dictum tellus posuere eu. Nulla vitae tincidunt odio, in cursus sem.\n" + 
			"\n" + 
			"Phasellus dictum euismod massa et elementum. Morbi vestibulum congue enim, ut rutrum diam mollis ut. Nulla facilisi. Pellentesque dapibus mollis congue. Curabitur laoreet id libero eget elementum. Nam aliquet risus non massa vestibulum faucibus in et nunc. In porta finibus bibendum. Nunc vel mi turpis. Etiam luctus iaculis aliquam. Ut tincidunt ipsum et magna fringilla, sed convallis sem volutpat.\n" + 
			"\n" + 
			"Nullam congue vitae lorem imperdiet tempor. Pellentesque augue lacus, tempor ut velit vel, tempor dictum tellus. Suspendisse potenti. Quisque dictum tellus vel elit ultricies, at finibus nunc luctus. Nunc maximus cursus mauris quis elementum. Nunc dignissim, odio quis consectetur congue, elit mauris ullamcorper nisl, nec maximus ex ipsum a tortor. Fusce egestas lorem ullamcorper eros mollis, ac commodo augue porttitor. Phasellus accumsan molestie felis ac porta. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; Cras in leo justo. Mauris id magna vitae odio malesuada facilisis id vitae nisi.\n" + 
			"\n" + 
			"Etiam quis egestas turpis, in consequat diam. Donec ut varius nunc. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; Fusce id lorem eu velit porttitor efficitur. In consequat vel risus non porta. Quisque nec diam sed justo pellentesque varius. Proin accumsan porttitor orci. Donec ac odio quis nunc congue venenatis a congue augue.";

}
