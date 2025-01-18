package com.pdfeditor;

import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.File;

public class DraggableImage extends ImageView {
    private Point2D dragDelta;
    private final File sourceFile;

    public DraggableImage(File file) {
        super(new Image(file.toURI().toString()));
        this.sourceFile = file;

        // Set initial size while maintaining aspect ratio
        setFitWidth(200);
        setPreserveRatio(true);

        // Enable dragging
        setOnMousePressed(event -> {
            dragDelta = new Point2D(getTranslateX() - event.getSceneX(),
                    getTranslateY() - event.getSceneY());
            toFront(); // Bring to front when selected
            event.consume();
        });

        setOnMouseDragged(event -> {
            setTranslateX(event.getSceneX() + dragDelta.getX());
            setTranslateY(event.getSceneY() + dragDelta.getY());
            event.consume();
        });

        // Enable rotation with right-click drag
        setOnMouseDragged(event -> {
            if (event.isSecondaryButtonDown()) {
                Point2D center = new Point2D(
                        getBoundsInParent().getCenterX(),
                        getBoundsInParent().getCenterY());
                Point2D mouse = new Point2D(event.getSceneX(), event.getSceneY());
                Point2D delta = mouse.subtract(center);
                double angle = Math.atan2(delta.getY(), delta.getX());
                setRotate(Math.toDegrees(angle));
            } else {
                setTranslateX(event.getSceneX() + dragDelta.getX());
                setTranslateY(event.getSceneY() + dragDelta.getY());
            }
            event.consume();
        });

        // Enable resizing with scroll
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