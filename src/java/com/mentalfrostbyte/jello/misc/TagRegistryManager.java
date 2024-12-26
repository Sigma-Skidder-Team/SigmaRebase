package com.mentalfrostbyte.jello.misc;


import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ITagCollection;
import net.minecraft.util.ResourceLocation;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

public class TagRegistryManager {
    private static final Map<ResourceLocation, ITagCollection<?>> field35957 = Maps.newHashMap();

    public static <T> Class7656<T> method29377(ResourceLocation var0, Function<Class8933, Class7984<T>> var1) {
        ITagCollection var4 = new ITagCollection(var1);
        Class7656 var5 = field35957.putIfAbsent(var0, var4);
        if (var5 == null) {
            return var4;
        } else {
            throw new IllegalStateException("Duplicate entry for static tag collection: " + var0);
        }
    }

    public static void method29378(Class8933 var0) {
        field35957.values().forEach(var1 -> var1.method25169(var0));
    }

    public static void method29379() {
        field35957.values().forEach(ITagCollection::method25168);
    }

    public static Multimap<ResourceLocation, ResourceLocation> method29380(Class8933 var0) {
        HashMultimap var3 = HashMultimap.create();
        field35957.forEach((var2, var3x) -> var3.putAll(var2, var3x.method25172(var0)));
        return var3;
    }

    public static void method29381() {
        ITagCollection[] var2 = new ITagCollection[]{
                BlockTags.getCollection(),
                /*Class5985.field26069, (BlockTags?)*/ FluidTags.collection.getCollection(),
                EntityTypeTags.getCollection()
        };
        boolean var3 = Stream.of(var2).anyMatch(var0 -> !field35957.containsValue(var0));
        if (var3) {
            throw new IllegalStateException("Missing helper registrations");
        }
    }
}