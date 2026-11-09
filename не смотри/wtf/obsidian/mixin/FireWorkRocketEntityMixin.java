package wtf.obsidian.mixin;

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























import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;




@Mixin({FireworkRocketEntity.class})
public abstract class FireWorkRocketEntityMixin extends ProjectileEntity {
   @Unique
   private Vec3d rotation;
   @Shadow
   private LivingEntity shooter;

   public FireWorkRocketEntityMixin(EntityType<? extends ProjectileEntity> entityType, World world) {
      super(entityType, world);
   }

   @Inject(
      method = {"tick"},
      at = {@At("HEAD")}
   )
   public void tick(CallbackInfo ci) {
      new Event_84((FireworkRocketEntity)(Object)this).call();
   }

   @ModifyExpressionValue(
      method = {"tick"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/entity/LivingEntity;getRotationVector()Lnet/minecraft/util/math/Vec3d;"
      )}
   )
   public Vec3d captureRotation(Vec3d original) {
      this.rotation = original;
      return this.rotation;
   }

   @Redirect(
      method = {"tick"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/util/math/Vec3d;add(DDD)Lnet/minecraft/util/math/Vec3d;",
         ordinal = 0
      )
   )
   public Vec3d modifyBoost(Vec3d velocity, double x, double y, double z) {
      MinecraftClient mc = MinecraftClient.getInstance();
      ElytraBoostModule elytraBoost = ElytraBoostModule.INSTANCE;
      if (mc.player == null || !mc.player.isGliding()) {
         return this.defaultBoost(velocity);
      } else {
         return elytraBoost != null && elytraBoost.isEnable() ? this.handleElytraBoost(mc, elytraBoost, velocity) : this.defaultBoost(velocity);
      }
   }

   @Unique
   private Vec3d handleElytraBoost(MinecraftClient mc, ElytraBoostModule elytraBoost, Vec3d velocity) {
      String modeName = elytraBoost.getMode().getCurrent();

      Vec3d boost = switch (modeName) {
         case "LonyGrief" -> ObsidianComponent_396.getBoost(mc.player);
         case "SlimeWorld" -> ObsidianComponent_396.getBoostslime(mc.player);
         case "BravoHVH" -> ObsidianComponent_396.getBoostbravo(mc.player);
         case "ReallyWorld" -> ObsidianComponent_396.getBoostrw(mc.player);
         default -> {
            Vec2f customBoost = elytraBoost.getBoostV2();
            yield new Vec3d((double)customBoost.x, (double)customBoost.y, (double)customBoost.x);
         }
      };
      return velocity.add(
         this.rotation.x * 0.1 + (this.rotation.x * boost.x - velocity.x) * 0.5,
         this.rotation.y * 0.1 + (this.rotation.y * boost.y - velocity.y) * 0.5,
         this.rotation.z * 0.1 + (this.rotation.z * boost.z - velocity.z) * 0.5
      );
   }

   @Unique
   private Vec3d defaultBoost(Vec3d velocity) {
      return velocity.add(
         this.rotation.x * 0.1 + (this.rotation.x * 1.5 - velocity.x) * 0.5,
         this.rotation.y * 0.1 + (this.rotation.y * 1.5 - velocity.y) * 0.5,
         this.rotation.z * 0.1 + (this.rotation.z * 1.5 - velocity.z) * 0.5
      );
   }
}
