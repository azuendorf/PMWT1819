package de.uniks.albert;

import de.uniks.albert.gui.ClickCounter;
import javafx.application.Platform;
// import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Label;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import org.junit.Assert;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.service.query.NodeQuery;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;

public class ClickCounterTestPreparation extends ApplicationTest
{

   private ClickCounter clickCounter;
   private Stage stage;

   @Override
   public void start(Stage stage) throws Exception
   {
      this.stage = stage;
      clickCounter = new ClickCounter();
      clickCounter.start(stage);
   }

   @Test
   public void test3Clicks()
   {
      clickOn(".button");
      clickOn(".label");
      clickOn("#clickMe");

      NodeQuery countLabel = lookup("#countLabel");
      Label node = countLabel.query();
      String text = node.getText();

      Assert.assertThat(text, equalTo("2"));

      Platform.runLater(() ->
      saveAsPng(fromAll().query(), "tmp/screenShot.png")
);

      sleep(300);

      System.out.println(clickCounter.counter);
   }

   public void saveAsPng(Node node, String fileName) {
      WritableImage image = node.snapshot(new SnapshotParameters(), null);

      // TODO: probably use a file chooser here
      File file = new File(fileName);

//      try {
//        // ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
//      } catch (IOException e) {
//         // TODO: handle exception here
//      }
   }
}
