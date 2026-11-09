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

























import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.TridentItem;

public class ItemReleaseModule extends Module {
   public static ItemReleaseModule INSTANCE = new ItemReleaseModule();
   public final MultiBooleanSetting items;
   public final SliderSetting tickBow;

   public ItemReleaseModule() {
      super("ItemRelease", "Автоматически выпускает предмет когда он полностью натянут", Category.COMBAT);
      BooleanSetting[] var10004 = new BooleanSetting[3];
      var10004[0] = new BooleanSetting("Лук", true);
      var10004[1] = new BooleanSetting("Трезубец", false);
      var10004[2] = new BooleanSetting("Арбалет", true);
      this.items = new MultiBooleanSetting("Предметы", var10004);
      this.tickBow = new SliderSetting(
            "Задержка выстрела",
            2.5f,
            2.0f,
            5.0f,
            0.05000000074505806f
         )
         .visible(() -> this.items.is("Лук"));
      Setting[] var10001 = new Setting[2];
      var10001[0] = this.items;
      var10001[1] = this.tickBow;
      this.addSettings(var10001);
   }

   @EventTarget
   public void onUpdate(EventTick e) {
      if (mc.player != null && mc.world != null) {
         if (this.items.is("Лук")
            && mc.player.getMainHandStack().getItem() instanceof BowItem
            && mc.player.isUsingItem()
            && (float)mc.player.getItemUseTime() >= this.tickBow.getValue().floatValue()) {
            mc.interactionManager.stopUsingItem(mc.player);
         }

         if (this.items.is("Трезубец")
            && mc.player.getMainHandStack().getItem() instanceof TridentItem
            && mc.player.isUsingItem()
            && mc.player.getItemUseTime() >= 10) {
            mc.interactionManager.stopUsingItem(mc.player);
         }

         if (this.items.is("Арбалет")
            && mc.player.getMainHandStack().getItem() instanceof CrossbowItem
            && mc.player.isUsingItem()
            && mc.player.getItemUseTime() >= CrossbowItem.getPullTime(mc.player.getMainHandStack(), mc.player)) {
            mc.interactionManager.stopUsingItem(mc.player);
         }
      }
   }
}
