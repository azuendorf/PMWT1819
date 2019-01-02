package de.uniks.party;

import de.uniks.party.model.Party;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class StartDialogController
{
   private VBox dialogRoot;
   private TextField nameField;
   private TextField locationField;
   private TextField dateField;
   private PartyController partyController;
   private String oldContent;

   public StartDialogController(PartyController partyController)
   {
      this.partyController = partyController;
   }

   public VBox getView()
   {
      if (dialogRoot == null)
      {
         Party party = ModelManager.getParty();

         // build it
         Label bigPartyLabel = new Label("Big Party:");

         Label nameLabel = new Label("Name:");
         nameField = new TextField();
         nameField.setId("nameField");
         nameField.textProperty().addListener(
               e -> party.setPartyName(nameField.getText()));

         Label locationLabel = new Label("Location:");
         locationField = new TextField();
         locationField.setId("locationField");
         locationField.textProperty().addListener(
               e -> party.setLocation(locationField.getText())
         );

         Label dateLabel = new Label("Date:");
         dateField = new TextField();
         dateField.setId("dateField");
         dateField.textProperty().addListener(
               e -> party.setDate(dateField.getText())
         );

         // budget

         // next screen
         Button nextScreenButton = new Button("OK");
         nextScreenButton.setOnAction(e -> okAction());
         Button resetButton = new Button("Reset");
         resetButton.setOnAction(e -> partyController.switchToStartScreen());

         HBox nextScreenHBox = new HBox(9, nextScreenButton, resetButton);
         nextScreenHBox.setAlignment(Pos.CENTER_RIGHT);

         dialogRoot = new VBox(18, bigPartyLabel,
               nameLabel, nameField,
               locationLabel, locationField,
               dateLabel, dateField,
               nextScreenHBox
         );
         dialogRoot.setAlignment(Pos.CENTER);
         dialogRoot.setPadding(new Insets(0, 18, 0, 18));

      }


      oldContent = String.format("%s|%s|%s",
            ModelManager.getParty().getPartyName(),
            ModelManager.getParty().getLocation(),
            ModelManager.getParty().getDate());
      nameField.setText(ModelManager.getParty().getPartyName());
      locationField.setText(ModelManager.getParty().getLocation());
      dateField.setText(ModelManager.getParty().getDate());

      Platform.runLater(()->nameField.requestFocus());

      return dialogRoot;
   }

   private void okAction()
   {
      ModelManager.get().haveParty(nameField.getText(), locationField.getText(), dateField.getText(), oldContent);
      partyController.switchToPeopleScreen();
   }


}
