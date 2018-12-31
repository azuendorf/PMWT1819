package de.uniks.party;

import de.uniks.party.model.ShoppingItem;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;

public class ShoppingItemCell extends ListCell<ShoppingItem>
{

   private PartyController partyController;

   public ShoppingItemCell(PartyController partyController)
   {
      this.partyController = partyController;
   }

   @Override
   protected void updateItem(ShoppingItem item, boolean empty)
   {
      super.updateItem(item, empty);

      if (empty)
      {
         setGraphic(null);
      }
      else
      {
         Label nameLabel = new Label(item.getDescription());
         nameLabel.setPrefWidth(120);

         String text = "n.n.";
         if (item.getResponsible() != null)
         {
            text = item.getResponsible().getName();
         }
         Label responsibleLabel = new Label(text);
         responsibleLabel.setPrefWidth(120);

         Label priceLabel = new Label(String.format("%.2f €", item.getPrice()));
         priceLabel.setAlignment(Pos.CENTER_RIGHT);

         Button editButton = new Button("<");
         editButton.setOnAction(e -> partyController.switchToAddEditShoppingItem(item));
         Button delButton = new Button("-");

         HBox participantLine = new HBox(9, nameLabel, responsibleLabel, priceLabel, editButton, delButton);
         participantLine.setAlignment(Pos.CENTER);

         setGraphic(participantLine);
      }
   }
}
