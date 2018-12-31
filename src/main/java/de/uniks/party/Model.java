package de.uniks.party;

import de.uniks.party.model.Participant;
import de.uniks.party.model.Party;
import de.uniks.party.model.ShoppingItem;
import org.fulib.yaml.ModelListener;

public class Model
{
   private static Party instance;

   public static Party getParty()
   {
      if (instance == null)
      {
         instance = new Party();

         // log changes to file
         DataManager.get().attach(instance, "tmp/partyApp");

         // keep saldi up to date
         new ModelListener(instance, e -> updateSaldi());

      }

      return instance;
   }

   private static void updateSaldi()
   {
      double budget = 0;

      for (ShoppingItem item : instance.getShoppingItems())
      {
         budget += item.getPrice();
      }

      for (Participant p : instance.getParticipants())
      {
         double sum = 0;
         for (ShoppingItem item : p.getItems())
         {
            sum += item.getPrice();
         }

         double share = budget / instance.getParticipants().size();

         double saldo = share - sum;

         p.setSaldo(saldo);
      }
   }
}
