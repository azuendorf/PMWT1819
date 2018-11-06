package de.uniks.albert;

import java.awt.geom.Ellipse2D;
import java.util.ArrayList;

public class Room
{
   private ArrayList<Student> students;

   private int credits;
   private String topic;

   public Room(String entrance, int credits)
   {
      setCredits(credits);
      setTopic(entrance);
   }

   public Room()
   {
      // empty
   }

   public String getTopic()
   {
      return topic;
   }

   public void setTopic(String topic)
   {
      this.topic = topic;
   }

   public int getCredits()
   {
      return credits;
   }

   public void setCredits(int credits)
   {
      this.credits = credits;
   }

   @Override
   public String toString()
   {
      return getTopic();
   }

   public ArrayList<Student> getStudents()
   {
      if (students == null)
      {
         students = new ArrayList<>();
      }
      return students;
   }

   public void setStudents(ArrayList<Student> students)
   {
      this.students = students;
   }

   public void withStudents(Student newStudent)
   {
      if (students == null)
      {
         students = new ArrayList<>();
      }

      if ( ! students.contains(newStudent))
      {
         students.add(newStudent);
         newStudent.setIn(this);
      }


   }

   public void withoutStudents(Student newStudent)
   {
      if (students == null)
      {
         return;
      }

      if ( students.contains(newStudent))
      {
         students.remove(newStudent);
         newStudent.setIn(null);
      }
   }
}
