package io.lumine.mythic.lib.glow.external;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.glow.GlowModule;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.inventivetalent.glow.GlowAPI;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @deprecated Not used yet, not tested
 */
@Deprecated
public class GlowAPIModule implements GlowModule {
    private final Map<ChatColor, GlowAPI.Color> glowColors = new HashMap<>();

    @Override
    public void enable() {

        // Populate hashmap
        glowColors.put(ChatColor.BLACK, GlowAPI.Color.BLACK);
        glowColors.put(ChatColor.DARK_BLUE, GlowAPI.Color.DARK_BLUE);
        glowColors.put(ChatColor.DARK_GREEN, GlowAPI.Color.DARK_GREEN);
        glowColors.put(ChatColor.DARK_AQUA, GlowAPI.Color.DARK_AQUA);
        glowColors.put(ChatColor.DARK_RED, GlowAPI.Color.DARK_RED);
        glowColors.put(ChatColor.DARK_PURPLE, GlowAPI.Color.DARK_PURPLE);
        glowColors.put(ChatColor.GOLD, GlowAPI.Color.GOLD);
        glowColors.put(ChatColor.GRAY, GlowAPI.Color.GRAY);
        glowColors.put(ChatColor.DARK_GRAY, GlowAPI.Color.DARK_GRAY);
        glowColors.put(ChatColor.BLUE, GlowAPI.Color.BLUE);
        glowColors.put(ChatColor.GREEN, GlowAPI.Color.GREEN);
        glowColors.put(ChatColor.AQUA, GlowAPI.Color.AQUA);
        glowColors.put(ChatColor.RED, GlowAPI.Color.RED);
        glowColors.put(ChatColor.LIGHT_PURPLE, GlowAPI.Color.PURPLE);
        glowColors.put(ChatColor.YELLOW, GlowAPI.Color.YELLOW);
        glowColors.put(ChatColor.WHITE, GlowAPI.Color.WHITE);
    }

    @Override
    public void disable() {
        // Nothing to do
    }

    @Override
    public void setGlowing(Entity entity, ChatColor color) {
        GlowAPI.setGlowing(entity, Objects.requireNonNull(glowColors.get(color), "Color not found"), MythicLib.getOnlinePlayers());
    }

    @Override
    public void disableGlowing(Entity entity) {
        GlowAPI.setGlowing(entity, GlowAPI.Color.NONE, MythicLib.getOnlinePlayers());
    }
}
