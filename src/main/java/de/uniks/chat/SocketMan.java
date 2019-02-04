package de.uniks.chat;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SocketMan
{

   private final ExecutorService executor;
   private final ExecutorService agentsExecutor;

   public SocketMan()
   {
      executor = Executors.newSingleThreadExecutor();
      agentsExecutor = Executors.newCachedThreadPool();
   }

   private ArrayList<Socket> socketList = new ArrayList<>();


   public void addNewSocket(Socket socket)
   {
      executor.execute(() -> doAddSocket(socket));
   }

   // internal executed only by a single thread
   private void doAddSocket(Socket socket)
   {
      socketList.add(socket);

      CallAgent callAgent = new CallAgent(this, socket);

      agentsExecutor.execute(callAgent);
   }

   public void newMessage(String line)
   {
      executor.execute(() -> doNewMessage(line));
   }

   private void doNewMessage(String line)
   {
      System.out.println("Sending\n" + line);
      for (Socket socket : socketList)
      {
         try
         {
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(line.getBytes());
            outputStream.flush();
         }
         catch (IOException e)
         {
            // kill socket
            removeSocket(socket);
         }
      }

   }

   public void removeSocket(Socket socket)
   {
      System.out.println("should remove socket");
   }
}
