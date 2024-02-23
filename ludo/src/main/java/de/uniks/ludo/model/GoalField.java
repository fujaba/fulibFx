package de.uniks.ludo.model;

public class GoalField extends Field
{
   public static final String PROPERTY_OWNER = "owner";
   private Player owner;

   public Player getOwner()
   {
      return this.owner;
   }

   public GoalField setOwner(Player value)
   {
      if (this.owner == value)
      {
         return this;
      }

      final Player oldValue = this.owner;
      if (this.owner != null)
      {
         this.owner = null;
         oldValue.withoutGoalFields(this);
      }
      this.owner = value;
      if (value != null)
      {
         value.withGoalFields(this);
      }
      this.firePropertyChange(PROPERTY_OWNER, oldValue, value);
      return this;
   }

   @Override
   public void removeYou()
   {
      super.removeYou();
      this.setOwner(null);
   }
}
