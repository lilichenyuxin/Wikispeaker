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

        HBox.setHgrow(creationSearchBar, Priority.ALWAYS);

        addCreationButton = new Button("add");
        Tooltip addTooltip = new Tooltip("Create new creation");
        addCreationButton.setTooltip(addTooltip);

        refreshCreationsButton = new Button("refresh");
        
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

}
