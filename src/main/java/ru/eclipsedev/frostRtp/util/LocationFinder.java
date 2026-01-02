package ru.eclipsedev.frostRtp.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;

import java.util.concurrent.ThreadLocalRandom;

public class LocationFinder {
    private static final int MAX_ATTEMPTS = 250;
    public static Location findSafeLocation(World world, double minRadius, double maxRadius) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        WorldBorder border = world.getWorldBorder();
        Location center = border.getCenter();
        double borderSize = border.getSize() / 2.0;
        double realMaxRadius = Math.min(maxRadius, borderSize);
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            double angle = random.nextDouble() * 2.0 * Math.PI;
            double radius = minRadius + random.nextDouble() * (realMaxRadius - minRadius);
            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);
            if (Math.abs(x - center.getX()) <= borderSize && Math.abs(z - center.getZ()) <= borderSize) {
                int y = world.getHighestBlockYAt((int) x, (int) z);
                if (isSafe(world, (int) x, y, (int) z)) {
                    return new Location(world, x + 0.5, y + 1, z + 0.5);
                }
            }
        }

        return null;
    }

    private static boolean isSafe(World world, int x, int y, int z) {
        Block blockFeet = world.getBlockAt(x, y, z);
        Block blockBody = world.getBlockAt(x, y + 1, z);
        Block blockHead = world.getBlockAt(x, y + 2, z);

        Material feetType = blockFeet.getType();
        Material bodyType = blockBody.getType();
        Material headType = blockHead.getType();
        return !isUnsafeMaterial(feetType) &&
                !isUnsafeMaterial(bodyType) &&
                !isUnsafeMaterial(headType) &&
                bodyType == Material.AIR &&
                headType == Material.AIR;
    }

    private static boolean isUnsafeMaterial(Material mat) {
        return mat == Material.WATER ||
                mat == Material.LAVA ||
                mat == Material.MAGMA_BLOCK ||
                mat == Material.CACTUS ||
                mat == Material.FIRE;
    }
}