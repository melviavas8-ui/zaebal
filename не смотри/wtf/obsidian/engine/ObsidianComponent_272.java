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
























import net.minecraft.item.Item;
import net.minecraft.item.Items;
public enum ObsidianComponent_272 {
   DISORIENTATION("дезориентация", "дезориентации", Items.ENDER_EYE, "Использовал дезориентацию!", "Дезориентация не найдена!"),
   TRAP("трапка", "трапки", Items.NETHERITE_SCRAP, "Использовал трапку!", "Трапка не найдена!"),
   PLAST("пласт", "пласта", Items.DRIED_KELP, "Использовал пласт!", "Пласт не найден!"),
   DUST("явная пыль", "пыли", Items.SUGAR, "Использовал пыль!", "Пыль не найдена!"),
   FREEZE_SNOW("заморозка", "снега", Items.SNOWBALL, "Использовал снег!", "Снег не найден!"),
   AURA("божья", "ауры", Items.PHANTOM_MEMBRANE, "Использовал ауру!", "Аура не найдена!");

   public final String query;
   public final String cooldownName;
   public final Item item;
   public final String successText;
   public final String failText;

   private ObsidianComponent_272(String query, String cooldownName, Item item, String successText, String failText) {
      this.query = query;
      this.cooldownName = cooldownName;
      this.item = item;
      this.successText = successText;
      this.failText = failText;
   }
}
