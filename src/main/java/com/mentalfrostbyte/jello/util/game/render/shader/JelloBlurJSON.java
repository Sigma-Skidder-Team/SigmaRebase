package com.mentalfrostbyte.jello.util.game.render.shader;

import net.minecraft.resources.IResource;
import net.minecraft.resources.data.IMetadataSectionSerializer;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class JelloBlurJSON implements IResource {

    @Override
    public void close() throws IOException {
    }

    @Override
    public ResourceLocation getLocation() {
        return null;
    }

    @Override
    public InputStream getInputStream() {
        String var3 = "{\"targets\":[\"jelloswap\",\"jello\"],\"passes\":[{\"name\":\"blur\",\"intarget\":\"minecraft:main\",\"outtarget\":\"jelloswap\",\"uniforms\":[{\"name\":\"BlurDir\",\"values\":[1,0]},{\"name\":\"Radius\",\"values\":[20]}]},{\"name\":\"blur\",\"intarget\":\"jelloswap\",\"outtarget\":\"jello\",\"uniforms\":[{\"name\":\"BlurDir\",\"values\":[0,1]},{\"name\":\"Radius\",\"values\":[20]}]}]}";
        return new ByteArrayInputStream(var3.getBytes());
    }

    @Override
    public <T> T getMetadata(IMetadataSectionSerializer<T> serializer) {
        return null;
    }

    @Override
    public String getPackName() {
        return null;
    }
}
