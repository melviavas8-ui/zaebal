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
























import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class ObsidianComponent_332 extends Random {
   private final ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();
   public Random random = null;
   public volatile boolean seedSet;
   public volatile boolean seedUpdated;
   public volatile long seed;
   public void validateRandom() {
      if (this.random == null) {
         this.random = new Random(this.seed);
         this.seedUpdated = false;
      } else if (this.seedUpdated) {
         this.random.setSeed(this.seed);
         this.seedUpdated = false;
      }
   }

   public static long mix(long left, long right) {
      left *= left * (6364136223846793005L)
         + (1442695040888963407L);
      return left + right;
   }

   @Override
   public void setSeed(long seed) {
      this.seed = seed;
      this.seedSet = true;
      this.seedUpdated = true;
   }

   @Override
   public void nextBytes(byte[] bytes) {
      if (this.seedSet) {
         this.validateRandom();
         this.random.nextBytes(bytes);
      } else {
         this.threadLocalRandom.nextBytes(bytes);
      }
   }

   @Override
   public int nextInt() {
      if (this.seedSet) {
         this.validateRandom();
         return this.random.nextInt();
      } else {
         return this.threadLocalRandom.nextInt();
      }
   }

   @Override
   public int nextInt(int bound) {
      if (this.seedSet) {
         this.validateRandom();
         return this.random.nextInt(bound);
      } else {
         return this.threadLocalRandom.nextInt(bound);
      }
   }

   @Override
   public long nextLong() {
      if (this.seedSet) {
         this.validateRandom();
         return this.random.nextLong();
      } else {
         return this.threadLocalRandom.nextLong();
      }
   }

   @Override
   public boolean nextBoolean() {
      if (this.seedSet) {
         this.validateRandom();
         return this.random.nextBoolean();
      } else {
         return this.threadLocalRandom.nextBoolean();
      }
   }

   @Override
   public float nextFloat() {
      if (this.seedSet) {
         this.validateRandom();
         return this.random.nextFloat();
      } else {
         return this.threadLocalRandom.nextFloat();
      }
   }

   @Override
   public double nextDouble() {
      if (this.seedSet) {
         this.validateRandom();
         return this.random.nextDouble();
      } else {
         return this.threadLocalRandom.nextDouble();
      }
   }

   @Override
   public double nextGaussian() {
      if (this.seedSet) {
         this.validateRandom();
         return this.random.nextGaussian();
      } else {
         return this.threadLocalRandom.nextGaussian();
      }
   }
}
