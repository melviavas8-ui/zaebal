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

























import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.Generated;
import net.minecraft.block.Blocks;
import net.minecraft.client.option.Perspective;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.BatEntity;
import net.minecraft.entity.passive.CodEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.AxeItem;
import net.minecraft.item.HoeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MaceItem;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ShieldItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;


public class KillAuraModule extends Module {
   public static KillAuraModule INSTANCE = new KillAuraModule();
   public final ModeSetting rotationType;
   public final ModeSetting spookyProfile;
   public final MultiBooleanSetting targets;
   public final SliderSetting range;
   public final SliderSetting aimRange;
   public final SliderSetting elytraAimRange;
   public final BooleanSetting smartCrit;
   public final BooleanSetting sprintReset;
   public final ModeSetting wallBypassMode;
   public final BooleanSetting raycast;
   public final BooleanSetting unpressShield;
   public final BooleanSetting breakShield;
   public final BooleanSetting attackOnEating;
   public final ModeSetting moveFix;
   public final ModeSetting priority;
   public LivingEntity target;
   public Vec2f currentRotations;
   public Vec2f targetRotations;
   public final ObsidianComponent_227 dataSystem;
   public final ObsidianComponent_372 attackTimer;
   public final ObsidianComponent_244 funTimeRotation;
   public final ObsidianComponent_419 funTimeRotationTest;
   public final ObsidianComponent_76 testRotation;
   public final ObsidianComponent_145 slothRotation;
   public final ObsidianComponent_338 spookyTimeRotation;
   public final BindSetting spookyTime4DigitsRotation;
   public final ObsidianComponent_110 spookyTimeDuelsRotation;
   public final ObsidianComponent_318 holyWorldRotation;
   public final ObsidianComponent_178 legitRotation;
   public final ObsidianComponent_372 backTimer;
   public TpsSyncModule tpsSync;
   public long cps;
   public boolean needSprintReset;
   public boolean sprintResetDone;
   public int sprintResetTicks;
   public int ticksToAttack;
   public LivingEntity lastDataTarget;
   public float lastYaw;
   public float lastPitch;
   public static float adjYaw;
   public static float adjPitch;
   public static float otvodkaYaw;
   public static float otvodkaPitch;
   public boolean isRotated;
   public boolean maceForceAttack;

   public KillAuraModule() {
      super("AttackAura", "Автоматически наводиться и бьёт таргета", Category.COMBAT);
      String[] var10005 = new String[4];
      var10005[0] = "Smooth";
      var10005[1] = "Легит";
      var10005[2] = "ФанТайм";
      var10005[3] = "Sloth";
      this.rotationType = new ModeSetting("Ротация", "Smooth", var10005);
      var10005 = new String[3];
      var10005[0] = "Спуки-дуэли";
      var10005[1] = "Спуки 1.16";
      var10005[2] = "Спуки 1.21";
      this.spookyProfile = new ModeSetting("Режим СпукиТайм", "Спуки 1.16", var10005).visible(() -> this.rotationType.is("СпукиТайм"));
      BooleanSetting[] var10004 = new BooleanSetting[5];
      var10004[0] = new BooleanSetting("Игроки", true);
      var10004[1] = new BooleanSetting("Голые", true);
      var10004[2] = new BooleanSetting("Невидимки", true);
      var10004[3] = new BooleanSetting("Мирные", false);
      var10004[4] = new BooleanSetting("Мобы", true);
      this.targets = new MultiBooleanSetting("Таргеты", var10004);
      this.range = new SliderSetting(
         "Дистанция атаки",
         3.0f,
         0.0f,
         6.0f,
         0.05000000074505806f
      );
      this.aimRange = new SliderSetting(
         "Дистанция наводки",
         3.0f,
         0.0f,
         6.0f,
         0.05000000074505806f
      );
      this.elytraAimRange = new SliderSetting(
         "Дистанция на элитрах",
         50.0f,
         10.0f,
         100.0f,
         0.05000000074505806f
      );
      this.smartCrit = new BooleanSetting("Умные криты", false);
      this.sprintReset = new BooleanSetting("Сброс спринта", true);
      var10005 = new String[3];
      var10005[0] = "Обычный";
      var10005[1] = "СпукиТайм";
      var10005[2] = "Выкл";
      this.wallBypassMode = new ModeSetting("Бить через стены", "Обычный", var10005);
      this.raycast = new BooleanSetting("Проверка на наведение", false);
      this.unpressShield = new BooleanSetting("Отжимать щит", false);
      this.breakShield = new BooleanSetting("Ломать щит", true);
      this.attackOnEating = new BooleanSetting("Не бить когда ешь", true);
      var10005 = new String[4];
      var10005[0] = "Нет";
      var10005[1] = "Свободная";
      var10005[2] = "Сфокусированная";
      var10005[3] = "Полная";
      this.moveFix = new ModeSetting("Коррекция", "Нет", var10005);
      var10005 = new String[4];
      var10005[0] = "Дистанция";
      var10005[1] = "Здоровье";
      var10005[2] = "Угол";
      var10005[3] = "Никакой";
      this.priority = new ModeSetting("Приоритет", "Дистанция", var10005);
      this.currentRotations = new Vec2f(
         0.0f, 0.0f
      );
      this.targetRotations = new Vec2f(
         0.0f, 0.0f
      );
      this.dataSystem = new ObsidianComponent_227();
      this.attackTimer = new ObsidianComponent_372();
      this.funTimeRotation = new ObsidianComponent_244(this);
      this.funTimeRotationTest = new ObsidianComponent_419(this);
      this.testRotation = new ObsidianComponent_76();
      this.slothRotation = new ObsidianComponent_145();
      this.spookyTimeRotation = new ObsidianComponent_338(this);
      this.spookyTime4DigitsRotation = new BindSetting(this);
      this.spookyTimeDuelsRotation = new ObsidianComponent_110(this);
      this.holyWorldRotation = new ObsidianComponent_318();
      this.legitRotation = new ObsidianComponent_178();
      this.backTimer = new ObsidianComponent_372();
      this.cps = 0L;
      this.needSprintReset = false;
      this.sprintResetDone = false;
      this.sprintResetTicks = 0;
      this.ticksToAttack = 0;
      this.lastDataTarget = null;
      this.lastYaw = 0.0f;
      this.lastPitch = 0.0f;
      this.maceForceAttack = false;
      Setting[] var10001 = new Setting[15];
      var10001[0] = this.rotationType;
      var10001[1] = this.spookyProfile;
      var10001[2] = this.targets;
      var10001[3] = this.range;
      var10001[4] = this.aimRange;
      var10001[5] = this.elytraAimRange;
      var10001[6] = this.smartCrit;
      var10001[7] = this.sprintReset;
      var10001[8] = this.attackOnEating;
      var10001[9] = this.wallBypassMode;
      var10001[10] = this.raycast;
      var10001[11] = this.unpressShield;
      var10001[12] = this.breakShield;
      var10001[13] = this.moveFix;
      var10001[14] = this.priority;
      this.addSettings(var10001);
   }

   @EventTarget
   public void onPlayerTick(EventTick e) {
      if (mc.player != null && mc.world != null) {
         if (this.tpsSync == null && Obsidian.INSTANCE != null && Obsidian.INSTANCE.moduleStorage != null) {
            this.tpsSync = ModuleManager.tpsSync;
         }

         this.lastYaw = this.lastYaw + 1.0f;
         this.updateTarget();
         if (this.dataSystem.isRecording()) {
            LivingEntity recordTarget = this.findTargetForRecording();
            this.dataSystem.recordTick(recordTarget, mc.player.getYaw(), mc.player.getPitch());
         }
      }
   }

   @EventTarget
   public void onAttackEntity(Event_358 event) {
      if (mc.player != null && mc.world != null) {
         if (event.getPlayer() == mc.player) {
            if (event.getTarget() instanceof LivingEntity living) {
               if (this.isValidTarget(living)) {
                  this.target = living;
               }
            }
         }
      }
   }

   @EventTarget
   public void onMoveInput(Event_102 event) {
      if (this.needSprintReset) {
         event.setForward(0.0f);
         event.setStrafe(0.0f);
         this.needSprintReset = false;
         this.sprintResetDone = true;
         this.sprintResetTicks = 0;
      } else {
         this.applyMoveFix(event);
      }
   }
   public void applyMoveFix(Event_102 event) {
      boolean isReturning = (RotationManager.instance != null && RotationManager.instance.currentTask() == ObsidianComponent_398.RESET);
      if (mc.player != null && (this.target != null || isReturning) && !this.rotationType.is("NoRotate") && this.moveFix.getIndex() != 0) {
         if (this.moveFix.getIndex() == 1) {
            ObsidianComponent_180.fixMovementFree(event);
         }
      }
   }

   @EventTarget
   public void onKeyboardInput(MotionEvent event) {
      boolean isReturning = (RotationManager.instance != null && RotationManager.instance.currentTask() == ObsidianComponent_398.RESET);
      if (mc.player != null && mc.world != null && (this.target != null || isReturning) && !this.rotationType.is("NoRotate")) {
         float correctionYaw = this.getCorrectionYaw();
         if (this.moveFix.getIndex() == 2) {
            event.setYaw(correctionYaw, mc.player.getYaw());
         } else if (this.moveFix.getIndex() == 3) {
            event.setYaw(correctionYaw, isReturning ? correctionYaw : this.getTargetDirectionYaw());
         }
      }
   }
   public float getCorrectionYaw() {
      return RotationManager.instance != null && RotationManager.instance.targetRotation() != null ? RotationManager.instance.targetRotation().getYaw() : mc.player.getYaw();
   }
   public float getTargetDirectionYaw() {
      return ObsidianComponent_374.getRotations(this.target.getBoundingBox().getCenter()).x;
   }
   public void applyMovementCorrection(Event_102 event, float yaw, float directionYaw) {
      float forward = event.getForward();
      float strafe = event.getStrafe();
      if (forward != 0.0f
         || strafe != 0.0f) {
         double angle = MathHelper.wrapDegrees(Math.toDegrees(ObsidianComponent_180.direction(directionYaw, forward, strafe)));
         float closestForward = 0.0f;
         float closestStrafe = 0.0f;
         float closestDifference = 340282346638528859811704183484516925440.0f;

         for (float predictedForward = -1.0f;
            predictedForward <= 1.0f;
            predictedForward += 1.0f
         ) {
            for (float predictedStrafe = -1.0f;
               predictedStrafe <= 1.0f;
               predictedStrafe += 1.0f
            ) {
               if (predictedForward != 0.0f
                  || predictedStrafe != 0.0f) {
                  double predictedAngle = MathHelper.wrapDegrees(Math.toDegrees(ObsidianComponent_180.direction(yaw, predictedForward, predictedStrafe)));
                  double difference = Math.abs(angle - predictedAngle);
                  if (difference < (double)closestDifference) {
                     closestDifference = (float)difference;
                     closestForward = predictedForward;
                     closestStrafe = predictedStrafe;
                  }
               }
            }
         }

         event.setForward(closestForward);
         event.setStrafe(closestStrafe);
      }
   }

   @EventTarget
   public void onGameUpdate(Event_93 e) {
      if (mc.player != null && mc.world != null && this.target != null) {
         this.rotate();
      }
   }

   @EventTarget
   public void onTick(EventTick e) {
      if (mc.player != null && mc.world != null) {
         if (this.rotationType.is("СпукиТайм") || this.rotationType.is("SpookyTime")) {
            this.spookyTimeRotation.checkAndAutoSwitchProfile();
         }

         if (this.ticksToAttack > 0) {
            this.ticksToAttack -= 1;
         }

         if (this.sprintResetDone) {
            this.sprintResetTicks += 1;
         }

         boolean packetCrits = (ModuleManager.packetCriticals.isEnable() && mc.player.hasStatusEffect(StatusEffects.SLOW_FALLING));
         if (!packetCrits) {
            this.processAttack();
         }

         if (this.dataSystem.isShowStats()
            && mc.player.age % (40) == 0
            && (this.dataSystem.isRecording() || this.dataSystem.isUsingNeuro())) {
            mc.player.sendMessage(Text.literal(this.dataSystem.getStatusString()), true);
         }
      }
   }

   @EventTarget
   public void onPost(EventPostTick e) {
      if (mc.player != null && mc.world != null) {
         boolean packetCrits = (ModuleManager.packetCriticals.isEnable() && mc.player.hasStatusEffect(StatusEffects.SLOW_FALLING));
         if (packetCrits
            && mc.player.fallDistance > 0.0f
            && mc.player.fallDistance < 1.0f) {
            this.processAttack();
         }
      }
   }

   @EventTarget
   public void onRender(EventRender2D event) {
      if (this.isEnable()) {
         if (mc.player != null && mc.world != null) {
            if (!mc.options.hudHidden) {
               if (mc.options.getPerspective() == Perspective.FIRST_PERSON) {
                  if (this.rotationType.is("ХолиВорлд ФОВ") || this.rotationType.is("ХолиВорлд")) {
                     this.drawFovCircle(event, this.holyWorldRotation.getCurrentFov());
                  }
               }
            }
         }
      }
   }
   public void drawFovCircle(EventRender2D event, float fov) {
      float scaledHeight = (float)mc.getWindow().getScaledHeight();
      float radius = (float)(
         (double)scaledHeight
            * 0.5
            * Math.tan(Math.toRadians((double)fov))
            / Math.tan(Math.toRadians(35.0))
      );
      float maxRadius = scaledHeight * 0.5f;
      radius = Math.min(radius, maxRadius * 1.5f);
      if (!(radius <= 1.0f)) {
         float centerX = (float)mc.getWindow().getScaledWidth() / 2.0f;
         float centerY = scaledHeight / 2.0f;
         RenderUtil.drawRingArc(
            event.getContext().getMatrices(),
            centerX - radius,
            centerY - radius,
            radius * 2.0f,
            1.5f,
            0.0f,
            360.0f,
            ObsidianComponent_olorUtil.getThemeColor()
         );
      }
   }
   public LivingEntity findTargetForRecording() {
      LivingEntity bestTarget = null;
      double bestDistance = 100.0;
      Vec3d eyePos = mc.player.getEyePos();

      for (Entity entity : mc.world.getEntities()) {
         if (entity instanceof LivingEntity) {
            LivingEntity living = (LivingEntity)entity;
            if (living != mc.player
               && living.isAlive()
               && !(living.getHealth() <= 0.0f)
               && !(living instanceof ArmorStandEntity)) {
               double distance = eyePos.squaredDistanceTo(living.getBoundingBox().getCenter());
               if (!(distance > bestDistance)) {
                  bestDistance = distance;
                  bestTarget = living;
               }
            }
         }
      }

      return bestTarget;
   }
   public void processAttack() {
      this.updateTarget();
      boolean maceMode = this.maceForceAttack;
      this.maceForceAttack = false;
      if (this.target != null) {
         if (this.shouldAttack(maceMode) && (this.cps <= System.currentTimeMillis() || maceMode)) {
            if (this.attackOnEating.isState() && this.shouldBlockAttackWhileUsingItem() && !maceMode) {
               return;
            }

            if (this.sprintReset.isState() && mc.player.isSprinting() && !this.sprintResetDone && !this.shouldSkipSprintResetInWater()) {
               if (!mc.player.isSwimming()) {
                  mc.player.input.movementForward = 0.0f;
                  mc.player.input.movementSideways = 0.0f;
                  mc.player.setSprinting(false);
               }

               this.needSprintReset = true;
               return;
            }

            if (this.sprintReset.isState() && this.sprintResetDone && this.sprintResetTicks < 1) {
               return;
            }

            this.attack();
            this.sprintResetDone = false;
            this.sprintResetTicks = 0;
         }
      } else {
         this.cps = System.currentTimeMillis();
         this.backTimer.reset();
         adjPitch = 0.0f;
         adjYaw = 0.0f;
         this.funTimeRotation.reset();
         this.testRotation.reset();
         this.slothRotation.reset();
         this.spookyTimeRotation.reset();
         this.spookyTime4DigitsRotation.reset();
         this.spookyTimeDuelsRotation.reset();
         this.holyWorldRotation.reset();
         this.legitRotation.reset();
         this.dataSystem.resetState();
         this.lastDataTarget = null;
         this.sprintResetDone = false;
         this.sprintResetTicks = 0;
         this.ticksToAttack = 0;
         if (RotationManager.instance != null && RotationManager.instance.currentTask() == ObsidianComponent_398.AIM) {
            RotationManager.instance.currentTask(ObsidianComponent_398.RESET);
         }
      }
   }

   public void Rotate() {
      this.rotate();
   }
   public void rotate() {
      if (this.target != null) {
         if (this.rotationType.is("Data") && this.target != this.lastDataTarget) {
            this.dataSystem.resetState();
            this.lastDataTarget = this.target;
         }

         CombatPredictionHelper system;
         if (this.rotationType.is("Smooth")) {
            system = new CombatPredictionHelper() {
               @Override
               public void updateRotations(LivingEntity target) {
                  if (!mc.player.isGliding()) {
                     Vec3d relativePos = target.getPos()
                        .add(
                           0.0,
                           (double)(target.getHeight() * 0.6000000238418579f),
                           0.0
                        )
                        .subtract(mc.player.getEyePos());
                     float yaw = (float)MathHelper.wrapDegrees(
                        Math.toDegrees(Math.atan2(relativePos.z, relativePos.x))
                           - 90.0
                     );
                     float pitch = (float)(-Math.toDegrees(Math.atan2(relativePos.y, Math.hypot(relativePos.x, relativePos.z))));
                     RotationManager.update(
                        new ObsidianComponent_olorSetting(yaw, pitch),
                        180.0f,
                        180.0f,
                        25.0f,
                        20.0f,
                        1,
                        1,
                        false
                     );
                  } else {
                     Vec3d interpolatedRotation = Vec3d.fromPolar(target.getLerpTargetPitch(), target.getLerpTargetYaw());
                     Vec3d rotationVector = target.getRotationVector();
                     Vec3d relativePos = target.getPos()
                        .add(
                           0.0,
                           (double)(target.getHeight() * 0.6000000238418579f),
                           0.0
                        )
                        .subtract(mc.player.getEyePos());
                     Vec3d blendedDirection = interpolatedRotation.normalize().lerp(rotationVector, interpolatedRotation.length());
                     float yaw = (float)MathHelper.wrapDegrees(
                        Math.toDegrees(Math.atan2(relativePos.z, relativePos.x))
                           - 90.0
                     );
                     float pitch = (float)(-Math.toDegrees(Math.atan2(relativePos.y, Math.hypot(relativePos.x, relativePos.z))));
                     RotationManager.update(
                        new ObsidianComponent_olorSetting(yaw, pitch),
                        180.0f,
                        180.0f,
                        25.0f,
                        20.0f,
                        1,
                        1,
                        false
                     );
                  }
               }
            };
         } else if (this.rotationType.is("Легит") || this.rotationType.is("Legit")) {
            system = this.legitRotation;
         } else if (this.rotationType.is("Sloth")) {
            system = this.slothRotation;
         } else if (this.rotationType.is("ФанТайм")) {
            system = this.funTimeRotation;
         } else if (this.rotationType.is("ФанТайм (Тест)")) {
            system = this.funTimeRotationTest;
         } else if (this.rotationType.is("ХолиВорлд ФОВ")) {
            system = this.holyWorldRotation;
         } else if (this.rotationType.is("Test")) {
            system = this.testRotation;
         } else if (this.rotationType.is("СпукиТайм")) {
            if (this.spookyProfile.is("Спуки-дуэли")) {
               system = this.spookyTimeDuelsRotation;
            } else if (this.spookyProfile.is("Спуки 1.16")) {
               system = this.spookyTime4DigitsRotation;
            } else {
               system = this.spookyTimeRotation;
            }
         } else if (this.rotationType.is("NoRotate")) {
            system = new CombatPredictionHelper() {
               @Override
               public void updateRotations(LivingEntity target) {
                  RotationManager.update(
                     new ObsidianComponent_olorSetting(ObsidianComponent_142.getFreeYaw(), ObsidianComponent_142.getFreePitch()),
                     MathLerpUtil.random(
                        100.0f, 170.0f
                     ),
                     MathLerpUtil.random(100.0f, 170.0f),
                     MathLerpUtil.random(100.0f, 170.0f),
                     MathLerpUtil.random(100.0f, 170.0f),
                     1,
                     6,
                     false
                  );
               }
            };
         } else if (this.rotationType.is("Data")) {
            system = new CombatPredictionHelper() {
               @Override
               public void updateRotations(LivingEntity target) {
                  boolean focusRotation = KillAuraModule.this.shouldFocusDataRotation();
                  Vec3d fallbackPoint = KillAuraModule.this.getDataRotationPoint(target);
                  Vec2f fallbackRot = ObsidianComponent_374.getRotations(fallbackPoint);
                  float currentYaw = mc.player.getYaw();
                  float currentPitch = mc.player.getPitch();
                  float yawDelta = Math.abs(MathHelper.wrapDegrees(fallbackRot.x - currentYaw));
                  float pitchDelta = Math.abs(fallbackRot.y - currentPitch);
                  boolean hardAcquire = (!(!(yawDelta > 70.0f)
                        && (
                           !(yawDelta > 42.0f)
                              || !(
                                 mc.player.squaredDistanceTo(target)
                                    < 9.0
                              )
                        )));
                  ObsidianComponent_olorSetting rotation = null;
                  if (this.shouldUseElytraPredict(target)) {
                     Vec2f rot = ObsidianComponent_374.getRotations(this.getPredictedPoint(target, fallbackPoint));
                     rotation = new ObsidianComponent_olorSetting(rot.x, rot.y);
                  } else if (!hardAcquire) {
                     rotation = KillAuraModule.this.dataSystem.getNeuroRotation(target, currentYaw, currentPitch, focusRotation);
                  }

                  if (rotation == null || hardAcquire) {
                     if (hardAcquire) {
                        KillAuraModule.this.dataSystem.resetState();
                     }

                     rotation = new ObsidianComponent_olorSetting(fallbackRot.x, fallbackRot.y);
                  }

                  KillAuraModule.this.targetRotations = new Vec2f(rotation.getYaw(), rotation.getPitch());
                  KillAuraModule.this.currentRotations = new Vec2f(currentYaw, currentPitch);
                  float yawSpeed = hardAcquire
                     ? Math.max(
                        95.0f,
                        Math.min(180.0f, yawDelta * 1.4500000476837158f)
                     )
                     : (
                        focusRotation
                           ? 24.0f
                           : 11.5f
                     );
                  float pitchSpeed = hardAcquire
                     ? Math.max(
                        55.0f,
                        Math.min(
                           110.0f,
                           Math.max(
                              18.0f,
                              pitchDelta * 1.350000023841858f
                           )
                        )
                     )
                     : (focusRotation ? 18.0f : 9.0f);
                  float yawReturnSpeed = hardAcquire
                     ? yawSpeed
                     : (
                        focusRotation
                           ? 18.0f
                           : 9.0f
                     );
                  float pitchReturnSpeed = hardAcquire
                     ? pitchSpeed
                     : (focusRotation ? 14.0f : 7.0f);
                  RotationManager.update(
                     rotation,
                     yawSpeed,
                     pitchSpeed,
                     yawReturnSpeed,
                     pitchReturnSpeed,
                     1,
                     1,
                     false
                  );
               }
            };
         } else {
            final Vec2f targetRot = ObsidianComponent_374.getRotations(
               this.getPredictedRotationPoint(this.target, this.target.getLeashPos(1.0f))
            );
            system = new CombatPredictionHelper() {
               @Override
               public void updateRotations(LivingEntity target) {
                  KillAuraModule.this.currentRotations = new Vec2f(mc.player.getYaw(), mc.player.getPitch());
                  RotationManager.update(
                     new ObsidianComponent_olorSetting(targetRot.x, targetRot.y),
                     360.0f,
                     360.0f,
                     360.0f,
                     360.0f,
                     1,
                     1,
                     false
                  );
               }
            };
         }

         system.updateRotations(this.target);
      }
   }
   public boolean shouldSkipSprintResetInWater() {
      return (mc.player != null
            && (mc.player.isTouchingWater() || mc.player.isSubmergedInWater())
            && SprintModule.INSTANCE != null
            && SprintModule.INSTANCE.shouldKeepSprintInWater());
   }
   public EntityHitResult getAttackRaycastResult() {
      Vec3d eyePos = mc.player.getCameraPosVec(1.0f);
      Vec3d lookVec = mc.player.getRotationVec(1.0f);
      float reach = this.range.getValue().floatValue() * 2.0f;
      Vec3d reachVec = eyePos.add(lookVec.multiply((double)reach));
      return ProjectileUtil.raycast(
         mc.player,
         eyePos,
         reachVec,
         mc.player.getBoundingBox().expand((double)reach),
         ex -> (ex != mc.player && ex.isAlive()),
         (double)(reach * reach)
      );
   }
   public boolean isTargetBehindWall(LivingEntity entity) {
      if (entity != null && mc.player != null && mc.world != null) {
         return (!(mc.player.canSee(entity) && !this.hasNarrowRwWallGap(entity)));
      } else {
         return false;
      }
   }
   public boolean hasNarrowRwWallGap(LivingEntity entity) {
      if (entity != null && mc.player != null && mc.world != null) {
         Vec3d eyePos = mc.player.getEyePos();
         Box box = entity.getBoundingBox();
         double centerX = box.getCenter().x;
         double centerZ = box.getCenter().z;
         Vec3d[] var10000 = new Vec3d[8];
         var10000[0] = box.getCenter();
         var10000[1] = this.getStableBodyPoint(entity);
         var10000[2] = new Vec3d(
            centerX, box.maxY - 0.08, centerZ
         );
         var10000[3] = new Vec3d(
            centerX, box.minY + 0.12, centerZ
         );
         var10000[4] = new Vec3d(
            box.minX + 0.04,
            box.minY + box.getLengthY() * 0.55,
            centerZ
         );
         var10000[5] = new Vec3d(
            box.maxX - 0.04,
            box.minY + box.getLengthY() * 0.55,
            centerZ
         );
         var10000[6] = new Vec3d(
            centerX,
            box.minY + box.getLengthY() * 0.55,
            box.minZ + 0.04
         );
         var10000[7] = new Vec3d(
            centerX,
            box.minY + box.getLengthY() * 0.55,
            box.maxZ - 0.04
         );
         Vec3d[] points = var10000;
         int blocked = 0;
         int clear = 0;
         Vec3d[] var11 = points;
         int var12 = points.length;

         for (int var13 = 0; var13 < var12; var13++) {
            Vec3d point = var11[var13];
            BlockHitResult hit = mc.world.raycast(new RaycastContext(eyePos, point, ShapeType.COLLIDER, FluidHandling.NONE, mc.player));
            if (hit != null && hit.getType() == Type.BLOCK) {
               blocked++;
            } else {
               clear++;
            }
         }

         return (clear > 0 && blocked >= clear);
      } else {
         return false;
      }
   }
   public Vec3d getPredictedRotationPoint(LivingEntity target, Vec3d point) {
      return point;
   }
   public Vec3d getDataRotationPoint(LivingEntity target) {
      if (this.isInsideOrNearHitbox(target)) {
         return this.getPredictedRotationPoint(target, this.getStableBodyPoint(target));
      } else {
         Vec3d point = ObsidianComponent_311.getNearestPoint(target);
         if (point == null) {
            point = ObsidianComponent_319.getClosestPoint(target);
         }

         if (point == null) {
            point = this.getStableBodyPoint(target);
         }

         return this.getPredictedRotationPoint(target, point);
      }
   }
   public boolean isInsideOrNearHitbox(LivingEntity target) {
      if (mc.player != null && target != null) {
         Vec3d eyePos = mc.player.getEyePos();
         Box box = target.getBoundingBox();
         if (box.expand(0.12).contains(eyePos)) {
            return true;
         } else {
            Vec3d stablePoint = this.getStableBodyPoint(target);
            return (eyePos.squaredDistanceTo(stablePoint)
                  <= 2.25);
         }
      } else {
         return false;
      }
   }
   public Vec3d getStableBodyPoint(LivingEntity target) {
      Box box = target.getBoundingBox();
      return new Vec3d(
         box.getCenter().x,
         box.minY + box.getLengthY() * 0.72,
         box.getCenter().z
      );
   }
   public LivingEntity findTarget() {
      List<LivingEntity> entities = new ArrayList<>();

      for (Entity entity : mc.world.getEntities()) {
         if (entity instanceof LivingEntity) {
            LivingEntity living = (LivingEntity)entity;
            if (this.isValidTarget(living)) {
               entities.add(living);
            }
         }
      }

      if (!entities.isEmpty() && this.isEnable()) {
         String var5 = this.priority.getCurrent();
         int var6 = -1;
         switch (var5.hashCode()) {
            case -1397728228:
               if (var5.equals("Здоровье")) {
                  var6 = 1;
               }
               break;
            case 32616493:
               if (var5.equals("Угол")) {
                  var6 = 2;
               }
               break;
            case 125477796:
               if (var5.equals("Никакой")) {
                  var6 = 3;
               }
               break;
            case 1835963915:
               if (var5.equals("Дистанция")) {
                  var6 = 0;
               }
         }

         switch (var6) {
            case 0:
               entities.sort(Comparator.comparingDouble(entityx -> entityx.getBoundingBox().getCenter().squaredDistanceTo(mc.player.getEyePos())));
               break;
            case 1:
               entities.sort(Comparator.comparingDouble(LivingEntity::getHealth));
               break;
            case 2:
               entities.sort(Comparator.comparingDouble(entityx -> {
                  Vec2f vec = ObsidianComponent_374.getRotations(entityx.getBoundingBox().getCenter());
                  double dy = (double)Math.abs(MathHelper.wrapDegrees(vec.x - mc.player.getYaw()));
                  double dp = (double)Math.abs(MathHelper.wrapDegrees(vec.y - mc.player.getPitch()));
                  return dy + dp;
               }));
            break;
         }

         return entities.isEmpty() ? null : entities.get(0);
      } else {
         return null;
      }
   }
   public void updateTarget() {
      if (!this.isEnable()) {
         this.target = null;
      } else {
         if (this.target != null) {
            if (this.isValidTarget(this.target)) {
               return;
            }

            this.target = null;
         }

         this.target = this.findTarget();
      }
   }
   public boolean shouldFocusDataRotation() {
      float cooldown = mc.player.getAttackCooldownProgress(1.5f);
      float focusThreshold = Math.max(
         0.8199999928474426f, ObsidianComponent_243.getAICooldown() - 0.07999999821186066f
      );
      boolean readyByCooldown = (cooldown >= focusThreshold);
      boolean fallingForCrit = (!mc.player.isOnGround()
            && mc.player.getVelocity().y < 0.0
            && mc.player.fallDistance > 0.0f);
      return (!(!readyByCooldown && !fallingForCrit));
   }
   public void attack() {
      Hand blockedShieldHand = null;
      if (this.unpressShield.isState()) {
         blockedShieldHand = this.getBlockingShieldHand();
         if (blockedShieldHand != null) {
            mc.interactionManager.stopUsingItem(mc.player);
         }
      }

      if (this.wallBypassMode.is("Обычный")) {
         this.tryBreakRwWallBlockPacket();
      } else if (this.wallBypassMode.is("СпукиТайм")) {
         this.trySpookyTimeWallBypassAttack();
      }

      boolean attacked = false;
      if (this.target instanceof PlayerEntity player && player.isBlocking() && this.breakShield.isState()) {
         attacked = this.shieldBreak(player);
      }

      if (!attacked) {
         mc.interactionManager.attackEntity(mc.player, this.target);
      }

      mc.player.swingHand(Hand.MAIN_HAND);
      if (blockedShieldHand != null) {
         this.restoreShieldBlocking(blockedShieldHand);
      }

      if (this.rotationType.is("WhiteRise")) {
         this.slothRotation.onAttack();
      }

      if (this.rotationType.is("СпукиТайм")) {
         if (this.spookyProfile.is("Спуки-дуэли")) {
            this.spookyTimeDuelsRotation.onAttack();
         } else if (this.spookyProfile.is("Спуки 1.16")) {
            this.spookyTime4DigitsRotation.onAttack();
         } else {
            this.spookyTimeRotation.onAttack();
         }
      }

      if (this.rotationType.is("ФанТайм")) {
         this.funTimeRotation.onAttack();
      }

      if (this.rotationType.is("ФанТайм (Тест)")) {
         this.funTimeRotationTest.onAttack();
      }

      long cooldown = 467L;
      if (this.tpsSync != null && this.tpsSync.isEnable()) {
         cooldown = (long)((float)this.tpsSync.getAdjustedCooldown(cooldown) * 1.100000023841858f);
      }

      this.cps = System.currentTimeMillis() + cooldown;
      this.ticksToAttack = 10;
      this.attackTimer.reset();
   }
   public void tryBreakRwWallBlockPacket() {
      if (this.target != null && mc.player != null && mc.world != null) {
         if (!mc.player.canSee(this.target)) {
            if (mc.player.networkHandler != null) {
               Vec3d start = mc.player.getEyePos();
               Vec3d end = this.target.getBoundingBox().getCenter();
               BlockHitResult hit = mc.world.raycast(new RaycastContext(start, end, ShapeType.COLLIDER, FluidHandling.NONE, mc.player));
               if (hit != null && hit.getType() == Type.BLOCK) {
                  BlockPos blockPos = hit.getBlockPos();
                  if (!mc.world.getBlockState(blockPos).isAir()) {
                     if (!(mc.world.getBlockState(blockPos).getHardness(mc.world, blockPos) < 0.0f)) {
                        Direction direction = hit.getSide() == null ? Direction.UP : hit.getSide();
                        mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, blockPos, direction));
                        mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, blockPos, direction));
                     }
                  }
               }
            }
         }
      }
   }
   public void trySpookyTimeWallBypassAttack() {
      if (this.target != null && mc.player != null && mc.world != null) {
         if (mc.player.networkHandler != null) {
            Vec3d eyePos = mc.player.getEyePos();
            Vec3d center = this.target.getBoundingBox().getCenter();
            BlockHitResult hit = mc.world.raycast(new RaycastContext(eyePos, center, ShapeType.COLLIDER, FluidHandling.NONE, mc.player));
            if (hit != null && hit.getType() == Type.BLOCK) {
               BlockPos blockPos = hit.getBlockPos();
               Direction direction = hit.getSide() == null ? Direction.UP : hit.getSide();
               mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, blockPos, direction));
               mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(Action.ABORT_DESTROY_BLOCK, blockPos, direction));
            }
         }
      }
   }
   public boolean shieldBreak(PlayerEntity entity) {
      InventorySlotHelper axeSlot = ItemNameUtil.getAxe();
      if (!axeSlot.found()) {
         return false;
      } else {
         int previousSlot = mc.player.getInventory().selectedSlot;
         if (axeSlot.slot() == previousSlot) {
            mc.interactionManager.attackEntity(mc.player, entity);
            return true;
         } else if (axeSlot.isInHotBar()) {
            return this.attackWithSilentHotbarSlot(entity, axeSlot.slot(), previousSlot);
         } else if (mc.player.currentScreenHandler.syncId != 0) {
            return false;
         } else {
            int swapHotbarSlot = this.findSilentSwapHotbarSlot(previousSlot);
            if (swapHotbarSlot == (-1)) {
               return false;
            } else {
               this.swapInventoryIntoHotbar(axeSlot.slot(), swapHotbarSlot);
               boolean attacked = false;

               try {
                  attacked = this.attackWithSilentHotbarSlot(entity, swapHotbarSlot, previousSlot);
               } finally {
                  this.swapInventoryIntoHotbar(axeSlot.slot(), swapHotbarSlot);
               }

               return attacked;
            }
         }
      }
   }
   public boolean attackWithSilentHotbarSlot(PlayerEntity entity, int attackSlot, int previousSlot) {
      if (mc.player != null && mc.player.networkHandler != null && mc.interactionManager != null) {
         mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(attackSlot));

         int var4;
         try {
            mc.interactionManager.attackEntity(mc.player, entity);
            var4 = 1;
         } finally {
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(previousSlot));
         }

         return (var4 != 0);
      } else {
         return false;
      }
   }
   public void swapInventoryIntoHotbar(int inventorySlot, int hotbarSlot) {
      if (mc.player != null && mc.interactionManager != null && mc.player.networkHandler != null) {
         int syncId = mc.player.currentScreenHandler.syncId;
         mc.interactionManager.clickSlot(syncId, inventorySlot, hotbarSlot, SlotActionType.SWAP, mc.player);
         mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(syncId));
      }
   }
   public int findSilentSwapHotbarSlot(int previousSlot) {
      for (int slot = 8; slot >= 0; slot--) {
         if (slot != previousSlot) {
            return slot;
         }
      }

      return previousSlot >= 0 && previousSlot < 9 ? previousSlot : -1;
   }
   public Hand getBlockingShieldHand() {
      if (mc.player != null && mc.player.isBlocking()) {
         Hand activeHand = mc.player.getActiveHand();
         if (activeHand == null) {
            return null;
         } else {
            return this.isShieldStack(mc.player.getStackInHand(activeHand)) ? activeHand : null;
         }
      } else {
         return null;
      }
   }
   public boolean shouldBlockAttackWhileUsingItem() {
      if (mc.player != null && mc.player.isUsingItem()) {
         return (!(this.unpressShield.isState() && this.getBlockingShieldHand() != null));
      } else {
         return false;
      }
   }
   public void restoreShieldBlocking(Hand hand) {
      if (mc.player != null && mc.interactionManager != null && hand != null) {
         if (this.isShieldStack(mc.player.getStackInHand(hand)) && !mc.player.isUsingItem()) {
            mc.interactionManager.interactItem(mc.player, hand);
         }
      }
   }
   public boolean isShieldStack(ItemStack stack) {
      return (!stack.isEmpty() && stack.getItem() instanceof ShieldItem);
   }
   public boolean isWeapon() {
      Item item = mc.player.getMainHandStack().getItem();
      return (!(item == Items.AIR
            || !(item instanceof SwordItem)
               && !(item instanceof PickaxeItem)
               && !(item instanceof AxeItem)
               && !(item instanceof HoeItem)
               && !(item instanceof ShovelItem)
               && !(item instanceof MaceItem)
               && item != Items.MACE));
   }
   public boolean isValidTarget(LivingEntity entity) {
      if (entity != null && entity != mc.player) {
         if (!entity.isAlive() || entity.getHealth() <= 0.0f) {
            return false;
         } else if (entity instanceof ArmorStandEntity) {
            return false;
         } else if (!(entity instanceof IronGolemEntity) && !(entity instanceof BatEntity)) {
            if (entity instanceof PlayerEntity player) {
               if (!this.targets.is("Игроки")) {
                  return false;
               }

               if (this.isNaked(player) && !this.targets.is("Голые")) {
                  return false;
               }

               if (player.hasStatusEffect(StatusEffects.INVISIBILITY) && !this.targets.is("Невидимки")) {
                  return false;
               }

               if (Obsidian.INSTANCE.friendStorage.isFriend(entity.getName().getString())) {
                  return false;
               }
            } else if (!(entity instanceof PassiveEntity) && !(entity instanceof CodEntity)) {
               if (!(entity instanceof HostileEntity)) {
                  return false;
               }

               if (!this.targets.is("Мобы")) {
                  return false;
               }
            } else if (!this.targets.is("Мирные")) {
               return false;
            }

            Vec3d nearestPoint = ObsidianComponent_311.getNearestPoint(entity);
            if (nearestPoint == null) {
               nearestPoint = ObsidianComponent_319.getClosestPoint(entity);
            }

            if (mc.player.getEyePos().distanceTo(nearestPoint) > (double)this.getMaxAimRange()) {
               return false;
            } else if (this.wallBypassMode.is("Выкл") && !mc.player.canSee(entity)) {
               return false;
            } else {
               return (!((this.rotationType.is("ХолиВорлд ФОВ") || this.rotationType.is("ХолиВорлд"))
                     && this.holyWorldRotation != null
                     && !this.holyWorldRotation.isInFov(entity)));
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }
   public boolean isNaked(PlayerEntity player) {
      for (ItemStack armorStack : player.getArmorItems()) {
         if (!armorStack.isEmpty()) {
            return false;
         }
      }

      return true;
   }
   public boolean shouldAttack(boolean maceMode) {
      if (!maceMode && mc.player.getAttackCooldownProgress(1.5f) < ObsidianComponent_243.getAICooldown()) {
         return false;
      } else {
         EntityHitResult result = this.getAttackRaycastResult();
         if (!this.raycast.isState()
            || result != null && result.getEntity() != null && result.getEntity() == this.target
            || mc.player.isGliding() && this.target.isGliding()) {
            if (this.rotationType.is("Data") && !this.isDataAimReady(result)) {
               return false;
            } else {
               if (mc.player.isGliding() && this.target.isGliding()) {
                  Vec3d targetPos = this.target
                     .getPos()
                     .add(
                        0.0,
                        (double)this.target.getHeight() / 2.0,
                        0.0
                     );
                  int predict = 0;
                  Vec3d predictedPos = ObsidianComponent_215.predict(this.target, targetPos, predict);
                  if (mc.player.getEyePos().distanceTo(predictedPos)
                     > 5.0) {
                     return false;
                  }
               } else {
                  double distanceCheck = mc.player.getEyePos().distanceTo(this.target.getBoundingBox().getCenter());
                  Vec3d checkPoint = distanceCheck > 3.0
                     ? ObsidianComponent_311.getNearestPoint(this.target)
                     : this.target.getBoundingBox().getCenter();
                  if (checkPoint == null) {
                     checkPoint = ObsidianComponent_319.getNearestPoint(this.target, (double)this.range.getValue().floatValue());
                  }

                  if (mc.player.getEyePos().distanceTo(checkPoint) > (double)this.range.getValue().floatValue()) {
                     return false;
                  }
               }

               return ObsidianComponent_243.canCritical(this.target);
            }
         } else {
            return false;
         }
      }
   }

   public int getWhiteRiseTicksToAttack() {
      return this.ticksToAttack;
   }
   public boolean isDataAimReady(EntityHitResult result) {
      float yawDiff = Math.abs(MathHelper.wrapDegrees(this.targetRotations.x - mc.player.getYaw()));
      float pitchDiff = Math.abs(this.targetRotations.y - mc.player.getPitch());
      boolean closeToAim = (yawDiff <= 1.149999976158142f
            && pitchDiff <= 0.8999999761581421f);
      boolean onTarget = (result != null && result.getEntity() == this.target);
      return (closeToAim && onTarget);
   }

   public boolean isAboveWater() {
      BlockPos pos = BlockPos.ofFloored(
         mc.player
            .getPos()
            .add(
               0.0,
               Double.longBitsToDouble(-4622494657533077094L),
               0.0
            )
      );
      return (!mc.player.isSubmergedInWater() && mc.world.getBlockState(pos).isOf(Blocks.WATER));
   }

   public float getAttackCooldown() {
      return MathHelper.clamp(
         (float)((ILivingEntity)mc.player).getLastAttackedTicks() / this.getAttackCooldownProgressPerTick(),
         0.0f,
         1.0f
      );
   }

   public float getAttackCooldownProgressPerTick() {
      return (float)(
         1.0
            / mc.player.getAttributeValue(EntityAttributes.ATTACK_SPEED)
            * 20.0
      );
   }
   public float getMaxAimRange() {
      return mc.player.isGliding() ? this.elytraAimRange.getValue().floatValue() : this.range.getValue().floatValue() + this.aimRange.getValue().floatValue();
   }

   @Override
   public void onDisable() {
      super.onDisable();
      if (RotationManager.instance != null && RotationManager.instance.currentTask() == ObsidianComponent_398.AIM) {
         RotationManager.instance.currentTask(ObsidianComponent_398.RESET);
      }

      if (this.target != null) {
         this.backTimer.reset();
      }

      this.target = null;
      this.maceForceAttack = false;
      this.funTimeRotation.reset();
      this.testRotation.reset();
      this.slothRotation.reset();
      this.spookyTimeRotation.reset();
      this.holyWorldRotation.reset();
      this.dataSystem.resetState();
      this.lastDataTarget = null;
      this.needSprintReset = false;
      this.sprintResetDone = false;
      this.sprintResetTicks = 0;
      this.ticksToAttack = 0;
   }

   @Override
   public void onEnable() {
      super.onEnable();
      this.funTimeRotation.reset();
      this.testRotation.reset();
      this.slothRotation.reset();
      this.spookyTimeRotation.reset();
      this.holyWorldRotation.reset();
      this.dataSystem.resetState();
      this.lastDataTarget = null;
      this.needSprintReset = false;
      this.sprintResetDone = false;
      this.sprintResetTicks = 0;
      this.ticksToAttack = 0;
      if (mc.player != null) {
         this.currentRotations = new Vec2f(mc.player.getYaw(), mc.player.getPitch());
      }
   }

   @Generated
   public SliderSetting getRange() {
      return this.range;
   }

   @Generated
   public LivingEntity getTarget() {
      return this.target;
   }

   @Generated
   public Vec2f getCurrentRotations() {
      return this.currentRotations;
   }

   @Generated
   public Vec2f getTargetRotations() {
      return this.targetRotations;
   }

   @Generated
   public ObsidianComponent_227 getDataSystem() {
      return this.dataSystem;
   }

   @Generated
   public ObsidianComponent_372 getAttackTimer() {
      return this.attackTimer;
   }
}
