package de.uniks.albert.gui;

import javafx.application.Application;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ClickCounter extends Application
{
   SimpleIntegerProperty counter = new SimpleIntegerProperty();

   @Override
   public void start(Stage stage) throws Exception
   {
      Button button = new Button("Click Me");
      button.setOnAction(e -> countClick());

      Label label = new Label("Clicks: ");
      Label counterLabel = new Label();
      counterLabel.textProperty().bind(counter.asString());

      HBox hBox = new HBox(18, label, counterLabel);
      hBox.setAlignment(Pos.CENTER);
      hBox.setStyle("-fx-background-color: white;");

      VBox vBox = new VBox(36, button, hBox);
      vBox.setAlignment(Pos.CENTER);
      vBox.setStyle("-fx-background-color: white;" +
            "-fx-font-size: 18");

      Scene scene = new Scene(vBox, 400, 600);
      stage.setScene(scene);
      stage.show();
   }

   private void countClick()
   {
      counter.setValue(counter.getValue() + 1);
}
}
