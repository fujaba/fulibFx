package io.github.sekassel.uno.model;

import io.github.sekassel.uno.util.CardColor;
import io.github.sekassel.uno.util.CardType;

import java.util.Objects;
import java.beans.PropertyChangeSupport;

public class Card
{
   public static final String PROPERTY_COLOR = "color";
   public static final String PROPERTY_OWNER = "owner";
   public static final String PROPERTY_GAME = "game";
   public static final String PROPERTY_TYPE = "type";
   protected PropertyChangeSupport listeners;
   private io.github.sekassel.uno.util.CardColor color = CardColor.WILD;
   private Player owner;
   private Game game;
   private io.github.sekassel.uno.util.CardType type = CardType.ZERO;

   public io.github.sekassel.uno.util.CardColor getColor()
   {
      return this.color;
   }

   public Card setColor(io.github.sekassel.uno.util.CardColor value)
   {
      if (Objects.equals(value, this.color))
      {
         return this;
      }

      final io.github.sekassel.uno.util.CardColor oldValue = this.color;
      this.color = value;
      this.firePropertyChange(PROPERTY_COLOR, oldValue, value);
      return this;
   }

   public Player getOwner()
   {
      return this.owner;
   }

   public Card setOwner(Player value)
   {

      if (this.owner == value)
      {
         return this;
      }

      final Player oldValue = this.owner;
      if (this.owner != null)
      {
         this.owner = null;
         oldValue.withoutCards(this);
      }
      this.owner = value;
      if (value != null)
      {
         value.withCards(this);
      }
      this.firePropertyChange(PROPERTY_OWNER, oldValue, value);
      return this;
   }

   public Game getGame()
   {
      return this.game;
   }

   public Card setGame(Game value)
   {
      if (this.game == value)
      {
         return this;
      }

      final Game oldValue = this.game;
      if (this.game != null)
      {
         this.game = null;
         oldValue.withoutCards(this);
      }
      this.game = value;
      if (value != null)
      {
         value.withCards(this);
      }
      this.firePropertyChange(PROPERTY_GAME, oldValue, value);
      return this;
   }

   public io.github.sekassel.uno.util.CardType getType()
   {
      return this.type;
   }

   public Card setType(io.github.sekassel.uno.util.CardType value)
   {
      if (Objects.equals(value, this.type))
      {
         return this;
      }

      final io.github.sekassel.uno.util.CardType oldValue = this.type;
      this.type = value;
      this.firePropertyChange(PROPERTY_TYPE, oldValue, value);
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
      this.setOwner(null);
   }

   /**
    * Determines whether this card is compatible with another card (can be played on top of the other card).
    * (Placed here for an easy way of checking card compatibility)
    *
    * @param below The card that would be below this card.
    * @return Whether the card could be played.
    */
   public boolean canBeOnTopOf(Card below) {
      return below.getColor() == this.getColor() || this.getColor() == CardColor.WILD || below.getType() == this.getType();
   }

   public String toString() { // no fulib
      return "[%s %s by %s in %s]".formatted(this.getName(), this.getColor(), this.getOwner(), this.getGame());
   }

   /**
    * @return The name of the card
    */
   public String getName() {
      return this.getType().toString();
   }
}
