package io.github.sekassel.uno.model;

import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Game
{
   public static final String PROPERTY_PLAYERS = "players";
   public static final String PROPERTY_CLOCKWISE = "clockwise";
   public static final String PROPERTY_CARDS = "cards";
   public static final String PROPERTY_CURRENT_PLAYER = "currentPlayer";
   public static final String PROPERTY_CURRENT_CARD = "currentCard";
   private List<Player> players;
   protected PropertyChangeSupport listeners;
   private boolean clockwise = true;
   private List<Card> cards;
   private Player currentPlayer;
   private Card currentCard;

   public List<Player> getPlayers()
   {
      return this.players != null ? Collections.unmodifiableList(this.players) : Collections.emptyList();
   }

   public Game withPlayers(Player value)
   {
      if (this.players == null)
      {
         this.players = new ArrayList<>();
      }
      if (!this.players.contains(value))
      {
         this.players.add(value);
         value.setGame(this);
         this.firePropertyChange(PROPERTY_PLAYERS, null, value);
      }
      return this;
   }

   public Game withPlayers(Player... value)
   {
      for (final Player item : value)
      {
         this.withPlayers(item);
      }
      return this;
   }

   public Game withPlayers(Collection<? extends Player> value)
   {
      for (final Player item : value)
      {
         this.withPlayers(item);
      }
      return this;
   }

   public Game withoutPlayers(Player value)
   {
      if (this.players != null && this.players.remove(value))
      {
         value.setGame(null);
         this.firePropertyChange(PROPERTY_PLAYERS, value, null);
      }
      return this;
   }

   public Game withoutPlayers(Player... value)
   {
      for (final Player item : value)
      {
         this.withoutPlayers(item);
      }
      return this;
   }

   public Game withoutPlayers(Collection<? extends Player> value)
   {
      for (final Player item : value)
      {
         this.withoutPlayers(item);
      }
      return this;
   }

   public boolean isClockwise()
   {
      return this.clockwise;
   }

   public Game setClockwise(boolean value)
   {
      if (value == this.clockwise)
      {
         return this;
      }

      final boolean oldValue = this.clockwise;
      this.clockwise = value;
      this.firePropertyChange(PROPERTY_CLOCKWISE, oldValue, value);
      return this;
   }

   public List<Card> getCards()
   {
      return this.cards != null ? Collections.unmodifiableList(this.cards) : Collections.emptyList();
   }

   public Game withCards(Card value)
   {
      if (this.cards == null)
      {
         this.cards = new ArrayList<>();
      }
      if (!this.cards.contains(value))
      {
         this.cards.add(value);
         value.setGame(this);
         this.firePropertyChange(PROPERTY_CARDS, null, value);
      }
      return this;
   }

   public Game withCards(Card... value)
   {
      for (final Card item : value)
      {
         this.withCards(item);
      }
      return this;
   }

   public Game withCards(Collection<? extends Card> value)
   {
      for (final Card item : value)
      {
         this.withCards(item);
      }
      return this;
   }

   public Game withoutCards(Card value)
   {
      if (this.cards != null && this.cards.remove(value))
      {
         value.setGame(null);
         this.firePropertyChange(PROPERTY_CARDS, value, null);
      }
      return this;
   }

   public Game withoutCards(Card... value)
   {
      for (final Card item : value)
      {
         this.withoutCards(item);
      }
      return this;
   }

   public Game withoutCards(Collection<? extends Card> value)
   {
      for (final Card item : value)
      {
         this.withoutCards(item);
      }
      return this;
   }

   public Player getCurrentPlayer()
   {
      return this.currentPlayer;
   }

   public Game setCurrentPlayer(Player value)
   {
      if (this.currentPlayer == value)
      {
         return this;
      }

      final Player oldValue = this.currentPlayer;
      this.currentPlayer = value;
      this.firePropertyChange(PROPERTY_CURRENT_PLAYER, oldValue, value);
      return this;
   }

   public Card getCurrentCard()
   {
      return this.currentCard;
   }

   public Game setCurrentCard(Card value)
   {
      if (this.currentCard == value)
      {
         return this;
      }

      final Card oldValue = this.currentCard;
      this.currentCard = value;
      this.firePropertyChange(PROPERTY_CURRENT_CARD, oldValue, value);
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
      this.setCurrentPlayer(null);
      this.setCurrentCard(null);
      this.withoutCards(new ArrayList<>(this.getCards()));
      this.withoutPlayers(new ArrayList<>(this.getPlayers()));
   }

   /**
    * Returns the first player of the game (which is always the human player when using game logic).
    * (Placed here for an easy way of accessing the human player (or first player) of a game)
    *
    * @return The first player of the game
    */
   public Player getFirstPlayer() {
      return this.players.get(0);
   }

   public String toString() { // no fulib
      StringBuilder builder = new StringBuilder("[");
      getPlayers().forEach(player -> builder.append(player).append(" "));
      return builder.substring(0, builder.length() - 1).toString() + "]";
   }
}
