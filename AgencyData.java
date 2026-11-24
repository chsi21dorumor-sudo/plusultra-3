package com.plusultra.agency;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

/** World-saved Hero Agencies. Members near each other get a stamina regen bonus. */
public class AgencyData extends SavedData {
    private static final String NAME = "plusultra_agencies";

    public static class Agency {
        public final String name;
        public UUID owner;
        public final Set<UUID> members = new HashSet<>();

        public Agency(String name, UUID owner) {
            this.name = name;
            this.owner = owner;
            this.members.add(owner);
        }
    }

    private final Map<String, Agency> agencies = new HashMap<>();

    public AgencyData() {}

    public static AgencyData get(MinecraftServer server) {
        return server.overworld().getDataStorage()
                .computeIfAbsent(new SavedData.Factory<>(AgencyData::new, AgencyData::load, null), NAME);
    }

    public static AgencyData load(CompoundTag tag, HolderLookup.Provider registries) {
        AgencyData data = new AgencyData();
        ListTag list = tag.getList("agencies", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag t = list.getCompound(i);
            Agency a = new Agency(t.getString("name"), NbtUtils.loadUUID(t.get("owner")));
            ListTag members = t.getList("members", Tag.TAG_INT_ARRAY);
            for (int k = 0; k < members.size(); k++) a.members.add(NbtUtils.loadUUID(members.get(k)));
            data.agencies.put(a.name.toLowerCase(), a);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (Agency a : agencies.values()) {
            CompoundTag t = new CompoundTag();
            t.putString("name", a.name);
            t.put("owner", NbtUtils.createUUID(a.owner));
            ListTag members = new ListTag();
            for (UUID m : a.members) members.add(NbtUtils.createUUID(m));
            t.put("members", members);
            list.add(t);
        }
        tag.put("agencies", list);
        return tag;
    }

    public Agency find(String name) {
        return agencies.get(name.toLowerCase());
    }

    public Agency agencyOf(UUID player) {
        for (Agency a : agencies.values()) if (a.members.contains(player)) return a;
        return null;
    }

    public List<Agency> all() {
        return new ArrayList<>(agencies.values());
    }

    /** @return null on success, otherwise an error message. */
    public String create(String name, UUID owner) {
        if (agencyOf(owner) != null) return "You are already in an agency. Leave it first.";
        if (name.length() < 3 || name.length() > 24) return "Agency names must be 3-24 characters.";
        if (find(name) != null) return "That agency name is taken.";
        agencies.put(name.toLowerCase(), new Agency(name, owner));
        setDirty();
        return null;
    }

    public String join(String name, UUID player) {
        if (agencyOf(player) != null) return "You are already in an agency. Leave it first.";
        Agency a = find(name);
        if (a == null) return "No agency with that name.";
        if (a.members.size() >= 8) return "That agency is full (8 members).";
        a.members.add(player);
        setDirty();
        return null;
    }

    public String leave(UUID player) {
        Agency a = agencyOf(player);
        if (a == null) return "You are not in an agency.";
        a.members.remove(player);
        if (a.members.isEmpty()) {
            agencies.remove(a.name.toLowerCase());
        } else if (a.owner.equals(player)) {
            a.owner = a.members.iterator().next();
        }
        setDirty();
        return null;
    }
}
