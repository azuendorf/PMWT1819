package de.uniks.albert.model;

import java.beans.PropertyChangeSupport;

import java.beans.PropertyChangeListener;

public class Student  
{

   public static final String PROPERTY_matNo = "matNo";

   private int matNo;

   public int getMatNo()
   {
      return matNo;
   }

   public Student setMatNo(int value)
   {
      if (value != this.matNo)
      {
         int oldValue = this.matNo;
         this.matNo = value;
         firePropertyChange("matNo", oldValue, value);
      }
      return this;
   }


   public static final String PROPERTY_name = "name";

   private String name;

   public String getName()
   {
      return name;
   }

   public Student setName(String value)
   {
      if (value == null ? this.name != null : ! value.equals(this.name))
      {
         String oldValue = this.name;
         this.name = value;
         firePropertyChange("name", oldValue, value);
      }
      return this;
   }


   public static final String PROPERTY_credits = "credits";

   private int credits;

   public int getCredits()
   {
      return credits;
   }

   public Student setCredits(int value)
   {
      if (value != this.credits)
      {
         int oldValue = this.credits;
         this.credits = value;
         firePropertyChange("credits", oldValue, value);
      }
      return this;
   }


   public static final String PROPERTY_motivation = "motivation";

   private int motivation;

   public int getMotivation()
   {
      return motivation;
   }

   public Student setMotivation(int value)
   {
      if (value != this.motivation)
      {
         int oldValue = this.motivation;
         this.motivation = value;
         firePropertyChange("motivation", oldValue, value);
      }
      return this;
   }


   private University uni = null;

   public University getUni()
   {
      return this.uni;
   }

   public Student setUni(University value)
   {
      if (this.uni != value)
      {
         University oldValue = this.uni;
         if (this.uni != null)
         {
            this.uni = null;
            oldValue.withoutStudents(this);
         }
         this.uni = value;
         if (value != null)
         {
            value.withStudents(this);
         }
         firePropertyChange("uni", oldValue, value);
      }
      return this;
   }



   public static final java.util.ArrayList<Assignment> EMPTY_done = new java.util.ArrayList<Assignment>()
   { @Override public boolean add(Assignment value){ throw new UnsupportedOperationException("No direct add! Use xy.withDone(obj)"); }};


   private java.util.ArrayList<Assignment> done = null;

   public java.util.ArrayList<Assignment> getDone()
   {
      if (this.done == null)
      {
         return EMPTY_done;
      }

      return this.done;
   }

   public Student withDone(Object... value)
   {
      if(value==null) return this;
      for (Object item : value)
      {
         if (item == null) continue;
         if (item instanceof java.util.Collection)
         {
            for (Object i : (java.util.Collection) item)
            {
               this.withDone(i);
            }
         }
         else if (item instanceof Assignment)
         {
            if (this.done == null)
            {
               this.done = new java.util.ArrayList<Assignment>();
            }
            if ( ! this.done.contains(item))
            {
               this.done.add((Assignment)item);
               ((Assignment)item).withStudents(this);
               firePropertyChange("done", null, item);
            }
         }
         else throw new IllegalArgumentException();
      }
      return this;
   }



   public Student withoutDone(Object... value)
   {
      if (this.done == null || value==null) return this;
      for (Object item : value)
      {
         if (item == null) continue;
         if (item instanceof java.util.Collection)
         {
            for (Object i : (java.util.Collection) item)
            {
               this.withoutDone(i);
            }
         }
         else if (item instanceof Assignment)
         {
            if (this.done.contains(item))
            {
               this.done.remove((Assignment)item);
               ((Assignment)item).withoutStudents(this);
               firePropertyChange("done", item, null);
            }
         }
      }
      return this;
   }


   private Room in = null;

   public Room getIn()
   {
      return this.in;
   }

   public Student setIn(Room value)
   {
      if (this.in != value)
      {
         Room oldValue = this.in;
         if (this.in != null)
         {
            this.in = null;
            oldValue.withoutStudents(this);
         }
         this.in = value;
         if (value != null)
         {
            value.withStudents(this);
         }
         firePropertyChange("in", oldValue, value);
      }
      return this;
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

   @Override
   public String toString()
   {
      StringBuilder result = new StringBuilder();

      result.append(" ").append(this.getName());


      return result.substring(1);
   }

   public void removeYou()
   {
      this.setUni(null);
      this.setIn(null);

      this.withoutDone(this.getDone().clone());


   }



   public static final String PROPERTY_points = "points";

   private int points;

   public int getPoints()
   {
      return points;
   }

   public Student setPoints(int value)
   {
      if (value != this.points)
      {
         int oldValue = this.points;
         this.points = value;
         firePropertyChange("points", oldValue, value);
      }
      return this;
   }


   public static final String PROPERTY_uni = "uni";

   public static final String PROPERTY_done = "done";

   public static final String PROPERTY_in = "in";

}