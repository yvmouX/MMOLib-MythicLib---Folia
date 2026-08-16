package io.lumine.mythic.lib.glow.provided;

import io.lumine.mythic.lib.glow.GlowModule;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;

/**
 * No-op glow module used as a fallback on servers which do not support
 * scoreboard teams, e.g. Folia where {@code registerNewTeam} throws
 * {@link UnsupportedOperationException}.
 */
public class NoGlowModule implements GlowModule {

    @Override
    public void setGlowing(Entity entity, ChatColor color) {
        // Not supported on this server
    }

    @Override
    public void disableGlowing(Entity entity) {
        // Not supported on this server
    }

    @Override
    public void enable() {
        // Nothing to do
    }

    @Override
    public void disable() {
        // Nothing to do
    }
}
