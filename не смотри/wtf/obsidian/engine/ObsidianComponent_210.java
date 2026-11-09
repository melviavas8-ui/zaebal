package wtf.obsidian.engine;

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
import java.util.List;
import lombok.Generated;

public class ObsidianComponent_210 {
   private final List<String> friends = new ArrayList<>();

   public void add(String friend) {
      if (!friend.isEmpty()) {
         this.friends.add(friend);
      }
   }

   public void remove(String friend) {
      this.friends.remove(friend);
   }

   public void clear() {
      this.friends.clear();
   }

   public boolean isFriend(String friend) {
      return this.friends.contains(friend);
   }

   public boolean isEmpty() {
      return this.friends.isEmpty();
   }

   @Generated
   public List<String> getFriends() {
      return this.friends;
   }
}
