package com.pdfeditor;

import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.File;

public class DraggableImage extends ImageView {
    private Point2D dragDelta;
    private final File sourceFile;
    private boolean dragging = false;

    public DraggableImage(File file) {
        super(new Image(file.toURI().toString()));
        this.sourceFile = file;

        setFitWidth(200);
        setPreserveRatio(true);

        setOnMousePressed(event -> {
            dragging = true;
            dragDelta = new Point2D(getTranslateX() - event.getSceneX(), getTranslateY() - event.getSceneY());
            toFront();
            event.consume();
        });

        setOnMouseDragged(event -> {
            if (dragging) {
                setTranslateX(event.getSceneX() + dragDelta.getX());
                setTranslateY(event.getSceneY() + dragDelta.getY());
            }
            event.consume();
        });

        setOnMouseReleased(event -> {
            dragging = false; // Stop dragging and keep the new position
            event.consume();
        });

        setOnScroll(event -> {
            double zoomFactor = 1.05;
            double deltaY = event.getDeltaY();

            if (deltaY < 0) {
                setFitWidth(getFitWidth() / zoomFactor);
            } else {
                setFitWidth(getFitWidth() * zoomFactor);
            }
            event.consume();
        });
    }

    public File getSourceFile() {
        return sourceFile;
    }

    public Point2D getPosition() {
        return new Point2D(getTranslateX(), getTranslateY());
    }
}
