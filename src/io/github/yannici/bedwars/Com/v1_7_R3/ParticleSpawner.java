package io.github.yannici.bedwars.Com.v1_7_R3;

import java.util.List;

import org.bukkit.craftbukkit.v1_7_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_7_R3.PacketPlayOutWorldParticles;

public class ParticleSpawner {
	
	public static void spawnParticle(List<Player> players, String particle, float x, float y, float z) {
		PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(particle, x, y, z, 0, 0, 0, 0, 1);
        for(Player player : players) {
        	CraftPlayer craftPlayer = (CraftPlayer)player;
        	craftPlayer.getHandle().playerConnection.sendPacket(packet);
        }
	}

}
