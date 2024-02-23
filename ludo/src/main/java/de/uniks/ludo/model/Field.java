package de.uniks.ludo.model;
import java.beans.PropertyChangeSupport;

public class Field
{
   public static final String PROPERTY_PIECE = "piece";
   public static final String PROPERTY_NEXT = "next";
   public static final String PROPERTY_PREV = "prev";
   private Piece piece;
   private Field next;
   private Field prev;
   protected PropertyChangeSupport listeners;

   public Piece getPiece()
   {
      return this.piece;
   }

   public Field setPiece(Piece value)
   {
      if (this.piece == value)
      {
         return this;
      }

      final Piece oldValue = this.piece;
      if (this.piece != null)
      {
         this.piece = null;
         oldValue.setOn(null);
      }
      this.piece = value;
      if (value != null)
      {
         value.setOn(this);
      }
      this.firePropertyChange(PROPERTY_PIECE, oldValue, value);
      return this;
   }

   public Field getNext()
   {
      return this.next;
   }

   public Field setNext(Field value)
   {
      if (this.next == value)
      {
         return this;
      }

      final Field oldValue = this.next;
      if (this.next != null)
      {
         this.next = null;
         oldValue.setPrev(null);
      }
      this.next = value;
      if (value != null)
      {
         value.setPrev(this);
      }
      this.firePropertyChange(PROPERTY_NEXT, oldValue, value);
      return this;
   }

   public Field getPrev()
   {
      return this.prev;
   }

   public Field setPrev(Field value)
   {
      if (this.prev == value)
      {
         return this;
      }

      final Field oldValue = this.prev;
      if (this.prev != null)
      {
         this.prev = null;
         oldValue.setNext(null);
      }
      this.prev = value;
      if (value != null)
      {
         value.setNext(this);
      }
      this.firePropertyChange(PROPERTY_PREV, oldValue, value);
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
      this.setPiece(null);
      this.setNext(null);
      this.setPrev(null);
   }
}
