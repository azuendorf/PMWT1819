package de.uniks.party;

import de.uniks.party.model.Participant;
import de.uniks.party.model.Party;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class PeopleController
{

   private VBox dialogRoot;
   private Label titleLabel;
   private ListView<Participant> memberListView;
   private SimpleListProperty<Participant> participantsList;

   public PeopleController(PartyController partyController)
   {

   }

   public VBox getView()
   {
      if (dialogRoot == null)
      {
         // test
         Model.getParty().withParticipants(new Participant().setName("Alice"));
         Model.getParty().withParticipants(new Participant().setName("Bob"));

         titleLabel = new Label("People View");

         Label label = new Label("People:");
         Button addPeopleButton = new Button("+");

         HBox addPeopleBox = new HBox(9, label, addPeopleButton);
         addPeopleBox.setAlignment(Pos.CENTER);

         memberListView = new ListView<Participant>();
         memberListView.setPrefHeight(300);
         participantsList = new SimpleListProperty<>(FXCollections.observableArrayList());

         memberListView.setItems(participantsList);
//         memberListView.setCellFactory(param -> new MemberCell(this));


         dialogRoot = new VBox(18, titleLabel, addPeopleBox, memberListView);
         dialogRoot.setAlignment(Pos.CENTER);
      }

      Party party = Model.getParty();
      String text = party.getPartyName() + " "
            + party.getLocation() + " "
            + party.getDate();

      titleLabel.setText(text);

      participantsList.clear();
      participantsList.addAll(Model.getParty().getParticipants());

      return dialogRoot;
   }
}
