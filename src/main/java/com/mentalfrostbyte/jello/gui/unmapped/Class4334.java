package com.mentalfrostbyte.jello.gui.unmapped;

import com.mentalfrostbyte.jello.gui.base.CustomGuiScreen;
import com.mentalfrostbyte.jello.gui.impl.ClassicScreenk;
import com.mentalfrostbyte.jello.module.ModuleCategory;
import com.mentalfrostbyte.jello.util.render.Resources;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.List;

public class Class4334 extends Class4333 {
   private List<ButtonPanel> field21150 = new ArrayList<ButtonPanel>();
   public Class4255 field21152;
   public Class4255 field21153;
   public Class4255 field21154;
   public Class4255 field21155;
   public Class4255 field21156;
   public Class4255 field21157;

   public Class4334(CustomGuiScreen var1, String var2, int var3, int var4) {
      super(var1, var2, var3 - 198, var4 - 298, 396, 596);
      this.addToList(this.field21152 = new Class4255(this, "combat", 24, 58, 170, 130, "Combat", Resources.combat,
            Resources.combat2));
      this.addToList(this.field21153 = new Class4255(this, "movement", 24, 208, 170, 130, "Movement",
            Resources.movement, Resources.movement2));
      this.addToList(this.field21157 = new Class4255(this, "world", 24, 358, 170, 130, "World", Resources.world,
            Resources.world2));
      this.addToList(this.field21155 = new Class4255(this, "player", 201, 58, 170, 130, "Player", Resources.player,
            Resources.player2));
      this.addToList(this.field21156 = new Class4255(this, "visuals", 201, 208, 170, 130, "Visuals", Resources.visuals,
            Resources.visuals2));
      this.addToList(this.field21154 = new Class4255(this, "others", 201, 358, 170, 130, "Others", Resources.others,
            Resources.others2));
      Class4361 var7;
      this.addToList(var7 = new Class4361(this, "exit", this.getWidthA() - 41, 9));
      var7.doThis((var0, var1x) -> MinecraftClient.getInstance().displayGuiScreen(null));
      ClassicScreenk var8 = (ClassicScreenk) this.getParent();
      this.field21152.doThis((var1x, var2x) -> var8.method13418("Combat", ModuleCategory.COMBAT));
      this.field21153.doThis((var1x, var2x) -> var8.method13418("Movement", ModuleCategory.MOVEMENT));
      this.field21157.doThis((var1x, var2x) -> var8.method13418("World", ModuleCategory.WORLD));
      this.field21155.doThis((var1x, var2x) -> var8.method13418("Player", ModuleCategory.PLAYER));
      this.field21156.doThis((var1x, var2x) -> var8.method13418("Visuals", ModuleCategory.RENDER, ModuleCategory.GUI));
      this.field21154.doThis((var1x, var2x) -> var8.method13418("Others", ModuleCategory.MISC));
      this.method13300(false);
   }

   @Override
   public void updatePanelDimensions(int newHeight, int newWidth) {
      super.updatePanelDimensions(newHeight, newWidth);
   }
}
