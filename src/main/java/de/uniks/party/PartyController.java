package de.uniks.party;

import de.uniks.party.model.Participant;
import de.uniks.party.model.ShoppingItem;
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
   private AddEditPersonDialogController addEditPersonDialogController;
   private AddEditShoppingItemController addEditShoppingItemController;
   private ShoppingController shoppingController;

   public PeopleController getPeopleController()
   {
      return peopleController;
   }

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
      shoppingButton.setOnAction(e -> switchToShoppingScreen());

      HBox buttonBar = new HBox(18, startButton, peopleButton, shoppingButton);
      buttonBar.setAlignment(Pos.CENTER);

      startDialogController = new StartDialogController(this);
      dialogView = startDialogController.getView();
      peopleController = new PeopleController(this);
      addEditPersonDialogController = new AddEditPersonDialogController(this);
      addEditShoppingItemController = new AddEditShoppingItemController(this);
      shoppingController = new ShoppingController(this);

      partyBox = new VBox(18, buttonBar, dialogView);
      partyBox.setAlignment(Pos.CENTER);
      partyBox.setStyle("-fx-background-color: white;" +
            "-fx-font-size: 18");



      return partyBox;
   }

   public void switchToShoppingScreen()
   {
      partyBox.getChildren().remove(dialogView);

      if (Model.getParty().getShoppingItems().isEmpty())
      {
         dialogView = addEditShoppingItemController.getView(null);
      }
      else
      {
         dialogView = shoppingController.getView();
      }

      partyBox.getChildren().add(dialogView);
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

      if (Model.getParty().getParticipants().isEmpty())
      {
         dialogView = addEditPersonDialogController.getView(null);
      }
      else
      {
         dialogView = peopleController.getView();
      }

      partyBox.getChildren().add(dialogView);
   }

   public void switchToAddEditPersonDialog(Participant participant)
   {
      partyBox.getChildren().remove(dialogView);
      dialogView = addEditPersonDialogController.getView(participant);
      partyBox.getChildren().add(dialogView);
   }

   public void switchToAddEditShoppingItem(ShoppingItem item)
   {
      partyBox.getChildren().remove(dialogView);
      dialogView = addEditShoppingItemController.getView(item);
      partyBox.getChildren().add(dialogView);
   }
}
