package de.uniks.party;

import de.uniks.party.model.Participant;
import de.uniks.party.model.Party;
import de.uniks.party.model.ShoppingItem;
import de.uniks.party.model.tables.ParticipantTable;
import de.uniks.party.model.tables.PartyTable;
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
   public static final String HAVE_CONNECTION = "haveConnection";
   public static final String REMOVE_SHOPPING_ITEM = "removeShoppingItem";
   public static final String REMOVE_PARTICIPANT = "removeParticipant";

   private static ModelManager mm;

   private ModelDistribution distributor;

   private ModelManager()
   {
      distributor = new ModelDistribution(this);
      distributor.start();
   }


   public static ModelManager get()
   {
      if (mm == null)
      {
         mm = new ModelManager();
      }

      return mm;
   }


   public ModelDistribution getDistributor()
   {
      return distributor;
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
         if (distributor.getEventSource().isOverwritten(map)) continue;

         String oldTimeStampString = map.get(EventSource.EVENT_TIMESTAMP);

         distributor.getEventSource().setOldEventTimeStamp(oldTimeStampString);
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
         else if (REMOVE_PARTICIPANT.equals(map.get(EventSource.EVENT_TYPE)))
         {
            Participant participant = theParty.getParticipants(map.get(EventSource.EVENT_KEY));

            if (participant == null) continue; //======================

            removeParticipant(participant);
         }
         else if (REMOVE_SHOPPING_ITEM.equals(map.get(EventSource.EVENT_TYPE)))
         {
            String description = map.get(EventSource.EVENT_KEY);
            ShoppingItem toDel = null;

            for (ShoppingItem shoppingItem : theParty.getShoppingItems())
            {
               if (StrUtil.stringEquals(description, shoppingItem.getDescription()))
               {
                  toDel = shoppingItem;
                  break;
               }
            }
            if (toDel == null) continue;

            removeShoppingItem(toDel);
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
               responsible = haveParticipantProxy(responsibleName);
            }

            haveShoppingItem(description, price, responsible);
         }
         else if (HAVE_CONNECTION.equals(map.get(EventSource.EVENT_TYPE)))
         {
            String address = map.get(ModelDistribution.PROPERTY_serverAddress);

            haveConnection(address);
         }
      }

      distributor.getEventSource().setOldEventTimeStamp(0);

      // call event listeners
      for (Runnable runnable : viewListeners)
      {
         runnable.run();
      }
   }


   public void haveConnection(String address)
   {
      // no change?
      if (StrUtil.stringEquals(distributor.getServerStatus(), ModelDistribution.CONNECTED)
            && StrUtil.stringEquals(distributor.getServerAddress(), address)) return; //<=============

      distributor.setServerAddress(address);

      // close old connection
      if (StrUtil.stringEquals(distributor.getServerStatus(), ModelDistribution.CONNECTED))
      {
         try
         {
            distributor.getBufferedwriter().close();
            distributor.setServerStatus(ModelDistribution.NO_CONNECTION);
         }
         catch (IOException e)
         {
            e.printStackTrace();
         }
      }

      // open new connection
      distributor.connectToPartyServer();

      // fire event
      StringBuilder buf = new StringBuilder()
            .append("- " + EventSource.EVENT_TYPE + ": ").append(HAVE_CONNECTION).append("\n")
            .append("  " + EventSource.EVENT_KEY + ": ").append(Yamler.encapsulate(ModelDistribution.PROPERTY_serverAddress)).append("\n")
            .append("  " + ModelDistribution.PROPERTY_serverAddress + ": ").append(Yamler.encapsulate(distributor.getServerAddress())).append("\n")
            .append("\n");

      distributor.getEventSource().append(buf.toString());

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

      distributor.getEventSource().append(buf.toString());

      return result;
   }


   public void removeShoppingItem(ShoppingItem item)
   {
      item.removeYou();

      updateSaldi();

      StringBuilder buf = new StringBuilder()
            .append("- " + EventSource.EVENT_TYPE + ": ").append(REMOVE_SHOPPING_ITEM).append("\n")
            .append("  " + EventSource.EVENT_KEY + ": ").append(Yamler.encapsulate(item.getDescription())).append("\n");
      buf.append("\n");

      distributor.getEventSource().append(buf.toString());

   }

   public Participant haveParticipantProxy(String name)
   {
      LinkedHashMap<String, String> map = distributor.getEventSource().getEvent(name);

      if (map != null)
      {
         String eventType = map.get(EventSource.EVENT_TYPE);

         if (StrUtil.stringEquals(eventType, REMOVE_PARTICIPANT))
         {
            // do not create proxy
            Logger.getGlobal().info("Avoided to recreate removed participant by late shopping item.");
            return null;
         }
      }

      return haveParticipant(name);
   }



   public Participant haveParticipant(String name)
   {
      if (name == null || "".equals(name)) return null; //====================================

      // find old participant
      Participant result = theParty.getParticipants(name);

      if (result != null) return result; //====================================

      result = new Participant()
            .setName(name)
            .setParty(theParty);

      updateSaldi();

      StringBuilder buf = new StringBuilder()
            .append("- " + EventSource.EVENT_TYPE + ": ").append(HAVE_PARTICIPANT).append("\n")
            .append("  " + EventSource.EVENT_KEY + ": ").append(Yamler.encapsulate(name)).append("\n")
            .append("  " + Participant.PROPERTY_name + ": ").append(Yamler.encapsulate(name)).append("\n")
            .append("\n");

      distributor.getEventSource().append(buf.toString());

      return result;
   }



   public void removeParticipant(Participant participant)
   {
      participant.removeYou();

      updateSaldi();

      StringBuilder buf = new StringBuilder()
            .append("- " + EventSource.EVENT_TYPE + ": ").append(REMOVE_PARTICIPANT).append("\n")
            .append("  " + EventSource.EVENT_KEY + ": ").append(Yamler.encapsulate(participant.getName())).append("\n")
            .append("\n");

      distributor.getEventSource().append(buf.toString());
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

         distributor.getEventSource().append(buf.toString());
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
      double budget = new PartyTable(theParty)
            .expandShoppingItems()
            .expandPrice()
            .sum();

      double share = budget / theParty.getParticipants().size();

      new PartyTable(theParty)
            .expandParticipants()
            .filter(
                  p -> {
                     double myCosts = new ParticipantTable(p)
                           .expandItems()
                           .expandPrice()
                           .sum();
                     double mySaldo = myCosts - share;
                     p.setSaldo(mySaldo);
                     return true;
                  }
            );
   }


   ArrayList<Runnable> viewListeners = new ArrayList<>();

   public void addViewListener(Runnable listener)
   {
      viewListeners.add(listener);
   }

}
