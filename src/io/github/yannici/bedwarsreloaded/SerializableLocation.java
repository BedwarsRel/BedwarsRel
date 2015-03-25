package io.github.yannici.bedwarsreloaded;

import java.util.Map;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

public class SerializableLocation extends Location implements ConfigurationSerializable {

    public SerializableLocation(World world, double x, double y, double z,
            float yaw, float pitch) {
        super(world, x, y, z, yaw, pitch);
    }

    public SerializableLocation(Location location) {
        super(location.getWorld(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }

    @Override
    public Map<String, Object> serialize() {

        return null;
    }

}
