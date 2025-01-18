package com.mentalfrostbyte.jello.gui.unmapped;

// Naming reason: `callUIHandlers` in `UIBase` calls everything in `uiHandlers` (which is a list of `UIHandler`s)
// and passes itself as the argument
public interface UIHandler {
   void handle(UIBase uiBase);
}
