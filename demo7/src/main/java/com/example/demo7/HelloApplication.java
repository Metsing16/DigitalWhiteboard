//Masejoseng Metsing-901016774-BSCSMY3S2
package com.example.demo7;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class HelloApplication extends Application {
    // Core components
    private Canvas canvas;
    private GraphicsContext gc;

    // Modes
    private boolean drawingMode = false;
    private boolean eraserMode = false;
    private boolean textMode = false;

    // Drawing properties
    private Color currentColor = Color.BLACK;
    private double brushSize = 2.0;

    // Media components
    private MediaPlayer mediaPlayer;
    private MediaView mediaView;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Interactive Digital Whiteboard");

        // Create canvas and graphics context
        canvas = new Canvas(800, 600);
        gc = canvas.getGraphicsContext2D();
        clearCanvas();

        // Create a media view and stack it with the canvas
        mediaView = new MediaView();
        mediaView.setFitWidth(800);
        mediaView.setFitHeight(600);
        mediaView.setPreserveRatio(true);
        mediaView.setVisible(false);

        StackPane canvasStack = new StackPane(canvas, mediaView);
        ScrollPane canvasScrollPane = new ScrollPane(canvasStack);
        canvasScrollPane.setPannable(true);

        // UI Controls
        ColorPicker colorPicker = new ColorPicker();
        colorPicker.setOnAction(e -> currentColor = colorPicker.getValue());

        Slider brushSizeSlider = new Slider(1, 20, 2);
        brushSizeSlider.setShowTickLabels(true);
        brushSizeSlider.setShowTickMarks(true);
        brushSizeSlider.valueProperty().addListener((obs, oldVal, newVal) -> brushSize = newVal.doubleValue());

        TextField textField = new TextField();
        textField.setPromptText("Enter text...");

        // Mode buttons
        Button drawButton = new Button("Draw Mode");
        drawButton.setOnAction(e -> setMode("draw"));
        drawButton.getStyleClass().add("button");


        Button eraserButton = new Button("Eraser Mode");
        eraserButton.setOnAction(e -> setMode("erase"));
        eraserButton.getStyleClass().add("button");

        Button textButton = new Button("Text Mode");
        textButton.setOnAction(e -> setMode("text"));
        textButton.getStyleClass().add("button");


        Button clearButton = new Button("Clear Canvas");
        clearButton.setOnAction(e -> clearCanvas());

        // Add text to canvas when clicked
        Button addTextButton = new Button("Set Text");
        addTextButton.setOnAction(e -> {
            if (textMode && !textField.getText().isEmpty()) {
                canvas.setOnMouseClicked(event -> {
                    gc.setFill(currentColor);
                    gc.fillText(textField.getText(), event.getX(), event.getY());
                    textField.clear();
                    canvas.setOnMouseClicked(null); // Reset click handler
                });
            } else {
                showAlert("Text Mode Required", "Please enable Text Mode and enter text first.");
            }
        });

        // Media buttons
        Button addImageButton = new Button("Add Image");
        addImageButton.setOnAction(e -> addImage(primaryStage));

        Button addVideoButton = new Button("Add Video");
        addVideoButton.setOnAction(e -> addVideo(primaryStage));

        Button addMusicButton = new Button("Add Music");
        addMusicButton.setOnAction(e -> addMusic(primaryStage));

        Button stopMediaButton = new Button("Stop Media");
        stopMediaButton.setOnAction(e -> {
            if (mediaPlayer != null) mediaPlayer.stop();
        });

        Button playPauseButton = new Button("Play/Pause");
        playPauseButton.setOnAction(e -> {
            if (mediaPlayer != null) {
                if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                    mediaPlayer.pause();
                } else {
                    mediaPlayer.play();
                }
            }
        });

        Button exitVideoButton = new Button("Exit Media View");
        exitVideoButton.setOnAction(e -> {
            mediaView.setMediaPlayer(null);
            mediaView.setVisible(false);
        });

        // Volume control
        Slider volumeSlider = new Slider(0, 1, 0.5);
        volumeSlider.setShowTickLabels(true);
        volumeSlider.setShowTickMarks(true);
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (mediaPlayer != null) mediaPlayer.setVolume(newVal.doubleValue());
        });

        // Save/load buttons
        Button saveButton = new Button("Save Canvas");
        saveButton.setOnAction(e -> saveCanvas(primaryStage));

        Button loadButton = new Button("Load Canvas");
        loadButton.setOnAction(e -> loadCanvas(primaryStage));

        // Toolbars and layout
        VBox leftToolbar = new VBox(10,
                colorPicker, drawButton, eraserButton, textButton,
                new Label("Brush Size"), brushSizeSlider,
                clearButton, textField, addTextButton,
                addImageButton, addVideoButton, addMusicButton,
                saveButton, loadButton);
        leftToolbar.setAlignment(Pos.TOP_LEFT);

        HBox mediaControls = new HBox(10,
                playPauseButton, stopMediaButton, exitVideoButton,
                new Label("Volume:"), volumeSlider);
        mediaControls.setAlignment(Pos.CENTER);

        VBox mediaBox = new VBox(mediaControls);
        mediaBox.setAlignment(Pos.CENTER);

        BorderPane layout = new BorderPane();
        layout.setLeft(leftToolbar);
        layout.setCenter(canvasScrollPane);
        layout.setBottom(mediaBox);

        Scene scene = new Scene(layout, 1000, 700);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.show();

        // Drawing and erasing functionality
        setupDrawingHandlers();
    }

    // Handle draw/erase modes
    private void setupDrawingHandlers() {
        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
            if (drawingMode) {
                gc.beginPath();
                gc.moveTo(e.getX(), e.getY());
                gc.setStroke(currentColor);
                gc.setLineWidth(brushSize);
                gc.stroke();
            } else if (eraserMode) {
                gc.clearRect(e.getX() - brushSize / 2, e.getY() - brushSize / 2, brushSize, brushSize);
            }
        });

        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> {
            if (drawingMode) {
                gc.lineTo(e.getX(), e.getY());
                gc.stroke();
            } else if (eraserMode) {
                gc.clearRect(e.getX() - brushSize / 2, e.getY() - brushSize / 2, brushSize, brushSize);
            }
        });
    }

    // Set the current mode (draw, erase, text)
    private void setMode(String mode) {
        drawingMode = "draw".equals(mode);
        eraserMode = "erase".equals(mode);
        textMode = "text".equals(mode);
    }

    // Clear the canvas to white
    private void clearCanvas() {
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    // Add image to canvas
    private void addImage(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            Image image = new Image(file.toURI().toString());
            gc.drawImage(image, 50, 50, 200, 150);
        }
    }

    // Add video to media view
    private void addVideo(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Video Files", "*.mp4", "*.avi", "*.mov"));
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            Media media = new Media(file.toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            mediaView.setMediaPlayer(mediaPlayer);
            mediaView.setVisible(true);
            mediaPlayer.setAutoPlay(true);
        }
    }

    // Add audio
    private void addMusic(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Audio Files", "*.mp3", "*.wav"));
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            Media media = new Media(file.toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setAutoPlay(true);
        }
    }

    // Save canvas to image file
    private void saveCanvas(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Image", "*.png"));
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            try {
                WritableImage image = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
                canvas.snapshot(null, image);
                ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    // Load image onto canvas
    private void loadCanvas(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            Image image = new Image(file.toURI().toString());
            gc.drawImage(image, 0, 0, canvas.getWidth(), canvas.getHeight());
        }
    }

    // Utility to show alert dialog
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
