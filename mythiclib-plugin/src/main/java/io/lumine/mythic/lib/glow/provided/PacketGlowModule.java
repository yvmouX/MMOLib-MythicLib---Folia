package io.lumine.mythic.lib.glow.provided;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.glow.GlowModule;
import io.lumine.mythic.lib.util.lang3.Validate;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Packet-based glow module that bypasses the Bukkit Scoreboard API.
 * <p>
 * Folia throws {@link UnsupportedOperationException} on
 * {@link org.bukkit.scoreboard.Scoreboard#registerNewTeam(String)}, which the
 * scoreboard-team based {@link MythicGlowModule} relies on. Instead of going
 * through the scoreboard, this module builds the underlying
 * {@code ClientboundSetPlayerTeamPacket}s directly via PacketEvents and sends
 * them to every online player's channel. Packet sending operates at the Netty
 * layer and is therefore safe to call from any thread on Folia.
 * <p>
 * This mirrors the approach used by craft-engine, which constructs the team
 * packets as raw byte buffers and injects them without touching the scoreboard.
 */
public class PacketGlowModule implements GlowModule, Listener {

    /**
     * One team name per color. Kept short ("ml_glow_0".."ml_glow_15") so it
     * fits within the 16-character team name limit of Minecraft &lt; 1.18.
     */
    private final Map<ChatColor, String> teamNames = new EnumMap<>(ChatColor.class);

    /**
     * Adventure color equivalents, used as the team color in the packet.
     */
    private final Map<ChatColor, NamedTextColor> namedColors = new EnumMap<>(ChatColor.class);

    /**
     * Tracks the active glow color of each entity so it can be removed from the
     * right team when the glow is disabled or switched.
     */
    private final Map<UUID, ChatColor> activeGlow = new ConcurrentHashMap<>();

    /**
     * Glow color is saved at this location in the entity NBT tag, mirroring
     * {@link MythicGlowModule} for cross-module compatibility.
     */
    private static final NamespacedKey COLOR_TAG_PATH = new NamespacedKey(MythicLib.plugin, "glow_color");

    @Override
    public void enable() {
        if (Bukkit.getPluginManager().getPlugin("packetevents") == null)
            throw new IllegalStateException("PacketEvents is not installed");

        int index = 0;
        for (ChatColor color : ChatColor.values())
            if (color.isColor()) {
                teamNames.put(color, "ml_glow_" + index++);
                namedColors.put(color, NamedTextColor.NAMES.value(color.name().toLowerCase()));
            }

        // Register every color team on all currently online clients.
        for (ChatColor color : ChatColor.values())
            if (color.isColor())
                broadcast(createTeamPacket(color));

        Bukkit.getPluginManager().registerEvents(this, MythicLib.plugin);
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent event) {
        // Late-joining players must know about the teams before any entity is
        // added to one, otherwise the color would not apply.
        for (ChatColor color : ChatColor.values())
            if (color.isColor())
                PacketEvents.getAPI().getPlayerManager().sendPacket(event.getPlayer(), createTeamPacket(color));
    }

    @Override
    public void setGlowing(Entity entity, ChatColor color) {
        Validate.isTrue(color.isColor(), "Not a color");
        final String teamName = teamNames.get(color);
        if (teamName == null) return;

        final UUID uuid = entity.getUniqueId();
        final ChatColor previous = activeGlow.put(uuid, color);
        if (previous != null && previous != color)
            broadcast(new WrapperPlayServerTeams(teamNames.get(previous),
                    WrapperPlayServerTeams.TeamMode.REMOVE_ENTITIES,
                    (WrapperPlayServerTeams.ScoreBoardTeamInfo) null, uuid.toString()));

        broadcast(new WrapperPlayServerTeams(teamName,
                WrapperPlayServerTeams.TeamMode.ADD_ENTITIES,
                (WrapperPlayServerTeams.ScoreBoardTeamInfo) null, uuid.toString()));

        // Entity metadata mutation must run on the entity's region thread on Folia.
        MythicLib.applyOn(entity, () -> {
            entity.getPersistentDataContainer().set(COLOR_TAG_PATH, PersistentDataType.STRING, color.name());
            entity.setGlowing(true);
        });
    }

    @Override
    public void disableGlowing(Entity entity) {
        final UUID uuid = entity.getUniqueId();
        final ChatColor color = activeGlow.remove(uuid);
        if (color != null)
            broadcast(new WrapperPlayServerTeams(teamNames.get(color),
                    WrapperPlayServerTeams.TeamMode.REMOVE_ENTITIES,
                    (WrapperPlayServerTeams.ScoreBoardTeamInfo) null, uuid.toString()));

        MythicLib.applyOn(entity, () -> {
            entity.getPersistentDataContainer().remove(COLOR_TAG_PATH);
            entity.setGlowing(false);
        });
    }

    @Override
    public void disable() {
        // Remove every team from all clients.
        for (ChatColor color : ChatColor.values())
            if (color.isColor())
                broadcast(new WrapperPlayServerTeams(teamNames.get(color),
                        WrapperPlayServerTeams.TeamMode.REMOVE,
                        (WrapperPlayServerTeams.ScoreBoardTeamInfo) null));
        activeGlow.clear();
    }

    /**
     * Builds a team-creation packet for the given color. The team is given a
     * neutral display name, empty prefix/suffix, always-visible name tags and
     * the matching Adventure color so glowing entities render in that color.
     */
    private WrapperPlayServerTeams createTeamPacket(ChatColor color) {
        final String name = teamNames.get(color);
        return new WrapperPlayServerTeams(
                name,
                WrapperPlayServerTeams.TeamMode.CREATE,
                new WrapperPlayServerTeams.ScoreBoardTeamInfo(
                        Component.text(name),
                        Component.empty(), Component.empty(),
                        WrapperPlayServerTeams.NameTagVisibility.ALWAYS,
                        WrapperPlayServerTeams.CollisionRule.ALWAYS,
                        namedColors.get(color),
                        WrapperPlayServerTeams.OptionData.NONE
                )
        );
    }

    private void broadcast(WrapperPlayServerTeams packet) {
        for (var player : MythicLib.getOnlinePlayers())
            PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
    }
}
