package de.uniks.albert;

public class Student
{
   private Room in;

   public Room getIn()
   {
      return in;
   }

   public void setIn(Room newRoom)
   {
      Room oldRoom = this.getIn();

      this.in = newRoom;

      if (oldRoom != null && oldRoom != newRoom)
      {
         oldRoom.withoutStudents(this);
      }

      if (newRoom != null && oldRoom != newRoom)
      {
         newRoom.withStudents(this);
      }


   }
}
