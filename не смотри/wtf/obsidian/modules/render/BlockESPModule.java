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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.world.chunk.WorldChunk;
import org.joml.Matrix4f;

public class BlockESPModule extends Module {
   public static BlockESPModule INSTANCE = new BlockESPModule();
   public static final float BOX_LINE_WIDTH = 2.0F;
   public static final float FILL_ALPHA = 0.18F;
   public static final float GREEN_R = 0.1F;
   public static final float GREEN_G = 1.0F;
   public static final float GREEN_B = 0.15F;
   public static final long SCAN_INTERVAL_MS = 50L;
   public static final int MAX_CHUNKS_PER_PASS = 2;
   private final SliderSetting distance = new SliderSetting(
      "Дистанция",
      60.0f,
      10.0f,
      120.0f,
      1.0f
   );
   private final Set<String> trackedBlocks = ConcurrentHashMap.newKeySet();
   private final Map<BlockPos, String> foundBlocks = new ConcurrentHashMap<>();
   private final Set<ChunkPos> scannedChunks = ConcurrentHashMap.newKeySet();
   public ChunkPos lastPlayerChunk;
   public int lastScanRadius = -1;
   public long lastScanTime;

   public BlockESPModule() {
      super("BlockESP", "Показывает выбранные блоки через стену", Category.RENDER);
      Setting[] var10001 = new Setting[1];
      var10001[0] = this.distance;
      this.addSettings(var10001);
   }

   @Override
   public void onEnable() {
      this.resetScanState();
      super.onEnable();
   }

   @Override
   public void onDisable() {
      this.resetScanState();
      super.onDisable();
   }

   @EventTarget(
      priority = 100
   )
   public void onRender3D(RenderEvent event) {
      if (mc.world != null && mc.player != null && !this.trackedBlocks.isEmpty()) {
         int scanRadius = this.getDistance();
         ChunkPos currentChunk = new ChunkPos(mc.player.getBlockPos());
         if (scanRadius != this.lastScanRadius) {
            this.resetScanState();
            this.lastScanRadius = scanRadius;
         }

         if (this.lastPlayerChunk == null || !this.lastPlayerChunk.equals(currentChunk)) {
            this.scannedChunks.clear();
            this.lastPlayerChunk = currentChunk;
         }

         long now = System.currentTimeMillis();
         if (now - this.lastScanTime >= (50L)) {
            this.scanNearbyBlocks(scanRadius);
            this.lastScanTime = now;
         }

         this.cleanupInvalidAndDistantBlocks(mc.player.getPos(), scanRadius);
         this.renderFoundBlocks(event.getMatrices());
      }
   }
   public void scanNearbyBlocks(int scanRadius) {
      if (mc.world != null && mc.player != null) {
         BlockPos playerPos = mc.player.getBlockPos();
         int playerChunkX = playerPos.getX() >> (4);
         int playerChunkZ = playerPos.getZ() >> (4);
         int chunkRange = (scanRadius >> 4) + 2;
         List<ChunkPos> candidates = new ArrayList<>();

         for (int cx = -chunkRange; cx <= chunkRange; cx++) {
            for (int cz = -chunkRange; cz <= chunkRange; cz++) {
               ChunkPos chunkPos = new ChunkPos(playerChunkX + cx, playerChunkZ + cz);
               if (!this.scannedChunks.contains(chunkPos)) {
                  candidates.add(chunkPos);
               }
            }
         }

         candidates.sort((a, b) -> {
            long da = this.chunkDistanceSq(a, playerChunkX, playerChunkZ);
            long db = this.chunkDistanceSq(b, playerChunkX, playerChunkZ);
            return Long.compare(da, db);
         });
         int scannedThisPass = 0;

         for (ChunkPos chunkPos : candidates) {
            if (scannedThisPass >= (2)) {
               break;
            }

            WorldChunk chunk = mc.world.getChunk(chunkPos.x, chunkPos.z);
            if (chunk != null) {
               this.scanChunk(chunk, playerPos, scanRadius);
               this.scannedChunks.add(chunkPos);
               scannedThisPass++;
            }
         }
      }
   }
   public void scanChunk(WorldChunk chunk, BlockPos playerPos, int scanRadius) {
      int minX = chunk.getPos().getStartX();
      int minZ = chunk.getPos().getStartZ();
      int maxX = minX + (15);
      int maxZ = minZ + 15;
      int minY = Math.max(mc.world.getBottomY(), playerPos.getY() - scanRadius);
      int maxY = Math.min(mc.world.getTopYInclusive(), playerPos.getY() + scanRadius);
      int radiusSq = scanRadius * scanRadius;
      Mutable mutable = new Mutable();

      for (int x = minX; x <= maxX; x++) {
         for (int z = minZ; z <= maxZ; z++) {
            for (int y = minY; y <= maxY; y++) {
               mutable.set(x, y, z);
               if (!(mutable.getSquaredDistance(playerPos) > (double)radiusSq)) {
                  BlockState state = chunk.getBlockState(mutable);
                  if (!state.isAir()) {
                     String blockName = Registries.BLOCK.getId(state.getBlock()).getPath().toLowerCase();
                     if (this.trackedBlocks.contains(blockName)) {
                        this.foundBlocks.put(mutable.toImmutable(), blockName);
                     }
                  }
               }
            }
         }
      }
   }
   public void cleanupInvalidAndDistantBlocks(Vec3d playerPos, int renderDistance) {
      if (mc.world == null) {
         this.foundBlocks.clear();
      } else {
         int renderDistanceSq = renderDistance * renderDistance;
         this.foundBlocks
            .entrySet()
            .removeIf(
               entry -> {
                  BlockPos pos = entry.getKey();
                  BlockState currentState = mc.world.getBlockState(pos);
                  if (currentState.isAir()) {
                     return true;
                  } else {
                     String currentBlockName = Registries.BLOCK.getId(currentState.getBlock()).getPath().toLowerCase();
                     if (!this.trackedBlocks.contains(currentBlockName)) {
                        return true;
                     } else {
                        return (pos.getSquaredDistance(playerPos) > (double)renderDistanceSq);
                     }
                  }
               }
            );
      }
   }
   public void renderFoundBlocks(MatrixStack matrices) {
      if (!this.foundBlocks.isEmpty()) {
         Vec3d camera = mc.gameRenderer.getCamera().getPos();
         matrices.push();
         matrices.translate(-camera.x, -camera.y, -camera.z);
         Matrix4f matrix = matrices.peek().getPositionMatrix();
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableCull();
         RenderSystem.disableDepthTest();
         RenderSystem.depthMask(false);
         RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
         Tessellator tessellator = Tessellator.getInstance();
         BufferBuilder fillBuffer = tessellator.begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);

         for (BlockPos pos : this.foundBlocks.keySet()) {
            this.addFilledBox(
               fillBuffer,
               matrix,
               pos,
               0.10000000149011612f,
               1.0f,
               0.15000000596046448f,
               0.18000000715255737f
            );
         }

         BufferRenderer.drawWithGlobalProgram(fillBuffer.end());
         RenderSystem.lineWidth(2.0f);
         BufferBuilder lineBuffer = tessellator.begin(DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

         for (BlockPos pos : this.foundBlocks.keySet()) {
            this.addOutlinedBox(
               lineBuffer,
               matrix,
               pos,
               0.10000000149011612f,
               1.0f,
               0.15000000596046448f,
               1.0f
            );
         }

         BufferRenderer.drawWithGlobalProgram(lineBuffer.end());
         RenderSystem.enableCull();
         RenderSystem.enableDepthTest();
         RenderSystem.depthMask(true);
         RenderSystem.disableBlend();
         matrices.pop();
      }
   }
   public void addFilledBox(BufferBuilder buffer, Matrix4f matrix, BlockPos pos, float r, float g, float b, float a) {
      float minX = (float)pos.getX();
      float minY = (float)pos.getY();
      float minZ = (float)pos.getZ();
      float maxX = minX + 1.0f;
      float maxY = minY + 1.0f;
      float maxZ = minZ + 1.0f;
      buffer.vertex(matrix, minX, minY, minZ).color(r, g, b, a);
      buffer.vertex(matrix, maxX, minY, minZ).color(r, g, b, a);
      buffer.vertex(matrix, maxX, minY, maxZ).color(r, g, b, a);
      buffer.vertex(matrix, minX, minY, maxZ).color(r, g, b, a);
      buffer.vertex(matrix, minX, maxY, minZ).color(r, g, b, a);
      buffer.vertex(matrix, minX, maxY, maxZ).color(r, g, b, a);
      buffer.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, a);
      buffer.vertex(matrix, maxX, maxY, minZ).color(r, g, b, a);
      buffer.vertex(matrix, minX, minY, minZ).color(r, g, b, a);
      buffer.vertex(matrix, minX, maxY, minZ).color(r, g, b, a);
      buffer.vertex(matrix, maxX, maxY, minZ).color(r, g, b, a);
      buffer.vertex(matrix, maxX, minY, minZ).color(r, g, b, a);
      buffer.vertex(matrix, minX, minY, maxZ).color(r, g, b, a);
      buffer.vertex(matrix, maxX, minY, maxZ).color(r, g, b, a);
      buffer.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, a);
      buffer.vertex(matrix, minX, maxY, maxZ).color(r, g, b, a);
      buffer.vertex(matrix, minX, minY, minZ).color(r, g, b, a);
      buffer.vertex(matrix, minX, minY, maxZ).color(r, g, b, a);
      buffer.vertex(matrix, minX, maxY, maxZ).color(r, g, b, a);
      buffer.vertex(matrix, minX, maxY, minZ).color(r, g, b, a);
      buffer.vertex(matrix, maxX, minY, minZ).color(r, g, b, a);
      buffer.vertex(matrix, maxX, maxY, minZ).color(r, g, b, a);
      buffer.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, a);
      buffer.vertex(matrix, maxX, minY, maxZ).color(r, g, b, a);
   }
   public void addOutlinedBox(BufferBuilder buffer, Matrix4f matrix, BlockPos pos, float r, float g, float b, float a) {
      float minX = (float)pos.getX();
      float minY = (float)pos.getY();
      float minZ = (float)pos.getZ();
      float maxX = minX + 1.0f;
      float maxY = minY + 1.0f;
      float maxZ = minZ + 1.0f;
      buffer.vertex(matrix, minX, minY, minZ).color(r, g, b, a);
      buffer.vertex(matrix, maxX, minY, minZ).color(r, g, b, a);
      buffer.vertex(matrix, maxX, minY, minZ).color(r, g, b, a);
      buffer.vertex(matrix, maxX, minY, maxZ).color(r, g, b, a);
      buffer.vertex(matrix, maxX, minY, maxZ).color(r, g, b, a);
      buffer.vertex(matrix, minX, minY, maxZ).color(r, g, b, a);
      buffer.vertex(matrix, minX, minY, maxZ).color(r, g, b, a);
      buffer.vertex(matrix, minX, minY, minZ).color(r, g, b, a);
      buffer.vertex(matrix, minX, maxY, minZ).color(r, g, b, a);
      buffer.vertex(matrix, maxX, maxY, minZ).color(r, g, b, a);
      buffer.vertex(matrix, maxX, maxY, minZ).color(r, g, b, a);
      buffer.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, a);
      buffer.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, a);
      buffer.vertex(matrix, minX, maxY, maxZ).color(r, g, b, a);
      buffer.vertex(matrix, minX, maxY, maxZ).color(r, g, b, a);
      buffer.vertex(matrix, minX, maxY, minZ).color(r, g, b, a);
      buffer.vertex(matrix, minX, minY, minZ).color(r, g, b, a);
      buffer.vertex(matrix, minX, maxY, minZ).color(r, g, b, a);
      buffer.vertex(matrix, maxX, minY, minZ).color(r, g, b, a);
      buffer.vertex(matrix, maxX, maxY, minZ).color(r, g, b, a);
      buffer.vertex(matrix, maxX, minY, maxZ).color(r, g, b, a);
      buffer.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, a);
      buffer.vertex(matrix, minX, minY, maxZ).color(r, g, b, a);
      buffer.vertex(matrix, minX, maxY, maxZ).color(r, g, b, a);
   }

   public void addBlock(String blockName) {
      this.trackedBlocks.add(blockName.toLowerCase());
      this.scannedChunks.clear();
      this.foundBlocks.clear();
   }

   public void removeBlock(String blockName) {
      this.trackedBlocks.remove(blockName.toLowerCase());
      this.foundBlocks.entrySet().removeIf(entry -> entry.getValue().equalsIgnoreCase(blockName));
   }

   public void clearBlocks() {
      this.trackedBlocks.clear();
      this.resetScanState();
   }

   public Set<String> getTrackedBlocks() {
      return new HashSet<>(this.trackedBlocks);
   }

   public boolean isTracking(String blockName) {
      return this.trackedBlocks.contains(blockName.toLowerCase());
   }
   public int getDistance() {
      return Math.round(this.distance.get());
   }
   public long chunkDistanceSq(ChunkPos chunkPos, int playerChunkX, int playerChunkZ) {
      long dx = (long)(chunkPos.x - playerChunkX);
      long dz = (long)(chunkPos.z - playerChunkZ);
      return dx * dx + dz * dz;
   }
   public void resetScanState() {
      this.foundBlocks.clear();
      this.scannedChunks.clear();
      this.lastPlayerChunk = null;
      this.lastScanTime = 0L;
      this.lastScanRadius = -1;
   }
}
