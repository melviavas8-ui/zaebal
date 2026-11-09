package wtf.obsidian.modules.render;

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
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.WorldChunk;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class LootTrackerModule extends Module {
   public static final LootTrackerModule INSTANCE = new LootTrackerModule();
   public static final float TAG_BOX_HEIGHT = 12.5F;
   public static final float TAG_PADDING = 5.0F;
   public static final float TAG_HUD_RADIUS = 1.1F;
   public static final int TAG_HUD_ALPHA = 204;
   private final BooleanSetting showSpawners = new BooleanSetting("Спавнеры", true);
   private final BooleanSetting showMinecarts = new BooleanSetting("Вагонетки", true);
   private final SliderSetting maxDistance = new SliderSetting(
      "Макс. дистанция",
      64.0f,
      16.0f,
      128.0f,
      1.0f
   );
   private final Matrix4f lastProjectionMatrix = new Matrix4f();
   private final Quaternionf lastCameraRotation = new Quaternionf();
   public Vec3d lastCameraPos = Vec3d.ZERO;
   public float lastTickDelta;
   public boolean hasProjection;
   private final List<ObsidianComponent_115> cachedSources = new ArrayList<>();
   public long lastCacheUpdate = 0L;
   public static final long CACHE_UPDATE_INTERVAL = 500L;

   public LootTrackerModule() {
      super("LootTracker", "Показывает залутанные спавнеры и вагонетки", Category.RENDER);
      Setting[] var10001 = new Setting[3];
      var10001[0] = this.showSpawners;
      var10001[1] = this.showMinecarts;
      var10001[2] = this.maxDistance;
      this.addSettings(var10001);
   }

   @Override
   public void onDisable() {
      this.hasProjection = false;
      this.cachedSources.clear();
      super.onDisable();
   }

   @EventTarget(
      priority = 100
   )
   public void onRender3D(RenderEvent event) {
      if (mc.player != null && mc.world != null) {
         this.hasProjection = true;
         this.lastProjectionMatrix.set(event.getProjectionMatrix());
         this.lastCameraPos = event.getCamera().getPos();
         this.lastCameraRotation.set(event.getCamera().getRotation());
         this.lastTickDelta = event.getTickDelta();
         long now = System.currentTimeMillis();
         if (now - this.lastCacheUpdate > (500L)) {
            this.updateCache();
            this.lastCacheUpdate = now;
         }
      }
   }

   @EventTarget(
      priority = 100
   )
   public void onRender2D(EventRender2D event) {
      if (this.hasProjection && mc.world != null && mc.player != null) {
         MatrixStack matrices = event.getContext().getMatrices();
         FontRenderer font = ObsidianComponent_201.getFont("sf_regular", 14);
         if (font != null) {
            for (ObsidianComponent_115 source : this.cachedSources) {
               if (!(
                  mc.player.squaredDistanceTo((double)source.pos.getX(), (double)source.pos.getY(), (double)source.pos.getZ())
                     > (double)(this.maxDistance.getValue().floatValue() * this.maxDistance.getValue().floatValue())
               )) {
                  Vec3d screenPos = this.worldToScreen(
                     new Vec3d(
                        (double)source.pos.getX() + 0.5,
                        (double)source.pos.getY() + 1.5,
                        (double)source.pos.getZ() + 0.5
                     )
                  );
                  if (screenPos != null) {
                     this.drawLootTag(matrices, font, (float)screenPos.x, (float)screenPos.y, source);
                  }
               }
            }
         }
      }
   }
   public void updateCache() {
      this.cachedSources.clear();
      if (this.showSpawners.isState()) {
         int renderDistance = (Integer)mc.options.getViewDistance().getValue();
         ChunkPos playerChunk = mc.player.getChunkPos();

         for (int cx = -renderDistance; cx <= renderDistance; cx++) {
            for (int cz = -renderDistance; cz <= renderDistance; cz++) {
               WorldChunk chunk = mc.world.getChunk(playerChunk.x + cx, playerChunk.z + cz);
               if (chunk != null) {
                  for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                     if (blockEntity instanceof MobSpawnerBlockEntity) {
                        MobSpawnerBlockEntity spawner = (MobSpawnerBlockEntity)blockEntity;
                        if (this.hasSingleChestNearby(spawner.getPos())) {
                           int delay = this.getSpawnerDelay(spawner);
                           boolean isLooted = (!((delay <= 0 || delay == (20)) && !this.isAreaExplored(spawner.getPos())));
                           this.cachedSources.add(new ObsidianComponent_115(spawner.getPos(), ObsidianComponent_179.SPAWNER, isLooted));
                        }
                     }
                  }
               }
            }
         }
      }

      if (this.showMinecarts.isState()) {
         for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof ChestMinecartEntity minecart) {
               boolean isLooted = this.isAreaExplored(minecart.getBlockPos());
               this.cachedSources.add(new ObsidianComponent_115(minecart.getBlockPos(), ObsidianComponent_179.MINECART, isLooted));
            }
         }
      }
   }
   public void drawLootTag(MatrixStack matrices, FontRenderer font, float x, float y, ObsidianComponent_115 source) {
      String typeText = source.type == ObsidianComponent_179.SPAWNER ? "Спавнер" : "Вагонетка";
      String statusText = source.isLooted ? " [Залутано]" : " [Не залутано]";
      float typeWidth = font.getStringWidth(typeText);
      float statusWidth = font.getStringWidth(statusText);
      float totalWidth = typeWidth + statusWidth;
      float boxWidth = totalWidth + 10.0f;
      float boxHeight = 12.5f;
      float tagX = x - boxWidth / 2.0f;
      float tagY = y - boxHeight / 2.0f;
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      this.drawDefaultTagPanel(matrices, tagX, tagY, boxWidth, boxHeight);
      float textX = tagX + 5.0f;
      float textY = tagY
         + boxHeight / 2.0f
         - font.getHeight() * 0.10000000149011612f;
      font.drawString(matrices, typeText, textX, textY, -1);
      textX += typeWidth;
      int statusColor = source.isLooted ? -43691 : -11141291;
      font.drawString(matrices, statusText, textX, textY, statusColor);
      RenderSystem.disableBlend();
   }
   public void drawDefaultTagPanel(MatrixStack matrices, float x, float y, float width, float height) {
      int themeColor = this.getStableThemeColor();
      RenderUtil.drawDefaultHudPanel(
         matrices,
         x,
         y,
         width,
         height,
         1.100000023841858f,
         1.100000023841858f,
         ObsidianComponent_olorUtil.rgba(
            50,
            50,
            50,
            204
         ),
         ObsidianComponent_olorUtil.setAlphaColor(ObsidianComponent_olorUtil.darken(themeColor, 0.15000000596046448f), 204),
         ObsidianComponent_olorUtil.setAlphaColor(
            ObsidianComponent_olorUtil.darken(themeColor, 0.05000000074505806f), 204
         )
      );
   }
   public int getStableThemeColor() {
      if (Obsidian.INSTANCE != null && Obsidian.INSTANCE.themeStorage != null && Obsidian.INSTANCE.themeStorage.getThemes() != null) {
         ObsidianComponent_olorPalette theme = Obsidian.INSTANCE.themeStorage.getThemes().getTheme();
         return theme != null && theme.color != null && theme.color.length != 0
            ? theme.color[0]
            : ObsidianComponent_olorUtil.getThemeColor(0);
      } else {
         return ObsidianComponent_olorUtil.getThemeColor(0);
      }
   }
   public boolean isAreaExplored(BlockPos pos) {
      int radius = 20;
      int airCount = 0;
      int checkCount = 0;

      for (int x = -radius; x <= radius; x += 4) {
         for (int y = -radius; y <= radius; y += 4) {
            for (int z = -radius; z <= radius; z += 4) {
               BlockPos checkPos = pos.add(x, y, z);
               BlockState state = mc.world.getBlockState(checkPos);
               checkCount++;
               if (state.isOf(Blocks.AIR) || state.isOf(Blocks.CAVE_AIR)) {
                  airCount++;
               }
            }
         }
      }

      return ((double)airCount > (double)checkCount * 0.3);
   }
   public int getSpawnerDelay(MobSpawnerBlockEntity spawner) {
      try {
         NbtCompound tag = spawner.createNbtWithIdentifyingData(mc.world.getRegistryManager());
         return tag.contains("Delay") ? tag.getShort("Delay") : 20;
      } catch (Exception var3) {
         return 20;
      }
   }
   public boolean hasSingleChestNearby(BlockPos spawnerPos) {
      int radius = 3;

      for (int x = -radius; x <= radius; x++) {
         for (int y = -radius; y <= radius; y++) {
            for (int z = -radius; z <= radius; z++) {
               BlockPos checkPos = spawnerPos.add(x, y, z);
               BlockState state = mc.world.getBlockState(checkPos);
               if (state.getBlock() instanceof ChestBlock && state.get(ChestBlock.CHEST_TYPE) == ChestType.SINGLE) {
                  return true;
               }
            }
         }
      }

      return false;
   }
   public Vec3d worldToScreen(Vec3d worldPos) {
      Vector3f relative = new Vector3f(
         (float)(worldPos.x - this.lastCameraPos.x), (float)(worldPos.y - this.lastCameraPos.y), (float)(worldPos.z - this.lastCameraPos.z)
      );
      Quaternionf invCameraRotation = new Quaternionf(this.lastCameraRotation).conjugate();
      relative.rotate(invCameraRotation);
      Vector4f clip = new Vector4f(relative.x, relative.y, relative.z, 1.0f);
      this.lastProjectionMatrix.transform(clip);
      float w = clip.w;
      if (w <= 9.999999747378752e-06f) {
         return null;
      } else {
         float ndcX = clip.x / w;
         float ndcY = clip.y / w;
         float screenX = (
               ndcX * 0.5f + 0.5f
            )
            * (float)mc.getWindow().getScaledWidth();
         float screenY = (
               1.0f
                  - (ndcY * 0.5f + 0.5f)
            )
            * (float)mc.getWindow().getScaledHeight();
         if (Float.isNaN(screenX) || Float.isNaN(screenY)) {
            return null;
         } else {
            return !Float.isInfinite(screenX) && !Float.isInfinite(screenY)
               ? new Vec3d((double)screenX, (double)screenY, 0.0)
               : null;
         }
      }
   }
}
