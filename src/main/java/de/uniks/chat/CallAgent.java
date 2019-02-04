package de.uniks.chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class CallAgent implements Runnable
{
   private SocketMan socketMan;
   private Socket socket;

   public CallAgent(SocketMan socketMan, Socket socket)
   {
      this.socketMan = socketMan;
      this.socket = socket;
   }

   public void run()
   {
      try
      {
         InputStream inputStream = socket.getInputStream();
         InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
         BufferedReader in = new BufferedReader(inputStreamReader);
         while (true)
         {
            String line = in.readLine() + "\n";

            socketMan.newMessage(line);
         }
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }

      socketMan.removeSocket(socket);
   }
}
