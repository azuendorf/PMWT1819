package de.uniks.party.model;

import java.beans.PropertyChangeSupport;

import java.beans.PropertyChangeListener;

public class Party  
{

   public static final String PROPERTY_partyName = "partyName";

   private String partyName;

   public String getPartyName()
   {
      return partyName;
   }

   public Party setPartyName(String value)
   {
      if (value == null ? this.partyName != null : ! value.equals(this.partyName))
      {
         String oldValue = this.partyName;
         this.partyName = value;
         firePropertyChange("partyName", oldValue, value);
      }
      return this;
   }


   public static final String PROPERTY_location = "location";

   private String location;

   public String getLocation()
   {
      return location;
   }

   public Party setLocation(String value)
   {
      if (value == null ? this.location != null : ! value.equals(this.location))
      {
         String oldValue = this.location;
         this.location = value;
         firePropertyChange("location", oldValue, value);
      }
      return this;
   }


   public static final String PROPERTY_date = "date";

   private String date;

   public String getDate()
   {
      return date;
   }

   public Party setDate(String value)
   {
      if (value == null ? this.date != null : ! value.equals(this.date))
      {
         String oldValue = this.date;
         this.date = value;
         firePropertyChange("date", oldValue, value);
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

      result.append(" ").append(this.getPartyName());
      result.append(" ").append(this.getLocation());
      result.append(" ").append(this.getDate());


      return result.substring(1);
   }

   public void removeYou()
   {
      this.withoutParticipants(this.getParticipants().clone());


   }


   public static final java.util.ArrayList<Participant> EMPTY_participants = new java.util.ArrayList<Participant>()
   { @Override public boolean add(Participant value){ throw new UnsupportedOperationException("No direct add! Use xy.withParticipants(obj)"); }};


   public static final String PROPERTY_participants = "participants";

   private java.util.ArrayList<Participant> participants = null;

   public java.util.ArrayList<Participant> getParticipants()
   {
      if (this.participants == null)
      {
         return EMPTY_participants;
      }

      return this.participants;
   }

   public Party withParticipants(Object... value)
   {
      if(value==null) return this;
      for (Object item : value)
      {
         if (item == null) continue;
         if (item instanceof java.util.Collection)
         {
            for (Object i : (java.util.Collection) item)
            {
               this.withParticipants(i);
            }
         }
         else if (item instanceof Participant)
         {
            if (this.participants == null)
            {
               this.participants = new java.util.ArrayList<Participant>();
            }
            if ( ! this.participants.contains(item))
            {
               this.participants.add((Participant)item);
               ((Participant)item).setParty(this);
               firePropertyChange("participants", null, item);
            }
         }
         else throw new IllegalArgumentException();
      }
      return this;
   }



   public Party withoutParticipants(Object... value)
   {
      if (this.participants == null || value==null) return this;
      for (Object item : value)
      {
         if (item == null) continue;
         if (item instanceof java.util.Collection)
         {
            for (Object i : (java.util.Collection) item)
            {
               this.withoutParticipants(i);
            }
         }
         else if (item instanceof Participant)
         {
            if (this.participants.contains(item))
            {
               this.participants.remove((Participant)item);
               ((Participant)item).setParty(null);
               firePropertyChange("participants", item, null);
            }
         }
      }
      return this;
   }


}