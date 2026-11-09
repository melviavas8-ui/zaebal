package wtf.obsidian.manager;

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























import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public final class BotState {
   public boolean enabled;
   public String modeAlias = "normal";
   public float packetsPerSecond = 100.0f;
   public float breakRadius = 4.0f;
   private boolean swing = true;
   public boolean autoSell = true;
   public boolean autoPay;
   public boolean preserveVisuals = true;
   public float payAmount = 1000.0f;
   public float intervalSeconds = 20.0f;
   public String payTarget = "";
   public BlockPos targetPos;
   public long lastBreakTime;
   public long lastPacketTime;
   public long lastSellTime;
   public long lastPayTime;
   public long lastNickReminderTime;
   private Map<BlockPos, BlockState> preservedBlocks = new HashMap<>();
   private Map<BlockPos, Long> lastUpdateTime = new HashMap<>();
   private Set<BlockPos> managedBlocks = new HashSet<>();

   public boolean enabled() {
      return this.enabled;
   }

   public void enabled(boolean value) {
      this.enabled = value;
   }

   public String modeAlias() {
      return this.modeAlias;
   }

   public void modeAlias(String value) {
      this.modeAlias = value == null ? "normal" : value;
   }

   public float packetsPerSecond() {
      return this.packetsPerSecond;
   }

   public void packetsPerSecond(float value) {
      this.packetsPerSecond = value;
   }

   public float breakRadius() {
      return this.breakRadius;
   }

   public void breakRadius(float value) {
      this.breakRadius = value;
   }

   public boolean swing() {
      return this.swing;
   }

   public void swing(boolean value) {
      this.swing = value;
   }

   public boolean autoSell() {
      return this.autoSell;
   }

   public void autoSell(boolean value) {
      this.autoSell = value;
   }

   public boolean autoPay() {
      return this.autoPay;
   }

   public void autoPay(boolean value) {
      this.autoPay = value;
   }

   public boolean preserveVisuals() {
      return this.preserveVisuals;
   }

   public void preserveVisuals(boolean value) {
      this.preserveVisuals = value;
   }

   public float payAmount() {
      return this.payAmount;
   }

   public void payAmount(float value) {
      this.payAmount = value;
   }

   public float intervalSeconds() {
      return this.intervalSeconds;
   }

   public void intervalSeconds(float value) {
      this.intervalSeconds = value;
   }

   public String payTarget() {
      return this.payTarget == null ? "" : this.payTarget;
   }

   public void payTarget(String value) {
      this.payTarget = value == null ? "" : value;
   }

   public BlockPos targetPos() {
      return this.targetPos;
   }

   public void targetPos(BlockPos value) {
      this.targetPos = value;
   }

   public long lastBreakTime() {
      return this.lastBreakTime;
   }

   public void lastBreakTime(long value) {
      this.lastBreakTime = value;
   }

   public long lastPacketTime() {
      return this.lastPacketTime;
   }

   public void lastPacketTime(long value) {
      this.lastPacketTime = value;
   }

   public long lastSellTime() {
      return this.lastSellTime;
   }

   public void lastSellTime(long value) {
      this.lastSellTime = value;
   }

   public long lastPayTime() {
      return this.lastPayTime;
   }

   public void lastPayTime(long value) {
      this.lastPayTime = value;
   }

   public long lastNickReminderTime() {
      return this.lastNickReminderTime;
   }

   public void lastNickReminderTime(long value) {
      this.lastNickReminderTime = value;
   }

   public Map<BlockPos, BlockState> preservedBlocks() {
      return this.preservedBlocks;
   }

   public void preservedBlocks(Map<BlockPos, BlockState> value) {
      this.preservedBlocks = (Map<BlockPos, BlockState>)(value == null ? new HashMap<>() : value);
   }

   public Map<BlockPos, Long> lastUpdateTime() {
      return this.lastUpdateTime;
   }

   public void lastUpdateTime(Map<BlockPos, Long> value) {
      this.lastUpdateTime = (Map<BlockPos, Long>)(value == null ? new HashMap<>() : value);
   }

   public Set<BlockPos> managedBlocks() {
      return this.managedBlocks;
   }

   public void managedBlocks(Set<BlockPos> value) {
      this.managedBlocks = (Set<BlockPos>)(value == null ? new HashSet<>() : value);
   }
}
