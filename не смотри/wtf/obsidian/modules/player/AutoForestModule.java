package wtf.obsidian.modules.player;

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

























import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;

public class AutoForestModule extends Module {
   public static AutoForestModule INSTANCE = new AutoForestModule();
   private static final double MAX_RANGE = 4.0;
   private static final double MAX_RANGE_SQ = 16.0;
   private static final long DEFAULT_BREAK_DELAY_MS = 3L;
   private static final float AUTO_FAST_BREAK_SPEED = 1.0F;
   private static final float DEFAULT_PACKETS_PER_SECOND = 100.0F;
   private static final long VISUAL_TTL_MS = 300000L;
   private static final long NICK_REMINDER_DELAY_MS = 5000L;
   private static final String MODE_NORMAL_ALIAS = "normal";
   private static final String MODE_FAST_ALIAS = "fast";
   private final ModeSetting breakMode;
   private final SliderSetting packetsPerSecond;
   private final SliderSetting breakRadius;
   private final BooleanSetting swing;
   private final BooleanSetting autoSell;
   private final BooleanSetting autoPay;
   private final BooleanSetting preserveVisuals;
   private final SliderSetting payAmount;
   private final SliderSetting intervalSeconds;
   private final Map<BlockPos, BlockState> preservedBlocks;
   private final Map<BlockPos, Long> lastUpdateTime;
   private final Set<BlockPos> managedBlocks;
   private boolean currentSessionEnabled;
   private BlockPos targetPos;
   private String payTarget;
   private long lastBreakTime;
   private long lastPacketTime;
   private long lastSellTime;
   private long lastPayTime;
   private long lastNickReminderTime;

   public AutoForestModule() {
      super("AutoForest", "Автоматически ломает бревна и переводит деньги", Category.PLAYER);
      String[] var10005 = new String[2];
      var10005[0] = "Обычный";
      var10005[1] = "Быстрый";
      this.breakMode = new ModeSetting("Режим ломания", "Обычный", var10005);
      this.packetsPerSecond = new SliderSetting(
            "Пакетов в секунду",
            100.0f,
            1.0f,
            100.0f,
            1.0f
         )
         .visible(() -> this.breakMode.is("Быстрый"));
      this.breakRadius = new SliderSetting(
         "Радиус",
         4.0f,
         1.0f,
         6.0f,
         0.5f
      );
      this.swing = new BooleanSetting("Махать рукой", true);
      this.autoSell = new BooleanSetting("Авто продажа дерева", true);
      this.autoPay = new BooleanSetting("AutoPay", false);
      this.preserveVisuals = new BooleanSetting("Сохранять визуализацию", true);
      this.payAmount = new SliderSetting(
            "Сумма перевода",
            1000.0f,
            500.0f,
            25000.0f,
            500.0f
         )
         .visible(this.autoPay::isState);
      this.intervalSeconds = new SliderSetting(
         "Задержка",
         20.0f,
         1.0f,
         60.0f,
         1.0f
      );
      this.preservedBlocks = new HashMap<>();
      this.lastUpdateTime = new HashMap<>();
      this.managedBlocks = new HashSet<>();
      this.payTarget = "";
      Setting[] var10001 = new Setting[9];
      var10001[0] = this.breakMode;
      var10001[1] = this.packetsPerSecond;
      var10001[2] = this.breakRadius;
      var10001[3] = this.swing;
      var10001[4] = this.autoSell;
      var10001[5] = this.autoPay;
      var10001[6] = this.preserveVisuals;
      var10001[7] = this.payAmount;
      var10001[8] = this.intervalSeconds;
      this.addSettings(var10001);
      EventManager.register(this);
   }

   @EventTarget
   public void onUpdate(EventTick event) {
      if (this.currentSessionEnabled && mc.player != null && mc.world != null) {
         this.tickCurrentSession();
      }

      this.tickFrozenBots();
   }

   private void tickCurrentSession() {
      if (mc.player != null && mc.world != null && mc.getNetworkHandler() != null) {
         long now = System.currentTimeMillis();
         long scheduleDelay = Math.max(
            Long.parseUnsignedLong("em02dj892tvj1", 32) ^ -1513124150671114871L,
            (long)(this.intervalSeconds.get() * 500.0f)
         );
         if (this.autoSell.isState() && now - this.lastSellTime >= scheduleDelay) {
            mc.getNetworkHandler().sendChatCommand("sellwood");
            this.lastSellTime = now;
         }

         if (this.autoPay.isState()) {
            if (this.payTarget.isBlank()) {
               if (now - this.lastNickReminderTime >= (Long.parseUnsignedLong("24p3h7mji1d9b", 32) ^ 2478230204571494051L)) {
                  this.lastNickReminderTime = now;
                  ObsidianComponent_hatUtil.sendMessage("Укажите ник для перевода через .autoles pay <nick>");
               }
            } else if (now - this.lastPayTime >= scheduleDelay + (Long.parseUnsignedLong("dhc0tmi3fcmsk", 32) ^ -2832731504420431012L)) {
               mc.getNetworkHandler().sendChatCommand("pay " + this.payTarget + " " + (int)this.payAmount.get());
               this.lastPayTime = now;
            }
         }

         if (this.targetPos != null && (!this.isLog(this.targetPos) || !this.isInRange(this.targetPos) || !this.isVisible(this.targetPos))) {
            this.targetPos = null;
         }

         if (this.targetPos == null) {
            this.targetPos = this.findNearestLog();
         }

         if (this.targetPos != null) {
            this.breakTarget(now);
         }

         if (this.preserveVisuals.isState()) {
            this.updateVisualization(now);
         }
      } else {
         this.targetPos = null;
      }
   }

   private void tickFrozenBots() {
      for (BotConnection bot : BotManager.getConnections()) {
         BotState state = bot.autoForestState();
         if (state != null && state.enabled() && bot.player() != null && bot.world() != null && bot.handler() != null) {
            try {
               this.tickBotSession(bot, state);
            } catch (Exception var5) {
               state.enabled(false);
               state.targetPos(null);
            }
         }
      }
   }

   private void tickBotSession(BotConnection bot, BotState state) {
      if (!bot.player().isRemoved() && bot.player().isAlive()) {
         long now = System.currentTimeMillis();
         long scheduleDelay = Math.max(
            Long.parseUnsignedLong("1cfhn5evq5m7n", 32) ^ 1602779178428717855L,
            (long)(
               Math.max(1.0f, state.intervalSeconds())
                  * 500.0f
            )
         );
         if (state.autoSell() && now - state.lastSellTime() >= scheduleDelay) {
            bot.handler().sendChatCommand("sellwood");
            state.lastSellTime(now);
         }

         if (state.autoPay()) {
            if (state.payTarget().isBlank()) {
               if (now - state.lastNickReminderTime() >= (Long.parseUnsignedLong("aochsfkvomsgv", 32) ^ -6038697641715736169L)) {
                  state.lastNickReminderTime(now);
               }
            } else if (now - state.lastPayTime() >= scheduleDelay + (Long.parseUnsignedLong("2r7jr3hl0di4p", 32) ^ 3287200139995170897L)) {
               bot.handler().sendChatCommand("pay " + state.payTarget() + " " + (int)state.payAmount());
               state.lastPayTime(now);
            }
         }

         if (state.targetPos() != null
            && (
               !this.isLog(bot.world(), state.targetPos())
                  || !this.isInRange(bot.player(), state.targetPos())
                  || !this.isVisible(bot.world(), bot.player(), state.targetPos())
            )) {
            state.targetPos(null);
         }

         if (state.targetPos() == null) {
            state.targetPos(this.findNearestLog(bot.world(), bot.player(), state.breakRadius()));
         }

         if (state.targetPos() != null) {
            if ("fast".equals(state.modeAlias())) {
               long interval = Math.max(
                  Long.parseUnsignedLong("fgrdtaegi2ohi", 32) ^ -545571812958707149L,
                  (long)(
                     1000.0f
                        / Math.max(1.0f, state.packetsPerSecond())
                  )
               );
               if (now - state.lastPacketTime() >= interval) {
                  this.performFastBreak(bot.handler(), bot.interactionManager(), bot.player(), bot.world(), state.targetPos(), state.swing());
                  state.lastPacketTime(now);
               }
            } else if (now - state.lastBreakTime() >= (Long.parseUnsignedLong("c5ugpj1ib1hsv", 32) ^ -4397173943858837604L)) {
               if (bot.interactionManager() != null) {
                  bot.interactionManager().attackBlock(state.targetPos(), Direction.UP);
                  bot.interactionManager().updateBlockBreakingProgress(state.targetPos(), Direction.UP);
               } else {
                  this.performFastBreak(bot.handler(), bot.interactionManager(), bot.player(), bot.world(), state.targetPos(), state.swing());
               }

               if (state.swing()) {
                  bot.handler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
               }

               state.lastBreakTime(now);
            }
         }
      } else {
         state.enabled(false);
         state.targetPos(null);
      }
   }

   @EventTarget
   public void onPacket(PacketEvent event) {
      if (this.currentSessionEnabled && this.preserveVisuals.isState() && mc.player != null && mc.world != null) {
         if (event.getType() == PacketType.SEND && event.getPacket() instanceof PlayerActionC2SPacket packet) {
            this.handleDigPacket(packet);
         } else {
            if (event.getType() == PacketType.RECEIVE && event.getPacket() instanceof BlockUpdateS2CPacket packet) {
               BlockPos pos = packet.getPos();
               BlockState savedState = this.preservedBlocks.get(pos);
               if (savedState == null) {
                  return;
               }

               BlockState serverState = packet.getState();
               if (serverState.isAir() || !serverState.equals(savedState)) {
                  event.cancel();
                  this.setClientBlock(pos, savedState);
                  this.lastUpdateTime.put(pos, System.currentTimeMillis());
               }
            }
         }
      }
   }

   private void breakTarget(long now) {
      if (this.targetPos != null && mc.player != null && mc.player.networkHandler != null && mc.interactionManager != null) {
         if (this.breakMode.is("Быстрый")) {
            long interval = Math.max(
               Long.parseUnsignedLong("6sosrh431dmst", 32) ^ 7954372379681414044L,
               (long)(
                  1000.0f
                     / Math.max(1.0f, this.packetsPerSecond.get())
               )
            );
            if (now - this.lastPacketTime >= interval) {
               this.performFastBreak(this.targetPos);
               this.lastPacketTime = now;
            }
         } else if (now - this.lastBreakTime >= (Long.parseUnsignedLong("audtbu8dil9p4", 32) ^ -5820994937026861273L)) {
            mc.interactionManager.attackBlock(this.targetPos, Direction.UP);
            mc.interactionManager.updateBlockBreakingProgress(this.targetPos, Direction.UP);
            if (this.swing.isState()) {
               mc.player.swingHand(Hand.MAIN_HAND);
            }

            this.lastBreakTime = now;
         }
      }
   }

   private void performFastBreak(BlockPos pos) {
      if (mc.player != null && mc.world != null && mc.player.networkHandler != null) {
         this.performFastBreak(mc.player.networkHandler, mc.interactionManager, mc.player, mc.world, pos, this.swing.isState());
      }
   }

   private void performFastBreak(
      ClientPlayNetworkHandler handler,
      ClientPlayerInteractionManager interactionManager,
      ClientPlayerEntity player,
      ClientWorld world,
      BlockPos pos,
      boolean shouldSwing
   ) {
      if (handler != null && player != null && pos != null) {
         boolean accelerated = false;
         if (interactionManager != null && world != null) {
            interactionManager.attackBlock(pos, Direction.UP);
            accelerated = FastBreakModule.accelerateClientBreak(
               interactionManager, player, world, pos, Direction.UP, 1.0f, shouldSwing
            );
         }

         if (!accelerated) {
            FastBreakModule.packetBreak(handler, player, pos, Direction.UP, shouldSwing);
         }
      }
   }

   private BlockPos findNearestLog() {
      return this.findNearestLog(mc.world, mc.player, this.breakRadius.get());
   }

   private BlockPos findNearestLog(ClientWorld world, ClientPlayerEntity player, float radiusValue) {
      if (player != null && world != null) {
         BlockPos playerPos = player.getBlockPos();
         int radius = Math.round(radiusValue);
         return BlockPos.stream(playerPos.add(-radius, -radius, -radius), playerPos.add(radius, radius, radius))
            .<BlockPos>map(BlockPos::toImmutable)
            .filter(pos -> this.isLog(world, pos))
            .filter(pos -> this.isInRange(player, pos))
            .filter(pos -> this.isVisible(world, player, pos))
            .min(Comparator.comparingDouble(pos -> player.squaredDistanceTo(Vec3d.ofCenter(pos))))
            .orElse(null);
      } else {
         return null;
      }
   }

   private boolean isInRange(BlockPos pos) {
      return this.isInRange(mc.player, pos);
   }

   private boolean isInRange(ClientPlayerEntity player, BlockPos pos) {
      return (boolean)(player != null
            && player.squaredDistanceTo(Vec3d.ofCenter(pos)) <= Double.longBitsToDouble(Long.parseUnsignedLong("ag0rt542nd3jk", 32) ^ -1714889417959240076L)
         ? 1
         : false);
   }

   private boolean isVisible(BlockPos pos) {
      return this.isVisible(mc.world, mc.player, pos);
   }

   private boolean isVisible(ClientWorld world, ClientPlayerEntity player, BlockPos pos) {
      if (player != null && world != null) {
         Vec3d eyePos = player.getEyePos();
         Vec3d targetCenter = Vec3d.ofCenter(pos);
         BlockHitResult hit = world.raycast(new RaycastContext(eyePos, targetCenter, ShapeType.COLLIDER, FluidHandling.NONE, player));
         return (boolean)(hit != null && hit.getType() != Type.MISS && !pos.equals(hit.getBlockPos())
            ? false : 1);
      } else {
         return false;
      }
   }

   private boolean isLog(BlockPos pos) {
      return this.isLog(mc.world, pos);
   }

   private boolean isLog(ClientWorld world, BlockPos pos) {
      return (boolean)(world != null && world.getBlockState(pos).isIn(BlockTags.LOGS)
         ? 1
         : false);
   }

   private void handleDigPacket(PlayerActionC2SPacket packet) {
      Action action = packet.getAction();
      if (action == Action.START_DESTROY_BLOCK || action == Action.STOP_DESTROY_BLOCK) {
         BlockPos pos = packet.getPos();
         if (this.isLog(pos)) {
            BlockState state = mc.world.getBlockState(pos);
            if (!state.isAir()) {
               this.preservedBlocks.put(pos, state);
               this.managedBlocks.add(pos);
               this.lastUpdateTime.put(pos, System.currentTimeMillis());
               this.setClientBlock(pos, state);
            }
         }
      }
   }

   private void updateVisualization(long now) {
      ClientWorld toRemove = mc.world;
      if (toRemove instanceof ClientWorld) {
         ClientWorld clientWorld = toRemove;
         HashSet<BlockPos> var11 = new HashSet<>();

         for (Entry<BlockPos, BlockState> entry : this.preservedBlocks.entrySet()) {
            BlockPos pos = entry.getKey();
            BlockState savedState = entry.getValue();
            BlockState currentState = clientWorld.getBlockState(pos);
            if (currentState == null || !currentState.equals(savedState)) {
               clientWorld.setBlockState(pos, savedState, 0);
               this.lastUpdateTime.put(pos, now);
            }

            Long lastSeen = this.lastUpdateTime.get(pos);
            if (lastSeen != null && now - lastSeen > (Long.parseUnsignedLong("bb0qedejjts8l", 32) ^ -5367360106621410571L)) {
               var11.add(pos);
            }
         }

         for (BlockPos posx : var11) {
            this.preservedBlocks.remove(posx);
            this.lastUpdateTime.remove(posx);
            this.managedBlocks.remove(posx);
         }
      }
   }

   private void restoreVisualState() {
      ClientWorld var2 = mc.world;
      if (!(var2 instanceof ClientWorld)) {
         this.preservedBlocks.clear();
         this.lastUpdateTime.clear();
         this.managedBlocks.clear();
      } else {
         ClientWorld clientWorld = var2;

         for (BlockPos pos : this.managedBlocks) {
            clientWorld.setBlockState(pos, mc.world.getBlockState(pos), 0);
         }

         this.preservedBlocks.clear();
         this.lastUpdateTime.clear();
         this.managedBlocks.clear();
      }
   }

   private void setClientBlock(BlockPos pos, BlockState state) {
      ClientWorld var4 = mc.world;
      if (var4 instanceof ClientWorld) {
         var4.setBlockState(pos, state, 0);
      }
   }

   public List<String> getModeSuggestions() {
      return List.of("normal", "fast");
   }

   public boolean setModeAlias(String alias) {
      if (alias != null && !alias.isBlank()) {
         String var2 = alias.trim().toLowerCase(Locale.ROOT);
         int var3 = -1;
         switch (var2.hashCode()) {
            case -1039745817:
               if (var2.equals("normal")) {
                  var3 = 0;
               }
               break;
            case -835687524:
               if (var2.equals("обычный")) {
                  var3 = 2;
               }
               break;
            case 3135580:
               if (var2.equals("fast")) {
                  var3 = 3;
               }
               break;
            case 107947501:
               if (var2.equals("quick")) {
                  var3 = 4;
               }
               break;
            case 1246643155:
               if (var2.equals("быстрый")) {
                  var3 = 5;
               }
               break;
            case 1544803905:
               if (var2.equals("default")) {
                  var3 = 1;
               }
         }
         return switch (var3) {
            case 0, 1, 2 -> {
               this.breakMode.set((String)this.breakMode.getMods().get(0));
               yield true;
            }
            case 3, 4, 5 -> {
               if (this.breakMode.getMods().size() < 2) {
                  yield false;
               } else {
                  this.breakMode.set((String)this.breakMode.getMods().get(1));
                  yield true;
               }
            }
            default -> false;
         };
      } else {
         return false;
      }
   }

   public String getModeAlias() {
      return this.breakMode.getMods().size() > (1)
            && this.breakMode.is((String)this.breakMode.getMods().get(1))
         ? "fast"
         : "normal";
   }

   public void enableForCurrentSession() {
      this.currentSessionEnabled = true;
      this.resetRuntimeState();
      this.restoreVisualState();
   }

   public void disableForCurrentSession() {
      this.currentSessionEnabled = false;
      this.restoreVisualState();
      this.resetRuntimeState();
   }

   public boolean isCurrentSessionEnabled() {
      return this.currentSessionEnabled;
   }

   public void setSwingEnabled(boolean value) {
      this.swing.setState(value);
   }

   public boolean isSwingEnabled() {
      return this.swing.isState();
   }

   public void setAutoSellEnabled(boolean value) {
      this.autoSell.setState(value);
   }

   public boolean isAutoSellEnabled() {
      return this.autoSell.isState();
   }

   public void setAutoPayEnabled(boolean value) {
      this.autoPay.setState(value);
      if (!value) {
         this.lastNickReminderTime = Long.parseUnsignedLong("9dqfi04g1b9jq", 32) ^ -7572255211799599494L;
      }
   }

   public boolean isAutoPayEnabled() {
      return this.autoPay.isState();
   }

   public void setPreserveVisualsEnabled(boolean value) {
      this.preserveVisuals.setState(value);
   }

   public boolean isPreserveVisualsEnabled() {
      return this.preserveVisuals.isState();
   }

   public void setPacketsPerSecond(float value) {
      this.packetsPerSecond.setValue(value);
   }

   public float getPacketsPerSecond() {
      return this.packetsPerSecond.get();
   }

   public void setBreakRadius(float value) {
      this.breakRadius.setValue(value);
   }

   public float getBreakRadius() {
      return this.breakRadius.get();
   }

   public void setPayAmount(float value) {
      this.payAmount.setValue(value);
   }

   public float getPayAmount() {
      return this.payAmount.get();
   }

   public void setIntervalSeconds(float value) {
      this.intervalSeconds.setValue(value);
   }

   public float getIntervalSeconds() {
      return this.intervalSeconds.get();
   }

   public boolean setPayTarget(String target) {
      String trimmed = target == null ? "" : target.trim();
      if (trimmed.isEmpty()) {
         return false;
      } else {
         this.payTarget = trimmed;
         this.lastNickReminderTime = Long.parseUnsignedLong("36ri05s37u62o", 32) ^ 3705970114089130072L;
         return true;
      }
   }

   public String getPayTarget() {
      return this.payTarget;
   }

   public boolean capturePayTargetFromChat(String message) {
      return false;
   }

   public void clearPayTarget() {
      this.payTarget = "";
      this.lastNickReminderTime = Long.parseUnsignedLong("b3jm01n9dltka", 32) ^ -5634354918189238646L;
   }

   public BotState captureState() {
      BotState state = new BotState();
      state.enabled(this.currentSessionEnabled);
      state.modeAlias(this.getModeAlias());
      state.packetsPerSecond(this.packetsPerSecond.get());
      state.breakRadius(this.breakRadius.get());
      state.swing(this.swing.isState());
      state.autoSell(this.autoSell.isState());
      state.autoPay(this.autoPay.isState());
      state.preserveVisuals(this.preserveVisuals.isState());
      state.payAmount(this.payAmount.get());
      state.intervalSeconds(this.intervalSeconds.get());
      state.payTarget(this.payTarget);
      state.targetPos(this.targetPos);
      state.lastBreakTime(this.lastBreakTime);
      state.lastPacketTime(this.lastPacketTime);
      state.lastSellTime(this.lastSellTime);
      state.lastPayTime(this.lastPayTime);
      state.lastNickReminderTime(this.lastNickReminderTime);
      state.preservedBlocks(new HashMap<>(this.preservedBlocks));
      state.lastUpdateTime(new HashMap<>(this.lastUpdateTime));
      state.managedBlocks(new HashSet<>(this.managedBlocks));
      return state;
   }

   public void applyState(BotState state) {
      if (state == null) {
         this.resetToDefaults();
      } else {
         this.currentSessionEnabled = state.enabled();
         this.setModeAlias(state.modeAlias());
         this.packetsPerSecond.setValue(state.packetsPerSecond());
         this.breakRadius.setValue(state.breakRadius());
         this.swing.setState(state.swing());
         this.autoSell.setState(state.autoSell());
         this.autoPay.setState(state.autoPay());
         this.preserveVisuals.setState(state.preserveVisuals());
         this.payAmount.setValue(state.payAmount());
         this.intervalSeconds.setValue(state.intervalSeconds());
         this.payTarget = state.payTarget();
         this.targetPos = state.targetPos();
         this.lastBreakTime = state.lastBreakTime();
         this.lastPacketTime = state.lastPacketTime();
         this.lastSellTime = state.lastSellTime();
         this.lastPayTime = state.lastPayTime();
         this.lastNickReminderTime = state.lastNickReminderTime();
         this.preservedBlocks.clear();
         this.preservedBlocks.putAll(state.preservedBlocks());
         this.lastUpdateTime.clear();
         this.lastUpdateTime.putAll(state.lastUpdateTime());
         this.managedBlocks.clear();
         this.managedBlocks.addAll(state.managedBlocks());
      }
   }

   public void resetToDefaults() {
      this.currentSessionEnabled = false;
      this.setModeAlias("normal");
      this.packetsPerSecond.setValue(100.0f);
      this.breakRadius.setValue(4.0f);
      this.swing.setState(true);
      this.autoSell.setState(true);
      this.autoPay.setState(false);
      this.preserveVisuals.setState(true);
      this.payAmount.setValue(1000.0f);
      this.intervalSeconds.setValue(20.0f);
      this.payTarget = "";
      this.restoreVisualState();
      this.resetRuntimeState();
   }

   private void resetRuntimeState() {
      this.targetPos = null;
      this.lastBreakTime = Long.parseUnsignedLong("5ivjpdg8kcme9", 32) ^ 6448725221494381001L;
      this.lastPacketTime = Long.parseUnsignedLong("3q2u1rmp9vbre", 32) ^ 4398822618986164078L;
      this.lastSellTime = Long.parseUnsignedLong("cvqur7liaj9d3", 32) ^ -3464434431603137117L;
      this.lastPayTime = Long.parseUnsignedLong("cfji5u4cbludo", 32) ^ -4049222113155155528L;
      this.lastNickReminderTime = Long.parseUnsignedLong("a3hugjk0jfv44", 32) ^ -6789228540488844156L;
      this.preservedBlocks.clear();
      this.lastUpdateTime.clear();
      this.managedBlocks.clear();
   }
}
