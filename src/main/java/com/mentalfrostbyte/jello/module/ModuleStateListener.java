package com.mentalfrostbyte.jello.module;

public interface ModuleStateListener {
   void onModuleEnabled(ModuleWithModuleSettings parent, Module module, boolean enabled);
}
