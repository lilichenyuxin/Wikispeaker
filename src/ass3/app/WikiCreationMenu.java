package ass3.app;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import ass3.app.tasks.CreateAudioFileTask;
import ass3.app.tasks.CreateAudioFileTask.Synthesiser;
import ass3.app.tasks.CreateCreationTask;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class WikiCreationMenu {
	
	private static ListView<AudioFileHBoxCell> audioFileListView = new ListView<>();
	private static ListView<AudioFileHBoxCell> creationAudioFileListView = new ListView<>();
	private static MediaPlayer currentAudioPreview = null;	
	private static MainMenu mainMenu;
	
	public static void createWindow(MainMenu mainMenu, Stage parentStage, String wikiTerm, String wikiText) {
		
		WikiCreationMenu.mainMenu = mainMenu;
				
		// DELETE PRE-EXISTING AUDIO FILES
		
		String cmd = "rm audio/*";
		ProcessBuilder pb = new ProcessBuilder("bash", "-c", cmd);
		try {
			pb.start().waitFor();
		} catch (InterruptedException | IOException e1) {
			e1.printStackTrace();
		}
		
		// GUI LAYOUTS
		
		VBox rootLayout = new VBox(10);
		rootLayout.setPadding(new Insets(10));
				
		HBox menuLayout = new HBox(10);
		VBox.setVgrow(menuLayout, Priority.ALWAYS);
		
		// EDITOR LAYOUT //
		
		VBox editorLayout = new VBox(10);
		HBox.setHgrow(editorLayout, Priority.ALWAYS);
		
		HBox utilityBar = new HBox(10);
		
		ObservableList<String> synthesiserOptions = FXCollections.observableArrayList(
			Synthesiser.Festival.name(),
			Synthesiser.eSpeak.name()
		);
		ComboBox synthesiserDropdown = new ComboBox(synthesiserOptions);
		synthesiserDropdown.getSelectionModel().selectFirst();
		synthesiserDropdown.setMinWidth(Control.USE_PREF_SIZE);
		
		ObservableList<String> voiceOptions = FXCollections.observableArrayList(
			Synthesiser.valueOf((String) synthesiserDropdown.getSelectionModel().getSelectedItem()).getVoiceNames()
		);
		ComboBox voiceDropdown = new ComboBox(voiceOptions);
		voiceDropdown.getSelectionModel().selectFirst();
		voiceDropdown.setMinWidth(Control.USE_PREF_SIZE);
		
		synthesiserDropdown.getSelectionModel().selectedItemProperty().addListener((c) -> {
			voiceDropdown.setItems(
				FXCollections.observableArrayList(
					Synthesiser.valueOf((String) synthesiserDropdown.getSelectionModel().getSelectedItem()).getVoiceNames()
				)
			);
			voiceDropdown.getSelectionModel().selectFirst();
		});
		
		Button previewButton = new Button("Preview selection");
		previewButton.setMinWidth(Control.USE_PREF_SIZE);
		previewButton.setDisable(true);
		
		TextField audioNameField = new TextField();
		audioNameField.setPromptText("Audio file name..");
		
		Button saveButton = new Button("Create audio file");
		saveButton.setMinWidth(Control.USE_PREF_SIZE);
		saveButton.setDisable(true);
				
		Pane spacer = new Pane();
		HBox.setHgrow(spacer, Priority.ALWAYS);
		
		utilityBar.getChildren().setAll(synthesiserDropdown, voiceDropdown, spacer, previewButton, audioNameField, saveButton);
		
		TextArea wikiTextArea = new TextArea();
		wikiTextArea.setText(wikiText);
		wikiTextArea.setStyle("-fx-text-alignment: justify");
		wikiTextArea.setWrapText(true);
		wikiTextArea.setMinHeight(400);
		VBox.setVgrow(wikiTextArea, Priority.ALWAYS);
		
		// displays number of characters the user has selected
		HBox footer = new HBox(7);
		
		Text descriptiveText = new Text("Number of words selected:");
		descriptiveText.setFill(Color.web("333333"));
		Text numCharactersText = new Text("0");
		numCharactersText.setFill(Color.RED);
		
		spacer = new Pane();
		HBox.setHgrow(spacer, Priority.ALWAYS);
		
		// progress bar and label used to update user as to the progress of the current operation
		
		ProgressBar progressBar = new ProgressBar(0);
		progressBar.setMaxHeight(15);
		Label progressLabel = new Label("");
		
		footer.getChildren().setAll(descriptiveText, numCharactersText, spacer, progressLabel, progressBar);
		
		// colour num words selected text according to whether valid number of words
		// also disable preview / save buttons as necessary
		wikiTextArea.selectedTextProperty().addListener((textProperty, oldValue, newValue) -> {
			
			String text = newValue.trim().replace("\n",  "");
			String[] words = text.split(" ");
			int numWords = words.length - ((text.length() == 0) ? 1 : 0);  // no text counts as one word for some reason (newline?)
			
			boolean invalidNumWords = numWords == 0 || numWords > 40;
			
			saveButton.setDisable(invalidNumWords || audioNameField.getText().length() == 0);
			previewButton.setDisable(invalidNumWords);
			
			if (invalidNumWords) {
				numCharactersText.setFill(Color.RED);
			} else {
				numCharactersText.setFill(Color.web("333333"));
			}
			
			numCharactersText.setText("" + numWords + ((numWords > 40) ? " (too many words)" : ""));
			
		});
		
		audioNameField.textProperty().addListener((observable, oldValue, newValue) -> {
			
			String text = wikiTextArea.getSelectedText().trim().replace("\n",  "");
			String[] words = text.split(" ");
			int numWords = words.length - ((text.length() == 0) ? 1 : 0);  // no text counts as one word for some reason (newline?)
			
			saveButton.setDisable(newValue.length() == 0 || numWords == 0 || numWords > 40);
			
		});
		
		previewButton.setOnAction( (e) -> {
			
			if (currentAudioPreview != null) {
				// prevent overlapping audio
				currentAudioPreview.stop();
			}
			
			previewButton.setDisable(true);
			
			String text = wikiTextArea.getSelectedText();
			Task<String> createAudioTask = new CreateAudioFileTask(
					(String) synthesiserDropdown.getSelectionModel().getSelectedItem(),
					(String) voiceDropdown.getSelectionModel().getSelectedItem(),
					text, null
			);
			
			progressBar.progressProperty().bind(createAudioTask.progressProperty());
			progressLabel.setText("Generating audio preview...");
			
			createAudioTask.setOnSucceeded((e_) -> {
				
				progressBar.progressProperty().unbind();
				progressLabel.setText("");
				
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
				
				ProcessBuilder builder = new ProcessBuilder("test", "-e", "audio/" + audioNameField.getText() + ".wav");
				int fileExists = builder.start().waitFor();
								
				if (fileExists == 0) {
					Alert alert = new Alert(AlertType.CONFIRMATION, "Would you like to overwrite it?", 
																	ButtonType.NO, ButtonType.YES);
					alert.setHeaderText("File with that name already exists");
					alert.showAndWait();
					if (alert.getResult() != ButtonType.YES) {
						return;
					}
				}
				
			} catch (InterruptedException | IOException ex) {
				ex.printStackTrace();
			}
			
			Task<String> createAudioTask = new CreateAudioFileTask(
					(String) synthesiserDropdown.getSelectionModel().getSelectedItem(),
					(String) voiceDropdown.getSelectionModel().getSelectedItem(),
					wikiTextArea.getSelectedText(), audioNameField.getText()
			);
			
			progressLabel.setText("Saving audio file...");
			progressBar.progressProperty().bind(createAudioTask.progressProperty());
			
			createAudioTask.setOnSucceeded((e_) -> {
				progressLabel.setText("");
				progressBar.progressProperty().unbind();
				progressBar.setProgress(0);
				updateAudioFileList();
			});
			
			(new Service<String>() {
				@Override 
				public Task<String> createTask() {
					return createAudioTask;
				}
			}).start();
		
			
		});
		
		editorLayout.getChildren().setAll(utilityBar, wikiTextArea, footer);
		
		// END EDITOR LAYOUT //
		
		Separator horizSeparator = new Separator(Orientation.VERTICAL);
		horizSeparator.setPadding(new Insets(0, 5, 0, 5));
			
		// CREATION MENU LAYOUT //
		
		VBox creationLayout = new VBox(10);
		
		creationLayout.setMinWidth(500);
		creationLayout.setPrefWidth(500);
		creationLayout.setMaxWidth(500);
		VBox.setVgrow(creationLayout, Priority.ALWAYS);
		
		VBox.setVgrow(audioFileListView, Priority.ALWAYS);
		updateAudioFileList();
		audioFileListView.setSelectionModel(new NoSelectionModel<>());
		audioFileListView.setFocusTraversable(false);
		
		VBox.setVgrow(creationAudioFileListView, Priority.ALWAYS);
		creationAudioFileListView.setItems(FXCollections.observableArrayList());
		creationAudioFileListView.setSelectionModel(new NoSelectionModel<>());
		creationAudioFileListView.setFocusTraversable(false);
				
		TextField creationNameField = new TextField();
		creationNameField.setPromptText("Creation name..");
		HBox.setHgrow(creationNameField, Priority.ALWAYS);
	
		ObservableList<Integer> numImagesOptions = FXCollections.observableArrayList();
		for (int i = 1; i <= 10; i++) {
			numImagesOptions.add(i);
		}
		ComboBox<Integer> numImagesDropdown = new ComboBox<>(numImagesOptions);
		numImagesDropdown.getSelectionModel().selectFirst();
		
		Button saveCreationButton = new Button("Save Creation");
		saveCreationButton.setOnAction((e) -> {
			
			String creationName = creationNameField.getText();
			
			// check if creation with specified name exists
			try {
				
				ProcessBuilder builder = new ProcessBuilder("test", "-e", "creations/" + creationName + ".mp4");
				int fileExists = builder.start().waitFor();
								
				if (fileExists == 0) {
					Alert alert = new Alert(AlertType.CONFIRMATION, "Would you like to overwrite it?", 
																	ButtonType.NO, ButtonType.YES);
					alert.setHeaderText("Creation with that name already exists");
					alert.showAndWait();
					if (alert.getResult() != ButtonType.YES) {
						return;
					}
				}
				
			} catch (InterruptedException | IOException ex) {
				ex.printStackTrace();
			}
			
			
			List<String> audioFilePaths = new ArrayList<String>();
			for (AudioFileHBoxCell cell : creationAudioFileListView.getItems()) {
				audioFilePaths.add(cell.getAudioFileName());
			}
			
			int numImages = numImagesDropdown.getSelectionModel().getSelectedItem();
			
			Task<String> createCreationTask = new CreateCreationTask("creations/" + creationName + ".mp4", audioFilePaths, wikiTerm, numImages);
			progressLabel.textProperty().bind(createCreationTask.messageProperty());
			progressBar.progressProperty().bind(createCreationTask.progressProperty());
			
			createCreationTask.setOnSucceeded((e_) -> {
				progressLabel.textProperty().unbind();
				progressBar.progressProperty().unbind();
				progressBar.setProgress(0);
				Alert alert = new Alert(AlertType.INFORMATION, "Creation '" + creationName + "' created successfully.");
				alert.show();
				mainMenu.updateCreationList();
			});
			
			Service<String> creationService = new Service<String>() {
				
				@Override
				public Task<String> createTask() {
					return createCreationTask;
				}
				
			};
			
			creationService.start();
			
			
		});
		saveCreationButton.setDisable(true);
		
		creationAudioFileListView.getItems().addListener((new ListChangeListener<AudioFileHBoxCell>() {

			@Override
			public void onChanged(Change<? extends AudioFileHBoxCell> arg0) {
				
				boolean disable = creationAudioFileListView.getItems().isEmpty() || creationNameField.getText().length() == 0;
				saveCreationButton.setDisable(disable);
				
			}
			
		}));
		
		creationNameField.setOnKeyReleased((e) -> {
			if (e.getCode() == KeyCode.ENTER && !saveCreationButton.isDisabled()) {
				saveCreationButton.fire();
			}
		});
		
		// if name field empty don't allow them to click save creation
		creationNameField.textProperty().addListener((c) -> {
			
			boolean disable = creationAudioFileListView.getItems().isEmpty() || creationNameField.getText().length() == 0;
			saveCreationButton.setDisable(disable);
		
		});
		
		HBox saveLayout = new HBox(10);
		saveLayout.getChildren().setAll(creationNameField, numImagesDropdown, saveCreationButton);
		
		creationLayout.getChildren().setAll(creationAudioFileListView, saveLayout);
				
		// END CREATION MENU LAYOUT //
		
		menuLayout.getChildren().setAll(editorLayout, horizSeparator, audioFileListView, creationLayout);
		rootLayout.getChildren().setAll(menuLayout);
		
		Scene scene = new Scene(rootLayout);
		
		Stage window = new Stage();
		window.initOwner(parentStage);
		window.setScene(scene);
		window.sizeToScene();
		window.show();
		window.setMinWidth(window.getWidth());
		window.setMinHeight(window.getHeight());
		
	}
	
	private static void updateAudioFileList() {
				
		String cmd = "ls audio/ | grep \".wav\"";
		try {
			
			ProcessBuilder builder = new ProcessBuilder("bash", "-c", cmd);
			Process process = builder.start();
			process.waitFor();
			
			InputStream stdout = process.getInputStream();
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stdout));
			
			ObservableList<AudioFileHBoxCell> list = audioFileListView.getItems();
			
			String fileName;
			List<String> fileNames = new ArrayList<String>();
			while ((fileName = bufferedReader.readLine()) != null) {
				fileNames.add(fileName);
			}
			
			ObservableList<AudioFileHBoxCell> updatedList = FXCollections.observableArrayList();
			for (AudioFileHBoxCell audioCell : list) {
				if (fileNames.contains(audioCell.getAudioFileName())) {
					updatedList.add(audioCell);
				}
			}
			
			for (String audioFileName : fileNames) {
				
				boolean exists = false;
				for (AudioFileHBoxCell audioCell : updatedList) {
					if (audioCell.getAudioFileName().equals(audioFileName)) {
						exists = true;
						break;
					}
				}
				
				if (!exists) {
					updatedList.add(new AddableAudioFileHBoxCell(audioFileListView, creationAudioFileListView, audioFileName));
				}
				
			}
			
			audioFileListView.setItems(updatedList);
			
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	
	public static class AudioFileHBoxCell extends HBox {
		
		protected Label nameLabel;
		protected Pane spacer;
		protected Button playButton, deleteButton;
		protected ListView<AudioFileHBoxCell> _listView;
			
		public AudioFileHBoxCell(ListView<AudioFileHBoxCell> listView, String audioFileName) {
			
			super(8);
			_listView = listView;
			setAlignment(Pos.CENTER);
			setPadding(new Insets(3));
			
			nameLabel = new Label(audioFileName);
			
			spacer = new Pane();
			HBox.setHgrow(spacer,  Priority.ALWAYS);
			
			playButton = new Button();
			playButton.setGraphic(new ImageView(mainMenu.getImageManager().getImage("play")));
			playButton.setOnAction((e) -> {
				
				if (currentAudioPreview != null) {
					currentAudioPreview.stop();
				}
				
				Media audio = new Media(new File("audio", audioFileName).toURI().toString());
				currentAudioPreview = new MediaPlayer(audio);
				currentAudioPreview.play();
				
			});
			playButton.setMinSize(playButton.USE_PREF_SIZE, playButton.USE_PREF_SIZE);
			
			deleteButton = new Button();
			deleteButton.setGraphic(new ImageView(mainMenu.getImageManager().getImage("delete")));
			deleteButton.setOnAction((e) -> {
				
				Alert confirmDialog = new Alert(AlertType.CONFIRMATION);
				confirmDialog.setHeaderText("Are you sure you want to delete this audio file?");
				confirmDialog.setContentText("Are you sure you want to delete \"" + audioFileName + "?");
				confirmDialog.setTitle("Confirm deletion");
				
				Optional<ButtonType> response = confirmDialog.showAndWait();
				if (!response.get().equals(ButtonType.OK)) {
					return;
				}
				
				try {
					
					ProcessBuilder pb = new ProcessBuilder("rm", "audio/" + audioFileName);
					pb.start().waitFor();
					updateAudioFileList();
					
				} catch (InterruptedException | IOException ex) {
					ex.printStackTrace();
				}
				
			});
			deleteButton.setOnMouseEntered((e) -> e.consume());

									
			getChildren().addAll(nameLabel, spacer, playButton, deleteButton);
			
		}
		
		@Override
		public boolean equals(Object o) {
			
			if (!(o instanceof AudioFileHBoxCell)) {
				return false;
			}
			
			AudioFileHBoxCell other = (AudioFileHBoxCell) o;
			return other.getAudioFileName().equals(this.getAudioFileName());
			
		}
		
		public String getAudioFileName() {
			return nameLabel.getText();
		}
		
		public void setAudioFileName(String name) {
			nameLabel.setText(name);
		}
		
	}
	
	public static class AddableAudioFileHBoxCell extends AudioFileHBoxCell {
		
		private Button addButton;

		public AddableAudioFileHBoxCell(ListView<AudioFileHBoxCell> listView, ListView<AudioFileHBoxCell> listViewToAddTo, String audioFileName) {
			
			super(listView, audioFileName);
			
			addButton = new Button();
			addButton.setGraphic(new ImageView(mainMenu.getImageManager().getImage("add")));
			
			addButton.setOnAction((e) -> {
				
				for (AudioFileHBoxCell cell : listViewToAddTo.getItems()) {
					if (cell.getAudioFileName().equals(audioFileName)) {
						return;
					}
				}
				
				AudioFileHBoxCell cell = new ShiftableAudioFileHBoxCell(listViewToAddTo, audioFileName);
				listViewToAddTo.getItems().add(cell);
					
			});
			
			this.getChildren().add(addButton);
			
		}
		
	}
	
	// class to represent each cell of the list view
	public static class ShiftableAudioFileHBoxCell extends AudioFileHBoxCell {
		
		private VBox shiftButtonContainer;
		private Button shiftUpButton, shiftDownButton;   // allow rearranging of audio file list
				
		private final Image shiftUpIcon   = new Image(getClass().getResourceAsStream("resources/shiftUpIcon.png"));
		private final Image shiftDownIcon = new Image(getClass().getResourceAsStream("resources/shiftDownIcon.png"));
		
		public ShiftableAudioFileHBoxCell(ListView<AudioFileHBoxCell> listView, String audioFileName) {
			
			super(listView, audioFileName);
			
			deleteButton.setOnAction((e) -> {
				_listView.getItems().remove(this);
			});
						
			shiftButtonContainer = new VBox(5);
			shiftUpButton = createShiftButton(-1);
			shiftDownButton = createShiftButton(1);
			
			shiftButtonContainer.getChildren().addAll(shiftUpButton, shiftDownButton);	
			
			ObservableList<Node> tempList = FXCollections.observableArrayList(
					shiftButtonContainer
			);
			tempList.addAll(this.getChildren());
			this.getChildren().setAll(tempList);
				
		}
		
		private void shift(int dir) {
			
			ObservableList<AudioFileHBoxCell> items = _listView.getItems();
			
			int thisIndex = items.indexOf(this),
				otherIndex = thisIndex + dir;
			
			if (otherIndex < 0 || otherIndex >= items.size()) {
				return;
			}
			
			String otherName = items.get(otherIndex).getAudioFileName(),
				   thisName = items.get(thisIndex).getAudioFileName();
			
			
			items.get(otherIndex).setAudioFileName(thisName);
			items.get(thisIndex).setAudioFileName(otherName);
	
		}
		
		private Button createShiftButton(int dir) {
						
			Button shiftButton = new Button();
			shiftButton.setOnAction((e) -> {
				shift(dir);
			});
			shiftButton.setGraphic(new ImageView((dir < 0) ? shiftUpIcon : shiftDownIcon));
			shiftButton.setPadding(new Insets(3, 4, 3, 4));
			
			return shiftButton;
			
		}
		
	}
	
	// simply a selection model class to prevent any list item being selected in the list view
	public static class NoSelectionModel<T> extends MultipleSelectionModel<T> {

	    @Override
	    public ObservableList<Integer> getSelectedIndices() {
	        return FXCollections.emptyObservableList();
	    }

	    @Override
	    public ObservableList<T> getSelectedItems() {
	        return FXCollections.emptyObservableList();
	    }

	    @Override
	    public void selectIndices(int index, int... indices) {
	    }

	    @Override
	    public void selectAll() {
	    }

	    @Override
	    public void selectFirst() {
	    }

	    @Override
	    public void selectLast() {
	    }

	    @Override
	    public void clearAndSelect(int index) {
	    }

	    @Override
	    public void select(int index) {
	    }

	    @Override
	    public void select(T obj) {
	    }

	    @Override
	    public void clearSelection(int index) {
	    }

	    @Override
	    public void clearSelection() {
	    }

	    @Override
	    public boolean isSelected(int index) {
	        return false;
	    }

	    @Override
	    public boolean isEmpty() {
	        return true;
	    }

	    @Override
	    public void selectPrevious() {
	    }

	    @Override
	    public void selectNext() {
	    }
	}
	
}
