package com.revilo.gatewayexpansion.gateway.builder;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

final class GeneratedGatewayStorage extends SavedData {

    private static final String DATA_NAME = "gatewayexpansion_generated_gateways";
    private static final String GATEWAYS_KEY = "gateways";
    private static final String ID_KEY = "id";
    private static final String JSON_KEY = "json";
    private static final String DISPLAY_NAME_KEY = "display_name";
    private static final String TIER_KEY = "tier";
    private static final String LEVEL_KEY = "level";
    private static final Factory<GeneratedGatewayStorage> FACTORY = new Factory<>(GeneratedGatewayStorage::new, GeneratedGatewayStorage::load);

    private final Map<ResourceLocation, StoredGateway> gateways = new LinkedHashMap<>();

    static GeneratedGatewayStorage get(ServerLevel level) {
        ServerLevel dataLevel = level.getServer().overworld() != null ? level.getServer().overworld() : level;
        return dataLevel.getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
    }

    static GeneratedGatewayStorage load(CompoundTag tag, HolderLookup.Provider registries) {
        GeneratedGatewayStorage storage = new GeneratedGatewayStorage();
        ListTag gateways = tag.getList(GATEWAYS_KEY, Tag.TAG_COMPOUND);
        for (Tag value : gateways) {
            CompoundTag entry = (CompoundTag) value;
            if (!entry.contains(ID_KEY) || !entry.contains(JSON_KEY)) {
                continue;
            }
            ResourceLocation id = ResourceLocation.tryParse(entry.getString(ID_KEY));
            if (id == null) {
                continue;
            }
            storage.gateways.put(id, new StoredGateway(entry.getString(JSON_KEY), entry.getString(DISPLAY_NAME_KEY), entry.getInt(TIER_KEY), entry.getInt(LEVEL_KEY)));
        }
        return storage;
    }

    void put(ResourceLocation id, String json, String displayName, int crystalTier, int crystalLevel) {
        StoredGateway existing = this.gateways.get(id);
        StoredGateway updated = new StoredGateway(json, displayName, crystalTier, crystalLevel);
        if (!updated.equals(existing)) {
            this.gateways.put(id, updated);
            this.setDirty();
        }
    }

    Map<ResourceLocation, StoredGateway> entries() {
        return this.gateways;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag gateways = new ListTag();
        for (Map.Entry<ResourceLocation, StoredGateway> entry : this.gateways.entrySet()) {
            CompoundTag gateway = new CompoundTag();
            gateway.putString(ID_KEY, entry.getKey().toString());
            gateway.putString(JSON_KEY, entry.getValue().json());
            gateway.putString(DISPLAY_NAME_KEY, entry.getValue().displayName());
            gateway.putInt(TIER_KEY, entry.getValue().crystalTier());
            gateway.putInt(LEVEL_KEY, entry.getValue().crystalLevel());
            gateways.add(gateway);
        }
        tag.put(GATEWAYS_KEY, gateways);
        return tag;
    }

    record StoredGateway(String json, String displayName, int crystalTier, int crystalLevel) {
    }
}
