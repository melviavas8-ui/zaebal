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
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public final class AutoExplosionModule extends Module {
   public static AutoExplosionModule INSTANCE = new AutoExplosionModule();
   public final ModeSetting modeBaxa;
   public final ObsidianComponent_261 bind;
   public final BooleanSetting explosionOnRightClick;
   public final BooleanSetting keepCrystal;
   public static final double INTERACT_RANGE = 4.5;
   public BlockPos targetPos;
   public int targetSlot;
   public int oldSlot;
   public boolean needSync;
   public Box crystalArea;
   public boolean blocked;
   public boolean internalInteract;

   public AutoExplosionModule() {
      super("AutoExplosion", "Автоматически взрывает кристалл", Category.COMBAT);
      String[] var10005 = new String[2];
      var10005[0] = "Авто";
      var10005[1] = "По бинду";
      this.modeBaxa = new ModeSetting("Режим взрыва", "Авто", var10005);
      this.bind = new ObsidianComponent_261("Бинд", -1).visible(() -> this.modeBaxa.is("По бинду"));
      this.explosionOnRightClick = new BooleanSetting("Взрыв по ПКМ", true);
      this.keepCrystal = new BooleanSetting("Оставлять кристалл", false);
      this.targetSlot = -1;
      this.oldSlot = -1;
      Setting[] var10001 = new Setting[4];
      var10001[0] = this.modeBaxa;
      var10001[1] = this.bind;
      var10001[2] = this.explosionOnRightClick;
      var10001[3] = this.keepCrystal;
      this.addSettings(var10001);
   }

   @EventTarget
   public void onBinding(InputEvent event) {
      if (mc.player != null && mc.world != null && mc.currentScreen == null) {
         if (this.modeBaxa.is("По бинду")) {
            boolean pressed = (this.bind.getKey() == -1
               ? (
                  event.getKey() == ObsidianComponent_349.createMouseBind(2)
                     ? true : false
               )
               : (event.getKey() == this.bind.getKey() ? true : false));
            if (pressed) {
               this.placeObsidianByCrosshair();
            }
         }
      }
   }

   @EventTarget
   public void onPacket(PacketEvent event) {
      if (mc.player != null && mc.world != null) {
         if (event.getType() == PacketType.SEND) {
            if (!this.internalInteract) {
               if (event.getPacket() instanceof PlayerInteractBlockC2SPacket packet) {
                  BlockHitResult hit = packet.getBlockHitResult();
                  BlockPos clickedPos = hit.getBlockPos();
                  BlockPos placePos = clickedPos.offset(hit.getSide());
                  if (this.isHoldingObsidian()
                     && this.isInRange(placePos)
                     && !mc.player.getItemCooldownManager().isCoolingDown(new ItemStack(Items.END_CRYSTAL))) {
                     int crystalSlot = this.findCrystalSlot();
                     if (crystalSlot != -1) {
                        this.targetPos = placePos;
                        this.targetSlot = crystalSlot;
                        this.blocked = true;
                     }
                  }

                  if (this.explosionOnRightClick.isState() && this.shouldPlaceByRightClick(clickedPos) && this.placeCrystalFromOffhand(hit, clickedPos)) {
                     event.cancel();
                  }
               }
            }
         }
      }
   }

   @EventTarget
   public void onTick(EventTick event) {
      if (mc.player != null && mc.world != null) {
         if (this.needSync) {
            this.needSync = false;
            this.restoreSelectedSlot();
         }

         if (this.targetPos != null) {
            if (mc.world.getBlockState(this.targetPos).isAir()) {
               this.targetPos = null;
            } else if (this.blocked) {
               this.blocked = false;
            } else {
               this.tryPlaceCrystalFast(this.targetPos);
            }
         }

         this.processCrystalArea();
      } else {
         this.reset();
      }
   }
   public void tryPlaceCrystalFast(BlockPos pos) {
      if (this.targetSlot >= 0 && this.targetSlot <= 8 && this.canPlaceCrystal(pos)) {
         this.rotateTo(Vec3d.ofCenter(pos));
         this.oldSlot = mc.player.getInventory().selectedSlot;
         mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(this.targetSlot));
         mc.player.getInventory().selectedSlot = this.targetSlot;
         Vec3d hitVec = Vec3d.ofCenter(pos)
            .add(
               0.0,
               0.5,
               0.0
            );
         BlockHitResult result = new BlockHitResult(hitVec, Direction.UP, pos, false);
         this.sendInteract(Hand.MAIN_HAND, result);
         mc.player.swingHand(Hand.MAIN_HAND);
         this.needSync = true;
         this.crystalArea = this.boxFromBlock(pos.up()).expand(0.1);
         this.targetPos = null;
      }
   }
   public void processCrystalArea() {
      if (this.crystalArea != null) {
         for (Entity entity : mc.world.getOtherEntities(null, this.crystalArea)) {
            if (entity instanceof EndCrystalEntity crystal && crystal.isAlive()) {
               if (!crystal.getBoundingBox().contains(mc.player.getEyePos())) {
                  this.rotateTo(crystal.getBoundingBox().getCenter());
               }

               this.attackCrystal(crystal);
               this.crystalArea = null;
               if (!this.keepCrystal.isState()) {
                  this.restoreSelectedSlot();
               }

               return;
            }
         }
      }
   }
   public boolean shouldPlaceByRightClick(BlockPos clickedPos) {
      if (mc.player.getItemCooldownManager().isCoolingDown(new ItemStack(Items.END_CRYSTAL))) {
         return false;
      } else if (this.isHoldingBlockForPlace()) {
         return false;
      } else {
         Block block = mc.world.getBlockState(clickedPos).getBlock();
         return (block != Blocks.OBSIDIAN && block != Blocks.BEDROCK
            ? false : mc.world.getBlockState(clickedPos.up()).isAir());
      }
   }
   public boolean placeCrystalFromOffhand(BlockHitResult hit, BlockPos clickedPos) {
      int slot = this.findScreenSlot(Items.END_CRYSTAL);
      if (slot == -1 && mc.player.getOffHandStack().getItem() != Items.END_CRYSTAL) {
         return false;
      } else {
         boolean swapped = false;
         if (mc.player.getOffHandStack().getItem() != Items.END_CRYSTAL) {
            this.swapSlotToOffhand(slot);
            swapped = true;
         }

         this.sendInteract(Hand.OFF_HAND, hit);
         mc.player.swingHand(Hand.OFF_HAND);
         this.crystalArea = this.boxFromBlock(clickedPos.up())
            .expand(0.1);
         if (swapped) {
            this.swapSlotToOffhand(slot);
            mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(0));
         }

         return true;
      }
   }
   public void placeObsidianByCrosshair() {
      int obsidianSlot = this.findScreenSlot(Items.OBSIDIAN);
      int crystalSlot = this.findCrystalSlot();
      if (obsidianSlot != -1 && crystalSlot != -1) {
         if (mc.crosshairTarget instanceof BlockHitResult hit) {
            if (hit.getType() == Type.BLOCK) {
               if (!mc.world.getBlockState(hit.getBlockPos()).isAir()) {
                  BlockPos placePos = hit.getBlockPos().offset(hit.getSide());
                  this.targetPos = placePos;
                  this.targetSlot = crystalSlot;
                  this.blocked = true;
                  this.swapSlotToOffhand(obsidianSlot);
                  this.sendInteract(Hand.OFF_HAND, hit);
                  mc.player.swingHand(Hand.OFF_HAND);
                  this.swapSlotToOffhand(obsidianSlot);
                  mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(0));
               }
            }
         }
      }
   }
   public void attackCrystal(EndCrystalEntity crystal) {
      mc.getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket.attack(crystal, false));
      mc.player.swingHand(Hand.MAIN_HAND);
   }
   public void sendInteract(Hand hand, BlockHitResult hitResult) {
      this.internalInteract = true;
      boolean var5 = false ;

      try {
         var5 = true;
         mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(hand, hitResult, 0));
         var5 = false;
      } finally {
         if (var5) {
            this.internalInteract = false;
         }
      }

      this.internalInteract = false;
   }
   public void rotateTo(Vec3d vec) {
      Vec2f rotation = ObsidianComponent_374.getRotations(vec);
      RotationManager.update(
         new ObsidianComponent_olorSetting(rotation.x, rotation.y),
         360.0f,
         360.0f,
         360.0f,
         360.0f,
         1,
         2,
         false
      );
   }
   public boolean canPlaceCrystal(BlockPos pos) {
      BlockPos up1 = pos.up();
      BlockPos up2 = pos.up(2);
      if (!mc.world.getBlockState(up1).isAir()) {
         return false;
      } else if (!mc.world.getBlockState(up2).isAir()) {
         return false;
      } else {
         Box box = new Box(
            (double)up1.getX(),
            (double)up1.getY(),
            (double)up1.getZ(),
            (double)up1.getX() + 1.0,
            (double)up1.getY() + 2.0,
            (double)up1.getZ() + 1.0
         );

         for (Entity entity : mc.world.getOtherEntities(null, box)) {
            if (!(entity instanceof EndCrystalEntity)) {
               return false;
            }
         }

         return true;
      }
   }
   public int findCrystalSlot() {
      for (int i = 0; i < (9); i++) {
         if (mc.player.getInventory().getStack(i).getItem() == Items.END_CRYSTAL) {
            return i;
         }
      }

      return -1;
   }
   public int findScreenSlot(Item item) {
      for (int i = 9; i < 45; i++) {
         ItemStack stack = mc.player.playerScreenHandler.getSlot(i).getStack();
         if (stack.getItem() == item) {
            return i;
         }
      }

      return -1;
   }
   public void swapSlotToOffhand(int slot) {
      if (slot >= 36 && slot <= 44) {
         mc.interactionManager
            .clickSlot(
               0,
               45,
               slot - (36),
               SlotActionType.SWAP,
               mc.player
            );
      } else {
         mc.interactionManager
            .clickSlot(0, slot, 0, SlotActionType.SWAP, mc.player);
         mc.interactionManager
            .clickSlot(
               0,
               45,
               0,
               SlotActionType.SWAP,
               mc.player
            );
         mc.interactionManager
            .clickSlot(0, slot, 0, SlotActionType.SWAP, mc.player);
      }
   }
   public void restoreSelectedSlot() {
      if (this.oldSlot != -1) {
         mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(this.oldSlot));
         mc.player.getInventory().selectedSlot = this.oldSlot;
         this.oldSlot = -1;
      }
   }
   public Box boxFromBlock(BlockPos pos) {
      return new Box(
         (double)pos.getX(),
         (double)pos.getY(),
         (double)pos.getZ(),
         (double)pos.getX() + 1.0,
         (double)pos.getY() + 1.0,
         (double)pos.getZ() + 1.0
      );
   }
   public boolean isHoldingObsidian() {
      return (!(mc.player.getMainHandStack().getItem() != Items.OBSIDIAN && mc.player.getOffHandStack().getItem() != Items.OBSIDIAN));
   }
   public boolean isHoldingBlockForPlace() {
      Item main = mc.player.getMainHandStack().getItem();
      Item off = mc.player.getOffHandStack().getItem();
      return (!((!(main instanceof BlockItem) || main == Items.PLAYER_HEAD) && (!(off instanceof BlockItem) || off == Items.PLAYER_HEAD)));
   }
   public boolean isInRange(BlockPos pos) {
      return (mc.player.getEyePos().distanceTo(Vec3d.ofCenter(pos)) <= 4.5);
   }
   public void reset() {
      if (this.oldSlot != -1 && mc.player != null && mc.getNetworkHandler() != null) {
         this.restoreSelectedSlot();
      }

      this.targetPos = null;
      this.targetSlot = -1;
      this.needSync = false;
      this.crystalArea = null;
      this.blocked = false;
      this.internalInteract = false;
   }

   @Override
   public void onEnable() {
      super.onEnable();
      this.reset();
   }

   @Override
   public void onDisable() {
      super.onDisable();
      this.reset();
   }

   @Generated
   public ModeSetting getModeBaxa() {
      return this.modeBaxa;
   }

   @Generated
   public ObsidianComponent_261 getBind() {
      return this.bind;
   }

   @Generated
   public BooleanSetting getExplosionOnRightClick() {
      return this.explosionOnRightClick;
   }

   @Generated
   public BooleanSetting getKeepCrystal() {
      return this.keepCrystal;
   }

   @Generated
   public BlockPos getTargetPos() {
      return this.targetPos;
   }

   @Generated
   public int getTargetSlot() {
      return this.targetSlot;
   }

   @Generated
   public int getOldSlot() {
      return this.oldSlot;
   }

   @Generated
   public boolean isNeedSync() {
      return this.needSync;
   }

   @Generated
   public Box getCrystalArea() {
      return this.crystalArea;
   }

   @Generated
   public boolean isBlocked() {
      return this.blocked;
   }

   @Generated
   public boolean isInternalInteract() {
      return this.internalInteract;
   }

   @Generated
   public void setTargetPos(BlockPos targetPos) {
      this.targetPos = targetPos;
   }

   @Generated
   public void setTargetSlot(int targetSlot) {
      this.targetSlot = targetSlot;
   }

   @Generated
   public void setOldSlot(int oldSlot) {
      this.oldSlot = oldSlot;
   }

   @Generated
   public void setNeedSync(boolean needSync) {
      this.needSync = needSync;
   }

   @Generated
   public void setCrystalArea(Box crystalArea) {
      this.crystalArea = crystalArea;
   }

   @Generated
   public void setBlocked(boolean blocked) {
      this.blocked = blocked;
   }

   @Generated
   public void setInternalInteract(boolean internalInteract) {
      this.internalInteract = internalInteract;
   }
}
