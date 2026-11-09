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
import java.util.Random;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import org.joml.Matrix4f;

public class ParticlesModule extends Module {
   public static ParticlesModule INSTANCE = new ParticlesModule();
   private static final Identifier STAR_TEXTURE = Identifier.of("obsidian", "textures/particle/star.png");
   private static final Identifier HEART_TEXTURE = Identifier.of("obsidian", "textures/particle/heart.png");
   private static final Identifier DOLLAR_TEXTURE = Identifier.of("obsidian", "textures/particle/dollar.png");
   private static final Identifier BLOOM_TEXTURE = Identifier.of("obsidian", "textures/particle/bloom.png");
   private static final Identifier SPARKLE_TEXTURE = Identifier.of("obsidian", "textures/particle/sparkle.png");
   public final ModeSetting type;
   public final MultiBooleanSetting reason;
   public final SliderSetting count;
   public final BooleanSetting glow;
   public final ArrayList<ObsidianComponent_188> particles;
   public final Random rnd;

   public ParticlesModule() {
      super("Particles", "Красивые партиклы при разных действиях", Category.RENDER);
      String[] var10005 = new String[5];
      var10005[0] = "Звездочки";
      var10005[1] = "Сердечки";
      var10005[2] = "Доллары";
      var10005[3] = "Блум";
      var10005[4] = "Сияние";
      this.type = new ModeSetting("Тип частиц", "Звездочки", var10005);
      BooleanSetting[] var10004 = new BooleanSetting[6];
      var10004[0] = new BooleanSetting("Бездействии", false);
      var10004[1] = new BooleanSetting("Беге", false);
      var10004[2] = new BooleanSetting("Ударе", true);
      var10004[3] = new BooleanSetting("Падении перла", false);
      var10004[4] = new BooleanSetting("Падении трезубца", false);
      var10004[5] = new BooleanSetting("Сносе тотема", true);
      this.reason = new MultiBooleanSetting("Добавлять при", var10004);
      this.count = new SliderSetting(
         "Количество",
         10.0f,
         2.0f,
         40.0f,
         1.0f
      );
      this.glow = new BooleanSetting("Свечение", true);
      this.particles = new ArrayList<>();
      this.rnd = new Random();
      Setting[] var10001 = new Setting[4];
      var10001[0] = this.type;
      var10001[1] = this.reason;
      var10001[2] = this.count;
      var10001[3] = this.glow;
      this.addSettings(var10001);
   }

   @Override
   public void onDisable() {
      this.particles.clear();
      super.onDisable();
   }
   public Identifier getTexture() {
      return switch (this.type.getIndex()) {
         case 1 -> HEART_TEXTURE;
         case 2 -> DOLLAR_TEXTURE;
         case 3 -> BLOOM_TEXTURE;
         case 4 -> SPARKLE_TEXTURE;
         default -> STAR_TEXTURE;
      };
   }
   public boolean isPositionInBlock(Vec3d position) {
      if (mc.world != null && mc.player != null) {
         BlockPos blockPos = BlockPos.ofFloored(position);
         if (mc.world.getBlockState(blockPos).isSolidBlock(mc.world, blockPos)) {
            return true;
         } else {
            RaycastContext context = new RaycastContext(
               new Vec3d(mc.player.getX(), mc.player.getY() + (double)mc.player.getStandingEyeHeight(), mc.player.getZ()),
               position,
               ShapeType.COLLIDER,
               FluidHandling.NONE,
               mc.player
            );
            BlockHitResult result = mc.world.raycast(context);
            return (result.getType() == Type.BLOCK);
         }
      } else {
         return true;
      }
   }
   public float random(float min, float max) {
      return min + this.rnd.nextFloat() * (max - min);
   }
   public boolean isMoving() {
      return (!(mc.player == null
            || mc.player.input.movementForward == 0.0f
               && mc.player.input.movementSideways == 0.0f));
   }

   @EventTarget
   public void onAttack(Event_358 event) {
      if (mc.player != null && mc.world != null) {
         if (this.reason.is("Ударе")) {
            Entity target = event.getTarget();
            if (target != null) {
               for (int i = 0; i < (35); i++) {
                  double targetX = target.getX()
                     + (double)this.random(-0.4000000059604645f, 0.4000000059604645f);
                  double targetY = target.getY()
                     + (double)this.random(
                        -0.4000000059604645f,
                        target.getHeight() + 0.4000000059604645f
                     );
                  double targetZ = target.getZ()
                     + (double)this.random(
                        -0.4000000059604645f, 0.4000000059604645f
                     );
                  if (!this.isPositionInBlock(new Vec3d(targetX, targetY, targetZ))) {
                     float baseMx = this.random(
                           -0.800000011920929f, 0.800000011920929f
                        )
                        * 2.0f;
                     float baseMy = this.random(-0.25f, 1.399999976158142f);
                     float baseMz = this.random(
                           -0.800000011920929f, 0.800000011920929f
                        )
                        * 2.0f;
                     Vec3d velocity = new Vec3d(
                        (double)(baseMx * 0.07500000298023224f),
                        (double)(baseMy * 0.07500000298023224f),
                        (double)(baseMz * 0.07500000298023224f)
                     );
                     long life = (long)this.random(
                        1000.0f, 1200.0f
                     );
                     this.addParticle(
                        targetX,
                        targetY,
                        targetZ,
                        velocity,
                        ObsidianComponent_olorUtil.getThemeColor(),
                        0.30000001192092896f,
                        life,
                        0.5F,
                        0.000699999975040555
                     );
                  }
               }
            }
         }
      }
   }

   @EventTarget
   public void onPacket(PacketEvent e) {
      if (mc.world != null && mc.player != null) {
         if (this.reason.is("Сносе тотема")) {
            if (e.getPacket() instanceof EntityStatusS2CPacket packet && packet.getStatus() == 35) {
               Entity entity = packet.getEntity(mc.world);
               if (entity != null) {
                  double centerX = entity.getX();
                  double centerY = entity.getY()
                     + (double)entity.getHeight() / 2.0;
                  double centerZ = entity.getZ();

                  for (int i = 0; i < 50; i++) {
                     double theta = this.rnd.nextDouble()
                        * 2.0
                        * 3.141592653589793;
                     double phi = this.rnd.nextDouble() * 3.141592653589793;
                     double speed = (
                           this.rnd.nextDouble() * 0.5
                              + 0.5
                        )
                        * 0.1;
                     double vx = Math.sin(phi) * Math.cos(theta) * speed;
                     double vy = Math.sin(phi) * Math.sin(theta) * speed;
                     double vz = Math.cos(phi) * speed;
                     double spawnX = centerX
                        + (double)this.random(-0.30000001192092896f, 0.30000001192092896f);
                     double spawnY = centerY
                        + (double)this.random(
                           -0.30000001192092896f, 0.30000001192092896f
                        );
                     double spawnZ = centerZ
                        + (double)this.random(
                           -0.30000001192092896f, 0.30000001192092896f
                        );
                     if (!this.isPositionInBlock(new Vec3d(spawnX, spawnY, spawnZ))) {
                        int color = this.rnd.nextDouble() < 0.7
                           ? -16711936
                           : -256;
                        long life = (long)this.random(1500.0f, 2000.0f);
                        this.addParticle(
                           spawnX,
                           spawnY,
                           spawnZ,
                           new Vec3d(vx, vy, vz),
                           color,
                           0.30000001192092896f,
                           life,
                           2.0f,
                           4.999999873689376e-05
                        );
                     }
                  }
               }
            }
         }
      }
   }

   @EventTarget
   public void onUpdate(EventTick e) {
      if (mc.player != null && mc.world != null) {
         int particleCount = (int)this.count.get();
         if (this.reason.is("Бездействии")) {
            Vec3d base = new Vec3d(
               mc.player.getX(),
               mc.player.getY() + (double)mc.player.getHeight() / 2.0,
               mc.player.getZ()
            );

            for (int i = 0; i < particleCount; i++) {
               double distance = (double)this.random(
                  7.0f, 35.0f
               );
               double angle = Math.toRadians((double)this.random(0.0F, 360.0f));
               double height = (double)this.random(-7.0f, 25.0f);
               double spawnX = base.x + Math.cos(angle) * distance;
               double spawnY = base.y + height;
               double spawnZ = base.z + Math.sin(angle) * distance;
               Vec3d spawnPos = new Vec3d(spawnX, spawnY, spawnZ);
               if (!this.isPositionInBlock(spawnPos)) {
                  long life = (long)this.random(
                     1500.0f, 2000.0f
                  );
                  double speed = this.rnd.nextDouble() < 0.8
                     ? (double)this.random(
                        0.014999999664723873f, 0.029999999329447746f
                     )
                     : 0.125;
                  double phi = Math.toRadians(
                     (double)this.random(0.0f, 360.0f)
                  );
                  Vec3d velocity = new Vec3d(
                     Math.cos(phi) * speed,
                     (double)this.random(
                        (float)(-speed * 0.10000000149011612),
                        (float)(speed * 0.10000000149011612)
                     ),
                     Math.sin(phi) * speed
                  );
                  this.addParticle(
                     spawnX,
                     spawnY,
                     spawnZ,
                     velocity,
                     ObsidianComponent_olorUtil.getThemeColor(),
                     0.30000001192092896f,
                     life,
                     3.0f,
                     4.999999873689376e-05
                  );
               }
            }
         }

         if (this.reason.is("Беге") && this.isMoving()) {
            Vec3d motion = mc.player.getVelocity();
            double speed = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
            Vec3d direction;
            if (speed < 0.01) {
               direction = mc.player.getRotationVector().multiply(Double.longBitsToDouble(-4616189618054758400L));
            } else if (mc.player.isGliding()) {
               direction = motion.normalize().multiply(Double.longBitsToDouble(-4616189618054758400L));
            } else {
               direction = new Vec3d(
                  -motion.x / speed, 0.0, -motion.z / speed
               );
            }

            double distanceBehind = (
                  mc.player.isGliding()
                     ? 1.2
                     : 0.5
               )
               + (
                  speed > 0.1
                     ? speed * 1.5
                     : 0.0
               );
            double offsetX = (double)this.random(-0.3499999940395355f, 0.3499999940395355f);
            double offsetZ = (double)this.random(
               -0.3499999940395355f, 0.3499999940395355f
            );
            double posX = mc.player.getX() + direction.x * distanceBehind + offsetX;
            double posY = mc.player.isGliding()
               ? mc.player.getY()
                  + (double)mc.player.getHeight() / 2.0
                  + direction.y * distanceBehind
                  + (double)this.random(-0.3499999940395355f, 0.3499999940395355f)
               : mc.player.getY()
                  + (double)this.random(
                     0.20000000298023224f,
                     mc.player.getHeight() + 0.10000000149011612f
                  );
            double posZ = mc.player.getZ() + direction.z * distanceBehind + offsetZ;
            if (!this.isPositionInBlock(new Vec3d(posX, posY, posZ))) {
               double baseSpeed = 0.075;
               Vec3d velocity = direction.multiply(baseSpeed)
                  .add(
                     (double)this.random(-0.009999999776482582f, 0.009999999776482582f),
                     (double)this.random(-0.05000000074505806f, 0.009999999776482582f),
                     (double)this.random(-0.009999999776482582f, 0.009999999776482582f)
                  )
                  .multiply(0.1);
               long life = (long)this.random(
                  1500.0f, 2000.0f
               );
               this.addParticle(
                  posX,
                  posY,
                  posZ,
                  velocity,
                  ObsidianComponent_olorUtil.getThemeColor(),
                  0.30000001192092896f,
                  life,
                  3.0f,
                  4.999999873689376e-05
               );
            }
         }

         boolean trackPearls = this.reason.is("Падении перла");
         boolean trackTridents = this.reason.is("Падении трезубца");
         if (trackPearls || trackTridents) {
            Box searchBox = mc.player.getBoundingBox().expand(100.0);

            for (Entity entity : mc.world.getOtherEntities(null, searchBox, e2 -> true)) {
               if (trackPearls && entity instanceof EnderPearlEntity pearl && !pearl.isOnGround()) {
                  this.createProjectileParticles(pearl.getPos(), 1);
               }

               if (trackTridents
                  && entity instanceof TridentEntity trident
                  && trident.getVelocity().lengthSquared() > 0.01) {
                  this.createProjectileParticles(trident.getPos(), 1);
               }
            }
         }
      }
   }
   public void createProjectileParticles(Vec3d position, int cnt) {
      int particleColor = ObsidianComponent_olorUtil.getThemeColor();

      for (int i = 0;
         (double)i < (double)cnt * 2.5;
         i++
      ) {
         double dy = (double)this.random(
            0.10000000149011612f, 0.3499999940395355f
         );
         Vec3d particlePos = new Vec3d(position.x, position.y + dy, position.z);
         if (!this.isPositionInBlock(particlePos)) {
            float speedMin = this.random(0.014999999664723873f, 0.03750000149011612f);
            float speedMax = this.random(0.05000000074505806f, 0.07500000298023224f);
            double speedFinal = (double)this.random(speedMin, speedMax);
            double speedFinalY = speedFinal * 0.4;
            double angleVel = Math.toRadians(
               (double)this.random(
                  0.0f, 360.0f
               )
            );
            Vec3d velocity = new Vec3d(
               Math.cos(angleVel) * speedFinal, (double)this.random((float)(-speedFinalY), (float)speedFinalY), Math.sin(angleVel) * speedFinal
            );
            long life = (long)this.random(
               2400.0f, 2800.0f
            );
            this.addParticle(
               particlePos.x,
               particlePos.y,
               particlePos.z,
               velocity,
               particleColor,
               0.25f,
               life,
               2.0f,
               4.999999873689376e-05
            );
         }
      }
   }
   public void addParticle(double x, double y, double z, Vec3d velocity, int color, float size, long lifeTime, float smooth, double gravity) {
      if (ObsidianComponent_188.checkCollision(x, y, z, size, mc)) {
         synchronized (this.particles) {
            this.particles.add(new ObsidianComponent_188(new Vec3d(x, y, z), velocity, color, size, lifeTime, smooth, gravity));
         }
      }
   }

   @EventTarget
   public void onRender3D(RenderEvent e) {
      if (mc.player != null && mc.world != null) {
         synchronized (this.particles) {
            this.particles.removeIf(ObsidianComponent_188::isDead);
         }

         if (!this.particles.isEmpty()) {
            MatrixStack matrices = e.getMatrices();
            Vec3d camera = mc.gameRenderer.getCamera().getPos();
            Identifier texture = this.getTexture();
            RenderSystem.enableBlend();
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.disableCull();
            if (this.glow.isState()) {
               RenderSystem.blendFunc(770, 1);
            } else {
               RenderSystem.defaultBlendFunc();
            }

            RenderSystem.setShaderTexture(0, texture);
            RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
            ArrayList<ObsidianComponent_188> renderList;
            synchronized (this.particles) {
               renderList = new ArrayList<>(this.particles);
            }

            for (ObsidianComponent_188 particle : renderList) {
               particle.update(mc);
               double x = particle.position.x - camera.x;
               double y = particle.position.y - camera.y;
               double z = particle.position.z - camera.z;
               matrices.push();
               matrices.translate((float)x, (float)y, (float)z);
               matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-mc.gameRenderer.getCamera().getYaw()));
               matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(mc.gameRenderer.getCamera().getPitch()));
               Matrix4f matrix = matrices.peek().getPositionMatrix();
               float half = particle.size / 2.0f;
               int alpha = (int)(particle.alpha * 255.0f);
               int r = particle.color >> 16 & (255);
               int g = particle.color >> (8) & (255);
               int b = particle.color & (255);
               BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
               buffer.vertex(matrix, -half, -half, 0.0f)
                  .texture(0.0f, 1.0f)
                  .color(r, g, b, alpha);
               buffer.vertex(matrix, -half, half, 0.0f)
                  .texture(0.0f, 0.0f)
                  .color(r, g, b, alpha);
               buffer.vertex(matrix, half, half, 0.0f)
                  .texture(1.0f, 0.0f)
                  .color(r, g, b, alpha);
               buffer.vertex(matrix, half, -half, 0.0f)
                  .texture(1.0f, 1.0f)
                  .color(r, g, b, alpha);
               BufferRenderer.drawWithGlobalProgram(buffer.end());
               matrices.pop();
            }

            RenderSystem.enableCull();
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
         }
      }
   }
}
