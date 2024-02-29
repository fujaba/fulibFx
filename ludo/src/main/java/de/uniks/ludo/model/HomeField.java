package de.uniks.ludo.model;

public class HomeField extends Field
{
   public static final String PROPERTY_OWNER = "owner";
   private Player owner;

   public Player getOwner()
   {
      return this.owner;
   }

   public HomeField setOwner(Player value)
   {
      if (this.owner == value)
      {
         return this;
      }

      final Player oldValue = this.owner;
      if (this.owner != null)
      {
         this.owner = null;
         oldValue.withoutHomeFields(this);
      }
      this.owner = value;
      if (value != null)
      {
         value.withHomeFields(this);
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
