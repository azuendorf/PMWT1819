package de.uniks.party;

import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class PartyController
{
   private VBox partyBox;
   private VBox dialogView;
   private StartDialogController startDialogController;
   private PeopleController peopleController;

   public Parent getDialogView()
   {
      if (partyBox != null)
      {
         return partyBox;
      }

      // build it
      // buttonBar
      Button startButton = new Button("Start");
      startButton.setOnAction(e -> switchToStartScreen());
      Button peopleButton = new Button("People");
      peopleButton.setOnAction(e -> switchToPeopleScreen());
      Button shoppingButton = new Button("Shopping");

      HBox buttonBar = new HBox(18, startButton, peopleButton, shoppingButton);
      buttonBar.setAlignment(Pos.CENTER);

      startDialogController = new StartDialogController(this);
      dialogView = startDialogController.getView();
      peopleController = new PeopleController(this);

      partyBox = new VBox(18, buttonBar, dialogView);
      partyBox.setAlignment(Pos.CENTER);
      partyBox.setStyle("-fx-background-color: white;" +
            "-fx-font-size: 18");



      return partyBox;
   }

   public void switchToStartScreen()
   {
      partyBox.getChildren().remove(dialogView);
      dialogView = startDialogController.getView();
      partyBox.getChildren().add(dialogView);
   }

   public void switchToPeopleScreen()
   {
      partyBox.getChildren().remove(dialogView);
      dialogView = peopleController.getView();
      partyBox.getChildren().add(dialogView);
   }
}
