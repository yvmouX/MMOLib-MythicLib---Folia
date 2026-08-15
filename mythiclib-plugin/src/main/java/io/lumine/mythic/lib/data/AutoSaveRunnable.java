package io.lumine.mythic.lib.data;

import cn.yvmou.ylib.scheduler.UniversalRunnable;

import java.util.logging.Level;

public class AutoSaveRunnable extends UniversalRunnable {
    private final SynchronizedDataManager<?, ?> manager;
    private final boolean log;

    /**
     * Minimum interval arbitrarily set to 60 seconds
     */
    private static final long MINIMUM_INTERVAL = 60;

    public AutoSaveRunnable(SynchronizedDataManager<?, ?> manager) {
        this.manager = manager;

        final var config = manager.getOwningPlugin().getConfig().getConfigurationSection("auto-save");
        log = config.getBoolean("log", false);
        final var timer = Math.max(MINIMUM_INTERVAL, config.getLong("interval", 60 * 30)) * 20;
        // This cannot run async because of events and player data loading.
        runTimer(manager.getOwningPlugin(), timer, timer);
    }

    @Override
    public void run() {
        if (log)
            manager.getOwningPlugin().getLogger().log(Level.INFO, "Autosaving " + manager.getLoaded().size() + " player datas, might take a while...");
        manager.autosave();
    }
}
