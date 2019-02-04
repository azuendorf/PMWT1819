package de.uniks.party.model;

import java.beans.PropertyChangeSupport;

import java.beans.PropertyChangeListener;

public class ShoppingItem  
{

   public static final String PROPERTY_description = "description";

   private String description;

   public String getDescription()
   {
      return description;
   }

   public ShoppingItem setDescription(String value)
   {
      if (value == null ? this.description != null : ! value.equals(this.description))
      {
         String oldValue = this.description;
         this.description = value;
         firePropertyChange("description", oldValue, value);
      }
      return this;
   }


   public static final String PROPERTY_price = "price";

   private double price;

   public double getPrice()
   {
      return price;
   }

   public ShoppingItem setPrice(double value)
   {
      if (value != this.price)
      {
         double oldValue = this.price;
         this.price = value;
         firePropertyChange("price", oldValue, value);
      }
      return this;
   }


   public static final String PROPERTY_party = "party";

   private Party party = null;

   public Party getParty()
   {
      return this.party;
   }

   public ShoppingItem setParty(Party value)
   {
      if (this.party != value)
      {
         Party oldValue = this.party;
         if (this.party != null)
         {
            this.party = null;
            oldValue.withoutShoppingItems(this);
         }
         this.party = value;
         if (value != null)
         {
            value.withShoppingItems(this);
         }
         firePropertyChange("party", oldValue, value);
      }
      return this;
   }



   public static final String PROPERTY_responsible = "responsible";

   private Participant responsible = null;

   public Participant getResponsible()
   {
      return this.responsible;
   }

   public ShoppingItem setResponsible(Participant value)
   {
      if (this.responsible != value)
      {
         Participant oldValue = this.responsible;
         if (this.responsible != null)
         {
            this.responsible = null;
            oldValue.withoutItems(this);
         }
         this.responsible = value;
         if (value != null)
         {
            value.withItems(this);
         }
         firePropertyChange("responsible", oldValue, value);
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

      result.append(" ").append(this.getDescription());


      return result.substring(1);
   }

   public void removeYou()
   {
      this.setParty(null);
      this.setResponsible(null);

   }


}