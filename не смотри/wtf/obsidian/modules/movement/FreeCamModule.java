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

























import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityPose;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class FreeCamModule extends Module {
   public static FreeCamModule INSTANCE = new FreeCamModule();
   public Vec3d pos;

   public FreeCamModule() {
      super("FreeCam", "Обзор местности за фейк игрока", Category.MOVEMENT);
   }

   @Override
   public void onEnable() {
      super.onEnable();
      if (mc.player != null) {
         this.pos = mc.player.getPos();
      }
   }

   @Override
   public void onDisable() {
      super.onDisable();
      if (mc.player != null && this.pos != null) {
         mc.player.setPosition(this.pos);
      }
   }

   @EventTarget
   public void onEvent(PacketEvent event) {
      Packet<?> packet = event.getPacket();
      if (packet instanceof PlayerMoveC2SPacket) {
         event.cancel();
      } else if (packet instanceof PlayerRespawnS2CPacket || packet instanceof GameJoinS2CPacket) {
         this.toggle();
      }
   }

   @EventTarget
   public void onEvent(RenderEvent event) {
      if (this.pos != null && mc.player != null) {
         float width = mc.player.getWidth() / 2.0f;
         float height = mc.player.getHeight();
         Box box = new Box(
            this.pos.x - (double)width,
            this.pos.y,
            this.pos.z - (double)width,
            this.pos.x + (double)width,
            this.pos.y + (double)height,
            this.pos.z + (double)width
         );
         this.drawHitbox(event.getMatrices(), box, event.getCamera().getPos());
      }
   }
   public void drawHitbox(MatrixStack matrices, Box box, Vec3d camera) {
      double x1 = box.minX - camera.x;
      double y1 = box.minY - camera.y;
      double z1 = box.minZ - camera.z;
      double x2 = box.maxX - camera.x;
      double y2 = box.maxY - camera.y;
      double z2 = box.maxZ - camera.z;
      Matrix4f matrix = matrices.peek().getPositionMatrix();
      Tessellator tessellator = Tessellator.getInstance();
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableCull();
      RenderSystem.disableDepthTest();
      RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
      RenderSystem.lineWidth(1.5f);
      BufferBuilder buffer = tessellator.begin(DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
      float r = 1.0f;
      float g = 1.0f;
      float b = 1.0f;
      float a = 1.0f;
      buffer.vertex(matrix, (float)x1, (float)y1, (float)z1).color(r, g, b, a);
      buffer.vertex(matrix, (float)x2, (float)y1, (float)z1).color(r, g, b, a);
      buffer.vertex(matrix, (float)x2, (float)y1, (float)z1).color(r, g, b, a);
      buffer.vertex(matrix, (float)x2, (float)y1, (float)z2).color(r, g, b, a);
      buffer.vertex(matrix, (float)x2, (float)y1, (float)z2).color(r, g, b, a);
      buffer.vertex(matrix, (float)x1, (float)y1, (float)z2).color(r, g, b, a);
      buffer.vertex(matrix, (float)x1, (float)y1, (float)z2).color(r, g, b, a);
      buffer.vertex(matrix, (float)x1, (float)y1, (float)z1).color(r, g, b, a);
      buffer.vertex(matrix, (float)x1, (float)y2, (float)z1).color(r, g, b, a);
      buffer.vertex(matrix, (float)x2, (float)y2, (float)z1).color(r, g, b, a);
      buffer.vertex(matrix, (float)x2, (float)y2, (float)z1).color(r, g, b, a);
      buffer.vertex(matrix, (float)x2, (float)y2, (float)z2).color(r, g, b, a);
      buffer.vertex(matrix, (float)x2, (float)y2, (float)z2).color(r, g, b, a);
      buffer.vertex(matrix, (float)x1, (float)y2, (float)z2).color(r, g, b, a);
      buffer.vertex(matrix, (float)x1, (float)y2, (float)z2).color(r, g, b, a);
      buffer.vertex(matrix, (float)x1, (float)y2, (float)z1).color(r, g, b, a);
      buffer.vertex(matrix, (float)x1, (float)y1, (float)z1).color(r, g, b, a);
      buffer.vertex(matrix, (float)x1, (float)y2, (float)z1).color(r, g, b, a);
      buffer.vertex(matrix, (float)x2, (float)y1, (float)z1).color(r, g, b, a);
      buffer.vertex(matrix, (float)x2, (float)y2, (float)z1).color(r, g, b, a);
      buffer.vertex(matrix, (float)x2, (float)y1, (float)z2).color(r, g, b, a);
      buffer.vertex(matrix, (float)x2, (float)y2, (float)z2).color(r, g, b, a);
      buffer.vertex(matrix, (float)x1, (float)y1, (float)z2).color(r, g, b, a);
      buffer.vertex(matrix, (float)x1, (float)y2, (float)z2).color(r, g, b, a);
      BufferRenderer.drawWithGlobalProgram(buffer.end());
      RenderSystem.enableDepthTest();
      RenderSystem.enableCull();
      RenderSystem.disableBlend();
   }

   @EventTarget
   public void onEvent(Event_321 event) {
      if (mc.player != null) {
         mc.player.noClip = true;
         double speed = 1.0;
         double forward = (double)mc.player.input.movementForward;
         double strafe = (double)mc.player.input.movementSideways;
         double yaw = Math.toRadians((double)mc.player.getYaw());
         double motionX = 0.0;
         double motionZ = 0.0;
         if (forward != 0.0
            || strafe != 0.0) {
            double angle = yaw + Math.atan2(-strafe, forward);
            motionX = -Math.sin(angle) * speed;
            motionZ = Math.cos(angle) * speed;
         }

         double motionY = 0.0;
         if (mc.options.jumpKey.isPressed()) {
            motionY = speed;
         } else if (mc.options.sneakKey.isPressed()) {
            motionY = -speed;
         }

         event.setMovePos(new Vec3d(motionX, motionY, motionZ));
      }
   }

   @EventTarget
   public void onEvent(Event_102 event) {
      if (mc.player != null) {
         if (mc.player.getPose() == EntityPose.CROUCHING || mc.player.getPose() == EntityPose.SWIMMING) {
            event.setStrafe(event.getStrafe() * 5.0f);
         }
      }
   }
}
