package de.uniks.party;

import de.uniks.party.model.Participant;
import de.uniks.party.model.ShoppingItem;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.fulib.yaml.StrUtil;

public class PartyController
{
   private VBox partyBox;
   private VBox dialogView;
   private StartDialogController startDialogController;
   private PeopleController peopleController;
   private AddEditPersonDialogController addEditPersonDialogController;
   private AddEditShoppingItemController addEditShoppingItemController;
   private ShoppingController shoppingController;
   private SettingsController settingsDialogController;

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
      Button settingsButton = new Button();
      Image imageSettings = new Image(ClassLoader.getSystemClassLoader().getResourceAsStream("images/settingsIcon.png"));
      settingsButton.setGraphic(new ImageView(imageSettings));
      settingsButton.setOnAction( e -> switchToSettingsView());

      HBox buttonBar = new HBox(18, startButton, peopleButton, shoppingButton /*, settingsButton */ );
      buttonBar.setAlignment(Pos.CENTER);

      startDialogController = new StartDialogController(this);
      dialogView = startDialogController.getView();
      peopleController = new PeopleController(this);
      addEditPersonDialogController = new AddEditPersonDialogController(this);
      addEditShoppingItemController = new AddEditShoppingItemController(this);
      shoppingController = new ShoppingController(this);
      settingsDialogController = new SettingsController(this);

      partyBox = new VBox(18, buttonBar, dialogView);
      partyBox.setPadding(new Insets(36, 0,0,0));
      partyBox.setAlignment(Pos.TOP_CENTER);
      partyBox.setStyle("-fx-background-color:white;" +
            "-fx-font-size: 18");

      ModelManager.get().addViewListener(() -> updateViews());
      ModelManager.get().getDistributor().addPropertyChangeListener(e -> setColorForSettingsButton(settingsButton));
      setColorForSettingsButton(settingsButton);

      return partyBox;
   }

   private void setColorForSettingsButton(Button settingsButton)
   {
      if (StrUtil.stringEquals(ModelManager.get().getDistributor().getServerStatus(), ModelDistribution.CONNECTED))
      {
         settingsButton.setStyle("-fx-background-color:green;");
      }
      else
      {
         settingsButton.setStyle("-fx-background-color:red;");
      }
   }

   private void updateViews()
   {
      startDialogController.getView();
      peopleController.getView();
      shoppingController.getView();
   }

   public void switchToShoppingScreen()
   {
      partyBox.getChildren().remove(dialogView);

      if (ModelManager.getParty().getShoppingItems().isEmpty())
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
      settingsDialogController.reconnectAction();
      partyBox.getChildren().remove(dialogView);
      dialogView = startDialogController.getView();
      partyBox.getChildren().add(dialogView);
   }

   public void switchToPeopleScreen()
   {
      settingsDialogController.reconnectAction();
      partyBox.getChildren().remove(dialogView);

      if (ModelManager.getParty().getParticipants().isEmpty())
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
      settingsDialogController.reconnectAction();
      partyBox.getChildren().remove(dialogView);
      dialogView = addEditPersonDialogController.getView(participant);
      partyBox.getChildren().add(dialogView);
   }

   public void switchToAddEditShoppingItem(ShoppingItem item)
   {
      settingsDialogController.reconnectAction();
      partyBox.getChildren().remove(dialogView);
      dialogView = addEditShoppingItemController.getView(item);
      partyBox.getChildren().add(dialogView);
   }

   public void switchToSettingsView()
   {
      partyBox.getChildren().remove(dialogView);
      dialogView = settingsDialogController.getView();
      partyBox.getChildren().add(dialogView);
   }
}
