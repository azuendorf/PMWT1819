package de.uniks.albert;

import de.uniks.party.ModelManager;
import de.uniks.party.PartyApp;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import java.io.File;

public class PartyAppTests extends ApplicationTest
{

   private PartyApp partyApp;

   @Override
   public void start(Stage stage) throws Exception
   {
      // remove old log files
      File file = new File("tmp/PartyData/partyEvents.yaml");
      if (file.exists()) file.delete();

      partyApp = new PartyApp();
      partyApp.start(stage);
   }

   @Test
   public void testXMasTest()
   {
      lookup("#nameField").query();
      assertThat(lookup("#nameField").query(), instanceOf(TextField.class));
      clickOn("#nameField");

      write("X Mas\t");

      assertThat(lookup(node -> node.isFocused()).query().getId(), equalTo("locationField"));

      write("SE Lab\t");

      assertThat(lookup(node -> node.isFocused()).query().getId(), equalTo("dateField"));

      write("Wednesday\t");

      clickOn("->");

      write("Nina");

      clickOn("#okButton");

      addParticipant("Albert");
      addParticipant("Eyshe");

      clickOn("Shopping");

      write("beer\t");
      write("22,00\t");
      clickOn("#choice");
      clickOn("Albert");
      clickOn("OK");

      assertThat(ModelManager.getParty().getShoppingItems().get(0).getPrice(), equalTo(22.0));

      clickOn("+");

      write("meat\t");
      write("42\t");
      clickOn("#choice");
      clickOn("Nina");
      clickOn("OK");

      assertThat(ModelManager.getParty().getShoppingItems().get(1).getPrice(), equalTo(42.0));

      clickOn("People");

      System.out.println();
   }

   private void addParticipant(String name)
   {
      clickOn("#addPeopleButton");

      write(name);

      clickOn("#okButton");
   }
}
