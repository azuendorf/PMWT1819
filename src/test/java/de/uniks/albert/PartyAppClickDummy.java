package de.uniks.albert;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class PartyAppClickDummy extends Application
{

   private Scene scene;
   private Stage stage;
   private VBox startScreen;
   private TextField nameField;
   private TextField locationField;
   private TextField dateField;
   private BorderPane root;
   private Label partyLabel;
   private TextField participantNameField;
   private VBox addPeopleDialog;
   private VBox peopleScreen;

   @Override
   public void start(Stage stage) throws Exception
   {
      this.stage = stage;

      buttonBar();

      startScreen();
      initStartScreenStepList();

      addPeopleDialog();

      peopleScreen();

      root = new BorderPane();
      root.setCenter(startScreen);
      root.setPadding(new Insets(18));
      root.setStyle("-fx-background-color:white; " +
         "-fx-font-family: 'Comic Sans MS'; -fx-font-size: 18;");

      scene = new Scene(root);
      scene.setRoot(root);

      stage.setWidth(400);
      stage.setHeight(500);

      stage.setScene(scene);
      stage.show();

      System.out.println("Started");

      List<String> raw = getParameters().getRaw();

      if (raw.size() > 0 )
      {
         Executors.newCachedThreadPool().execute(() -> autoRun());
      }
   }

   private void peopleScreen()
   {
      Label label = new Label("Participants: ");
      Label albertLabel = new Label("Albert");
      Label albertSaldo = new Label("0,00 €");
      Button editAlbert = new Button("<");
      Button delAlbert = new Button("-");
      HBox hBox = new HBox(18, albertLabel, albertSaldo, editAlbert, delAlbert);
      peopleScreen = new VBox(18, buttonBar(), partyLabel, label, hBox);
      peopleScreen.setAlignment(Pos.TOP_CENTER);
      peopleScreen.setOnMouseClicked(e -> addPeopleDialogStep());
   }

   int currentPeopleScreenStep = 0;

   private void peopleScreenStep()
   {
      if (currentPeopleScreenStep == 0)
      {
         currentPeopleScreenStep++;
         root.setCenter(peopleScreen);
      }
   }

   private void addPeopleDialog()
   {
      partyLabel = new Label("X-Mas Party, Wednesday, SE Lab:");
      Label label = new Label("Add / Edit participant: ");
      Label nameLabel = new Label("Name: ");
      participantNameField = new TextField();
      Button okButton = new Button("OK");
      okButton.setOnAction(e -> peopleScreenStep());
      addPeopleDialog = new VBox(18, buttonBar(), partyLabel, label, nameLabel, participantNameField, okButton);
      addPeopleDialog.setAlignment(Pos.TOP_CENTER);
      addPeopleDialog.setOnMouseClicked(e -> addPeopleDialogStep());
   }

   int currentPeopleDialogStep = 0;

   private void addPeopleDialogStep()
   {
      if (currentPeopleDialogStep == 0)
      {
         currentPeopleDialogStep++;
         root.setCenter(addPeopleDialog);
      }
      else if (currentPeopleDialogStep == 1)
      {
         currentPeopleDialogStep++;
         participantNameField.setText("Albert");
      }
      else if (currentPeopleDialogStep == 2)
      {
         currentPeopleDialogStep++;
         peopleScreenStep();
      }
   }


   private VBox startScreen()
   {
      Label nameLabel = new Label("Party Name: ");
      nameField = new TextField();

      Label locationLabel = new Label("Location: ");
      locationField = new TextField();

      Label dateLabel = new Label("Date: ");
      dateField = new TextField();

      Button nextButton = new Button("->");
      nextButton.setOnAction(e -> addPeopleDialogStep());

      startScreen = new VBox(18, buttonBar(), nameLabel, nameField, locationLabel, locationField, dateLabel, dateField, nextButton);
      startScreen.setAlignment(Pos.TOP_CENTER);

      startScreen.setOnMouseClicked(value -> startScreenStep());

      return startScreen;
   }


   int currentStartScreenStep = 0;
   ArrayList<Runnable> startScreenStepList = null;

   public void initStartScreenStepList ()
   {
      startScreenStepList = new ArrayList<>();

      startScreenStepList.add(() ->
      {
         doScreenDump("tmp/clickCounter/EmptyStartScreen.png");
         nameField.setText("X-Mas Party");
      });
      startScreenStepList.add(() -> locationField.setText("SE Lab"));
      startScreenStepList.add(() -> dateField.setText("Wednesday"));
      startScreenStepList.add(() ->
      {
         doScreenDump("tmp/clickCounter/FilledStartScreen.png");
         addPeopleDialogStep();
      });
   }

   private void startScreenStep()
   {
      if (currentStartScreenStep < startScreenStepList.size())
      {
         startScreenStepList.get(currentStartScreenStep).run();
         currentStartScreenStep++;
      }

   }



   private HBox buttonBar()
   {
      Button start = new Button("Start");
      Button people = new Button("People");
      Button shopping = new Button("Shopping");

      HBox myBar = new HBox(18, start, people, shopping);

      return myBar;
   }


   private void autoRun()
   {
      // run auto
      try
      {
         for (Runnable r : startScreenStepList)
         {
            Platform.runLater(r);
            Thread.sleep(1000);
         }



         Thread.sleep(1000);
         Platform.runLater(()-> addPeopleDialogStep()); // empty
         Platform.runLater(() -> doScreenDump("tmp/clickCounter/EmptyAddPeople.png"));

         Thread.sleep(1000);
         Platform.runLater(()-> addPeopleDialogStep()); // Albert
         Platform.runLater(() -> doScreenDump("tmp/clickCounter/AlbertAddPeople.png"));

         Thread.sleep(1000);
         Platform.runLater(()-> peopleScreenStep()); // people list with Albert
         Platform.runLater(() -> doScreenDump("tmp/clickCounter/PeopleScreenWithAlbert.png"));

         Thread.sleep(1000);

         System.exit(0);
      }
      catch (InterruptedException e)
      {
         e.printStackTrace();
      }
   }

   private void doScreenDump(String fileName)
   {
      WritableImage image = scene.snapshot(null);

      // TODO: probably use a file chooser here



      File file = new File(fileName);

      file.mkdirs();

      try {
         ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
      } catch (Exception e) {
         // TODO: handle exception here
      }
   }


}
