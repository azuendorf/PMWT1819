package de.uniks.party;

import de.uniks.party.model.Participant;
import de.uniks.party.model.Party;
import de.uniks.party.model.ShoppingItem;
import javafx.application.Platform;
import org.fulib.yaml.StrUtil;
import org.fulib.yaml.Yamler;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.*;
import java.net.Socket;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ModelManager
{
   public static final String HAVE_PARTY = "haveParty";
   public static final String HAVE_PARTICIPANT = "haveParticipant";
   public static final String HAVE_SHOPPING_ITEM = "haveShoppingItem";
   public static final String CONNECTED = "connected";
   public static final String NO_CONNECTION = "noConnection";
   public static final String HAVE_CONNECTION = "haveConnection";
   private static ModelManager mm;
   private EventFiler eventFiler;
   private static String historyFileName;
   private EventSource eventSource;
   private BufferedWriter bufferedwriter;


   public static final String PROPERTY_serverAddress = "serverAddress";
   private String serverAddress = "localhost:42424";
   private ExecutorService readerService;

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
      ModelManager.historyFileName = historyFileName;
   }


   public static ModelManager get()
   {
      if (mm == null)
      {
         ModelManager.getParty();
         mm = new ModelManager();
      }

      return mm;
   }


   private ModelManager()
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
      applyEvents(yaml);

      eventFiler.storeHistory();

      eventFiler.startEventLogging();

   }

   private Socket socket = null;


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

               Platform.runLater(() -> applyEvents(text));

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


   // event handling
   public void applyEvents(String yaml)
   {
      if (yaml == null) return;

      Yamler yamler = new Yamler();
      ArrayList<LinkedHashMap<String, String>> list = yamler.decodeList(yaml);
      applyEvents(list);
   }

   public void applyEvents(ArrayList<LinkedHashMap<String, String>> events)
   {
      for (LinkedHashMap<String, String> map : events)
      {
         if (eventSource.isOverwritten(map)) continue;

         String oldTimeStampString = map.get(EventSource.EVENT_TIMESTAMP);

         eventSource.setOldEventTimeStamp(oldTimeStampString);
         if (HAVE_PARTY.equals(map.get(EventSource.EVENT_TYPE)))
         {
            String name = map.get(Party.PROPERTY_partyName);
            String location = map.get(Party.PROPERTY_location);
            String date = map.get(Party.PROPERTY_date);

            haveParty(name, location, date);
         }
         else if (HAVE_PARTICIPANT.equals(map.get(EventSource.EVENT_TYPE)))
         {
            String name = map.get(Participant.PROPERTY_name);

            haveParticipant(name);
         }
         else if (HAVE_SHOPPING_ITEM.equals(map.get(EventSource.EVENT_TYPE)))
         {
            String description = map.get(ShoppingItem.PROPERTY_description);
            String priceTxt = map.get(ShoppingItem.PROPERTY_price);
            String responsibleName = map.get(ShoppingItem.PROPERTY_responsible);

            double price = 0.0;
            try
            {
               price = NumberFormat.getInstance(Locale.ENGLISH).parse(priceTxt).doubleValue();
            }
            catch (ParseException e)
            {
               Logger.getGlobal().log(Level.SEVERE, "price unparsable " + priceTxt, e);
            }

            Participant responsible = null;
            if (responsibleName != null && ! "".equals(responsibleName.trim()))
            {
               responsible = haveParticipant(responsibleName);
            }

            haveShoppingItem(description, price, responsible);
         }
         else if (HAVE_CONNECTION.equals(map.get(EventSource.EVENT_TYPE)))
         {
            String address = map.get(PROPERTY_serverAddress);

            haveConnection(address);
         }
      }

      eventSource.setOldEventTimeStamp(0);

      // call event listeners
      for (Runnable runnable : viewListeners)
      {
         runnable.run();
      }
   }

   public void haveConnection(String address)
   {
      // no change?
      if (StrUtil.stringEquals(this.serverStatus, CONNECTED) && StrUtil.stringEquals(this.serverAddress, address)) return; //<=============

      setServerAddress(address);

      // close old connection
      if (StrUtil.stringEquals(this.serverStatus, CONNECTED))
      {
         try
         {
            bufferedwriter.close();
            setServerStatus(NO_CONNECTION);
         }
         catch (IOException e)
         {
            e.printStackTrace();
         }
      }

      // open new connection
      connectToPartyServer();

      // fire event
      StringBuilder buf = new StringBuilder()
            .append("- " + EventSource.EVENT_TYPE + ": ").append(HAVE_CONNECTION).append("\n")
            .append("  " + EventSource.EVENT_KEY + ": ").append(Yamler.encapsulate(PROPERTY_serverAddress)).append("\n")
            .append("  " + PROPERTY_serverAddress + ": ").append(Yamler.encapsulate(serverAddress)).append("\n")
            .append("\n");

      eventSource.append(buf.toString());

   }


   public ShoppingItem haveShoppingItem(String description, double price, Participant responsible)
   {
      if (description == null || "".equals(description))
      {
         return null;
      }

      // find old item
      ShoppingItem result = null;
      for (ShoppingItem i : theParty.getShoppingItems())
      {
         if (description.equals(i.getDescription()))
         {
            result = i;
            break;
         }
      }

      // no change?
      if (result != null && result.getPrice() == price && result.getResponsible() == responsible) return result;

      // do it
      if (result == null)
      {
         result = new ShoppingItem().setParty(theParty);
      }

      result.setDescription(description)
            .setPrice(price)
            .setResponsible(responsible);

      updateSaldi();

      // fire event
      StringBuilder buf = new StringBuilder()
            .append("- " + EventSource.EVENT_TYPE + ": ").append(HAVE_SHOPPING_ITEM).append("\n")
            .append("  " + EventSource.EVENT_KEY + ": ").append(Yamler.encapsulate(description)).append("\n")
            .append("  " + ShoppingItem.PROPERTY_description + ": ").append(Yamler.encapsulate(description)).append("\n")
            .append("  " + ShoppingItem.PROPERTY_price + ": ").append("" + price).append("\n");
      if (responsible != null)
      {
         buf.append("  " + ShoppingItem.PROPERTY_responsible + ": ").append(Yamler.encapsulate(responsible.getName())).append("\n");
      }
      buf.append("\n");

      eventSource.append(buf.toString());

      return result;
   }


   public Participant haveParticipant(String name)
   {
      if (name == null || "".equals(name))
      {
         return null;
      }

      // find old participant
      Participant result = null;
      for (Participant p : theParty.getParticipants())
      {
         if (name.equals(p.getName()))
         {
            result = p;
            break;
         }
      }

      if (result != null) return result;

      result = new Participant()
            .setName(name)
            .setParty(theParty);

      updateSaldi();

      StringBuilder buf = new StringBuilder()
            .append("- " + EventSource.EVENT_TYPE + ": ").append(HAVE_PARTICIPANT).append("\n")
            .append("  " + EventSource.EVENT_KEY + ": ").append(Yamler.encapsulate(name)).append("\n")
            .append("  " + Participant.PROPERTY_name + ": ").append(Yamler.encapsulate(name)).append("\n")
            .append("\n");

      eventSource.append(buf.toString());

      return result;
   }


   // model api
   public Party haveParty(String name, String location, String date)
   {
      return haveParty(name, location, date, null);
   }

   public Party haveParty(String name, String location, String date, String oldContent)
   {
      Party party = mm.getParty();

      if (oldContent == null)
      {
         oldContent = String.format("%s|%s|%s", party.getPartyName(), party.getLocation(), party.getDate());
      }

      party.setPartyName(name)
            .setLocation(location)
            .setDate(date);

      String newContent = String.format("%s|%s|%s", party.getPartyName(), party.getLocation(), party.getDate());

      if ( ! newContent.equals(oldContent))
      {
         // fire event
         StringBuilder buf = new StringBuilder()
               .append("- " + EventSource.EVENT_TYPE + ": ").append(HAVE_PARTY).append("\n")
               .append("  " + EventSource.EVENT_KEY + ": ").append("theParty").append("\n")
               .append("  " + Party.PROPERTY_partyName + ": ").append(Yamler.encapsulate(name)).append("\n")
               .append("  " + Party.PROPERTY_location + ": ").append(Yamler.encapsulate(location)).append("\n")
               .append("  " + Party.PROPERTY_date + ": ").append(Yamler.encapsulate(date)).append("\n")
               .append("\n");

         eventSource.append(buf.toString());
      }

      return party;
   }


   //============= old stuff
   private static Party theParty;

   public static Party getParty()
   {
      if (theParty == null)
      {
         theParty = new Party();

         // DataManager.get().attach(theParty, "tmp/partyApp");

         ModelManager.get();

         // keep saldi up to date
         // new ModelListener(theParty, e -> updateSaldi());

      }

      return theParty;
   }



   private static void updateSaldi()
   {
      double budget = 0;

      for (ShoppingItem item : theParty.getShoppingItems())
      {
         budget += item.getPrice();
      }

      for (Participant p : theParty.getParticipants())
      {
         double sum = 0;
         for (ShoppingItem item : p.getItems())
         {
            sum += item.getPrice();
         }

         double share = budget / theParty.getParticipants().size();

         double saldo = share - sum;

         p.setSaldo(saldo);
      }
   }

   ArrayList<Runnable> viewListeners = new ArrayList<>();

   public void addViewListener(Runnable listener)
   {
      viewListeners.add(listener);
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
