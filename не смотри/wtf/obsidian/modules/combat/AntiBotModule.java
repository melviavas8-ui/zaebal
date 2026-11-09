package wtf.obsidian.modules.combat;

import wtf.obsidian.*;
import wtf.obsidian.manager.*;
import wtf.obsidian.utils.*;
import wtf.obsidian.engine.*;
import wtf.obsidian.event.*;
import wtf.obsidian.commands.*;
import wtf.obsidian.settings.*;
import wtf.obsidian.hud.*;
import wtf.obsidian.modules.combat.*;
import wtf.obsidian.modules.movement.*;
import wtf.obsidian.modules.render.*;
import wtf.obsidian.modules.player.*;
import wtf.obsidian.modules.misc.*;
import wtf.obsidian.mixin.*;
import wtf.obsidian.Module;
import wtf.obsidian.event.Event;

























import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class AntiBotModule extends Module {
   public static AntiBotModule INSTANCE = new AntiBotModule();
   public static final List<Entity> isBot = new ArrayList<>();

   public AntiBotModule() {
      super("AntiBot", "Определяет ботов на сервере", Category.COMBAT);
   }

   @EventTarget
   public void onUpdate(EventTick event) {
      this.newMatrix();
   }

   public void newMatrix() {
      if (mc.world != null) {
         for (PlayerEntity player : mc.world.getPlayers()) {
            if (mc.player != player
               && ((ItemStack)player.getInventory().armor.get(0)).getItem() != Items.AIR
               && ((ItemStack)player.getInventory().armor.get(1)).getItem() != Items.AIR
               && ((ItemStack)player.getInventory().armor.get(2)).getItem() != Items.AIR
               && ((ItemStack)player.getInventory().armor.get(3)).getItem() != Items.AIR
               && ((ItemStack)player.getInventory().armor.get(0)).isEnchantable()
               && ((ItemStack)player.getInventory().armor.get(1)).isEnchantable()
               && ((ItemStack)player.getInventory().armor.get(2)).isEnchantable()
               && ((ItemStack)player.getInventory().armor.get(3)).isEnchantable()
               && player.getOffHandStack().getItem() == Items.AIR
               && (
                  ((ItemStack)player.getInventory().armor.get(0)).getItem() == Items.LEATHER_BOOTS
                     || ((ItemStack)player.getInventory().armor.get(1)).getItem() == Items.LEATHER_LEGGINGS
                     || ((ItemStack)player.getInventory().armor.get(2)).getItem() == Items.LEATHER_CHESTPLATE
                     || ((ItemStack)player.getInventory().armor.get(3)).getItem() == Items.LEATHER_HELMET
                     || ((ItemStack)player.getInventory().armor.get(0)).getItem() == Items.IRON_BOOTS
                     || ((ItemStack)player.getInventory().armor.get(1)).getItem() == Items.IRON_LEGGINGS
                     || ((ItemStack)player.getInventory().armor.get(2)).getItem() == Items.IRON_CHESTPLATE
                     || ((ItemStack)player.getInventory().armor.get(3)).getItem() == Items.IRON_HELMET
               )
               && player.getMainHandStack().getItem() != Items.AIR
               && !((ItemStack)player.getInventory().armor.get(0)).isDamaged()
               && !((ItemStack)player.getInventory().armor.get(1)).isDamaged()
               && !((ItemStack)player.getInventory().armor.get(2)).isDamaged()
               && !((ItemStack)player.getInventory().armor.get(3)).isDamaged()
               && player.getHungerManager().getFoodLevel() == (20)) {
               if (!isBot.contains(player)) {
                  isBot.add(player);
               }

               return;
            }

            isBot.remove(player);
         }
      }
   }

   public static boolean checkBot(LivingEntity entity) {
      return (entity instanceof PlayerEntity && isBot.contains(entity));
   }

   @Override
   public void onDisable() {
      super.onDisable();
      isBot.clear();
   }
}
