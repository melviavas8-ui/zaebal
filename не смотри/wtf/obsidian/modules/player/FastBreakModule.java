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

























import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class FastBreakModule extends Module {
   public static FastBreakModule INSTANCE = new FastBreakModule();
   private final SliderSetting speed = new SliderSetting(
      "Ускорение",
      0.5f,
      0.30000001192092896f,
      1.0f,
      0.10000000149011612f
   );

   public FastBreakModule() {
      super("FastBreak", "Ускоряет ломание блоков", Category.PLAYER);
      Setting[] var10001 = new Setting[1];
      var10001[0] = this.speed;
      this.addSettings(var10001);
   }

   @EventTarget
   public void onUpdate(EventTick event) {
      if (mc.player != null && mc.world != null && mc.interactionManager != null) {
         if (mc.crosshairTarget instanceof BlockHitResult hit) {
            if (mc.options.attackKey.isPressed()) {
               accelerateClientBreak(
                  mc.interactionManager,
                  mc.player,
                  mc.world,
                  hit.getBlockPos(),
                  hit.getSide(),
                  this.speed.get(),
                  true
               );
            }
         }
      }
   }

   public float getSpeed() {
      return this.speed.get();
   }

   public static int getExtraTicks(float speed) {
      return Math.max(
         1,
         Math.round(Math.max(0.30000001192092896f, speed) / 0.3499999940395355f)
      );
   }

   public static boolean accelerateClientBreak(
      ClientPlayerInteractionManager interactionManager, ClientPlayerEntity player, ClientWorld world, BlockPos pos, Direction side, float speed, boolean swing
   ) {
      if (interactionManager != null && player != null && world != null && pos != null) {
         BlockState state = world.getBlockState(pos);
         if (state != null && !state.isAir()) {
            Direction breakSide = side == null ? Direction.UP : side;
            int extraTicks = getExtraTicks(speed);

            for (int i = 0; i < extraTicks; i++) {
               interactionManager.updateBlockBreakingProgress(pos, breakSide);
            }

            if (swing) {
               player.swingHand(Hand.MAIN_HAND);
            }

            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public static boolean packetBreak(ClientPlayNetworkHandler handler, ClientPlayerEntity player, BlockPos pos, Direction side, boolean swing) {
      if (handler != null && player != null && pos != null) {
         Direction breakSide = side == null ? Direction.UP : side;
         handler.sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, pos, breakSide));
         handler.sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, pos, breakSide));
         if (swing) {
            handler.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
         }

         return true;
      } else {
         return false;
      }
   }
}
