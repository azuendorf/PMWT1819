package de.uniks.party;

import de.uniks.party.model.Participant;
import de.uniks.party.model.Party;
import de.uniks.party.model.ShoppingItem;
import jdk.jfr.Event;
import org.fulib.yaml.ModelListener;
import org.fulib.yaml.Yamler;

import java.io.*;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ModelManager
{
   public static final String HAVE_PARTY = "haveParty";
   public static final String HAVE_PARTICIPANT = "haveParticipant";
   public static final String HAVE_SHOPPING_ITEM = "haveShoppingItem";
   private static ModelManager mm;
   private final EventFiler eventFiler;
   private final String historyFileName;
   private EventSource eventSource;

   public static ModelManager get()
   {
      if (mm == null)
      {
         mm = new ModelManager();
      }

      return mm;
   }

   private ModelManager()
   {
      eventSource = new EventSource();
      historyFileName = "tmp/PartyData/partyEvents.yaml";
      eventFiler = new EventFiler(eventSource).setHistoryFileName(historyFileName);

      String yaml = loadHistory();

      applyEvents(yaml);

      storeHistory();

      eventFiler.startEventLogging();
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
      }

      eventSource.setOldEventTimeStamp(0);
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
            .append("  " + ShoppingItem.PROPERTY_price + ": ").append("" + price).append("\n")
            .append("  " + ShoppingItem.PROPERTY_responsible + ": ").append(Yamler.encapsulate(responsible.getName())).append("\n")
            .append("\n");

      eventSource.append(buf);

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

      eventSource.append(buf);

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

         eventSource.append(buf);
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

   public String loadHistory()
   {
      File historyFile = new File(historyFileName);
      String content = null;
      try
      {
         byte[] bytes = new byte[(int) historyFile.length()];
         InputStream inputStream = new FileInputStream(historyFile);
         int read = inputStream.read(bytes);
         content = new String(bytes);
      }
      catch (Exception e)
      {
         // Logger.getGlobal().log(Level.SEVERE, "could not load history", e);
      }

      return content;
   }

   public boolean storeHistory()
   {
      File historyFile = new File(historyFileName);

      String yaml = eventSource.encodeYaml();
      try {
         PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(historyFileName)));
         out.print(yaml);
         out.close();
      } catch (IOException e) {
         Logger.getGlobal().log(Level.SEVERE, "could not write to historyFile " + historyFileName, e);
         return false;
      }

      return true;
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
}
