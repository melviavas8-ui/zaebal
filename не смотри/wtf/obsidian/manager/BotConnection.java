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























import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.session.Session;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.ClientConnection;

public final class BotConnection {
   public final String name;
   public final String address;
   public final ClientConnection connection;
   public final ClientPlayNetworkHandler handler;
   public final ClientWorld world;
   public final ClientPlayerEntity player;
   public final ClientPlayerInteractionManager interactionManager;
   public final Session session;
   public final ServerInfo serverInfo;
   public final BotState autoForestState;

   public BotConnection(
      String name,
      String address,
      ClientConnection connection,
      ClientPlayNetworkHandler handler,
      ClientWorld world,
      ClientPlayerEntity player,
      ClientPlayerInteractionManager interactionManager,
      Session session,
      ServerInfo serverInfo,
      BotState autoForestState
   ) {
      this.name = name;
      this.address = address;
      this.connection = connection;
      this.handler = handler;
      this.world = world;
      this.player = player;
      this.interactionManager = interactionManager;
      this.session = session;
      this.serverInfo = serverInfo;
      this.autoForestState = autoForestState;
   }

   public String name() {
      return this.name;
   }

   public String address() {
      return this.address;
   }

   public ClientConnection connection() {
      return this.connection;
   }

   public ClientPlayNetworkHandler handler() {
      return this.handler;
   }

   public ClientWorld world() {
      return this.world;
   }

   public ClientPlayerEntity player() {
      return this.player;
   }

   public ClientPlayerInteractionManager interactionManager() {
      return this.interactionManager;
   }

   public Session session() {
      return this.session;
   }

   public ServerInfo serverInfo() {
      return this.serverInfo;
   }

   public BotState autoForestState() {
      return this.autoForestState;
   }
}
