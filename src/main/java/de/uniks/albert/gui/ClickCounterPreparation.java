package de.uniks.albert.gui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public class ClickCounterPreparation extends Application
{
   @Override
   public void start(Stage stage) throws Exception
   {
      Label label = new Label("Hello");
      label.setId("l1");
      VBox vbox = new VBox(18d, label);
      vbox.setPadding(new Insets(6));
      vbox.setStyle("-fx-border-color: black; -fx-background: yellow");
      Scene scene = new Scene(vbox, 400, 600);
      stage.setScene(scene);
      stage.show();
   }
}
