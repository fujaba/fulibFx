package de.uniks.ludo.model;

public class BaseField extends Field
{
   public static final String PROPERTY_OWNER = "owner";
   private Player owner;

   public Player getOwner()
   {
      return this.owner;
   }

   public BaseField setOwner(Player value)
   {
      if (this.owner == value)
      {
         return this;
      }

      final Player oldValue = this.owner;
      if (this.owner != null)
      {
         this.owner = null;
         oldValue.withoutBaseFields(this);
      }
      this.owner = value;
      if (value != null)
      {
         value.withBaseFields(this);
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
