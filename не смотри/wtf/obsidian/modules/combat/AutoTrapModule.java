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
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public class AutoTrapModule extends Module {
   public static AutoTrapModule INSTANCE = new AutoTrapModule();
   public final ModeSetting mode;
   public final SliderSetting distance;
   public final ObsidianComponent_261 bind;
   public final BooleanSetting fromInventory;
   public final BooleanSetting rotation;
   public final MultiBooleanSetting targets;
   public final BooleanSetting reverseRotate;
   public PlayerEntity target;
   public int oldSlot;
   public int inventorySlot;
   public boolean placing;
   public boolean use;
   public final List<BlockPos> blocksToPlace;
   public int placeIndex;
   public BlockPos currentBlock;
   public boolean waitingForRotation;
   public int rotationTicks;
   public float restoreYaw;
   public float restorePitch;

   public AutoTrapModule() {
      super("AutoTrap", "Автоматически ставит ловушку вокруг игрока", Category.COMBAT);
      String[] var10005 = new String[2];
      var10005[0] = "Obsidian";
      var10005[1] = "CobWeb";
      this.mode = new ModeSetting("Мод", "Obsidian", var10005);
      this.distance = new SliderSetting(
         "Дистанция",
         3.0f,
         1.0f,
         5.0f,
         0.10000000149011612f
      );
      this.bind = new ObsidianComponent_261("Бинд", -1);
      this.fromInventory = new BooleanSetting("Из инвентаря", false);
      this.rotation = new BooleanSetting("Ротация", true);
      BooleanSetting[] var10004 = new BooleanSetting[3];
      var10004[0] = new BooleanSetting("Игроки", true);
      var10004[1] = new BooleanSetting("Невидимые", true);
      var10004[2] = new BooleanSetting("Себя", false);
      this.targets = new MultiBooleanSetting("Таргеты", var10004);
      this.reverseRotate = new BooleanSetting("Реверс ротейт", true).visible(this.rotation::isState);
      this.oldSlot = -1;
      this.inventorySlot = -1;
      this.placing = false;
      this.use = false;
      this.blocksToPlace = new ArrayList<>();
      this.placeIndex = 0;
      this.currentBlock = null;
      this.waitingForRotation = false;
      this.rotationTicks = 0;
      Setting[] var10001 = new Setting[7];
      var10001[0] = this.mode;
      var10001[1] = this.distance;
      var10001[2] = this.bind;
      var10001[3] = this.fromInventory;
      var10001[4] = this.rotation;
      var10001[5] = this.reverseRotate;
      var10001[6] = this.targets;
      this.addSettings(var10001);
   }

   @EventTarget
   public void onBinding(InputEvent event) {
      if (mc.currentScreen == null) {
         if (event.getKey() == this.bind.getKey()) {
            this.use = true;
         }
      }
   }

   @EventTarget
   public void onGameUpdate(Event_93 e) {
      if (mc.player != null && mc.world != null) {
         if (this.placing && this.currentBlock != null && this.rotation.isState()) {
            this.rotateToBlock(this.currentBlock);
         }
      }
   }

   @EventTarget
   public void onUpdate(EventTick e) {
      if (mc.player != null && mc.world != null) {
         if (this.use && !this.placing) {
            this.target = this.findTarget();
            if (this.target != null) {
               this.startPlacing();
            }

            this.use = false;
         }

         if (this.placing) {
            this.processPlacing();
         }
      } else {
         this.use = false;
      }
   }
   public void rotateToBlock(BlockPos pos) {
      Direction side = this.getPlaceSide(pos);
      if (side != null) {
         BlockPos neighbor = pos.offset(side);
         Direction opposite = side.getOpposite();
         Vec3d hitVec = this.getHitVec(neighbor, opposite);
         Vec2f targetRot = ObsidianComponent_374.getRotations(hitVec);
         RotationManager.update(
            new ObsidianComponent_olorSetting(targetRot.x, targetRot.y),
            360.0f,
            360.0f,
            360.0f,
            360.0f,
            5,
            1,
            false
         );
      }
   }
   public Vec3d getHitVec(BlockPos neighbor, Direction face) {
      Vec3d center = Vec3d.ofCenter(neighbor);
      return center.add(
         (double)face.getOffsetX() * 0.5,
         (double)face.getOffsetY() * 0.5,
         (double)face.getOffsetZ() * 0.5
      );
   }
   public boolean isRotatedToBlock(BlockPos pos) {
      if (!this.rotation.isState()) {
         return true;
      } else {
         Direction side = this.getPlaceSide(pos);
         if (side == null) {
            return false;
         } else {
            BlockPos neighbor = pos.offset(side);
            Direction opposite = side.getOpposite();
            Vec3d hitVec = this.getHitVec(neighbor, opposite);
            Vec2f targetRot = ObsidianComponent_374.getRotations(hitVec);
            float yawDiff = Math.abs(MathHelper.wrapDegrees(targetRot.x - mc.player.getYaw()));
            float pitchDiff = Math.abs(MathHelper.wrapDegrees(targetRot.y - mc.player.getPitch()));
            return (yawDiff < 5.0f
                  && pitchDiff < 5.0f);
         }
      }
   }
   public void startPlacing() {
      this.blocksToPlace.clear();
      this.placeIndex = 0;
      this.waitingForRotation = false;
      this.rotationTicks = 0;
      BlockPos targetPos = this.target.getBlockPos();
      if (this.mode.is("Obsidian")) {
         this.blocksToPlace.add(targetPos.add(1, 0, 0));
         this.blocksToPlace.add(targetPos.add(-1, 0, 0));
         this.blocksToPlace.add(targetPos.add(0, 0, 1));
         this.blocksToPlace
            .add(targetPos.add(0, 0, -1));
         this.blocksToPlace.add(targetPos.add(1, 1, 0));
         this.blocksToPlace
            .add(targetPos.add(-1, 1, 0));
         this.blocksToPlace
            .add(
               targetPos.add(
                  0, 1, 1
               )
            );
         this.blocksToPlace
            .add(targetPos.add(0, 1, -1));
         this.blocksToPlace.add(targetPos.add(0, 2, 0));
         this.blocksToPlace
            .add(targetPos.add(1, 2, 0));
         this.blocksToPlace.add(targetPos.add(-1, 2, 0));
         this.blocksToPlace.add(targetPos.add(0, 2, 1));
         this.blocksToPlace.add(targetPos.add(0, 2, -1));
      } else {
         this.blocksToPlace.add(targetPos);
         this.blocksToPlace.add(targetPos.up());
      }

      if (this.fromInventory.isState()) {
         this.oldSlot = mc.player.getInventory().selectedSlot;
         int slot = this.findItemSlot();
         if (slot == (-1)) {
            this.placing = false;
            return;
         }

         if (slot < (9)) {
            mc.player.getInventory().selectedSlot = slot;
            this.inventorySlot = -1;
         } else {
            this.inventorySlot = slot;
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slot, this.oldSlot, SlotActionType.SWAP, mc.player);
         }
      }

      this.placing = true;
   }
   public void processPlacing() {
      if (this.target == null
         || this.target.isAlive() && !AntiBotModule.checkBot(this.target) && !(mc.player.distanceTo(this.target) > this.distance.getValue().floatValue())) {
         if (this.placeIndex >= this.blocksToPlace.size()) {
            this.finishPlacing();
         } else {
            BlockPos pos = this.blocksToPlace.get(this.placeIndex);
            this.currentBlock = pos;
            if (!mc.world.getBlockState(pos).isReplaceable()) {
               this.placeIndex += 1;
               this.waitingForRotation = false;
               this.rotationTicks = 0;
            } else {
               Direction side = this.getPlaceSide(pos);
               if (side == null) {
                  this.placeIndex += 1;
                  this.waitingForRotation = false;
                  this.rotationTicks = 0;
               } else {
                  if (this.rotation.isState()) {
                     if (!this.waitingForRotation) {
                        this.rotateToBlock(pos);
                        this.waitingForRotation = true;
                        this.rotationTicks = 0;
                        return;
                     }

                     this.rotationTicks += 1;
                     if (!this.isRotatedToBlock(pos) || this.rotationTicks < 2) {
                        this.rotateToBlock(pos);
                        return;
                     }
                  }

                  this.placeBlock(pos);
                  this.placeIndex += 1;
                  this.waitingForRotation = false;
                  this.rotationTicks = 0;
               }
            }
         }
      } else {
         this.finishPlacing();
      }
   }
   public void finishPlacing() {
      if (this.fromInventory.isState()) {
         if (this.inventorySlot != (-1)) {
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, this.inventorySlot, this.oldSlot, SlotActionType.SWAP, mc.player);
            this.inventorySlot = -1;
         } else if (this.oldSlot != -1) {
            mc.player.getInventory().selectedSlot = this.oldSlot;
         }

         this.oldSlot = -1;
      }

      this.placing = false;
      this.target = null;
      this.currentBlock = null;
      this.blocksToPlace.clear();
      this.placeIndex = 0;
      this.waitingForRotation = false;
      this.rotationTicks = 0;
   }
   public Direction getPlaceSide(BlockPos pos) {
      Direction[] var10000 = new Direction[6];
      var10000[0] = Direction.DOWN;
      var10000[1] = Direction.UP;
      var10000[2] = Direction.NORTH;
      var10000[3] = Direction.SOUTH;
      var10000[4] = Direction.WEST;
      var10000[5] = Direction.EAST;
      Direction[] priority = var10000;
      Direction[] var3 = priority;
      int var4 = priority.length;

      for (int var5 = 0; var5 < var4; var5++) {
         Direction dir = var3[var5];
         BlockPos neighbor = pos.offset(dir);
         BlockState state = mc.world.getBlockState(neighbor);
         if (!state.isReplaceable() && !state.isLiquid() && state.isSolidBlock(mc.world, neighbor)) {
            return dir;
         }
      }

      var3 = priority;
      var4 = priority.length;

      for (int var11 = 0; var11 < var4; var11++) {
         Direction dir = var3[var11];
         BlockPos neighbor = pos.offset(dir);
         BlockState state = mc.world.getBlockState(neighbor);
         if (!state.isReplaceable() && !state.isLiquid()) {
            return dir;
         }
      }

      return null;
   }
   public void placeBlock(BlockPos pos) {
      Direction side = this.getPlaceSide(pos);
      if (side != null) {
         BlockPos neighbor = pos.offset(side);
         Direction opposite = side.getOpposite();
         Vec3d hitVec = this.getHitVec(neighbor, opposite);
         BlockHitResult result = new BlockHitResult(hitVec, opposite, neighbor, false);
         mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, result);
         mc.player.swingHand(Hand.MAIN_HAND);
      }
   }
   public int findItemSlot() {
      Item item = this.mode.is("Obsidian") ? Items.OBSIDIAN : Items.COBWEB;

      for (int i = 0; i < (36); i++) {
         if (mc.player.getInventory().getStack(i).getItem() == item) {
            return i;
         }
      }

      return -1;
   }
   public PlayerEntity findTarget() {
      if (this.targets.is("Себя")) {
         return mc.player;
      } else {
         List<PlayerEntity> playerTargets = new ArrayList<>();

         for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof PlayerEntity) {
               PlayerEntity player = (PlayerEntity)entity;
               if (player != mc.player
                  && player.isAlive()
                  && !AntiBotModule.checkBot(player)
                  && this.targets.is("Игроки")
                  && (!player.hasStatusEffect(StatusEffects.INVISIBILITY) || this.targets.is("Невидимые"))
                  && !Obsidian.INSTANCE.friendStorage.isFriend(player.getName().getString())
                  && !(mc.player.distanceTo(player) > this.distance.getValue().floatValue())) {
                  playerTargets.add(player);
               }
            }
         }

         if (playerTargets.isEmpty()) {
            return null;
         } else {
            playerTargets.sort(Comparator.comparingDouble(p -> (double)mc.player.distanceTo(p)));
            return playerTargets.get(0);
         }
      }
   }

   @Override
   public void onDisable() {
      super.onDisable();
      if (this.placing) {
         this.finishPlacing();
      }

      this.target = null;
      this.placing = false;
      this.use = false;
      this.currentBlock = null;
      this.blocksToPlace.clear();
      this.placeIndex = 0;
      this.oldSlot = -1;
      this.inventorySlot = -1;
      this.waitingForRotation = false;
      this.rotationTicks = 0;
   }

   @Override
   public void onEnable() {
      super.onEnable();
      this.placing = false;
      this.use = false;
      this.currentBlock = null;
      this.blocksToPlace.clear();
      this.placeIndex = 0;
      this.oldSlot = -1;
      this.inventorySlot = -1;
      this.waitingForRotation = false;
      this.rotationTicks = 0;
   }
}
