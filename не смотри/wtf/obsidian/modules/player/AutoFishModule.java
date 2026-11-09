package wtf.obsidian.modules.player;

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

























import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;

public class AutoFishModule extends Module {
   public static AutoFishModule INSTANCE = new AutoFishModule();
   private final BooleanSetting takeRod = new BooleanSetting("Автоматически брать удочку", true);
   public boolean isCached = false;
   public boolean needCached = false;
   public int rodHotbarSlot = -1;
   public long lastActionTime = 0L;
   public long catchTime = 0L;

   public AutoFishModule() {
      super("AutoFish", "Автоматизирует процесс рыбалки", Category.PLAYER);
      Setting[] var10001 = new Setting[1];
      var10001[0] = this.takeRod;
      this.addSettings(var10001);
   }

   @Override
   public void onDisable() {
      this.isCached = false;
      this.needCached = false;
      this.rodHotbarSlot = -1;
      this.lastActionTime = 0L;
      this.catchTime = 0L;
      super.onDisable();
   }

   @EventTarget
   public void onUpdate(EventTick event) {
      if (mc.player != null && mc.world != null) {
         if (this.takeRod.isState() && this.rodHotbarSlot == (-1)) {
            this.findBestFishingRodInHotbar();
         }

         if (this.rodHotbarSlot != -1 && mc.player.getInventory().selectedSlot != this.rodHotbarSlot) {
            mc.player.getInventory().selectedSlot = this.rodHotbarSlot;
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(this.rodHotbarSlot));
         }

         long currentTime = System.currentTimeMillis();
         if (this.isCached && currentTime - this.catchTime >= (600L)) {
            this.useFishingRod();
            this.isCached = false;
            this.needCached = true;
            this.lastActionTime = currentTime;
         }

         if (this.needCached && currentTime - this.lastActionTime >= (300L)) {
            this.useFishingRod();
            this.needCached = false;
            this.lastActionTime = currentTime;
         }
      }
   }

   @EventTarget
   public void onPacket(PacketEvent event) {
      if (mc.player != null && mc.world != null) {
         if (event.getPacket() instanceof PlaySoundS2CPacket packet && packet.getSound().value() == SoundEvents.ENTITY_FISHING_BOBBER_SPLASH) {
            this.isCached = true;
            this.catchTime = System.currentTimeMillis();
         }
      }
   }
   public void useFishingRod() {
      if (mc.player != null && mc.interactionManager != null) {
         if (this.rodHotbarSlot != (-1) && this.rodHotbarSlot < (9)) {
            ItemStack stack = mc.player.getInventory().getStack(this.rodHotbarSlot);
            if (stack.getItem() instanceof FishingRodItem) {
               if (mc.player.getInventory().selectedSlot != this.rodHotbarSlot) {
                  mc.player.getInventory().selectedSlot = this.rodHotbarSlot;
                  mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(this.rodHotbarSlot));
               }

               mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            }
         }
      }
   }
   public void findBestFishingRodInHotbar() {
      if (mc.player != null) {
         int bestRodSlot = -1;
         int maxEnchantments = -1;

         for (int i = 0; i < (9); i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() instanceof FishingRodItem) {
               int enchantmentCount = EnchantmentHelper.getEnchantments(stack).getSize();
               if (enchantmentCount > maxEnchantments) {
                  maxEnchantments = enchantmentCount;
                  bestRodSlot = i;
               }
            }
         }

         if (bestRodSlot != -1) {
            this.rodHotbarSlot = bestRodSlot;
         }
      }
   }
}
