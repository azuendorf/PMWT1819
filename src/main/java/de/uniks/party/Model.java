package de.uniks.party;

import de.uniks.party.model.Party;

public class Model
{
   private static Party instance;

   public static Party getParty()
   {
      if (instance == null)
      {
         instance = new Party();
      }

      return instance;
   }
}
