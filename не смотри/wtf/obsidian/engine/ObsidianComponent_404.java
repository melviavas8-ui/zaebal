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
























import java.util.HashMap;
import java.util.Map;

public class ObsidianComponent_404 {
   private static final Map<Integer, String> REPLACEMENTS = new HashMap<>();
   private static final Map<Integer, Integer> RANK_COLORS = new HashMap<>();
   public static final int[] RANKS;

   public static String replaceCodePoint(int codePoint) {
      return REPLACEMENTS.get(codePoint);
   }

   public static int getGradientColorForReplacement(int codePoint, int charIndex, int totalChars, float alpha, int currentColor) {
      if (isRank(codePoint)) {
         Integer baseColor = RANK_COLORS.get(codePoint);
         if (baseColor == null) {
            return withOpacity(currentColor, alpha);
         } else {
            int endColor = ObsidianComponent_olorUtil.darken(baseColor, 0.800000011920929f);
            float ratio = totalChars <= 1
               ? 1.0f
               : (float)charIndex / (float)(totalChars - 1);
            int interpolatedColor = ObsidianComponent_olorUtil.interpolateColor(endColor, baseColor, ratio);
            return withOpacity(interpolatedColor, alpha);
         }
      } else {
         return withOpacity(currentColor, alpha);
      }
   }
   public static boolean isRank(int codePoint) {
      int[] var1 = RANKS;
      int var2 = var1.length;

      for (int var3 = 0; var3 < var2; var3++) {
         int rank = var1[var3];
         if (rank == codePoint) {
            return true;
         }
      }

      return false;
   }
   public static int withOpacity(int color, float alpha) {
      int a = Math.max(
         0,
         Math.min(255, (int)(alpha * 255.0f))
      );
      return ObsidianComponent_olorUtil.setAlphaColor(color, a);
   }

   static {
      int[] var10000 = new int[50];
      var10000[0] = 42240;
      var10000[1] = 42244;
      var10000[2] = 42248;
      var10000[3] = 42258;
      var10000[4] = 42262;
      var10000[5] = 42272;
      var10000[6] = 42276;
      var10000[7] = 42280;
      var10000[8] = 42336;
      var10000[9] = 42290;
      var10000[10] = 42294;
      var10000[11] = 42308;
      var10000[12] = 42326;
      var10000[13] = 42312;
      var10000[14] = 42304;
      var10000[15] = 42322;
      var10000[16] = 42249;
      var10000[17] = 42259;
      var10000[18] = 42263;
      var10000[19] = 42273;
      var10000[20] = 42277;
      var10000[21] = 42281;
      var10000[22] = 42291;
      var10000[23] = 42295;
      var10000[24] = 42241;
      var10000[25] = 42245;
      var10000[26] = 42313;
      var10000[27] = 4144;
      var10000[28] = 4138;
      var10000[29] = 4132;
      var10000[30] = 4134;
      var10000[31] = 4140;
      var10000[32] = 4139;
      var10000[33] = 4151;
      var10000[34] = 4130;
      var10000[35] = 4148;
      var10000[36] = 4141;
      var10000[37] = 4152;
      var10000[38] = 4133;
      var10000[39] = 4131;
      var10000[40] = 4149;
      var10000[41] = 4150;
      var10000[42] = 4137;
      var10000[43] = 4129;
      var10000[44] = 4145;
      var10000[45] = 4143;
      var10000[46] = 4146;
      var10000[47] = 4135;
      var10000[48] = 4153;
      var10000[49] = 4126;
      RANKS = var10000;
      REPLACEMENTS.put(9889, "");
      REPLACEMENTS.put(9733, "");
      REPLACEMENTS.put(42240, "PLAYER");
      REPLACEMENTS.put(42244, "HERO");
      REPLACEMENTS.put(42248, "TITAN");
      REPLACEMENTS.put(42258, "AVENGER");
      REPLACEMENTS.put(42262, "OVERLORD");
      REPLACEMENTS.put(42272, "MAGISTER");
      REPLACEMENTS.put(42276, "IMPERATOR");
      REPLACEMENTS.put(42280, "DRAGON");
      REPLACEMENTS.put(42336, "D.HELPER");
      REPLACEMENTS.put(42290, "BULL");
      REPLACEMENTS.put(42294, "TIGER");
      REPLACEMENTS.put(42308, "VAMPIRE");
      REPLACEMENTS.put(42326, "BUNNY");
      REPLACEMENTS.put(42312, "COBRA");
      REPLACEMENTS.put(42304, "HYDRA");
      REPLACEMENTS.put(42322, "RABBIT");
      REPLACEMENTS.put(42249, "HELPER");
      REPLACEMENTS.put(42259, "ML.MODER");
      REPLACEMENTS.put(42263, "MODER");
      REPLACEMENTS.put(42273, "MODER+");
      REPLACEMENTS.put(42277, "ST.MODER");
      REPLACEMENTS.put(42281, "GL.MODER");
      REPLACEMENTS.put(42291, "ML.ADMIN");
      REPLACEMENTS.put(42295, "ADMIN");
      REPLACEMENTS.put(42241, "MEDIA");
      REPLACEMENTS.put(42245, "YT");
      REPLACEMENTS.put(42305, "GOD");
      REPLACEMENTS.put(4144, "HERO");
      REPLACEMENTS.put(4138, "TITAN");
      REPLACEMENTS.put(4132, "PRINCE");
      REPLACEMENTS.put(4134, "PHOENIX");
      REPLACEMENTS.put(4140, "OVERLORD");
      REPLACEMENTS.put(4139, "GUARDIAN");
      REPLACEMENTS.put(4151, "KRATOS");
      REPLACEMENTS.put(4130, "PHANTOM");
      REPLACEMENTS.put(4148, "CUSTOM");
      REPLACEMENTS.put(4141, "WINTER");
      REPLACEMENTS.put(4152, "SAKURA");
      REPLACEMENTS.put(4133, "SUMMER");
      REPLACEMENTS.put(4131, "HALLOWEEN");
      REPLACEMENTS.put(4149, "TIKTOK");
      REPLACEMENTS.put(4150, "TIKTOK+");
      REPLACEMENTS.put(4137, "MEDIA");
      REPLACEMENTS.put(4129, "YOUTUBE");
      REPLACEMENTS.put(4145, "HELPER");
      REPLACEMENTS.put(4143, "ML.ADMIN");
      REPLACEMENTS.put(4146, "MODER");
      REPLACEMENTS.put(4135, "CURATOR");
      REPLACEMENTS.put(4153, "SPECTATOR");
      REPLACEMENTS.put(4126, "DEVELOPER");
      REPLACEMENTS.put(7424, "A");
      REPLACEMENTS.put(665, "B");
      REPLACEMENTS.put(7428, "C");
      REPLACEMENTS.put(7429, "D");
      REPLACEMENTS.put(7431, "E");
      REPLACEMENTS.put(42800, "F");
      REPLACEMENTS.put(610, "G");
      REPLACEMENTS.put(668, "H");
      REPLACEMENTS.put(618, "I");
      REPLACEMENTS.put(7434, "J");
      REPLACEMENTS.put(7435, "K");
      REPLACEMENTS.put(671, "L");
      REPLACEMENTS.put(7437, "M");
      REPLACEMENTS.put(628, "N");
      REPLACEMENTS.put(7439, "O");
      REPLACEMENTS.put(7448, "P");
      REPLACEMENTS.put(491, "Q");
      REPLACEMENTS.put(640, "R");
      REPLACEMENTS.put(7451, "T");
      REPLACEMENTS.put(7452, "U");
      REPLACEMENTS.put(42801, "S");
      REPLACEMENTS.put(7456, "V");
      REPLACEMENTS.put(7457, "W");
      REPLACEMENTS.put(7521, "X");
      REPLACEMENTS.put(655, "Y");
      REPLACEMENTS.put(7458, "Z");
      RANK_COLORS.put(
         42240,
         ObsidianComponent_olorUtil.rgb(141, 143, 141)
      );
      RANK_COLORS.put(
         42244, ObsidianComponent_olorUtil.rgb(100, 113, 251)
      );
      RANK_COLORS.put(
         42248, ObsidianComponent_olorUtil.rgb(245, 220, 29)
      );
      RANK_COLORS.put(
         42258,
         ObsidianComponent_olorUtil.rgb(79, 201, 83)
      );
      RANK_COLORS.put(
         42262,
         ObsidianComponent_olorUtil.rgb(85, 255, 255)
      );
      RANK_COLORS.put(
         42272, ObsidianComponent_olorUtil.rgb(224, 138, 52)
      );
      RANK_COLORS.put(
         42276,
         ObsidianComponent_olorUtil.rgb(202, 60, 60)
      );
      RANK_COLORS.put(
         42280,
         ObsidianComponent_olorUtil.rgb(245, 51, 238)
      );
      RANK_COLORS.put(42336, ObsidianComponent_olorUtil.rgb(214, 200, 42));
      RANK_COLORS.put(
         42290,
         ObsidianComponent_olorUtil.rgb(121, 81, 202)
      );
      RANK_COLORS.put(
         42294,
         ObsidianComponent_olorUtil.rgb(202, 130, 60)
      );
      RANK_COLORS.put(
         42308, ObsidianComponent_olorUtil.rgb(202, 60, 60)
      );
      RANK_COLORS.put(
         42326,
         ObsidianComponent_olorUtil.rgb(68, 65, 66)
      );
      RANK_COLORS.put(42312, ObsidianComponent_olorUtil.rgb(127, 214, 86));
      RANK_COLORS.put(
         42304,
         ObsidianComponent_olorUtil.rgb(92, 120, 7)
      );
      RANK_COLORS.put(42322, ObsidianComponent_olorUtil.rgb(230, 232, 230));
      RANK_COLORS.put(
         42249,
         ObsidianComponent_olorUtil.rgb(214, 200, 42)
      );
      RANK_COLORS.put(
         42259,
         ObsidianComponent_olorUtil.rgb(100, 113, 251)
      );
      RANK_COLORS.put(
         42263,
         ObsidianComponent_olorUtil.rgb(100, 113, 251)
      );
      RANK_COLORS.put(42273, ObsidianComponent_olorUtil.rgb(121, 81, 202));
      RANK_COLORS.put(
         42277,
         ObsidianComponent_olorUtil.rgb(100, 113, 251)
      );
      RANK_COLORS.put(
         42281, ObsidianComponent_olorUtil.rgb(121, 81, 202)
      );
      RANK_COLORS.put(
         42291, ObsidianComponent_olorUtil.rgb(64, 151, 214)
      );
      RANK_COLORS.put(42295, ObsidianComponent_olorUtil.rgb(202, 60, 60));
      RANK_COLORS.put(
         42241,
         ObsidianComponent_olorUtil.rgb(121, 81, 202)
      );
      RANK_COLORS.put(
         42245,
         ObsidianComponent_olorUtil.rgb(255, 255, 255)
      );
      RANK_COLORS.put(
         42305, ObsidianComponent_olorUtil.rgb(245, 198, 29)
      );
      RANK_COLORS.put(
         42313,
         ObsidianComponent_olorUtil.rgb(202, 130, 60)
      );
      RANK_COLORS.put(4144, ObsidianComponent_olorUtil.rgb(13, 176, 209));
      RANK_COLORS.put(
         4138, ObsidianComponent_olorUtil.rgb(21, 232, 24)
      );
      RANK_COLORS.put(
         4132,
         ObsidianComponent_olorUtil.rgb(232, 169, 21)
      );
      RANK_COLORS.put(4134, ObsidianComponent_olorUtil.rgb(237, 215, 19));
      RANK_COLORS.put(
         4140,
         ObsidianComponent_olorUtil.rgb(64, 163, 152)
      );
      RANK_COLORS.put(
         4139,
         ObsidianComponent_olorUtil.rgb(86, 196, 99)
      );
      RANK_COLORS.put(
         4151,
         ObsidianComponent_olorUtil.rgb(147, 46, 230)
      );
      RANK_COLORS.put(
         4130,
         ObsidianComponent_olorUtil.rgb(230, 46, 46)
      );
      RANK_COLORS.put(
         4148,
         ObsidianComponent_olorUtil.rgb(16, 35, 179)
      );
      RANK_COLORS.put(
         4141,
         ObsidianComponent_olorUtil.rgb(55, 154, 184)
      );
      RANK_COLORS.put(4152, ObsidianComponent_olorUtil.rgb(184, 39, 159));
      RANK_COLORS.put(4133, ObsidianComponent_olorUtil.rgb(255, 182, 56));
      RANK_COLORS.put(
         4131,
         ObsidianComponent_olorUtil.rgb(232, 60, 30)
      );
      RANK_COLORS.put(
         4149,
         ObsidianComponent_olorUtil.rgb(0, 0, 0)
      );
      RANK_COLORS.put(4150, ObsidianComponent_olorUtil.rgb(0, 0, 0));
      RANK_COLORS.put(
         4137,
         ObsidianComponent_olorUtil.rgb(37, 232, 30)
      );
      RANK_COLORS.put(
         4129, ObsidianComponent_olorUtil.rgb(232, 30, 30)
      );
      RANK_COLORS.put(
         4145, ObsidianComponent_olorUtil.rgb(30, 134, 232)
      );
      RANK_COLORS.put(4143, ObsidianComponent_olorUtil.rgb(89, 167, 227));
      RANK_COLORS.put(
         4146,
         ObsidianComponent_olorUtil.rgb(62, 137, 194)
      );
      RANK_COLORS.put(
         4135,
         ObsidianComponent_olorUtil.rgb(56, 235, 74)
      );
      RANK_COLORS.put(
         4153, ObsidianComponent_olorUtil.rgb(173, 184, 174)
      );
      RANK_COLORS.put(4126, ObsidianComponent_olorUtil.rgb(255, 0, 25));
   }
}
