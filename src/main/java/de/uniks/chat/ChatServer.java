package de.uniks.chat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChatServer
{
   public static void main(String[] args)
   {
      try
      {
         SocketMan socketMan = new SocketMan();
         ServerSocket serverSocket = new ServerSocket(42424);

         while (true)
         {
            try
            {
               Socket socket = serverSocket.accept();
               socketMan.addNewSocket(socket);
               System.out.println("accepted new connection");
            }
            catch (IOException e)
            {
               Logger.getGlobal().log(Level.SEVERE, "no accept", e);
            }
         }

      }
      catch (Exception e)
      {
         Logger.getGlobal().log(Level.SEVERE, "upss", e);
      }

   }
}
