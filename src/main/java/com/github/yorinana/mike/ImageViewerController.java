package com.github.yorinana.mike;

import javafx.beans.Observable;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ImageViewerController extends HBox {
    protected HBox canvasL;
    protected ImageView imageViewL;
    protected HBox canvasR;
    protected ImageView imageViewR;

    ImageViewerController(HBox imageViewer) {
        ObservableList<Node> canvases = imageViewer.getChildren();
        this.canvasL = (HBox) canvases.get(0);
        this.canvasR = (HBox) canvases.get(1);
        this.imageViewL = (ImageView) canvasL.getChildren().get(0);
        this.imageViewR = (ImageView) canvasR.getChildren().get(0);
    }

    public void add(Image image) {

    }

    public void add(Integer i, Image image) {

    }

    public void close() {

    }

    public void close(Integer i) {

    }

    public void next() {

    }

    public void previous() {

    }

    public void apply() {

    }

    public void apply(Integer i) {

    }

    public void applyAll() {

    }
}
