package de.uniks.ludo.model;
import java.beans.PropertyChangeSupport;

public class Piece
{
   public static final String PROPERTY_ON = "on";
   public static final String PROPERTY_OWNER = "owner";
   private Field on;
   private Player owner;
   protected PropertyChangeSupport listeners;

   public Field getOn()
   {
      return this.on;
   }

   public Piece setOn(Field value)
   {
      if (this.on == value)
      {
         return this;
      }

      final Field oldValue = this.on;
      if (this.on != null)
      {
         this.on = null;
         oldValue.setPiece(null);
      }
      this.on = value;
      if (value != null)
      {
         value.setPiece(this);
      }
      this.firePropertyChange(PROPERTY_ON, oldValue, value);
      return this;
   }

   public Player getOwner()
   {
      return this.owner;
   }

   public Piece setOwner(Player value)
   {
      if (this.owner == value)
      {
         return this;
      }

      final Player oldValue = this.owner;
      if (this.owner != null)
      {
         this.owner = null;
         oldValue.withoutPieces(this);
      }
      this.owner = value;
      if (value != null)
      {
         value.withPieces(this);
      }
      this.firePropertyChange(PROPERTY_OWNER, oldValue, value);
      return this;
   }

   public boolean firePropertyChange(String propertyName, Object oldValue, Object newValue)
   {
      if (this.listeners != null)
      {
         this.listeners.firePropertyChange(propertyName, oldValue, newValue);
         return true;
      }
      return false;
   }

   public PropertyChangeSupport listeners()
   {
      if (this.listeners == null)
      {
         this.listeners = new PropertyChangeSupport(this);
      }
      return this.listeners;
   }

   public void removeYou()
   {
      this.setOn(null);
      this.setOwner(null);
   }
}
