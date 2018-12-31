package de.uniks.party;

import de.uniks.party.model.Participant;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class AddEditPersonDialogController
{
   private final PartyController partyController;
   private VBox dialogRoot = null;
   private TextField nameField;
   private Participant participant;

   public AddEditPersonDialogController(PartyController partyController)
   {
      this.partyController = partyController;
   }

   public VBox getView(Participant participant)
   {
      this.participant = participant;

      if (dialogRoot == null)
      {
         Label label = new Label("add / edit person name:");

         nameField = new TextField();

         Button okButton = new Button("OK");
         okButton.setId("okButton");
         okButton.setOnAction( e -> okButtonAction());

         Button escapeButton = new Button("Escape");

         HBox buttonLine = new HBox(9, okButton, escapeButton);
         buttonLine.setAlignment(Pos.CENTER_RIGHT);

         dialogRoot = new VBox(18, label, nameField, buttonLine);
         dialogRoot.setAlignment(Pos.CENTER);
         dialogRoot.setPadding(new Insets(18));

      }

      if (participant == null)
      {
         nameField.setText("");
      }
      else
      {
         nameField.setText(participant.getName());
      }

      Platform.runLater(() -> nameField.requestFocus());

      return dialogRoot;
   }

   private void okButtonAction()
   {
      if (participant == null)
      {
         participant = new Participant();
         Model.getParty().withParticipants(participant);
         // partyController.getPeopleController().getParticipantsList().add(participant);
      }

      participant.setName(nameField.getText());

      partyController.switchToPeopleScreen();
   }
}
