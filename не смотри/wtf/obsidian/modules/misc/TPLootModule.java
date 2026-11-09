package wtf.obsidian.modules.misc;

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

























import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.OnGroundOnly;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.PositionAndOnGround;
import net.minecraft.util.math.Vec3d;

public final class TPLootModule extends Module {
   
   private final SliderSetting range = new SliderSetting(
      "Дистанция",
      10.0f,
      3.0f,
      50.0f,
      1.0f
   );
   private final SliderSetting lootDelay = new SliderSetting(
      "Задержка лута",
      500.0f,
      100.0f,
      5000.0f,
      50.0f
   );
   public final ModeSetting afterLoot;
   public final SliderSetting actionDelay;
   public final ObsidianComponent_372 lootTimer;
   public final ObsidianComponent_372 actionTimer;
   public Vec3d originalPos;
   public boolean waitingAction;
   public static final List<Item> TARGET_ITEMS;

   public TPLootModule() {
      super("TPLoot", "Телепортирует к ресурсам", Category.MISC);
      String[] var10005 = new String[2];
      var10005[0] = "Возвращаться";
      var10005[1] = "Тепаться на спавн";
      this.afterLoot = new ModeSetting("После лута", "Возвращаться", var10005);
      this.actionDelay = new SliderSetting(
         "Задержка действия",
         1000.0f,
         200.0f,
         10000.0f,
         100.0f
      );
      this.lootTimer = new ObsidianComponent_372();
      this.actionTimer = new ObsidianComponent_372();
      this.originalPos = null;
      this.waitingAction = false;
      Setting[] var10001 = new Setting[4];
      var10001[0] = this.range;
      var10001[1] = this.lootDelay;
      var10001[2] = this.afterLoot;
      var10001[3] = this.actionDelay;
      this.addSettings(var10001);
   }

   @EventTarget
   public void onTick(EventTick e) {
      if (mc.player != null && mc.world != null) {
         if (this.waitingAction) {
            if (this.actionTimer.finished((long)this.actionDelay.getValue().floatValue())) {
               if (this.afterLoot.is("Возвращаться") && this.originalPos != null) {
                  this.teleportTo(this.originalPos);
                  ObsidianComponent_hatUtil.sendMessage("TpLoot: возврат на исходную позицию");
               }

               if (this.afterLoot.is("Тепаться на спавн")) {
                  mc.player.networkHandler.sendChatCommand("spawn");
                  ObsidianComponent_hatUtil.sendMessage("TpLoot: выполнен /spawn");
               }

               this.waitingAction = false;
               this.originalPos = null;
               this.lootTimer.reset();
            }
         } else if (this.lootTimer.finished((long)this.lootDelay.getValue().floatValue())) {
            ItemEntity targetItem = this.findTargetItem();
            if (targetItem != null) {
               this.originalPos = mc.player.getPos();
               Vec3d itemPos = targetItem.getPos();
               this.teleportTo(itemPos);
               ItemStack stack = targetItem.getStack();
               ObsidianComponent_hatUtil.sendMessage("TpLoot: подобран " + stack.getName().getString());
               this.lootTimer.reset();
               this.waitingAction = true;
               this.actionTimer.reset();
            }
         }
      }
   }
   public ItemEntity findTargetItem() {
      double maxRange = this.range.getValue().doubleValue();
      ItemEntity closest = null;
      double closestDist = 179769313486231570814527423731704356798070567525844996598917476803157260780028538760589558632766878171540458953514382464234321326889464182768467546703537516986049910576551282076245490090389328944075868508455133942304583236903222948165808559332123348274797826204144723168738177180919299881250404026184124858368.0;

      for (Entity entity : mc.world.getEntities()) {
         if (entity instanceof ItemEntity) {
            ItemEntity itemEntity = (ItemEntity)entity;
            ItemStack stack = itemEntity.getStack();
            if (this.isTargetItem(stack.getItem())) {
               double dist = mc.player.squaredDistanceTo(entity);
               if (!(dist > maxRange * maxRange) && dist < closestDist) {
                  closestDist = dist;
                  closest = itemEntity;
               }
            }
         }
      }

      return closest;
   }
   public boolean isTargetItem(Item item) {
      return TARGET_ITEMS.contains(item);
   }
   public void teleportTo(Vec3d pos) {
      int packets = (int)Math.ceil(
         mc.player.getPos().distanceTo(pos) / 10.0
      );
      packets = Math.max(packets, 3);

      for (int i = 0; i < packets; i++) {
         mc.player.networkHandler.sendPacket(new OnGroundOnly(mc.player.isOnGround(), mc.player.horizontalCollision));
      }

      mc.player
         .networkHandler
         .sendPacket(new PositionAndOnGround(pos.x, pos.y, pos.z, false, mc.player.horizontalCollision));
      mc.player.setPosition(pos.x, pos.y, pos.z);
   }

   @Override
   public void onEnable() {
      this.originalPos = null;
      this.waitingAction = false;
      this.lootTimer.reset();
      this.actionTimer.reset();
      super.onEnable();
   }

   @Override
   public void onDisable() {
      this.originalPos = null;
      this.waitingAction = false;
      super.onDisable();
   }

   static {
      Item[] var10000 = new Item[11];
      var10000[0] = Items.NETHERITE_SWORD;
      var10000[1] = Items.NETHERITE_HELMET;
      var10000[2] = Items.NETHERITE_CHESTPLATE;
      var10000[3] = Items.NETHERITE_LEGGINGS;
      var10000[4] = Items.NETHERITE_BOOTS;
      var10000[5] = Items.PLAYER_HEAD;
      var10000[6] = Items.GOLDEN_APPLE;
      var10000[7] = Items.ENCHANTED_GOLDEN_APPLE;
      var10000[8] = Items.END_CRYSTAL;
      var10000[9] = Items.TOTEM_OF_UNDYING;
      var10000[10] = Items.ELYTRA;
      TARGET_ITEMS = List.of(var10000);
   }
   public static TPLootModule INSTANCE = new TPLootModule();

}
