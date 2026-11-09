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
import java.util.Optional;
import net.minecraft.block.Blocks;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.TridentItem;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import org.joml.Matrix4f;

public class TrajectoriesModule extends Module {
   public static TrajectoriesModule INSTANCE = new TrajectoriesModule();
   public static final int MAX_STEPS = 440;
   public static final double SIMULATION_STEP = 0.5;
   public static final double SPLASH_RADIUS = 4.0;
   private static final Identifier GLOW_TEXTURE = Identifier.of("obsidian", "textures/trajectories/glow.png");
   private final SliderSetting lineWidth = new SliderSetting(
      "Ширина линии",
      2.200000047683716f,
      0.5f,
      5.0f,
      0.10000000149011612f
   );

   public TrajectoriesModule() {
      super("Trajectories", "Показывает траекторию предмета в руке", Category.RENDER);
      Setting[] var10001 = new Setting[1];
      var10001[0] = this.lineWidth;
      this.addSettings(var10001);
   }

   @EventTarget
   public void onRender3D(RenderEvent event) {
      if (mc.player != null && mc.world != null) {
         ItemStack stack = this.getHeldProjectileStack();
         if (!stack.isEmpty()) {
            ObsidianComponent_111 params = this.getParams(stack);
            if (params != null) {
               float tickDelta = event.getTickDelta();
               Vec3d startPos = mc.player.getCameraPosVec(tickDelta);
               Vec3d[] directions = this.getShotDirections(stack, tickDelta);
               ObsidianComponent_314[] results = new ObsidianComponent_314[directions.length];
               int resultCount = 0;
               Vec3d[] matrices = directions;
               int camera = directions.length;

               for (int cameraPos = 0; cameraPos < camera; cameraPos++) {
                  Vec3d direction = matrices[cameraPos];
                  ObsidianComponent_314 result = this.predict(mc.player, params, startPos, direction);
                  if (result != null && result.points.length >= (2)) {
                     results[resultCount++] = result;
                  }
               }

               if (resultCount != 0) {
                  MatrixStack matricesx = event.getMatrices();
                  Camera camerax = event.getCamera();
                  Vec3d cameraPosx = camerax.getPos();
                  int themeColor = ObsidianComponent_olorUtil.getThemeColor();
                  RenderSystem.enableBlend();
                  RenderSystem.defaultBlendFunc();
                  RenderSystem.disableCull();
                  RenderSystem.enableDepthTest();
                  RenderSystem.depthMask(false);
                  RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
                  RenderSystem.lineWidth(this.lineWidth.getValue().floatValue());
                  matricesx.push();
                  matricesx.translate(-cameraPosx.x, -cameraPosx.y, -cameraPosx.z);
                  Matrix4f matrix = matricesx.peek().getPositionMatrix();

                  for (int i = 0; i < resultCount; i++) {
                     ObsidianComponent_314 result = results[i];
                     this.drawTrajectoryLine(matrix, result.points, ObsidianComponent_olorUtil.setAlphaColor(themeColor, 190));
                     if (result.entityHit != null && result.entityHit.isAlive()) {
                        this.drawEntityBox(
                           matrix,
                           result.entityHit,
                           ObsidianComponent_olorUtil.rgba(
                              255,
                              70,
                              70,
                              210
                           )
                        );
                     } else if (result.blockHit != null) {
                        this.drawImpactMarker(
                           matrix, result.hitPos, result.blockHit.getSide(), ObsidianComponent_olorUtil.setAlphaColor(themeColor, 230)
                        );
                     }

                     if (stack.isOf(Items.SPLASH_POTION) && result.hitPos != null) {
                        this.drawPotionRadiusGlow(matricesx, result.hitPos, themeColor);
                     }
                  }

                  matricesx.pop();
                  RenderSystem.depthMask(true);
                  RenderSystem.enableCull();
                  RenderSystem.disableBlend();
                  RenderSystem.defaultBlendFunc();
               }
            }
         }
      }
   }
   public ItemStack getHeldProjectileStack() {
      ItemStack main = mc.player.getMainHandStack();
      if (!main.isEmpty() && this.getParams(main) != null) {
         return main;
      } else {
         ItemStack off = mc.player.getOffHandStack();
         return !off.isEmpty() && this.getParams(off) != null ? off : ItemStack.EMPTY;
      }
   }
   public ObsidianComponent_111 getParams(ItemStack stack) {
      Item item = stack.getItem();
      if (item == Items.ENDER_PEARL || item == Items.SNOWBALL || item == Items.EGG) {
         return new ObsidianComponent_111(
            1.5,
            0.03,
            0.99
         );
      } else if (item == Items.SPLASH_POTION || item == Items.LINGERING_POTION) {
         return new ObsidianComponent_111(
            0.5,
            0.05,
            0.99
         );
      } else if (item instanceof BowItem) {
         float power = 1.0f;
         if (mc.player.isUsingItem() && mc.player.getActiveItem() == stack) {
            float use = (float)mc.player.getItemUseTime();
            float f = use / 20.0f;
            f = (f * f + f * 2.0f) / 3.0f;
            power = Math.min(f, 1.0f);
         }

         double velocity = 3.0 * (double)power;
         return velocity <= 0.01
            ? null
            : new ObsidianComponent_111(
               velocity,
               0.05,
               0.99
            );
      } else if (item instanceof CrossbowItem) {
         return !CrossbowItem.isCharged(stack)
            ? null
            : new ObsidianComponent_111(
               3.15,
               0.05,
               0.99
            );
      } else {
         return item instanceof TridentItem
            ? new ObsidianComponent_111(
               2.5,
               0.05,
               0.99
            )
            : null;
      }
   }
   public Vec3d[] getShotDirections(ItemStack stack, float tickDelta) {
      Vec3d baseDir = mc.player.getRotationVec(tickDelta).normalize();
      if (stack.getItem() instanceof CrossbowItem && ObsidianComponent_225.getEnchantmentLevel(stack, Enchantments.MULTISHOT) > 0) {
         float baseYaw = (float)(
               MathHelper.atan2(baseDir.z, baseDir.x) * 57.29577951308232
            )
            - 90.0f;
         float basePitch = (float)(
            -(
               MathHelper.atan2(baseDir.y, (double)MathHelper.sqrt((float)(baseDir.x * baseDir.x + baseDir.z * baseDir.z)))
                  * 57.29577951308232
            )
         );
         Vec3d[] var6 = new Vec3d[3];
         var6[0] = this.getDirectionFromYawPitch(
            baseYaw - 10.0f, basePitch
         );
         var6[1] = baseDir;
         var6[2] = this.getDirectionFromYawPitch(
            baseYaw + 10.0f, basePitch
         );
         return var6;
      } else {
         Vec3d[] var10000 = new Vec3d[1];
         var10000[0] = baseDir;
         return var10000;
      }
   }
   public Vec3d getDirectionFromYawPitch(float yawDeg, float pitchDeg) {
      float yaw = yawDeg * 0.01745329238474369f;
      float pitch = pitchDeg * 0.01745329238474369f;
      float x = MathHelper.sin(-yaw - 3.1415927410125732f) * -MathHelper.cos(-pitch);
      float y = MathHelper.sin(-pitch);
      float z = MathHelper.cos(-yaw - 3.1415927410125732f) * -MathHelper.cos(-pitch);
      return new Vec3d((double)x, (double)y, (double)z).normalize();
   }
   public ObsidianComponent_314 predict(PlayerEntity player, ObsidianComponent_111 params, Vec3d startPos, Vec3d direction) {
      Vec3d pos = startPos;
      Vec3d motion = direction.normalize().multiply(params.velocity);
      Vec3d[] points = new Vec3d[441];
      int count = 0;
      points[count++] = startPos;
      Entity entityHit = null;
      Vec3d entityHitPos = null;

      for (int i = 0; i < 440; i++) {
         Vec3d next = pos.add(motion.multiply(0.5));
         if (entityHit == null) {
            FontGlyphMap hit = this.rayTraceEntities(pos, next, player);
            if (hit != null) {
               entityHit = hit.entity;
               entityHitPos = hit.hitPos;
            }
         }

         BlockHitResult blockHit = mc.world.raycast(new RaycastContext(pos, next, ShapeType.COLLIDER, FluidHandling.NONE, player));
         if (blockHit.getType() == Type.BLOCK) {
            points[count++] = blockHit.getPos();
            return new ObsidianComponent_314(this.copyPoints(points, count), blockHit, blockHit.getPos(), entityHit, entityHitPos);
         }

         points[count++] = next;
         pos = next;
         boolean inWater = mc.world.getBlockState(BlockPos.ofFloored(next)).isOf(Blocks.WATER);
         double drag = Math.pow(
            inWater ? 0.8 : params.drag,
            0.5
         );
         motion = motion.multiply(drag)
            .subtract(
               0.0,
               params.gravity * 0.5,
               0.0
            );
         if (next.y <= (double)mc.world.getBottomY()) {
            break;
         }
      }

      Vec3d hitPos = entityHitPos != null ? entityHitPos : points[count - 1];
      return new ObsidianComponent_314(this.copyPoints(points, count), null, hitPos, entityHit, entityHitPos);
   }
   public Vec3d[] copyPoints(Vec3d[] points, int count) {
      Vec3d[] out = new Vec3d[count];
      System.arraycopy(points, 0, out, 0, count);
      return out;
   }
   public FontGlyphMap rayTraceEntities(Vec3d from, Vec3d to, Entity owner) {
      Box search = new Box(from, to).expand(1.0);
      Entity closest = null;
      Vec3d closestHit = null;
      double closestDistance = 179769313486231570814527423731704356798070567525844996598917476803157260780028538760589558632766878171540458953514382464234321326889464182768467546703537516986049910576551282076245490090389328944075868508455133942304583236903222948165808559332123348274797826204144723168738177180919299881250404026184124858368.0;

      for (Entity entity : mc.world
         .getOtherEntities(
            owner,
            search,
            entityx -> (entityx != null && entityx.isAlive() && entityx.canHit())
         )) {
         Optional<Vec3d> hit = entity.getBoundingBox()
            .expand(0.3)
            .raycast(from, to);
         if (!hit.isEmpty()) {
            double distance = from.squaredDistanceTo(hit.get());
            if (distance < closestDistance) {
               closestDistance = distance;
               closest = entity;
               closestHit = hit.get();
            }
         }
      }

      return closest == null ? null : new FontGlyphMap(closest, closestHit);
   }
   public void drawTrajectoryLine(Matrix4f matrix, Vec3d[] points, int color) {
      int r = color >> 16 & 255;
      int g = color >> (8) & 255;
      int b = color & 255;
      int a = color >> 24 & (255);
      BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

      for (int i = 0; i < points.length - 1; i++) {
         Vec3d start = points[i];
         Vec3d end = points[i + 1];
         buffer.vertex(matrix, (float)start.x, (float)start.y, (float)start.z).color(r, g, b, a);
         buffer.vertex(matrix, (float)end.x, (float)end.y, (float)end.z).color(r, g, b, a);
      }

      BufferRenderer.drawWithGlobalProgram(buffer.end());
   }
   public void drawImpactMarker(Matrix4f matrix, Vec3d pos, Direction side, int color) {
      Vec3d normal = Vec3d.of(side.getVector()).normalize();
      Vec3d u = side != Direction.UP && side != Direction.DOWN
         ? normal.crossProduct(
               new Vec3d(
                  0.0,
                  1.0,
                  0.0
               )
            )
            .normalize()
         : new Vec3d(
            1.0,
            0.0,
            0.0
         );
      Vec3d v = normal.crossProduct(u).normalize();
      Vec3d center = pos.add(normal.multiply(0.004));
      double radius = 0.35;
      int r = color >> 16 & (255);
      int g = color >> (8) & (255);
      int b = color & 255;
      int a = color >> 24 & 255;
      BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
      int segments = 48;
      Vec3d previous = null;

      for (int i = 0; i <= segments; i++) {
         double angle = 6.283185307179586 * (double)i / (double)segments;
         Vec3d point = center.add(u.multiply(Math.cos(angle) * radius)).add(v.multiply(Math.sin(angle) * radius));
         if (previous != null) {
            buffer.vertex(matrix, (float)previous.x, (float)previous.y, (float)previous.z).color(r, g, b, a);
            buffer.vertex(matrix, (float)point.x, (float)point.y, (float)point.z).color(r, g, b, a);
         }

         previous = point;
      }

      Vec3d left = center.add(u.multiply(-radius));
      Vec3d right = center.add(u.multiply(radius));
      Vec3d down = center.add(v.multiply(-radius));
      Vec3d up = center.add(v.multiply(radius));
      buffer.vertex(matrix, (float)left.x, (float)left.y, (float)left.z).color(r, g, b, a);
      buffer.vertex(matrix, (float)right.x, (float)right.y, (float)right.z).color(r, g, b, a);
      buffer.vertex(matrix, (float)down.x, (float)down.y, (float)down.z).color(r, g, b, a);
      buffer.vertex(matrix, (float)up.x, (float)up.y, (float)up.z).color(r, g, b, a);
      BufferRenderer.drawWithGlobalProgram(buffer.end());
   }
   public void drawEntityBox(Matrix4f matrix, Entity entity, int color) {
      Box box = entity.getBoundingBox();
      int r = color >> 16 & (255);
      int g = color >> 8 & (255);
      int b = color & 255;
      int a = color >> (24) & 255;
      BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
      this.vertexBox(buffer, matrix, box, r, g, b, a);
      BufferRenderer.drawWithGlobalProgram(buffer.end());
   }
   public void vertexBox(BufferBuilder buffer, Matrix4f matrix, Box box, int r, int g, int b, int a) {
      float minX = (float)box.minX;
      float minY = (float)box.minY;
      float minZ = (float)box.minZ;
      float maxX = (float)box.maxX;
      float maxY = (float)box.maxY;
      float maxZ = (float)box.maxZ;
      this.line(buffer, matrix, minX, minY, minZ, maxX, minY, minZ, r, g, b, a);
      this.line(buffer, matrix, maxX, minY, minZ, maxX, minY, maxZ, r, g, b, a);
      this.line(buffer, matrix, maxX, minY, maxZ, minX, minY, maxZ, r, g, b, a);
      this.line(buffer, matrix, minX, minY, maxZ, minX, minY, minZ, r, g, b, a);
      this.line(buffer, matrix, minX, maxY, minZ, maxX, maxY, minZ, r, g, b, a);
      this.line(buffer, matrix, maxX, maxY, minZ, maxX, maxY, maxZ, r, g, b, a);
      this.line(buffer, matrix, maxX, maxY, maxZ, minX, maxY, maxZ, r, g, b, a);
      this.line(buffer, matrix, minX, maxY, maxZ, minX, maxY, minZ, r, g, b, a);
      this.line(buffer, matrix, minX, minY, minZ, minX, maxY, minZ, r, g, b, a);
      this.line(buffer, matrix, maxX, minY, minZ, maxX, maxY, minZ, r, g, b, a);
      this.line(buffer, matrix, maxX, minY, maxZ, maxX, maxY, maxZ, r, g, b, a);
      this.line(buffer, matrix, minX, minY, maxZ, minX, maxY, maxZ, r, g, b, a);
   }
   public void line(BufferBuilder buffer, Matrix4f matrix, float x1, float y1, float z1, float x2, float y2, float z2, int r, int g, int b, int a) {
      buffer.vertex(matrix, x1, y1, z1).color(r, g, b, a);
      buffer.vertex(matrix, x2, y2, z2).color(r, g, b, a);
   }
   public void drawPotionRadiusGlow(MatrixStack matrices, Vec3d pos, int themeColor) {
      int color = ObsidianComponent_olorUtil.setAlphaColor(themeColor, 82);
      int r = color >> (16) & 255;
      int g = color >> (8) & 255;
      int b = color & (255);
      int a = color >> (24) & 255;
      float radius = 4.0f;
      RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
      RenderSystem.setShaderTexture(0, GLOW_TEXTURE);
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      matrices.push();
      matrices.translate(pos.x, pos.y + 0.012, pos.z);
      Matrix4f matrix = matrices.peek().getPositionMatrix();
      BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
      buffer.vertex(matrix, -radius, 0.0f, -radius)
         .texture(0.0f, 0.0f)
         .color(r, g, b, a);
      buffer.vertex(matrix, -radius, 0.0f, radius)
         .texture(0.0f, 1.0f)
         .color(r, g, b, a);
      buffer.vertex(matrix, radius, 0.0f, radius)
         .texture(1.0f, 1.0f)
         .color(r, g, b, a);
      buffer.vertex(matrix, radius, 0.0f, -radius)
         .texture(1.0f, 0.0f)
         .color(r, g, b, a);
      BufferRenderer.drawWithGlobalProgram(buffer.end());
      matrices.pop();
      RenderSystem.setShaderTexture(0, 0);
      RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
   }
}
