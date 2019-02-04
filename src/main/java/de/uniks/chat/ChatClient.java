package de.uniks.chat;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;

public class ChatClient extends Application
{

   private String userName;
   private VBox chatBox;
   private TextField inputLine;
   private Socket socket;

   @Override
   public void start(Stage stage) throws Exception
   {
      List<String> paramList = getParameters().getRaw();

      userName = paramList.get(0);


      socket = new Socket("localhost", 42424);

      OutputStream outputStream = socket.getOutputStream();
      String msg = "Hello from " + userName + "\n";
      outputStream.write(msg.getBytes());
      outputStream.flush();

      new ClientCallAgent(this, socket);

      Label title = new Label("Chat for " + userName);

      chatBox = new VBox(18);

      inputLine = new TextField();
      inputLine.setOnKeyTyped(e -> onKeyTyped(e));
      Button sendButton = new Button("Send");
      HBox hBox = new HBox(18, inputLine, sendButton);
      sendButton.setOnAction(e -> sendAction());


      VBox root = new VBox(18, title, chatBox, hBox);
      root.setPadding(new Insets(18));
      root.setStyle("-fx-font-size: 18");
      Scene scene = new Scene(root, 400, 600);
      stage.setScene(scene);
      stage.show();
   }

   private void onKeyTyped(KeyEvent e)
   {
      if (e.getCharacter().equals("\r"))
      {
         sendAction();
      }
   }

   private void sendAction()
   {
      String text = inputLine.getText() + "\n";
      if (text != null && ! text.trim().equals(""))
      {
         try
         {
            OutputStream outputStream = socket.getOutputStream();

            outputStream.write(text.getBytes());
            outputStream.flush();
         }
         catch (IOException e)
         {
            e.printStackTrace();
         }

         inputLine.setText("");
         inputLine.requestFocus();
      }
   }

   public void addMessage(String line)
   {
      Label label = new Label(line);
      chatBox.getChildren().add(label);
   }
}
