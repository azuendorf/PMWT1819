package de.uniks.party;

import de.uniks.party.model.Party;
import de.uniks.party.model.ShoppingItem;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ShoppingController
{
   private final PartyController partyController;
   private VBox dialogRoot;
   private Label titleLabel;
   private ObservableList<ShoppingItem> itemsList;
   private ListView<ShoppingItem> itemsListView;
   private Button addItemButton;

   public ShoppingController(PartyController partyController)
   {
      this.partyController = partyController;
   }

   public VBox getView()
   {
      if (dialogRoot == null)
      {
         // test
         titleLabel = new Label("Shopping View");

         Label label = new Label("Items:");
         addItemButton = new Button("+");
         addItemButton.setId("addPeopleButton");
         addItemButton.setOnAction(e -> partyController.switchToAddEditShoppingItem(null));

         HBox addItemBox = new HBox(9, label, addItemButton);
         addItemBox.setAlignment(Pos.CENTER);

         itemsList = FXCollections.observableArrayList();

         itemsListView = new ListView<ShoppingItem>(itemsList);
         itemsListView.setPrefHeight(300);
         itemsListView.setCellFactory(param -> new ShoppingItemCell(partyController));

         dialogRoot = new VBox(18, titleLabel, addItemBox, itemsListView);
         dialogRoot.setAlignment(Pos.CENTER);
      }

      Party party = ModelManager.getParty();
      String text = party.getPartyName() + " "
            + party.getLocation() + " "
            + party.getDate();

      itemsList.clear();
      itemsList.addAll(ModelManager.getParty().getShoppingItems());

      titleLabel.setText(text);

      Platform.runLater(()-> addItemButton.requestFocus());

      return dialogRoot;
   }
}
