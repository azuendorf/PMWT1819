package de.uniks.party;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class PartyServer
{

   private ExecutorService serverService;
   private ExecutorService readersService;

   public static void main(String[] args)
   {
      new PartyServer().run();
   }

   private ArrayList<Socket> sockets = new ArrayList<>();
   private LinkedHashMap<Socket,OutputStreamWriter> writers = new LinkedHashMap<>();
   private StringBuilder allMessages = new StringBuilder();

   private void run()
   {
      try
      {
         ServerSocket serverSocket = new ServerSocket(42424);

         serverService = Executors.newSingleThreadExecutor();
         readersService = Executors.newCachedThreadPool();

         while (true)
         {
            Socket newSocket = serverSocket.accept();
            serverService.execute(() -> addNewSocket(newSocket));

         }

      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }

   // executed by serverService in a single thread
   private void addNewSocket(Socket newSocket)
   {
      readersService.execute( () -> readChannel(newSocket));
      sockets.add(newSocket);

      try
      {
         OutputStream outputStream = newSocket.getOutputStream();
         OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
         writers.put(newSocket, outputStreamWriter);

         outputStreamWriter.write(allMessages.toString());
         outputStreamWriter.flush();
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }

   private void removeSocket(Socket newSocket)
   {
      writers.remove(newSocket);
      sockets.remove(newSocket);
   }



   // executed by serverService in a single thread
   private void handleMessage(String message)
   {
      allMessages.append(message);

      // send message to all sockets
      for (Socket socket : sockets)
      {
         try
         {
            OutputStreamWriter out = writers.get(socket);
            out.write(message);
            out.flush();

            System.out.println("have send:\n" + message);
         }
         catch (IOException e)
         {
            e.printStackTrace();
         }
      }
      System.out.println("handling done: ");
   }

   // executed in parallel by many readerServices
   private void readChannel(Socket newSocket)
   {
      try
      {
         InputStream inputStream = newSocket.getInputStream();
         InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
         BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

         StringBuilder buf = new StringBuilder();
         while (true)
         {
            String line = bufferedReader.readLine();

            if (line == null) break;

            buf.append(line).append("\n");

            if (line.equals(""))
            {
               String text = buf.toString();

               if (text.trim().isEmpty()) continue;

               // end of one event
               System.out.println("Channel reader got:\n" + buf.toString());

               serverService.execute(() -> handleMessage(text));

               buf.setLength(0);
            }
         }

      }
      catch (IOException e)
      {
         // socket is dead, remove it
         serverService.execute(() -> removeSocket(newSocket));
      }
   }



}
