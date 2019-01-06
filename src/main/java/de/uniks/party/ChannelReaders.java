package de.uniks.party;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChannelReaders
{
   private ModelAccess modelAccess;
   private ExecutorService readersExecutor;

   public ChannelReaders(ModelAccess modelAccess)
   {
      this.modelAccess = modelAccess;
      readersExecutor = Executors.newCachedThreadPool();
   }

   public void executeReadChannel(Socket newSocket)
   {
      readersExecutor.execute( () -> readChannel(newSocket));

   }

   // executed in parallel by many readerServices
   public void readChannel(Socket newSocket)
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
               // System.out.println("Channel reader got:\n" + buf.toString());

               modelAccess.executeHandleMessage(text);

               buf.setLength(0);
            }
         }

      }
      catch (IOException e)
      {
         // socket is dead, remove it
         modelAccess.executeRemoveSocket(newSocket);
      }
   }

}
