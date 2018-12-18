package de.uniks.party;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class PartyApp extends Application
{
   @Override
   public void start(Stage stage) throws Exception
   {
      PartyController partyController = new PartyController();

      Parent view = partyController.getDialogView();


      Scene scene = new Scene(view, 600, 800);
      stage.setScene(scene);
      stage.show();
   }
}
