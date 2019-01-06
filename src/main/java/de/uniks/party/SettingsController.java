package de.uniks.party;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class SettingsController
{

   private PartyController partyController;

   private VBox dialogRoot;
   private TextField serverAddrField;
   private String oldContent = null;


   public SettingsController(PartyController partyController)
   {

      this.partyController = partyController;
   }


   public VBox getView()
   {
      if (dialogRoot == null)
      {
         Label titleLabel = new Label("Connection Settings");
         Label serverLabel = new Label("server address and port:");
         serverAddrField = new TextField();
         serverAddrField.setAlignment(Pos.CENTER);

         Label statusLabel = new Label(ModelManager.get().getServerStatus());
         ModelManager.get().addPropertyChangeListener(e -> updateStatusLabel(statusLabel));

         Button connectButton = new Button("(Re-) Connect");
         connectButton.setOnAction(e -> reconnectAction());
         HBox connectLine = new HBox(18, statusLabel, connectButton);
         connectLine.setAlignment(Pos.CENTER);

         dialogRoot = new VBox(18, titleLabel, serverLabel, serverAddrField, connectLine);
         dialogRoot.setAlignment(Pos.CENTER);
         dialogRoot.setPadding(new Insets(0, 18, 0, 18));
      }

      oldContent = ModelManager.get().getServerAddress();
      serverAddrField.setText(oldContent);

      return dialogRoot;
   }

   public void reconnectAction()
   {
      if (serverAddrField == null) return; //===========================

      ModelManager.get().haveConnection(serverAddrField.getText());
   }

   private void updateStatusLabel(Label statusLabel)
   {
      String serverStatus = ModelManager.get().getServerStatus();
      statusLabel.setText(serverStatus);
      System.out.println("New status label " + serverStatus);
   }
}
