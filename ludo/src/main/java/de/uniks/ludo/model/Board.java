package de.uniks.ludo.model;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Collection;
import java.beans.PropertyChangeSupport;

public class Board
{
   public static final String PROPERTY_FIELDS = "fields";
   public static final String PROPERTY_GAME = "game";
   private List<Field> fields;
   private Game game;
   protected PropertyChangeSupport listeners;

   public List<Field> getFields()
   {
      return this.fields != null ? Collections.unmodifiableList(this.fields) : Collections.emptyList();
   }

   public Board withFields(Field value)
   {
      if (this.fields == null)
      {
         this.fields = new ArrayList<>();
      }
      if (!this.fields.contains(value))
      {
         this.fields.add(value);
         value.setBoard(this);
         this.firePropertyChange(PROPERTY_FIELDS, null, value);
      }
      return this;
   }

   public Board withFields(Field... value)
   {
      for (final Field item : value)
      {
         this.withFields(item);
      }
      return this;
   }

   public Board withFields(Collection<? extends Field> value)
   {
      for (final Field item : value)
      {
         this.withFields(item);
      }
      return this;
   }

   public Board withoutFields(Field value)
   {
      if (this.fields != null && this.fields.remove(value))
      {
         value.setBoard(null);
         this.firePropertyChange(PROPERTY_FIELDS, value, null);
      }
      return this;
   }

   public Board withoutFields(Field... value)
   {
      for (final Field item : value)
      {
         this.withoutFields(item);
      }
      return this;
   }

   public Board withoutFields(Collection<? extends Field> value)
   {
      for (final Field item : value)
      {
         this.withoutFields(item);
      }
      return this;
   }

   public Game getGame()
   {
      return this.game;
   }

   public Board setGame(Game value)
   {
      if (this.game == value)
      {
         return this;
      }

      final Game oldValue = this.game;
      if (this.game != null)
      {
         this.game = null;
         oldValue.setBoard(null);
      }
      this.game = value;
      if (value != null)
      {
         value.setBoard(this);
      }
      this.firePropertyChange(PROPERTY_GAME, oldValue, value);
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
      this.withoutFields(new ArrayList<>(this.getFields()));
      this.setGame(null);
   }
}
