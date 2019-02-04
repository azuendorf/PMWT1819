package de.uniks.albert;

import org.fulib.Fulib;
import org.fulib.builder.ClassBuilder;
import org.fulib.builder.ClassModelBuilder;

public class PartyModelGen
{
   public static void main(String[] args)
   {
      ClassModelBuilder mb = Fulib.classModelBuilder("de.uniks.party.model");

      ClassBuilder party = mb.buildClass("Party")
            .buildAttribute("partyName", mb.STRING)
            .buildAttribute("location", mb.STRING)
            .buildAttribute("date", mb.STRING);

      ClassBuilder participant = mb.buildClass("Participant")
            .buildAttribute("name", mb.STRING)
            .buildAttribute("saldo", mb.DOUBLE);

      ClassBuilder item = mb.buildClass("ShoppingItem")
            .buildAttribute("description", mb.STRING)
            .buildAttribute("price", mb.DOUBLE);

      party.buildAssociation(participant, "participants", mb.MANY,
            "party", mb.ONE);

      party.buildAssociation(item, "shoppingItems", mb.MANY,
            "party", mb.ONE);

      item.buildAssociation(participant, "responsible", mb.ONE, "items", mb.MANY);

      Fulib.generator().generate(mb.getClassModel());
      Fulib.tablesGenerator().generate(mb.getClassModel());
   }
}
