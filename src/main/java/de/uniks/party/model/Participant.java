package de.uniks.party.model;

import java.beans.PropertyChangeSupport;

import java.beans.PropertyChangeListener;

public class Participant  
{

   public static final String PROPERTY_name = "name";

   private String name;

   public String getName()
   {
      return name;
   }

   public Participant setName(String value)
   {
      if (value == null ? this.name != null : ! value.equals(this.name))
      {
         String oldValue = this.name;
         this.name = value;
         firePropertyChange("name", oldValue, value);
      }
      return this;
   }


   public static final String PROPERTY_party = "party";

   private Party party = null;

   public Party getParty()
   {
      return this.party;
   }

   public Participant setParty(Party value)
   {
      if (this.party != value)
      {
         Party oldValue = this.party;
         if (this.party != null)
         {
            this.party = null;
            oldValue.withoutParticipants(this);
         }
         this.party = value;
         if (value != null)
         {
            value.withParticipants(this);
         }
         firePropertyChange("party", oldValue, value);
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
      this.setParty(null);

      this.withoutItems(this.getItems().clone());


   }


   public static final String PROPERTY_saldo = "saldo";

   private double saldo;

   public double getSaldo()
   {
      return saldo;
   }

   public Participant setSaldo(double value)
   {
      if (value != this.saldo)
      {
         double oldValue = this.saldo;
         this.saldo = value;
         firePropertyChange("saldo", oldValue, value);
      }
      return this;
   }


   public static final java.util.ArrayList<ShoppingItem> EMPTY_items = new java.util.ArrayList<ShoppingItem>()
   { @Override public boolean add(ShoppingItem value){ throw new UnsupportedOperationException("No direct add! Use xy.withItems(obj)"); }};


   public static final String PROPERTY_items = "items";

   private java.util.ArrayList<ShoppingItem> items = null;

   public java.util.ArrayList<ShoppingItem> getItems()
   {
      if (this.items == null)
      {
         return EMPTY_items;
      }

      return this.items;
   }

   public Participant withItems(Object... value)
   {
      if(value==null) return this;
      for (Object item : value)
      {
         if (item == null) continue;
         if (item instanceof java.util.Collection)
         {
            for (Object i : (java.util.Collection) item)
            {
               this.withItems(i);
            }
         }
         else if (item instanceof ShoppingItem)
         {
            if (this.items == null)
            {
               this.items = new java.util.ArrayList<ShoppingItem>();
            }
            if ( ! this.items.contains(item))
            {
               this.items.add((ShoppingItem)item);
               ((ShoppingItem)item).setResponsible(this);
               firePropertyChange("items", null, item);
            }
         }
         else throw new IllegalArgumentException();
      }
      return this;
   }



   public Participant withoutItems(Object... value)
   {
      if (this.items == null || value==null) return this;
      for (Object item : value)
      {
         if (item == null) continue;
         if (item instanceof java.util.Collection)
         {
            for (Object i : (java.util.Collection) item)
            {
               this.withoutItems(i);
            }
         }
         else if (item instanceof ShoppingItem)
         {
            if (this.items.contains(item))
            {
               this.items.remove((ShoppingItem)item);
               ((ShoppingItem)item).setResponsible(null);
               firePropertyChange("items", item, null);
            }
         }
      }
      return this;
   }


}