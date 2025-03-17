package com.mentalfrostbyte.jello.module.settings.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mentalfrostbyte.jello.module.settings.Setting;
import com.mentalfrostbyte.jello.module.settings.SettingType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BooleanListSetting extends Setting<List<String>> {
    private boolean enabled;

    public BooleanListSetting(String name, String description, boolean enabled, String... values) {
        super(name, description, SettingType.BOOLEAN2, Arrays.asList(values));
        this.setEnabled(enabled);
    }

    @Override
    public JsonObject buildUpSettingData(JsonObject jsonObject) {
        jsonObject.addProperty("name", this.getName());

        try {
            JsonArray jsonArray = new JsonArray();
            for (Object value : this.currentValue) {
                jsonArray.add(value.toString());
            }

            jsonObject.add("value", jsonArray);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return jsonObject;
    }

    @Override
    public JsonObject loadCurrentValueFromJSONObject(JsonObject jsonObject) {
        JsonArray jsonArray = jsonObject.getAsJsonArray("value");
        this.currentValue = new ArrayList<>();
        if (jsonArray != null) {
            for (int i = 0; i < jsonArray.size(); i++) {
                try {
                    this.currentValue.add(jsonArray.get(i).getAsString());
                } catch (JsonParseException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return jsonObject;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
