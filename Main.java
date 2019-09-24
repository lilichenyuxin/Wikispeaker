package softeng_206.ass2;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import softeng_206.ass2.gui.CreationNamingGUI;
import softeng_206.ass2.gui.CreationSearchGUI;
import softeng_206.ass2.gui.GUITools;
import softeng_206.ass2.gui.SentenceSelectionGUI;

import java.util.ArrayList;
import java.util.List;

public class Main extends Application {

    ImageManager imageManager;

    private Stage window;

    private VBox root;
    private BorderPane topPane;
    private ScrollPane scrollPane;
    private VBox creationsPane;

    private TextField creationSearchBar;
    private Button addCreationButton, refreshCreationsButton;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage window) throws Exception {

        loadImages();
        initGUI(window);

    }

    private void initGUI(Stage stage) {

        window = stage;
        window.setTitle("WikiSpeak");
        window.setMinHeight(300);
        window.setMinWidth(300);

        creationSearchBar = new TextField();
        creationSearchBar.setPromptText("Search..");
        creationSearchBar.textProperty().addListener((observable, oldText, newText) -> {
            loadCreations();
        });
        HBox.setHgrow(creationSearchBar, Priority.ALWAYS);


        addCreationButton = new Button();
        addCreationButton.setGraphic(new ImageView(imageManager.getImage("add")));
        addCreationButton.setOnAction(e -> {
            List<String> sentences = CreationSearchGUI.createSearchWindow(window).showAndReturn();
            if (sentences == null) {
                return;
            }
            String wikiName = sentences.get(sentences.size()-1);
            sentences.remove(sentences.size()-1);
            String text = SentenceSelectionGUI.createSentenceSelectionWindow(window, sentences).showAndReturn();
            if (text == null) {
                return;
            }
            String creationName = CreationNamingGUI.createCreationNamingWindow(window).showAndReturn();
            if (creationName == null) {
                return;
            }

            Service<Void> createCreationService = new CreationCreatorService(text, creationName, wikiName);
            createCreationService.setOnSucceeded(event -> {
                loadCreations();
            });
            createCreationService.start();
        });
        Tooltip addTooltip = new Tooltip("Create new creation");
        addCreationButton.setTooltip(addTooltip);

        refreshCreationsButton = new Button();
        refreshCreationsButton.setGraphic(new ImageView(imageManager.getImage("refresh")));
        refreshCreationsButton.setOnAction(e -> {
            loadCreations();
        });
        Tooltip refreshTooltip = new Tooltip("Refresh creations list");
        refreshCreationsButton.setTooltip(refreshTooltip);

        HBox topPane = new HBox();
        topPane.setSpacing(8);
        topPane.getChildren().addAll(creationSearchBar, addCreationButton, refreshCreationsButton);

        scrollPane = new ScrollPane();
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        creationsPane = new VBox();
        creationsPane.setSpacing(8);
        creationsPane.setPadding(new Insets(10));
        creationsPane.setStyle(
                "-fx-border-width: 1;" +
                "-fx-border-radius: 4;" +
                "-fx-border-color: rgb(170, 170, 170);" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 2, 0, 2, 2);");

        scrollPane.setContent(creationsPane);
        VBox.setVgrow(creationsPane, Priority.ALWAYS);

        loadCreations();

        root = new VBox();
        root.setPadding(new Insets(10));
        root.setSpacing(10);
        root.getChildren().addAll(topPane, creationsPane);

        Scene mainScene = new Scene(root, 400, 400);
        window.setScene(mainScene);
        window.show();

    }

    private void loadCreations() {

        List<Node> creationPanes = new ArrayList<Node>();

        // get creations
        List<Creation> creations = CreationUtils.queryCreations(creationSearchBar.getText(),
                new Creation.AlphabeticalComparator(false));
        for (Creation c : creations) {

            HBox creationPane = new HBox();
            creationPane.setPadding(new Insets(7, 12, 7, 12));
            creationPane.setSpacing(8);
            creationPane.setAlignment(Pos.CENTER);
            creationPane.setStyle("-fx-background-color: rgb(225, 225, 225);" +
                    "-fx-background-radius: 4");

            VBox labelPane = new VBox();
            labelPane.setAlignment(Pos.CENTER_LEFT);

            Label nameLabel = new Label(c.getName());
            Label dateLabel = new Label(c.getFormattedDate());

            labelPane.getChildren().addAll(nameLabel, dateLabel);

            Button playButton = new Button();
            playButton.setGraphic(new ImageView(imageManager.getImage("play")));
            playButton.setOnAction(e -> {
                CreationUtils.playCreation(nameLabel.getText());
            });
            Tooltip playTooltip = new Tooltip("Play creation");
            playButton.setTooltip(playTooltip);


            Button deleteButton = new Button();
            deleteButton.setGraphic(new ImageView(imageManager.getImage("delete")));
            deleteButton.setOnAction(e -> {

                String msg = "Are you sure you want to delete \"" + nameLabel.getText() + "\"?";
                boolean confirmation = GUITools.createConfirmationDialog("Delete \"" + nameLabel.getText() + "\"", msg);
                if (confirmation) {
                    CreationUtils.deleteCreation(nameLabel.getText());
                    loadCreations();
                }

            });
            Tooltip deleteTooltip = new Tooltip("Delete creation");
            deleteButton.setTooltip(deleteTooltip);

            Pane spacer = new Pane();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            creationPane.getChildren().addAll(labelPane, spacer, playButton, deleteButton);
            creationPanes.add(creationPane);

        }

        creationsPane.getChildren().setAll(creationPanes);

    }

    private void loadImages() {

        imageManager = new ImageManager();
        imageManager.loadImage("refresh", "resources/refresh.png", 15, 15);
        imageManager.loadImage("delete", "resources/delete.png", 15, 15);
        imageManager.loadImage("add", "resources/add.png", 15, 15);
        imageManager.loadImage("play", "resources/play.png", 15, 15);

    }

}
