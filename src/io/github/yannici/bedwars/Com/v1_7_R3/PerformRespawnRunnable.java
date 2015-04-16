package io.github.yannici.bedwars.Com.v1_7_R3;

import net.minecraft.server.v1_7_R3.PacketPlayInClientCommand;
import net.minecraft.server.v1_7_R3.EnumClientCommand;

import org.bukkit.craftbukkit.v1_7_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class PerformRespawnRunnable extends BukkitRunnable {
    
    private Player player = null;

    public PerformRespawnRunnable(Player player) {
        this.player = player;
    }

    @Override
    public void run() {
        PacketPlayInClientCommand clientCommand = new PacketPlayInClientCommand(EnumClientCommand.PERFORM_RESPAWN);
        CraftPlayer cp = (CraftPlayer)player;
        
        cp.getHandle().playerConnection.a(clientCommand);
    }

}
