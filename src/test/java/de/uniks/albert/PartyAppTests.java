package de.uniks.albert;

import de.uniks.party.PartyApp;
import javafx.stage.Stage;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

public class PartyAppTests extends ApplicationTest
{

   private PartyApp partyApp;

   @Override
   public void start(Stage stage) throws Exception
   {
      partyApp = new PartyApp();
      partyApp.start(stage);
   }

   @Test
   public void testXMasTest()
   {
      clickOn("#nameField");

      write("X Mas\t");
      write("SE Lab\t");
      write("Wednesday\t");

      clickOn("->");



      System.out.println();
   }
}
