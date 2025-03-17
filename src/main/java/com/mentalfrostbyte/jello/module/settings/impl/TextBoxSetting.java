package com.mentalfrostbyte.jello.module.settings.impl;

import com.google.gson.JsonObject;
import com.mentalfrostbyte.jello.module.settings.Setting;
import com.mentalfrostbyte.jello.module.settings.SettingType;

public class TextBoxSetting extends Setting<Integer> {
    private final String[] options;

    public TextBoxSetting(String name, String description, Integer defaultValue, String... options) {
        super(name, description, SettingType.TEXTBOX, defaultValue);
        this.options = options;
    }

    @Override
    public JsonObject loadCurrentValueFromJSONObject(JsonObject jsonObject) {
        this.currentValue = jsonObject.get("value").getAsInt();
        return jsonObject;
    }

    public String[] getOptions() {
        return this.options;
    }
}
