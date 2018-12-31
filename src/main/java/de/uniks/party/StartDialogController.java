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
import org.fulib.yaml.ModelListener;

public class StartDialogController
{
   private VBox dialogRoot;
   private TextField nameField;
   private TextField locationField;
   private TextField dateField;
   private PartyController partyController;

   public StartDialogController(PartyController partyController)
   {
      this.partyController = partyController;
   }

   public VBox getView()
   {
      if (dialogRoot == null)
      {
         Party party = Model.getParty();

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
         Button nextScreenButton = new Button("->");
         nextScreenButton.setOnAction(e -> partyController.switchToPeopleScreen());

         HBox nextScreenHBox = new HBox(nextScreenButton);
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

      nameField.setText(Model.getParty().getPartyName());
      locationField.setText(Model.getParty().getLocation());
      dateField.setText(Model.getParty().getDate());

      Platform.runLater(()->nameField.requestFocus());

      return dialogRoot;
   }



}
