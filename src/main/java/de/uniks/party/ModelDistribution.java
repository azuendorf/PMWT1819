package de.uniks.party;

import javafx.application.Platform;
import org.fulib.yaml.StrUtil;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.*;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ModelDistribution
{
   public static final String CONNECTED = "connected";
   public static final String NO_CONNECTION = "noConnection";


   private EventFiler eventFiler;
   private EventSource eventSource;
   private static String historyFileName;
   private BufferedWriter bufferedwriter;
   public static final String PROPERTY_serverAddress = "serverAddress";
   private String serverAddress = "localhost:42424";
   private Socket socket = null;
   private ExecutorService readerService;
   private ModelManager mm;

   public ModelDistribution(ModelManager mm)
   {
      this.mm = mm;
   }

   public ModelDistribution start()
   {
      eventSource = new EventSource();
      if (historyFileName == null)
      {
         historyFileName = "tmp/PartyData/partyEvents.yaml";
      }

      eventFiler = new EventFiler(eventSource).setHistoryFileName(historyFileName);

      connectToPartyServer();

      eventSource.addEventListener(event -> publishEvent(event));

      String yaml = eventFiler.loadHistory();
      mm.applyEvents(yaml);

      eventFiler.storeHistory();

      eventFiler.startEventLogging();

      return this;
   }

   public EventSource getEventSource()
   {
      if (eventSource == null)
      {
         eventSource = new EventSource();
      }
      return eventSource;
   }

   public BufferedWriter getBufferedwriter()
   {
      return bufferedwriter;
   }

   public String getServerAddress()
   {
      return serverAddress;
   }

   public void setServerAddress(String serverAddress)
   {
      this.serverAddress = serverAddress;
   }

   public static final String PROPERTY_serverStatus = "serverStatus";
   private String serverStatus = "unknown";

   public String getServerStatus()
   {
      return serverStatus;
   }

   public void setServerStatus(String serverStatus)
   {
      if (StrUtil.stringEquals(serverStatus, this.serverStatus)) return;

      String oldStatus = this.serverStatus;
      this.serverStatus = serverStatus;

      firePropertyChange(PROPERTY_serverStatus, oldStatus, this.serverStatus);
   }

   public static void setHistoryFileName(String historyFileName)
   {
      ModelDistribution.historyFileName = historyFileName;
   }



   // called on new events within gui thread
   private void publishEvent(LinkedHashMap<String, String> event)
   {
      if ( ! StrUtil.stringEquals(this.serverStatus, CONNECTED)) return; //<===== sudden death

      String yaml = EventSource.encodeYaml(event);

      try
      {
         // System.out.println("sending \n" + yaml);
         bufferedwriter.write(yaml);
         bufferedwriter.flush();
      }
      catch (IOException e)
      {
         setServerStatus(NO_CONNECTION);
      }
   }



   // run by single thread executor
   private void readMessages(Socket socket)
   {
      try
      {
         InputStream inputStream = socket.getInputStream();
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
               // System.out.println("party got:\n" + buf.toString());

               Platform.runLater(() -> mm.applyEvents(text));

               buf.setLength(0);
            }
         }

      }
      catch (IOException e)
      {
         Platform.runLater( () ->setServerStatus(NO_CONNECTION));
      }
      // System.out.println("Closing old reader service");
   }

   public void connectToPartyServer()
   {
      if (StrUtil.stringEquals(serverStatus, CONNECTED)) return; //<======== sudden death

      try
      {
         String[] split = serverAddress.split(":");
         String host = split[0];
         int port = Integer.parseInt(split[1]);
         socket = new Socket(host, port);
         bufferedwriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
         if (readerService == null)
         {
            readerService = Executors.newSingleThreadExecutor();
         }
         readerService.execute(() -> readMessages(socket));

         bufferedwriter.write(eventSource.encodeYaml());
         bufferedwriter.flush();

         setServerStatus(CONNECTED);

      }
      catch (IOException e)
      {
         setServerStatus(NO_CONNECTION);
      }
   }

   protected PropertyChangeSupport listeners = null;

   public boolean firePropertyChange(String propertyName, Object oldValue, Object newValue)
   {
      if (listeners != null)
      {
         listeners.firePropertyChange(propertyName, oldValue, newValue);
         return true;
      }
      return false;
   }

   public boolean addPropertyChangeListener(PropertyChangeListener listener)
   {
      if (listeners == null)
      {
         listeners = new PropertyChangeSupport(this);
      }
      listeners.addPropertyChangeListener(listener);
      return true;
   }

   public boolean addPropertyChangeListener(String propertyName, PropertyChangeListener listener)
   {
      if (listeners == null)
      {
         listeners = new PropertyChangeSupport(this);
      }
      listeners.addPropertyChangeListener(propertyName, listener);
      return true;
   }

   public boolean removePropertyChangeListener(PropertyChangeListener listener)
   {
      if (listeners != null)
      {
         listeners.removePropertyChangeListener(listener);
      }
      return true;
   }

   public boolean removePropertyChangeListener(String propertyName,PropertyChangeListener listener)
   {
      if (listeners != null)
      {
         listeners.removePropertyChangeListener(propertyName, listener);
      }
      return true;
   }
}
