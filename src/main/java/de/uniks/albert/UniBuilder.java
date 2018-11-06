package de.uniks.albert;

public class UniBuilder
{
   public Room buildRoom(String topic, int credits)
   {
      Room result = new Room();
      result.setTopic(topic);
      result.setCredits(credits);
      return result;
   }
}
