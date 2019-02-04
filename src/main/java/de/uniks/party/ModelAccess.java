package de.uniks.party;

import org.fulib.yaml.Yamler;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ModelAccess
{
   private final ChannelReaders channelReaders;
   private ExecutorService singleExecutor;

   private ArrayList<Socket> sockets = new ArrayList<>();
   private LinkedHashMap<Socket,OutputStreamWriter> writers = new LinkedHashMap<>();
   private EventFiler eventFiler;
   private static String historyFileName;
   private EventSource eventSource;
   private Yamler yamler = new Yamler();



   public ModelAccess()
   {
      eventSource = new EventSource();

      if (historyFileName == null)
      {
         historyFileName = "tmp/PartyData/serverEvents.yaml";
      }

      eventFiler = new EventFiler(eventSource).setHistoryFileName(historyFileName);

      String yaml = eventFiler.loadHistory();
      eventSource.append(yaml);

      eventFiler.storeHistory();

      eventFiler.startEventLogging();

      singleExecutor = Executors.newSingleThreadExecutor();
      channelReaders = new ChannelReaders(this);
   }

   public void executeAddNewSocket(Socket newSocket)
   {
      singleExecutor.execute(() -> addNewSocket(newSocket));
   }

   // executed by singleExecutor in a single thread
   private void addNewSocket(Socket newSocket)
   {
      channelReaders.executeReadChannel(newSocket);
      sockets.add(newSocket);

      try
      {
         OutputStream outputStream = newSocket.getOutputStream();
         OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
         writers.put(newSocket, outputStreamWriter);

         outputStreamWriter.write(eventSource.encodeYaml());
         outputStreamWriter.flush();
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }


   public void executeRemoveSocket(Socket newSocket)
   {
      singleExecutor.execute(() -> removeSocket(newSocket));
   }

   private void removeSocket(Socket newSocket)
   {
      writers.remove(newSocket);
      sockets.remove(newSocket);
   }



   public void executeHandleMessage(String text)
   {
      singleExecutor.execute(() -> handleMessage(text));
   }

   // executed by singleExecutor in a single thread
   private void handleMessage(String message)
   {
      ArrayList<LinkedHashMap<String, String>> list = yamler.decodeList(message);

      for (LinkedHashMap<String, String> event : list)
      {
         if (eventSource.isOverwritten(event)) continue; //==========================

         eventSource.append(event);

         // send message to all sockets
         for (Socket socket : sockets)
         {
            try
            {
               OutputStreamWriter out = writers.get(socket);
               out.write(message);
               out.flush();

               // System.out.println("have send:\n" + message);
            }
            catch (IOException e)
            {
               e.printStackTrace();
            }
         }
      }
      System.out.println("handling done: \n" + message +"\n");
   }

}
