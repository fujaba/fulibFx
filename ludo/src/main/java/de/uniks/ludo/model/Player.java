package de.uniks.ludo.model;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Collections;
import java.util.Collection;
import java.beans.PropertyChangeSupport;

public class Player
{
   public static final String PROPERTY_ID = "id";
   public static final String PROPERTY_START_FIELD = "startField";
   public static final String PROPERTY_GAME = "game";
   public static final String PROPERTY_GOAL_FIELDS = "goalFields";
   public static final String PROPERTY_HOME_FIELDS = "homeFields";
   public static final String PROPERTY_PIECES = "pieces";
   private int id;
   private Field startField;
   private Game game;
   private List<GoalField> goalFields;
   private List<HomeField> homeFields;
   private List<Piece> pieces;
   protected PropertyChangeSupport listeners;

   public int getId()
   {
      return this.id;
   }

   public Player setId(int value)
   {
      if (value == this.id)
      {
         return this;
      }

      final int oldValue = this.id;
      this.id = value;
      this.firePropertyChange(PROPERTY_ID, oldValue, value);
      return this;
   }

   public Field getStartField()
   {
      return this.startField;
   }

   public Player setStartField(Field value)
   {
      if (Objects.equals(value, this.startField))
      {
         return this;
      }

      final Field oldValue = this.startField;
      this.startField = value;
      this.firePropertyChange(PROPERTY_START_FIELD, oldValue, value);
      return this;
   }

   public Game getGame()
   {
      return this.game;
   }

   public Player setGame(Game value)
   {
      if (this.game == value)
      {
         return this;
      }

      final Game oldValue = this.game;
      if (this.game != null)
      {
         this.game = null;
         oldValue.withoutPlayers(this);
      }
      this.game = value;
      if (value != null)
      {
         value.withPlayers(this);
      }
      this.firePropertyChange(PROPERTY_GAME, oldValue, value);
      return this;
   }

   public List<GoalField> getGoalFields()
   {
      return this.goalFields != null ? Collections.unmodifiableList(this.goalFields) : Collections.emptyList();
   }

   public Player withGoalFields(GoalField value)
   {
      if (this.goalFields == null)
      {
         this.goalFields = new ArrayList<>();
      }
      if (!this.goalFields.contains(value))
      {
         this.goalFields.add(value);
         value.setOwner(this);
         this.firePropertyChange(PROPERTY_GOAL_FIELDS, null, value);
      }
      return this;
   }

   public Player withGoalFields(GoalField... value)
   {
      for (final GoalField item : value)
      {
         this.withGoalFields(item);
      }
      return this;
   }

   public Player withGoalFields(Collection<? extends GoalField> value)
   {
      for (final GoalField item : value)
      {
         this.withGoalFields(item);
      }
      return this;
   }

   public Player withoutGoalFields(GoalField value)
   {
      if (this.goalFields != null && this.goalFields.remove(value))
      {
         value.setOwner(null);
         this.firePropertyChange(PROPERTY_GOAL_FIELDS, value, null);
      }
      return this;
   }

   public Player withoutGoalFields(GoalField... value)
   {
      for (final GoalField item : value)
      {
         this.withoutGoalFields(item);
      }
      return this;
   }

   public Player withoutGoalFields(Collection<? extends GoalField> value)
   {
      for (final GoalField item : value)
      {
         this.withoutGoalFields(item);
      }
      return this;
   }

   public List<HomeField> getHomeFields()
   {
      return this.homeFields != null ? Collections.unmodifiableList(this.homeFields) : Collections.emptyList();
   }

   public Player withHomeFields(HomeField value)
   {
      if (this.homeFields == null)
      {
         this.homeFields = new ArrayList<>();
      }
      if (!this.homeFields.contains(value))
      {
         this.homeFields.add(value);
         value.setOwner(this);
         this.firePropertyChange(PROPERTY_HOME_FIELDS, null, value);
      }
      return this;
   }

   public Player withHomeFields(HomeField... value)
   {
      for (final HomeField item : value)
      {
         this.withHomeFields(item);
      }
      return this;
   }

   public Player withHomeFields(Collection<? extends HomeField> value)
   {
      for (final HomeField item : value)
      {
         this.withHomeFields(item);
      }
      return this;
   }

   public Player withoutHomeFields(HomeField value)
   {
      if (this.homeFields != null && this.homeFields.remove(value))
      {
         value.setOwner(null);
         this.firePropertyChange(PROPERTY_HOME_FIELDS, value, null);
      }
      return this;
   }

   public Player withoutHomeFields(HomeField... value)
   {
      for (final HomeField item : value)
      {
         this.withoutHomeFields(item);
      }
      return this;
   }

   public Player withoutHomeFields(Collection<? extends HomeField> value)
   {
      for (final HomeField item : value)
      {
         this.withoutHomeFields(item);
      }
      return this;
   }

   public List<Piece> getPieces()
   {
      return this.pieces != null ? Collections.unmodifiableList(this.pieces) : Collections.emptyList();
   }

   public Player withPieces(Piece value)
   {
      if (this.pieces == null)
      {
         this.pieces = new ArrayList<>();
      }
      if (!this.pieces.contains(value))
      {
         this.pieces.add(value);
         value.setOwner(this);
         this.firePropertyChange(PROPERTY_PIECES, null, value);
      }
      return this;
   }

   public Player withPieces(Piece... value)
   {
      for (final Piece item : value)
      {
         this.withPieces(item);
      }
      return this;
   }

   public Player withPieces(Collection<? extends Piece> value)
   {
      for (final Piece item : value)
      {
         this.withPieces(item);
      }
      return this;
   }

   public Player withoutPieces(Piece value)
   {
      if (this.pieces != null && this.pieces.remove(value))
      {
         value.setOwner(null);
         this.firePropertyChange(PROPERTY_PIECES, value, null);
      }
      return this;
   }

   public Player withoutPieces(Piece... value)
   {
      for (final Piece item : value)
      {
         this.withoutPieces(item);
      }
      return this;
   }

   public Player withoutPieces(Collection<? extends Piece> value)
   {
      for (final Piece item : value)
      {
         this.withoutPieces(item);
      }
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
      this.setGame(null);
      this.withoutGoalFields(new ArrayList<>(this.getGoalFields()));
      this.withoutHomeFields(new ArrayList<>(this.getHomeFields()));
      this.withoutPieces(new ArrayList<>(this.getPieces()));
   }
}
