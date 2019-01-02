package de.uniks.party;

import de.uniks.party.model.Participant;
import de.uniks.party.model.Party;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class PeopleController
{

   private final PartyController partyController;
   private VBox dialogRoot;
   private Label titleLabel;
   private ListView<Participant> memberListView;
   private ObservableList<Participant> participantsList;
   private Button addPeopleButton;

   public ObservableList<Participant> getParticipantsList()
   {
      return participantsList;
   }

   public PeopleController(PartyController partyController)
   {
      this.partyController = partyController;
   }

   public VBox getView()
   {
      if (dialogRoot == null)
      {
         // test
         titleLabel = new Label("People View");

         Label label = new Label("People:");
         addPeopleButton = new Button("+");
         addPeopleButton.setId("addPeopleButton");
         addPeopleButton.setOnAction(e -> partyController.switchToAddEditPersonDialog(null));

         HBox addPeopleBox = new HBox(9, label, addPeopleButton);
         addPeopleBox.setAlignment(Pos.CENTER);

         participantsList = FXCollections.observableArrayList();

         memberListView = new ListView<Participant>(participantsList);
         memberListView.setPrefHeight(300);
         memberListView.setCellFactory(param -> new ParticipantCell(this));

         dialogRoot = new VBox(18, titleLabel, addPeopleBox, memberListView);
         dialogRoot.setAlignment(Pos.CENTER);
      }

      Party party = ModelManager.getParty();
      String text = party.getPartyName() + " "
            + party.getLocation() + " "
            + party.getDate();

      participantsList.clear();
      participantsList.addAll(ModelManager.getParty().getParticipants());

      titleLabel.setText(text);

      Platform.runLater(()->addPeopleButton.requestFocus());

      return dialogRoot;
   }
}
