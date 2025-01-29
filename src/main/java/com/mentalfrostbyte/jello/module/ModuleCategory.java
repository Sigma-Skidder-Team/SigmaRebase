package com.mentalfrostbyte.jello.module;

public record ModuleCategory(String name) {
    public static final ModuleCategory RENDER = new ModuleCategory("Render");
    public static final ModuleCategory PLAYER = new ModuleCategory("Player");
    public static final ModuleCategory COMBAT = new ModuleCategory("Combat");
    public static final ModuleCategory WORLD = new ModuleCategory("World");
    public static final ModuleCategory MISC = new ModuleCategory("Misc");
    public static final ModuleCategory EXPLOIT = new ModuleCategory("Exploit");
    public static final ModuleCategory MOVEMENT = new ModuleCategory("Movement");
    public static final ModuleCategory GUI = new ModuleCategory("Gui");
    public static final ModuleCategory ITEM = new ModuleCategory("Item");

    @Override
    public boolean equals(Object category) {
        return category instanceof ModuleCategory && ((ModuleCategory) category).name.equals(this.name);
    }

    @Override
    public String toString() {
        return this.name;
    }
}
