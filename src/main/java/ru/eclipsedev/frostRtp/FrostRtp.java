package ru.eclipsedev.frostRtp;

import org.bukkit.plugin.java.JavaPlugin;

public final class FrostRtp extends JavaPlugin {

    private RtpCommand rtpCommand;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.rtpCommand = new RtpCommand(this);
        this.getCommand("nrtp").setExecutor(rtpCommand);
        this.getCommand("nrtp").setTabCompleter(rtpCommand);
    }

    public void rreload() {
        this.reloadConfig();
    }

    @Override
    public void onDisable() {;}
}
