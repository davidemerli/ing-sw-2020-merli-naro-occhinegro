package it.polimi.ingsw.psp1.santorini.gui;

import it.polimi.ingsw.psp1.santorini.gui.controllers.EnumTransition;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.Transition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.Objects;

//TODO

/**
 * Defines the GUI of the game
 */
public class Gui extends Application {

    private static final Gui instance = new Gui();
    private static Stage primaryStage;

    private EnumScene currentScene;

    /**
     * Launches the applications
     * @param args java arguments (ignored)
     */
    public static void launch(String[] args) {
        Application.launch("");
    }

    /**
     * @return Gui singleton instance
     */
    public static Gui getInstance() {
        return instance;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Starts GUI
     *
     * @param primaryStage primary stage
     */
    @Override
    public void start(Stage primaryStage) {
        try {
            Font.loadFont(getClass().getResourceAsStream("/fonts/Diogenes.ttf"), 20);
            Font.loadFont(getClass().getResourceAsStream("/fonts/Mount Olympus.otf"), 20);

            //removes undesired behaviour from SPACE and ENTER
            primaryStage.addEventFilter(KeyEvent.KEY_PRESSED, k -> {
                if (k.getCode() == KeyCode.SPACE || k.getCode() == KeyCode.ENTER) {
                    k.consume();
                }
            });

            EnumScene.GAME.load();

            Gui.primaryStage = primaryStage;
            changeScene(EnumScene.IP_SELECT);

            primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/title_island.png")));

            primaryStage.setTitle("Santorini");
            primaryStage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void stop() throws Exception {
        super.stop();

        System.exit(0);
    }

    /**
     * Changes a scene waiting for javafx thread
     *
     * @param scene new scene
     */
    public void changeSceneSync(EnumScene scene) {
        Platform.runLater(() -> {
            if (scene.equals(currentScene)) {
                return;
            }

            try {
                Parent newScene = scene.load();
                Parent oldScene = primaryStage.getScene().getRoot();

                Parent newPane = new AnchorPane(newScene);
                Parent oldPane = new AnchorPane(oldScene);
                AnchorPane pane = new AnchorPane(oldPane);

                setAnchors(newScene, oldScene, newPane, oldPane, pane);

                pane.getChildren().add(newPane);
                pane.setStyle("-fx-background-color: black");

                primaryStage.getScene().setRoot(pane);

                FadeTransition oldFt = new FadeTransition(Duration.millis(400), oldPane);
                oldFt.setInterpolator(Interpolator.EASE_OUT);
                oldFt.setFromValue(1);
                oldFt.setToValue(0);
                FadeTransition newFt = new FadeTransition(Duration.millis(400), newPane);
                newFt.setInterpolator(Interpolator.EASE_IN);
                newFt.setFromValue(0);
                newFt.setToValue(1);
                newFt.setDelay(Duration.millis(300));
//                newFt.setOnFinished(e -> changeScene(scene));

                ParallelTransition pt = new ParallelTransition(oldFt, newFt);
                pt.play();
//                pt.setOnFinished(e -> changeScene(scene));

                currentScene = scene;
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Changes scene
     *
     * @param scene valid scene
     */
    public void changeScene(EnumScene scene) {
        if (Objects.equals(currentScene, scene)) {
            return;
        }

        try {
            if (primaryStage.getScene() == null) {
                primaryStage.setScene(new Scene(scene.load()));
            } else {
                primaryStage.getScene().setRoot(scene.load());
            }

            currentScene = scene;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Changes scene
     *
     * @param scenePane valid scene pane
     */
    public void changeScene(Pane scenePane) {
        Scene newScene = new Scene(scenePane, primaryStage.getScene().getWidth(), primaryStage.getScene().getHeight());
        primaryStage.setScene(newScene);
    }

    /**
     * Changes scene
     *
     * @param scene      valid scene
     * @param transition transition type
     */
    public void changeSceneSync(EnumScene scene, EnumTransition transition) {
        Platform.runLater(() -> {
            if (Objects.equals(currentScene, scene)) {
                return;
            }

            try {
                Parent toReplace = primaryStage.getScene().getRoot();
                AnchorPane anchor = new AnchorPane(toReplace);
                AnchorPane.setTopAnchor(toReplace, 0D);
                AnchorPane.setBottomAnchor(toReplace, 0D);
                AnchorPane.setLeftAnchor(toReplace, 0D);
                AnchorPane.setRightAnchor(toReplace, 0D);

                Pane currentPane = new StackPane(anchor);

                Parent root = scene.load();
                AnchorPane pane = new AnchorPane(root);
                AnchorPane.setTopAnchor(root, 0D);
                AnchorPane.setBottomAnchor(root, 0D);
                AnchorPane.setLeftAnchor(root, 0D);
                AnchorPane.setRightAnchor(root, 0D);
                changeScene(currentPane);

                Transition t = transition.getTransition(currentPane, pane);
                t.play();

                currentScene = scene;
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
    }

    /**
     * @return current displayed scene
     */
    public EnumScene getCurrentScene() {
        return currentScene;
    }

    private void setAnchors(Parent... elements) {
        for (Parent elem : elements) {
            AnchorPane.setLeftAnchor(elem, 0D);
            AnchorPane.setTopAnchor(elem, 0D);
            AnchorPane.setRightAnchor(elem, 0D);
            AnchorPane.setBottomAnchor(elem, 0D);
        }
    }
}