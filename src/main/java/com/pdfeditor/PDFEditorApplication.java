package com.pdfeditor;

import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.util.Matrix;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PDFEditorApplication extends Application {
    private final Pane pageCanvas;
    private final BorderPane root;
    private final List<DraggableImage> images = new ArrayList<>();

    public PDFEditorApplication() {
        root = new BorderPane();
        pageCanvas = new Pane();
        setupCanvas();
        setupToolbar();
    }

    private void setupCanvas() {
        pageCanvas.setStyle(
                "-fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 0);");
        pageCanvas.setPrefSize(PDRectangle.A4.getWidth(), PDRectangle.A4.getHeight());

        ScrollPane scrollPane = new ScrollPane(pageCanvas);
        scrollPane.setStyle("-fx-background-color: #404040;");
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        root.setCenter(scrollPane);
    }

    private void setupToolbar() {
        HBox toolbar = new HBox(10);
        toolbar.setStyle("-fx-padding: 10; -fx-background-color: #f0f0f0;");

        Button addImageButton = new Button("Add Image");
        addImageButton.setOnAction(e -> handleAddImage());

        Button addFolderButton = new Button("Add Folder");
        addFolderButton.setOnAction(e -> handleAddFolder());

        Button saveButton = new Button("Save PDF");
        saveButton.setOnAction(e -> handleSavePDF());

        toolbar.getChildren().addAll(addImageButton, addFolderButton, saveButton);
        root.setTop(toolbar);
    }

    private void handleAddImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));

        File file = fileChooser.showOpenDialog(root.getScene().getWindow());
        if (file != null) {
            addImageToCanvas(file);
        }
    }

    private void handleAddFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Image Folder");

        File folder = directoryChooser.showDialog(root.getScene().getWindow());
        if (folder != null && folder.isDirectory()) {
            File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".png") ||
                    name.toLowerCase().endsWith(".jpg") ||
                    name.toLowerCase().endsWith(".jpeg"));

            if (files != null) {
                Arrays.sort(files); // Ensure consistent order
                for (File file : files) {
                    addImageToCanvas(file);
                }
            }
        }
    }

    private void addImageToCanvas(File file) {
        try {
            DraggableImage image = new DraggableImage(file);
            images.add(image);
            pageCanvas.getChildren().add(image);
        } catch (Exception e) {
            showError("Error Adding Image",
                    "Failed to add image: " + file.getName(),
                    e.getMessage());
        }
    }

    private void handleSavePDF() {
        if (images.isEmpty()) {
            showError("No Content",
                    "Cannot save empty PDF",
                    "Please add at least one image before saving.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

        File file = fileChooser.showSaveDialog(root.getScene().getWindow());
        if (file != null) {
            try (PDDocument document = new PDDocument()) {
                PDPage page = new PDPage(PDRectangle.A4);
                document.addPage(page);

                try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                    for (DraggableImage draggableImage : images) {
                        addImageToPDF(document, contentStream, draggableImage);
                    }
                }
                document.save(file);
            } catch (IOException e) {
                showError("Save Error",
                        "Failed to save PDF",
                        e.getMessage());
            }
        }
    }

    private void addImageToPDF(PDDocument document, PDPageContentStream contentStream,
            DraggableImage draggableImage) throws IOException {
        Point2D position = draggableImage.getPosition();
        PDImageXObject image = PDImageXObject.createFromFile(
                draggableImage.getSourceFile().getAbsolutePath(), document);

        float pdfY = PDRectangle.A4.getHeight() -
                (float) position.getY() -
                (float) draggableImage.getBoundsInParent().getHeight();

        contentStream.saveGraphicsState();

        Matrix transform = new Matrix();
        transform.translate((float) position.getX(), pdfY);

        if (draggableImage.getRotate() != 0) {
            float centerX = (float) draggableImage.getBoundsInParent().getWidth() / 2;
            float centerY = (float) draggableImage.getBoundsInParent().getHeight() / 2;
            transform.translate(centerX, centerY);
            transform.rotate(Math.toRadians(draggableImage.getRotate()));
            transform.translate(-centerX, -centerY);
        }

        contentStream.transform(transform);

        contentStream.drawImage(image, 0, 0,
                (float) draggableImage.getBoundsInParent().getWidth(),
                (float) draggableImage.getBoundsInParent().getHeight());

        contentStream.restoreGraphicsState();
    }

    private void showError(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @Override
    public void start(Stage primaryStage) {
        Scene scene = new Scene(root, 1024, 768);
        primaryStage.setTitle("Interactive PDF Editor");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
