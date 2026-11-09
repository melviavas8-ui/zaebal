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

























import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public class ItemAimModule extends Module {
   public static ItemAimModule INSTANCE = new ItemAimModule();
   public MultiBooleanSetting element;

   public ItemAimModule() {
      super("ItemAim", "Автоматически наводиться на предмет", Category.PLAYER);
      BooleanSetting[] var10004 = new BooleanSetting[2];
      var10004[0] = new BooleanSetting("Шары", true);
      var10004[1] = new BooleanSetting("Элитры", true);
      this.element = new MultiBooleanSetting("Лутать", var10004);
      Setting[] var10001 = new Setting[1];
      var10001[0] = this.element;
      this.addSettings(var10001);
   }

   @EventTarget
   public void onEvent(EventTick event) {
      if (mc.player != null && mc.world != null) {
         ItemEntity targetItem = this.findTargetItem();
         if (targetItem != null) {
            Vec2f rotations = this.getItemRotations(targetItem);
            RotationManager.update(
               new ObsidianComponent_olorSetting(rotations.x, rotations.y),
               360.0f,
               360.0f,
               360.0f,
               360.0f,
               0,
               1,
               false
            );
         }
      }
   }
   public ItemEntity findTargetItem() {
      ItemEntity bestItem = null;
      double bestDistance = 179769313486231570814527423731704356798070567525844996598917476803157260780028538760589558632766878171540458953514382464234321326889464182768467546703537516986049910576551282076245490090389328944075868508455133942304583236903222948165808559332123348274797826204144723168738177180919299881250404026184124858368.0;

      for (Entity entity : mc.world.getEntities()) {
         if (entity instanceof ItemEntity) {
            ItemEntity itemEntity = (ItemEntity)entity;
            if (this.isWantedItem(itemEntity)) {
               double distance = mc.player.squaredDistanceTo(itemEntity);
               if (distance < bestDistance) {
                  bestDistance = distance;
                  bestItem = itemEntity;
               }
            }
         }
      }

      return bestItem;
   }
   public boolean isWantedItem(ItemEntity itemEntity) {
      return (!((!this.element.is("Шары") || !itemEntity.getStack().isOf(Items.PLAYER_HEAD))
            && (!this.element.is("Элитры") || !itemEntity.getStack().isOf(Items.ELYTRA))));
   }
   public Vec2f getItemRotations(ItemEntity itemEntity) {
      Vec3d targetPos = itemEntity.getBoundingBox().getCenter();
      return ObsidianComponent_374.getRotations(targetPos);
   }
}
