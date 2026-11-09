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























import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.network.ServerInfo.ServerType;
import net.minecraft.client.session.Session;
import net.minecraft.client.session.Session.AccountType;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.c2s.common.CommonPongC2SPacket;
import net.minecraft.network.packet.c2s.common.KeepAliveC2SPacket;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket.Status;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.Full;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.OnGroundOnly;
import net.minecraft.network.packet.s2c.common.CommonPingS2CPacket;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;
import net.minecraft.network.packet.s2c.common.KeepAliveS2CPacket;
import net.minecraft.network.packet.s2c.common.ResourcePackSendS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityPositionSyncS2CPacket;
import net.minecraft.network.packet.s2c.play.HealthUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;


public final class BotManager {
   private static final List<BotConnection> connections = new CopyOnWriteArrayList<>();
   public static volatile boolean ignoreBotMessages;
   public static volatile boolean bypassResourcePacksDuringBotConnect;

   public static List<BotConnection> getConnections() {
      pruneDeadConnections();
      return new ArrayList<>(connections);
   }

   public static boolean shouldBypassResourcePacks() {
      return bypassResourcePacksDuringBotConnect;
   }

   public static void finishBotConnectStage() {
      bypassResourcePacksDuringBotConnect = false;
   }

   public static String getCurrentSessionName() {
      MinecraftClient mc = MinecraftClient.getInstance();
      return mc.getSession() == null ? "" : mc.getSession().getUsername();
   }

   public static List<String> getSessionNames(boolean includeCurrent) {
      pruneDeadConnections();
      Set<String> names = new LinkedHashSet<>();
      if (includeCurrent) {
         String currentName = getCurrentSessionName();
         if (!currentName.isBlank()) {
            names.add(currentName);
         }
      }

      for (BotConnection bot : connections) {
         if (bot.name() != null && !bot.name().isBlank()) {
            names.add(bot.name());
         }
      }

      return new ArrayList<>(names);
   }

   public static boolean toggleIgnoreBotMessages() {
      ignoreBotMessages = (!ignoreBotMessages);
      return ignoreBotMessages;
   }

   public static boolean isIgnoreBotMessages() {
      return ignoreBotMessages;
   }

   public static void connect(String name, String address) {
      MinecraftClient mc = MinecraftClient.getInstance();
      if (mc.getSession() != null && name != null && !name.isBlank() && address != null && !address.isBlank()) {
         Session originalSession = mc.getSession();
         ServerInfo originalServerInfo = mc.getCurrentServerEntry();
         pruneDeadConnections();
         disconnectSessionsByName(name, Text.literal("Replaced"));
         BotConnection previous = freezeCurrentSession();
         ModuleManager.autoForest.resetToDefaults();
         ((IMinecraftClientAccessor)mc).setSession(createSessionWithName(mc.getSession(), name));
         bypassResourcePacksDuringBotConnect = true;
         mc.execute(
            () -> {
               try {
                  ConnectScreen.connect(
                     new MultiplayerScreen(new TitleScreen()),
                     mc,
                     ServerAddress.parse(address),
                     new ServerInfo(address, address, ServerType.OTHER),
                     false,
                     null
                  );
               } catch (Exception var6) {
                  bypassResourcePacksDuringBotConnect = false;
                  restoreAfterConnectFailure(mc, previous, originalSession, originalServerInfo);
               }
            }
         );
      }
   }

   public static void pulseBots(boolean rightClick) {
      for (BotConnection bot : connections) {
         if (isConnectionUsable(bot)) {
            if (rightClick) {
               bot.handler()
                  .sendPacket(
                     new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, 0, bot.player().getYaw(), bot.player().getPitch())
                  );
            } else {
               bot.handler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            }
         }
      }
   }

   public static void sayAll(String message) {
      for (BotConnection bot : connections) {
         if (isConnectionUsable(bot)) {
            if (message.startsWith("/")) {
               bot.handler().sendChatCommand(message.substring(1));
            } else {
               bot.handler().sendChatMessage(message);
            }
         }
      }
   }

   public static boolean control(String name) {
      if (name != null && !name.isBlank()) {
         pruneDeadConnections();
         MinecraftClient mc = MinecraftClient.getInstance();
         return (mc.player != null && mc.world != null && name.equalsIgnoreCase(getCurrentSessionName())
            ? true : connections.stream().filter(bot -> matchesName(bot.name(), name)).findFirst().map(bot -> {
               if (!isConnectionUsable(bot)) {
                  connections.remove(bot);
                  return Boolean.valueOf(false);
               } else {
                  BotConnection previous = freezeCurrentSession();
                  if (!activateSession(bot)) {
                     if (previous != null && activateSession(previous)) {
                        connections.remove(previous);
                     }

                     return Boolean.valueOf(false);
                  } else {
                     connections.remove(bot);
                     return Boolean.valueOf(true);
                  }
               }
            }).orElse(Boolean.valueOf(false)));
      } else {
         return false;
      }
   }

   public static boolean say(String name, String message) {
      pruneDeadConnections();
      return connections.stream().filter(bot -> matchesName(bot.name(), name)).findFirst().map(bot -> {
         if (!isConnectionUsable(bot)) {
            connections.remove(bot);
            return Boolean.valueOf(false);
         } else {
            if (message.startsWith("/")) {
               bot.handler().sendChatCommand(message.substring(1));
            } else {
               bot.handler().sendChatMessage(message);
            }

            return Boolean.valueOf(true);
         }
      }).orElse(Boolean.valueOf(false));
   }

   public static boolean remove(String name) {
      if (name != null && !name.isBlank()) {
         return (disconnectSessionsByName(name, Text.literal("Removed")) > 0);
      } else {
         return false;
      }
   }

   public static boolean restore() {
      return restore(null);
   }

   public static boolean restore(String name) {
      pruneDeadConnections();
      String targetName = name != null && !name.isBlank()
         ? name
         : (connections.isEmpty() ? "" : connections.get(connections.size() - (1)).name());
      return (!targetName.isBlank() && control(targetName));
   }
   public static BotConnection freezeCurrentSession() {
      MinecraftClient mc = MinecraftClient.getInstance();
      if (mc.getNetworkHandler() != null && mc.world != null && mc.player != null) {
         ClientPlayNetworkHandler handler = mc.getNetworkHandler();
         makeNettyBot(handler, mc.getSession().getUsername(), mc.player);
         BotConnection connection = new BotConnection(
            mc.getSession().getUsername(),
            mc.getCurrentServerEntry() != null ? mc.getCurrentServerEntry().address : "",
            handler.getConnection(),
            handler,
            mc.world,
            mc.player,
            mc.interactionManager,
            mc.getSession(),
            mc.getCurrentServerEntry(),
            ModuleManager.autoForest.captureState()
         );
         replaceConnection(connection);
         clearActiveSession(mc);
         return connection;
      } else {
         return null;
      }
   }
   public static boolean activateSession(BotConnection bot) {
      if (!isConnectionUsable(bot)) {
         return false;
      } else {
         MinecraftClient mc = MinecraftClient.getInstance();
         IMinecraftClientAccessor accessor = (IMinecraftClientAccessor)mc;
         Channel channel = getChannel(bot.connection());
         if (channel != null && channel.pipeline().get("bot_filter") != null) {
            channel.pipeline().remove("bot_filter");
         }

         try {
            setMinecraftClientField(mc, ClientPlayNetworkHandler.class, bot.handler());
            accessor.setSession(bot.session() != null ? bot.session() : createSessionWithName(mc.getSession(), bot.name()));
            setMinecraftClientField(mc, ServerInfo.class, bot.serverInfo() != null ? bot.serverInfo() : createServerInfo(bot.name(), bot.address()));
            accessor.setItemUseCooldown(0);
            mc.world = bot.world();
            mc.player = bot.player();
            mc.cameraEntity = bot.player();
            mc.interactionManager = bot.interactionManager();
            if (mc.worldRenderer != null) {
               mc.worldRenderer.setWorld(bot.world());
            }

            ModuleManager.autoForest.applyState(bot.autoForestState());
            bot.handler()
               .sendPacket(
                  new Full(
                     bot.player().getX(),
                     bot.player().getY(),
                     bot.player().getZ(),
                     bot.player().getYaw(),
                     bot.player().getPitch(),
                     bot.player().isOnGround(),
                     bot.player().horizontalCollision
                  )
               );
            mc.setScreen(null);
            return true;
         } catch (Exception var5) {
            return false;
         }
      }
   }
   public static void clearActiveSession(MinecraftClient mc) {
      IMinecraftClientAccessor accessor = (IMinecraftClientAccessor)mc;
      setMinecraftClientField(mc, ClientPlayNetworkHandler.class, null);
      accessor.setItemUseCooldown(0);
      mc.world = null;
      mc.player = null;
      mc.cameraEntity = null;
      mc.interactionManager = null;
      if (mc.worldRenderer != null) {
         mc.worldRenderer.setWorld(null);
      }
   }
   public static void replaceConnection(BotConnection connection) {
      disconnectSessionsByName(connection.name(), Text.literal("Replaced"));
      connections.add(connection);
   }
   public static void makeNettyBot(ClientPlayNetworkHandler handler, String name, ClientPlayerEntity botPlayer) {
      Channel channel = getChannel(handler.getConnection());
      if (channel != null) {
         if (channel.pipeline().get("bot_filter") != null) {
            channel.pipeline().remove("bot_filter");
         }

         if (channel.pipeline().get("packet_handler") != null) {
            channel.pipeline()
               .addBefore(
                  "packet_handler",
                  "bot_filter",
                  new ChannelDuplexHandler() {
                     public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                        if (msg instanceof KeepAliveS2CPacket packet) {
                           handler.getConnection().send(new KeepAliveC2SPacket(packet.getId()));
                           if (botPlayer != null) {
                              handler.sendPacket(new OnGroundOnly(botPlayer.isOnGround(), botPlayer.horizontalCollision));
                           }

                           ReferenceCountUtil.release(msg);
                        } else if (msg instanceof CommonPingS2CPacket packet) {
                           handler.getConnection().send(new CommonPongC2SPacket(packet.getParameter()));
                           ReferenceCountUtil.release(msg);
                        } else if (msg instanceof ResourcePackSendS2CPacket packet) {
                           handler.sendPacket(new ResourcePackStatusC2SPacket(packet.id(), Status.ACCEPTED));
                           handler.sendPacket(new ResourcePackStatusC2SPacket(packet.id(), Status.SUCCESSFULLY_LOADED));
                           ReferenceCountUtil.release(msg);
                        } else if (msg instanceof PlayerPositionLookS2CPacket packet) {
                           BotManager.applyFrozenPositionLook(botPlayer, packet);
                           handler.sendPacket(new TeleportConfirmC2SPacket(packet.teleportId()));
                           if (botPlayer != null) {
                              handler.sendPacket(
                                 new Full(
                                    botPlayer.getX(),
                                    botPlayer.getY(),
                                    botPlayer.getZ(),
                                    botPlayer.getYaw(),
                                    botPlayer.getPitch(),
                                    botPlayer.isOnGround(),
                                    botPlayer.horizontalCollision
                                 )
                              );
                           }

                           ReferenceCountUtil.release(msg);
                        } else if (msg instanceof EntityPositionSyncS2CPacket packet) {
                           BotManager.applyFrozenEntityPositionSync(botPlayer, packet);
                           ReferenceCountUtil.release(msg);
                        } else if (msg instanceof HealthUpdateS2CPacket packet) {
                           if (botPlayer != null) {
                              botPlayer.setHealth(packet.getHealth());
                           }

                           ReferenceCountUtil.release(msg);
                        } else if (msg instanceof DisconnectS2CPacket) {
                           BotManager.connections.removeIf(bot -> BotManager.matchesName(bot.name(), name));
                           ctx.close();
                           ReferenceCountUtil.release(msg);
                        } else {
                           String packetName = msg.getClass().getSimpleName();
                           if (!packetName.contains("Sound")
                              && !packetName.contains("Particle")
                              && !packetName.contains("Screen")
                              && (!BotManager.ignoreBotMessages || !BotManager.isBotMessagePacket(packetName))
                              && !packetName.contains("Explosion")
                              && !packetName.contains("BossBar")
                              && !packetName.contains("Scoreboard")
                              && !packetName.contains("OverlayMessage")) {
                              super.channelRead(ctx, msg);
                           } else {
                              ReferenceCountUtil.release(msg);
                           }
                        }
                     }

                     public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                        BotManager.connections.removeIf(bot -> BotManager.matchesName(bot.name(), name));
                        super.channelInactive(ctx);
                     }

                     public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                        BotManager.connections.removeIf(bot -> BotManager.matchesName(bot.name(), name));
                        ctx.close();
                     }
                  }
               );
         }
      }
   }
   public static boolean isBotMessagePacket(String packetName) {
      return (!(!packetName.contains("Chat") && !packetName.contains("Message") && !packetName.contains("Title") && !packetName.contains("Overlay")));
   }
   public static Channel getChannel(ClientConnection connection) {
      try {
         Field[] var1 = ClientConnection.class.getDeclaredFields();
         int var2 = var1.length;

         for (int var3 = 0; var3 < var2; var3++) {
            Field field = var1[var3];
            if (Channel.class.isAssignableFrom(field.getType())) {
               field.setAccessible(true);
               return (Channel)field.get(connection);
            }
         }
      } catch (Exception var5) {
      }

      return null;
   }
   public static void applyFrozenPositionLook(ClientPlayerEntity botPlayer, PlayerPositionLookS2CPacket packet) {
      if (botPlayer != null && packet != null) {
         double x = readPacketDouble(packet, "x", botPlayer.getX());
         double y = readPacketDouble(packet, "y", botPlayer.getY());
         double z = readPacketDouble(packet, "z", botPlayer.getZ());
         float yaw = (float)readPacketDouble(packet, "yaw", (double)botPlayer.getYaw());
         float pitch = (float)readPacketDouble(packet, "pitch", (double)botPlayer.getPitch());
         Object change = readPacketComponent(packet, "change");
         if (change == null) {
            change = readPacketComponent(packet, "flags");
         }

         if (hasRelativeFlag(change, "X")) {
            x += botPlayer.getX();
         }

         if (hasRelativeFlag(change, "Y")) {
            y += botPlayer.getY();
         }

         if (hasRelativeFlag(change, "Z")) {
            z += botPlayer.getZ();
         }

         if (hasRelativeFlag(change, "Y_ROT")) {
            yaw += botPlayer.getYaw();
         }

         if (hasRelativeFlag(change, "X_ROT")) {
            pitch += botPlayer.getPitch();
         }

         pitch = MathHelper.clamp(pitch, -90.0f, 90.0f);
         botPlayer.refreshPositionAndAngles(x, y, z, yaw, pitch);
         botPlayer.setYaw(yaw);
         botPlayer.setPitch(pitch);
      }
   }
   public static void applyFrozenEntityPositionSync(ClientPlayerEntity botPlayer, EntityPositionSyncS2CPacket packet) {
      if (botPlayer != null && packet != null && packet.id() == botPlayer.getId() && packet.values() != null) {
         Vec3d position = packet.values().position();
         if (position != null) {
            float yaw = packet.values().yaw();
            float pitch = MathHelper.clamp(
               packet.values().pitch(),
               -90.0f,
               90.0f
            );
            botPlayer.refreshPositionAndAngles(position.x, position.y, position.z, yaw, pitch);
            botPlayer.setYaw(yaw);
            botPlayer.setPitch(pitch);
            if (packet.values().deltaMovement() != null) {
               botPlayer.setVelocity(packet.values().deltaMovement());
            }

            botPlayer.setOnGround(packet.onGround());
         }
      }
   }
   public static double readPacketDouble(Object packet, String name, double fallback) {
      return readPacketComponent(packet, name) instanceof Number number ? number.doubleValue() : fallback;
   }
   public static Object readPacketComponent(Object packet, String name) {
      if (packet != null && name != null && !name.isBlank()) {
         try {
            Method method = packet.getClass().getMethod(name);
            method.setAccessible(true);
            return method.invoke(packet);
         } catch (Exception var10) {
            try {
               Method methodx = packet.getClass()
                  .getMethod(
                     "get"
                        + Character.toUpperCase(name.charAt(0))
                        + name.substring(1)
                  );
               methodx.setAccessible(true);
               return methodx.invoke(packet);
            } catch (Exception var9) {
               try {
                  RecordComponent[] components = packet.getClass().getRecordComponents();
                  if (components != null) {
                     RecordComponent[] var3 = components;
                     int var4 = components.length;

                     for (int field = 0; field < var4; field++) {
                        RecordComponent component = var3[field];
                        if (name.equals(component.getName())) {
                           return component.getAccessor().invoke(packet);
                        }
                     }
                  }
               } catch (Exception var8) {
               }

               try {
                  Field[] var11 = packet.getClass().getDeclaredFields();
                  int var14 = var11.length;

                  for (int var15 = 0; var15 < var14; var15++) {
                     Field fieldx = var11[var15];
                     if (name.equalsIgnoreCase(fieldx.getName())) {
                        fieldx.setAccessible(true);
                        return fieldx.get(packet);
                     }
                  }
               } catch (Exception var7) {
               }

               return null;
            }
         }
      } else {
         return null;
      }
   }
   public static boolean hasRelativeFlag(Object flags, String flagName) {
      if (flags instanceof Iterable<?> iterable && flagName != null) {
         for (Object flag : iterable) {
            if (flag instanceof Enum<?> enumFlag && flagName.equals(enumFlag.name())) {
               return true;
            }
         }

         return false;
      }

      return false;
   }
   public static Session createSessionWithName(Session current, String name) {
      try {
         Class[] var10001 = new Class[6];
         var10001[0] = String.class;
         var10001[1] = UUID.class;
         var10001[2] = String.class;
         var10001[3] = Optional.class;
         var10001[4] = Optional.class;
         var10001[5] = AccountType.class;
         Constructor<Session> constructor = Session.class.getDeclaredConstructor(var10001);
         constructor.setAccessible(true);
         Object[] var4 = new Object[6];
         var4[0] = name;
         var4[1] = UUID.randomUUID();
         var4[2] = current == null ? "" : current.getAccessToken();
         var4[3] = Optional.empty();
         var4[4] = Optional.empty();
         var4[5] = AccountType.MOJANG;
         return constructor.newInstance(var4);
      } catch (Exception var3) {
         throw new RuntimeException(var3);
      }
   }
   public static void setMinecraftClientField(MinecraftClient mc, Class<?> fieldType, Object value) {
      try {
         Field[] var3 = MinecraftClient.class.getDeclaredFields();
         int var4 = var3.length;

         for (int var5 = 0; var5 < var4; var5++) {
            Field field = var3[var5];
            if (field.getType() == fieldType) {
               field.setAccessible(true);
               field.set(mc, value);
               return;
            }
         }
      } catch (Exception var7) {
      }
   }
   public static ServerInfo createServerInfo(String name, String address) {
      String safeAddress = address == null ? "" : address;
      String safeName = name != null && !name.isBlank() ? name : safeAddress;
      return new ServerInfo(safeName, safeAddress, ServerType.OTHER);
   }
   public static void restoreAfterConnectFailure(MinecraftClient mc, BotConnection previous, Session originalSession, ServerInfo originalServerInfo) {
      try {
         bypassResourcePacksDuringBotConnect = false;
         if (previous != null && activateSession(previous)) {
            connections.remove(previous);
            return;
         }

         IMinecraftClientAccessor accessor = (IMinecraftClientAccessor)mc;
         accessor.setSession(originalSession);
         setMinecraftClientField(mc, ServerInfo.class, originalServerInfo);
      } catch (Exception var5) {
      }
   }
   public static int disconnectSessionsByName(String name, Text reason) {
      if (name != null && !name.isBlank()) {
         int removed = 0;

         for (BotConnection bot : new ArrayList<>(connections)) {
            if (matchesName(bot.name(), name)) {
               connections.remove(bot);
               removed++;

               try {
                  if (bot.connection() != null) {
                     bot.connection().disconnect(reason);
                  }
               } catch (Exception var6) {
               }
            }
         }

         return removed;
      } else {
         return 0;
      }
   }
   public static void pruneDeadConnections() {
      connections.removeIf(bot -> (!isConnectionUsable(bot)));
   }
   public static boolean isConnectionUsable(BotConnection bot) {
      if (bot == null || bot.name() == null || bot.name().isBlank()) {
         return false;
      } else if (bot.connection() != null && bot.handler() != null && bot.world() != null && bot.player() != null) {
         if (bot.player().networkHandler != bot.handler()) {
            return false;
         } else {
            Channel channel = getChannel(bot.connection());
            return (!(channel != null && !channel.isOpen()));
         }
      } else {
         return false;
      }
   }
   public static boolean matchesName(String left, String right) {
      return (left != null && right != null && left.equalsIgnoreCase(right));
   }
}
