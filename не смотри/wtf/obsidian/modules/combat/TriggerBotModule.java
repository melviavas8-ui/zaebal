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

























import lombok.Generated;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.CodEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.AxeItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;

public class TriggerBotModule extends Module {
   public static TriggerBotModule INSTANCE = new TriggerBotModule();
   private final SliderSetting range = new SliderSetting(
      "Дистанция атаки",
      3.0f,
      0.0f,
      6.0f,
      0.05000000074505806f
   );
   public final MultiBooleanSetting options;
   public final MultiBooleanSetting targets;
   public LivingEntity target;
   public final ObsidianComponent_372 attackTimer;
   public boolean needSprintReset;
   public boolean sprintResetDone;
   public int sprintResetTicks;

   public TriggerBotModule() {
      super("TriggerBot", "Автоматически атакует при наведении на цель", Category.COMBAT);
      BooleanSetting[] var10004 = new BooleanSetting[6];
      var10004[0] = new BooleanSetting("Умные криты", true);
      var10004[1] = new BooleanSetting("Сброс спринта", true);
      var10004[2] = new BooleanSetting("Бить через стены", false);
      var10004[3] = new BooleanSetting("Проверка на наведение", true);
      var10004[4] = new BooleanSetting("Отжимать щит", false);
      var10004[5] = new BooleanSetting("Ломать щит", true);
      this.options = new MultiBooleanSetting("Опции", var10004);
      var10004 = new BooleanSetting[4];
      var10004[0] = new BooleanSetting("Игроки", true);
      var10004[1] = new BooleanSetting("Невидимки", true);
      var10004[2] = new BooleanSetting("Мирные", false);
      var10004[3] = new BooleanSetting("Мобы", true);
      this.targets = new MultiBooleanSetting("Таргеты", var10004);
      this.attackTimer = new ObsidianComponent_372();
      this.needSprintReset = false;
      this.sprintResetDone = false;
      this.sprintResetTicks = 0;
      Setting[] var10001 = new Setting[3];
      var10001[0] = this.range;
      var10001[1] = this.options;
      var10001[2] = this.targets;
      this.addSettings(var10001);
   }

   @EventTarget
   public void onMoveInput(Event_102 event) {
      if (this.needSprintReset) {
         event.setForward(0.0f);
         event.setStrafe(0.0f);
         this.needSprintReset = false;
         this.sprintResetDone = true;
         this.sprintResetTicks = 0;
      }
   }

   @EventTarget
   public void onUpdate(EventTick e) {
      if (mc.player != null && mc.world != null) {
         if (AutoEatModule.shouldSuppressCombat()) {
            this.target = null;
            this.resetSprintState();
         } else {
            if (this.sprintResetDone) {
               this.sprintResetTicks += 1;
            }

            this.target = this.getTargetUnderCrosshair();
            if (this.target != null) {
               this.processAttack();
            } else {
               this.resetSprintState();
            }
         }
      }
   }
   public void processAttack() {
      if (this.shouldAttack()) {
         if (this.options.is("Сброс спринта") && mc.player.isSprinting() && !this.sprintResetDone && !this.shouldSkipSprintResetInWater()) {
            this.needSprintReset = true;
         } else if (!this.options.is("Сброс спринта") || !this.sprintResetDone || this.sprintResetTicks >= 1) {
            this.attack();
            this.sprintResetDone = false;
            this.sprintResetTicks = 0;
         }
      }
   }
   public LivingEntity getTargetUnderCrosshair() {
      Vec3d eyePos = mc.player.getCameraPosVec(1.0f);
      Vec3d lookVec = mc.player.getRotationVec(1.0f);
      float rangeValue = this.range.getValue().floatValue();
      Vec3d reachVec = eyePos.add(lookVec.multiply((double)rangeValue));
      EntityHitResult result = ProjectileUtil.raycast(
         mc.player,
         eyePos,
         reachVec,
         mc.player.getBoundingBox().expand((double)rangeValue),
         entity -> (entity != mc.player && entity.isAlive() && entity instanceof LivingEntity),
         (double)(rangeValue * rangeValue)
      );
      return result != null && result.getEntity() instanceof LivingEntity living && this.isValidTarget(living) ? living : null;
   }
   public void attack() {
      if (this.options.is("Отжимать щит") && mc.player.isBlocking()) {
         mc.interactionManager.stopUsingItem(mc.player);
      }

      label19: {
         if (this.target instanceof PlayerEntity player && player.isBlocking() && this.options.is("Ломать щит")) {
            this.shieldBreak(player);
            break label19;
         }

         mc.interactionManager.attackEntity(mc.player, this.target);
      }

      mc.player.swingHand(Hand.MAIN_HAND);
      this.attackTimer.reset();
   }
   public void shieldBreak(PlayerEntity entity) {
      int axeSlot = this.findAxeSlot();
      if (axeSlot != (-1)) {
         int prevSlot = mc.player.getInventory().selectedSlot;
         mc.player.getInventory().selectedSlot = axeSlot;
         mc.interactionManager.attackEntity(mc.player, entity);
         mc.player.swingHand(Hand.MAIN_HAND);
         mc.player.getInventory().selectedSlot = prevSlot;
      } else {
         mc.interactionManager.attackEntity(mc.player, entity);
      }
   }
   public int findAxeSlot() {
      for (int i = 0; i < 9; i++) {
         if (mc.player.getInventory().getStack(i).getItem() instanceof AxeItem) {
            return i;
         }
      }

      return -1;
   }
   public boolean isValidTarget(LivingEntity entity) {
      if (entity != null && entity != mc.player) {
         if (!entity.isAlive() || entity.getHealth() <= 0.0f) {
            return false;
         } else if (entity instanceof ArmorStandEntity) {
            return false;
         } else {
            if (entity instanceof PlayerEntity player) {
               if (!this.targets.is("Игроки")) {
                  return false;
               }

               if (player.hasStatusEffect(StatusEffects.INVISIBILITY) && !this.targets.is("Невидимки")) {
                  return false;
               }

               if (Obsidian.INSTANCE.friendStorage.isFriend(entity.getName().getString())) {
                  return false;
               }
            } else if (!(entity instanceof PassiveEntity) && !(entity instanceof CodEntity)) {
               if (entity instanceof HostileEntity && !this.targets.is("Мобы")) {
                  return false;
               }
            } else if (!this.targets.is("Мирные")) {
               return false;
            }

            if (mc.player.getEyePos().distanceTo(entity.getBoundingBox().getCenter()) > (double)this.range.getValue().floatValue()) {
               return false;
            } else {
               return (!(!this.options.is("Бить через стены") && !mc.player.canSee(entity)));
            }
         }
      } else {
         return false;
      }
   }
   public boolean shouldAttack() {
      if (mc.player.getAttackCooldownProgress(1.5f) < ObsidianComponent_243.getAICooldown()) {
         return false;
      } else {
         if (this.options.is("Проверка на наведение")) {
            Vec3d eyePos = mc.player.getCameraPosVec(1.0f);
            Vec3d lookVec = mc.player.getRotationVec(1.0f);
            float rangeValue = this.range.getValue().floatValue();
            Vec3d reachVec = eyePos.add(lookVec.multiply((double)rangeValue));
            EntityHitResult result = ProjectileUtil.raycast(
               mc.player,
               eyePos,
               reachVec,
               mc.player.getBoundingBox().expand((double)rangeValue),
               ex -> (ex != mc.player && ex.isAlive()),
               (double)(rangeValue * rangeValue)
            );
            if (result == null || result.getEntity() != this.target) {
               return false;
            }
         }

         return (!(this.options.is("Умные криты") && !ObsidianComponent_243.canCritical(this.target)));
      }
   }
   public void resetSprintState() {
      this.sprintResetDone = false;
      this.sprintResetTicks = 0;
   }
   public boolean shouldSkipSprintResetInWater() {
      return (mc.player != null
            && (mc.player.isTouchingWater() || mc.player.isSubmergedInWater())
            && SprintModule.INSTANCE != null
            && SprintModule.INSTANCE.shouldKeepSprintInWater());
   }

   @Override
   public void onDisable() {
      super.onDisable();
      this.target = null;
      this.needSprintReset = false;
      this.sprintResetDone = false;
      this.sprintResetTicks = 0;
   }

   @Override
   public void onEnable() {
      super.onEnable();
      this.needSprintReset = false;
      this.sprintResetDone = false;
      this.sprintResetTicks = 0;
   }

   @Generated
   public LivingEntity getTarget() {
      return this.target;
   }
}
