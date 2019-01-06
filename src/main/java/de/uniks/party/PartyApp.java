package de.uniks.party;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.List;

public class PartyApp extends Application
{
   @Override
   public void start(Stage stage) throws Exception
   {
      Parameters parameters = getParameters();
      List<String> parametersRaw = parameters.getRaw();
      if (parametersRaw != null && parametersRaw.size() > 0)
      {
         String fileName = parametersRaw.get(0);
         ModelDistribution.setHistoryFileName(fileName);
      }

      ModelManager.get();

      PartyController partyController = new PartyController();

      Parent view = partyController.getDialogView();


      Scene scene = new Scene(view, 600, 800);
      stage.setScene(scene);
      stage.setX(50);

      stage.setOnCloseRequest(e -> System.exit(0));

      stage.show();
   }
}
