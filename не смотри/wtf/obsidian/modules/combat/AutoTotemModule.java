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

























import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.screen.slot.SlotActionType;

public class AutoTotemModule extends Module {
   public static AutoTotemModule INSTANCE = new AutoTotemModule();
   public final MultiBooleanSetting triggers;
   public final SliderSetting hp;
   public final SliderSetting hpOnElytra;
   public final SliderSetting crystalRadius;
   public final SliderSetting fallHeight;
   public final BooleanSetting saveEnchanted;
   public final BooleanSetting returnTotem;
   public final SliderSetting returnDelay;
   public final BooleanSetting bypassgrim;
   public final ModeSetting swapVersion;
   public int bypassTicks;
   public boolean sprintPaused;
   public int swapCooldown;
   public int savedTotemSlot;
   public ItemStack originalOffhandItem;
   public boolean totemTakenByUs;
   public boolean returnMode;
   public boolean needFastSwap;
   public int safeTicks;

   public AutoTotemModule() {
      super("AutoTotem", "Автоматически берёт тотем в опасности", Category.COMBAT);
      BooleanSetting[] var10004 = new BooleanSetting[4];
      var10004[0] = new BooleanSetting("Кристалл рядом", true);
      var10004[1] = new BooleanSetting("Кристалл в руке", true);
      var10004[2] = new BooleanSetting("Обсидиан в руке", true);
      var10004[3] = new BooleanSetting("Падения", true);
      this.triggers = new MultiBooleanSetting("Брать от", var10004);
      this.hp = new SliderSetting(
         "Брать от хп",
         6.0f,
         1.0f,
         20.0f,
         0.5f
      );
      this.hpOnElytra = new SliderSetting(
         "Хп на элитрах",
         10.0f,
         1.0f,
         20.0f,
         0.5f
      );
      this.crystalRadius = new SliderSetting(
            "Радиус кристалла",
            6.0f,
            1.0f,
            12.0f,
            0.5f
         )
         .visible(this::isCrystalRadiusVisible);
      this.fallHeight = new SliderSetting(
            "Высота падения",
            10.0f,
            3.0f,
            50.0f,
            1.0f
         )
         .visible(() -> this.triggers.is("Падения"));
      this.saveEnchanted = new BooleanSetting("Сохранять зачар", true);
      this.returnTotem = new BooleanSetting("Возвращать тотем", true);
      this.returnDelay = new SliderSetting(
            "Задержка возврата",
            20.0f,
            5.0f,
            100.0f,
            5.0f
         )
         .visible(this.returnTotem::isState);
      this.bypassgrim = new BooleanSetting("Обходить Grim", true);
      String[] var10005 = new String[2];
      var10005[0] = "1.21.4";
      var10005[1] = "1.16.5";
      this.swapVersion = new ModeSetting("Версия свапа", "1.21.4", var10005);
      this.savedTotemSlot = -1;
      this.originalOffhandItem = ItemStack.EMPTY;
      this.totemTakenByUs = false;
      this.returnMode = false;
      this.needFastSwap = false;
      this.safeTicks = 0;
      Setting[] var10001 = new Setting[10];
      var10001[0] = this.hp;
      var10001[1] = this.hpOnElytra;
      var10001[2] = this.saveEnchanted;
      var10001[3] = this.bypassgrim;
      var10001[4] = this.returnTotem;
      var10001[5] = this.swapVersion;
      var10001[6] = this.returnDelay;
      var10001[7] = this.triggers;
      var10001[8] = this.crystalRadius;
      var10001[9] = this.fallHeight;
      this.addSettings(var10001);
   }

   @EventTarget
   public void onInput(Event_102 e) {
      if (this.bypassgrim.isState() && this.bypassTicks > 0) {
         if (mc.player == null) {
            return;
         }

         mc.player.setSprinting(false);
         e.setForward(0.0f);
         e.setStrafe(0.0f);
         e.setJump(false);
         e.setSneak(false);
      }
   }

   @EventTarget
   public void onUpdate(EventTick e) {
      if (mc.player != null && mc.world != null) {
         boolean isCrystalDanger = this.isCrystalDanger();
         if (isCrystalDanger) {
            this.needFastSwap = true;
            this.safeTicks = 0;
         }

         if (this.swapCooldown > 0) {
            this.swapCooldown -= 1;
         }

         if (this.bypassgrim.isState() && this.bypassTicks > 0) {
            mc.player.setSprinting(false);
            this.bypassTicks -= 1;
            if (this.bypassTicks <= 0) {
               if (this.returnMode) {
                  this.performReturn();
               } else {
                  this.performSwap();
               }

               this.restoreSprint();
            }
         } else {
            boolean needTotem = this.shouldTakeTotem(isCrystalDanger);
            if (needTotem && !this.hasTotemInOffhand()) {
               int totemSlot = this.findTotemSlot();
               if (totemSlot == -1) {
                  return;
               }

               if (!this.needFastSwap && this.swapCooldown > 0) {
                  return;
               }

               if (this.originalOffhandItem.isEmpty() && !this.totemTakenByUs) {
                  this.originalOffhandItem = mc.player.getOffHandStack().copy();
               }

               this.savedTotemSlot = totemSlot;
               this.returnMode = false;
               this.safeTicks = 0;
               if (this.bypassgrim.isState()) {
                  this.disableSprint();
                  this.bypassTicks = this.needFastSwap ? 1 : 2;
                  this.swapCooldown = this.needFastSwap ? 0 : 2;
               } else {
                  this.performSwap();
                  this.swapCooldown = this.needFastSwap ? 0 : 2;
               }
            }

            boolean isSafe = (!needTotem);
            if (isSafe) {
               this.safeTicks += 1;
            } else {
               this.safeTicks = 0;
            }

            if (this.returnTotem.isState()
               && !needTotem
               && this.hasTotemInOffhand()
               && this.totemTakenByUs
               && this.safeTicks >= this.returnDelay.getValue().intValue()) {
               if (!this.needFastSwap && this.swapCooldown > 0) {
                  return;
               }

               this.returnMode = true;
               if (this.bypassgrim.isState()) {
                  this.disableSprint();
                  this.bypassTicks = this.needFastSwap ? 1 : 2;
                  this.swapCooldown = this.needFastSwap ? 0 : 2;
               } else {
                  this.performReturn();
                  this.swapCooldown = this.needFastSwap ? 0 : 2;
               }
            }

            if (!isCrystalDanger) {
               this.needFastSwap = false;
            }
         }
      }
   }
   public boolean isCrystalDanger() {
      float radius = this.crystalRadius.getValue().floatValue();
      double radiusSq = (double)(radius * radius);
      if (this.triggers.is("Кристалл рядом")) {
         for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof EndCrystalEntity && mc.player.squaredDistanceTo(entity) <= radiusSq) {
               return true;
            }
         }
      }

      if (this.triggers.is("Кристалл в руке")) {
         for (PlayerEntity player : mc.world.getPlayers()) {
            if (player != mc.player
               && mc.player.squaredDistanceTo(player) <= radiusSq
               && (player.getMainHandStack().isOf(Items.END_CRYSTAL) || player.getOffHandStack().isOf(Items.END_CRYSTAL))) {
               return true;
            }
         }
      }

      return false;
   }
   public boolean shouldTakeTotem(boolean isCrystalDanger) {
      float currentHp = mc.player.getHealth() + mc.player.getAbsorptionAmount();
      boolean isGliding = (mc.player.getEquippedStack(EquipmentSlot.CHEST).isOf(Items.ELYTRA) && mc.player.isGliding());
      float hpThreshold = isGliding ? this.hpOnElytra.getValue().floatValue() : this.hp.getValue().floatValue();
      if (currentHp <= hpThreshold) {
         return true;
      } else if (isCrystalDanger) {
         return true;
      } else {
         float radius = this.crystalRadius.getValue().floatValue();
         double radiusSq = (double)(radius * radius);
         if (this.triggers.is("Обсидиан в руке")) {
            for (PlayerEntity player : mc.world.getPlayers()) {
               if (player != mc.player
                  && mc.player.squaredDistanceTo(player) <= radiusSq
                  && (player.getMainHandStack().isOf(Items.OBSIDIAN) || player.getOffHandStack().isOf(Items.OBSIDIAN))) {
                  return true;
               }
            }
         }

         return (this.triggers.is("Падения") && mc.player.fallDistance >= this.fallHeight.getValue().floatValue() && !isGliding);
      }
   }
   public boolean hasTotemInOffhand() {
      return mc.player.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING);
   }
   public int findTotemSlot() {
      int normalTotem = -1;
      int enchantedTotem = -1;

      for (int i = 9; i < (45); i++) {
         ItemStack stack = mc.player.playerScreenHandler.getSlot(i).getStack();
         if (stack.isOf(Items.TOTEM_OF_UNDYING)) {
            boolean isEnchanted = stack.hasEnchantments();
            if (isEnchanted) {
               if (enchantedTotem == (-1)) {
                  enchantedTotem = i;
               }
            } else if (normalTotem == -1) {
               normalTotem = i;
            }
         }
      }

      if (this.saveEnchanted.isState()) {
         return normalTotem != -1 ? normalTotem : enchantedTotem;
      } else {
         return enchantedTotem != (-1) ? enchantedTotem : normalTotem;
      }
   }
   public void performSwap() {
      int totemSlot = this.findTotemSlot();
      if (totemSlot != (-1)) {
         this.savedTotemSlot = totemSlot;
         this.doSwap(totemSlot);
         this.totemTakenByUs = true;
         mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(0));
      }
   }
   public void performReturn() {
      if (!this.hasTotemInOffhand()) {
         this.totemTakenByUs = false;
      } else {
         if (!this.originalOffhandItem.isEmpty()) {
            int slotToReturn = this.findSlotForItem(this.originalOffhandItem);
            if (slotToReturn != -1) {
               this.doSwap(slotToReturn);
            } else {
               if (this.savedTotemSlot == (-1)) {
                  this.savedTotemSlot = 9;
               }

               this.doSwap(this.savedTotemSlot);
            }
         } else {
            if (this.savedTotemSlot == (-1)) {
               this.savedTotemSlot = 9;
            }

            this.doSwap(this.savedTotemSlot);
         }

         this.totemTakenByUs = false;
         this.savedTotemSlot = -1;
         this.originalOffhandItem = ItemStack.EMPTY;
         mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(0));
      }
   }
   public int findSlotForItem(ItemStack item) {
      if (item.isEmpty()) {
         return -1;
      } else {
         for (int i = 9; i < (45); i++) {
            ItemStack stack = mc.player.playerScreenHandler.getSlot(i).getStack();
            if (ItemStack.areItemsEqual(stack, item) && ItemStack.areEqual(stack, item)) {
               return i;
            }
         }

         return -1;
      }
   }
   public void doSwap(int slot) {
      if (this.swapVersion.is("1.16.5")) {
         this.doSwap1165(slot);
      } else {
         this.doSwap1214(slot);
      }
   }
   public void doSwap1214(int slot) {
      if (slot >= 36 && slot <= (44)) {
         int hotbarSlot = slot - (36);
         mc.interactionManager.clickSlot(0, 45, hotbarSlot, SlotActionType.SWAP, mc.player);
      } else {
         mc.interactionManager.clickSlot(0, slot, 0, SlotActionType.SWAP, mc.player);
         mc.interactionManager
            .clickSlot(0, 45, 0, SlotActionType.SWAP, mc.player);
         mc.interactionManager.clickSlot(0, slot, 0, SlotActionType.SWAP, mc.player);
      }
   }
   public void doSwap1165(int slot) {
      mc.interactionManager.clickSlot(0, slot, 40, SlotActionType.SWAP, mc.player);
   }
   public void disableSprint() {
      if (!this.sprintPaused) {
         SprintModule.pushPause(1000L);
         this.sprintPaused = true;
      }
   }
   public void restoreSprint() {
      if (this.sprintPaused) {
         this.sprintPaused = false;
         SprintModule.popPause();
      }
   }
   public boolean isCrystalRadiusVisible() {
      return !this.triggers.is("Кристалл рядом") && !this.triggers.is("Кристалл в руке") && !this.triggers.is("Обсидиан в руке")
         ? false : true;
   }

   @Override
   public void onDisable() {
      this.bypassTicks = 0;
      this.swapCooldown = 0;
      this.savedTotemSlot = -1;
      this.originalOffhandItem = ItemStack.EMPTY;
      this.totemTakenByUs = false;
      this.returnMode = false;
      this.needFastSwap = false;
      this.safeTicks = 0;
      this.restoreSprint();
      super.onDisable();
   }
}
