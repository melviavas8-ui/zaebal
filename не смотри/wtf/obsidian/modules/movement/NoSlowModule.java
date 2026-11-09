package wtf.obsidian.modules.movement;

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

























import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;

public class NoSlowModule extends Module {
   public static NoSlowModule INSTANCE = new NoSlowModule();
   public final ModeSetting mode;
   public final BooleanSetting sprint;

   public NoSlowModule() {
      super("NoSlow", "Убирает замедление во время еды", Category.MOVEMENT);
      String[] var10005 = new String[2];
      var10005[0] = "Grim Old";
      var10005[1] = "Grim Last";
      this.mode = new ModeSetting("Мод", "Grim Old", var10005);
      this.sprint = new BooleanSetting("Спринт", true);
      Setting[] var10001 = new Setting[2];
      var10001[0] = this.mode;
      var10001[1] = this.sprint;
      this.addSettings(var10001);
   }

   @EventTarget
   public void onSlowDown(Event_150 event) {
      if (mc.player != null && mc.player.isUsingItem()) {
         if (this.mode.is("Grim Last") && mc.player.getItemUseTime() % 2 == 0) {
            event.setCancelled(true);
         }

         if (this.mode.is("Grim Old")) {
            Hand activeHand = mc.player.getActiveHand();
            boolean legacyProtocol = ProtocolUtil.isTargetProtocolBelowOneNineteen();
            if (this.sprint.isState()) {
               mc.player
                  .setSprinting(
                     ((ModuleManager.sprint.isEnable() && SprintModule.isSprinting() || mc.options.sprintKey.isPressed())
                           && mc.player.input.movementForward > 0.0f
                           && (!legacyProtocol || !mc.player.horizontalCollision && !mc.player.collidedSoftly)
                           && !mc.player.isGliding())
                  );
            }

            Hand otherHand = activeHand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND;
            mc.getNetworkHandler()
               .sendPacket(new PlayerInteractItemC2SPacket(otherHand, 0, mc.player.getYaw(), mc.player.getPitch()));
            event.setCancelled(true);
         }
      }
   }
}
