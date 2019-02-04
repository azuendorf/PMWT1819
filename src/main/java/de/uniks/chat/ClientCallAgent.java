package de.uniks.chat;

import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientCallAgent
{
   private ChatClient chatClient;
   private Socket socket;

   public ClientCallAgent(ChatClient chatClient, Socket socket)
   {
      this.chatClient = chatClient;
      this.socket = socket;

      ExecutorService executor = Executors.newSingleThreadExecutor();
      executor.execute(() -> doReadMessages());
   }

   private void doReadMessages()
   {
      try
      {
         InputStream inputStream = socket.getInputStream();
         InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
         BufferedReader in = new BufferedReader(inputStreamReader);
         while (true)
         {
            String line = in.readLine() + "\n";

            Platform.runLater(() -> chatClient.addMessage(line));
         }
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }

   }
}
