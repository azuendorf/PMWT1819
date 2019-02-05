package de.uniks.party;

import de.uniks.party.model.Participant;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;

import java.util.Locale;

public class ParticipantCell extends ListCell<Participant>
{
   public ParticipantCell(PeopleController peopleController)
   {
      // System.out.println("P-Cell created");
   }

   @Override
   protected void updateItem(Participant participant, boolean empty)
   {
      super.updateItem(participant, empty);

      if (empty)
      {
         setGraphic(null);
      }
      else
      {
         Label nameLabel = new Label(participant.getName());
         nameLabel.setPrefWidth(120);

         Label saldoLabel = new Label();
         saldoLabel.setPrefWidth(80);
         saldoLabel.setAlignment(Pos.CENTER_RIGHT);
         updateSaldo(participant, saldoLabel);
         participant.addPropertyChangeListener(Participant.PROPERTY_saldo, e -> updateSaldo(participant, saldoLabel));

         Button editButton = new Button("<");
         Button delButton = new Button("-");
         delButton.setOnAction( e -> ModelManager.get().removeParticipant(participant));

         HBox participantLine = new HBox(9, nameLabel, saldoLabel, editButton, delButton);
         participantLine.setAlignment(Pos.CENTER);

         setGraphic(participantLine);
      }
   }

   private void updateSaldo(Participant participant, Label saldoLabel)
   {
      String text = String.format("%.2f € ", participant.getSaldo());
      saldoLabel.setText(text);
   }
}
