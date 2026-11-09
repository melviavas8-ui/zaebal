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
























public class ObsidianComponent_312 {
   public final String username;
   public final int uid;
   public final String role;
   public final String hwid;
   public final String expireDate;

   public ObsidianComponent_312(String username, int uid, String role, String hwid, String expireDate) {
      this.username = username;
      this.uid = uid;
      this.role = role;
      this.hwid = hwid;
      this.expireDate = expireDate;
   }

   public static ObsidianComponent_312 empty() {
      return new ObsidianComponent_312("Unknown", 0, "Unknown", "", "");
   }

   public String getUsername() {
      return this.username;
   }

   public int getUid() {
      return this.uid;
   }

   public String getRole() {
      return this.role;
   }

   public String getHwid() {
      return this.hwid;
   }

   public String getExpireDate() {
      return this.expireDate;
   }
}
