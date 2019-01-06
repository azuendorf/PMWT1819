package de.uniks.party;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PartyServer
{

   private ModelAccess modelAccess;

   public static void main(String[] args)
   {
      new PartyServer().run();
   }


   private void run()
   {
      try
      {
         ServerSocket serverSocket = new ServerSocket(42424);

         modelAccess = new ModelAccess();

         while (true)
         {
            Socket newSocket = serverSocket.accept();
            modelAccess.executeAddNewSocket(newSocket);

         }

      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }










}
