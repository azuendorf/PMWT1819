package de.uniks.party;

import de.uniks.party.model.Participant;
import de.uniks.party.model.ShoppingItem;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

public class AddEditShoppingItemController
{
   private final PartyController partyController;
   private TextField descriptionField;
   private TextField priceField;
   private ChoiceBox<Participant> responsibleChoice;
   private VBox root;
   private ObservableList<Participant> participantsList;
   private ShoppingItem item;

   public AddEditShoppingItemController(PartyController partyController)
   {
      this.partyController = partyController;
   }

   public VBox getView(ShoppingItem item)
   {
      this.item = item;
      if (root == null)
      {
         Label label1 = new Label("Description:");

         descriptionField = new TextField();

         Label label2 = new Label("Price:");

         priceField = new TextField();

         Label label3 = new Label("Responsible:");

         participantsList = FXCollections.observableArrayList();

         responsibleChoice = new ChoiceBox<>(participantsList);
         responsibleChoice.setPrefWidth(120);
         responsibleChoice.setId("choice");

         Button okButton = new Button("OK");
         okButton.setOnAction(e -> okAction());
         Button escapeButton = new Button("Escape");

         HBox hbox = new HBox(9, okButton, escapeButton);
         hbox.setAlignment(Pos.CENTER_RIGHT);

         root = new VBox(18, label1, descriptionField, label2, priceField, label3, responsibleChoice, hbox);
         root.setPadding(new Insets(18));
      }

      participantsList.clear();
      participantsList.addAll(Model.getParty().getParticipants());

      if (item == null)
      {
         descriptionField.setText("");
         priceField.setText(String.format("%.2f", 0.0));
         responsibleChoice.setValue(null);
      }
      else
      {
         descriptionField.setText(item.getDescription());
         priceField.setText(String.format("%.2f", item.getPrice()));
         responsibleChoice.setValue(item.getResponsible());
      }

      Platform.runLater(() -> descriptionField.requestFocus());

      return root;
   }

   private void okAction()
   {
      if (item == null)
      {
         item = new ShoppingItem();
         Model.getParty().withShoppingItems(item);
      }

      item.setDescription(descriptionField.getText());

      double price = 0.0;
      try
      {
         price = NumberFormat.getInstance().parse(priceField.getText()).doubleValue();
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }

      item.setPrice(price);

      item.setResponsible(responsibleChoice.getValue());

      partyController.switchToShoppingScreen();
   }
}
