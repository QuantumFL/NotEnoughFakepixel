package org.ginafro.notenoughfakepixel.features.skyblock.diana;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.network.play.server.S2APacketParticles;
import net.minecraft.util.BlockPos;
import org.ginafro.notenoughfakepixel.Configuration;
import org.ginafro.notenoughfakepixel.utils.SoundUtils;
import org.ginafro.notenoughfakepixel.utils.Waypoint;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class ParticleProcessor {
    String waypointSound = "random.pop";
    String waypointTreasureSound = "random.pop";
    float volumeWaypointSound = 4.0f;
    float volumeWaypointTreasureSound = 3.0f;
    private final float distanceThreshold = 2.0f; // Distance (in blocks) to distinguish if it's the same burrow
    private final int particleThreshold = 6; // Number of particles needed to be considered a burrow

    private final Queue<S2APacketParticles> particleEnchantmentTableQueue = new ConcurrentLinkedQueue<>();
    private final Queue<S2APacketParticles> particleCritQueue = new ConcurrentLinkedQueue<>();
    private final Queue<S2APacketParticles> particleMagicCritQueue = new ConcurrentLinkedQueue<>();
    private final Queue<S2APacketParticles> particleDripLavaQueue = new ConcurrentLinkedQueue<>();

    private final List<Waypoint> waypoints = new ArrayList<>();

    public void addParticle(S2APacketParticles particle) {
        switch (particle.getParticleType().getParticleName()) {
            case "crit":
                particleCritQueue.add(particle);
                break;
            case "magicCrit":
                particleMagicCritQueue.add(particle);
                break;
            case "enchantmenttable":
                particleEnchantmentTableQueue.add(particle);
                break;
            case "dripLava":
                particleDripLavaQueue.add(particle);
                break;
        }

        List<Waypoint> safeWaypoints = new ArrayList<>();
        synchronized (getWaypoints()) {
            try {
                safeWaypoints = new ArrayList<>(getWaypoints());
            } catch (Exception ignored) {}
        }

        try {
            for (Waypoint r : safeWaypoints) {
                if (!areCoordinatesClose(new int[]{Minecraft.getMinecraft().thePlayer.getPosition().getX(),
                        Minecraft.getMinecraft().thePlayer.getPosition().getY(),
                        Minecraft.getMinecraft().thePlayer.getPosition().getZ()}, r.getCoordinates(), 64)) {
                    if (!r.getType().equals("MINOS")) waypoints.remove(r);
                }
            }
        } catch (Exception ignored) {}


        // Process particles
        processParticles();
    }

    public void processParticles() {
        if (particleEnchantmentTableQueue.isEmpty()) {
            return; // No processing needed if there's no enchantmenttable particle
        }

        // Detect and process results based on the queues
        Waypoint result = detectResult();
        if (result == null) return;
        if (!isDuplicateResult(result)) {
            BlockPos block = new BlockPos(result.getCoordinates()[0], result.getCoordinates()[1]-1, result.getCoordinates()[2]);
            if (Minecraft.getMinecraft().theWorld.isAirBlock(block)) return; // prevent particles in the air getting detected
            waypoints.add(result);
            //System.out.println("Detected new result: " + result);
            if (Configuration.dianaWaypointSounds) {
                if (result.getType().equals("EMPTY") || result.getType().equals("MOB")) {
                    SoundUtils.playSound(result.getCoordinates(), waypointSound, volumeWaypointSound, 2.0f);
                } else if (result.getType().equals("TREASURE")) {
                    SoundUtils.playSound(result.getCoordinates(), waypointTreasureSound, volumeWaypointTreasureSound, 0.5f);
                }
            }

            // Flush lists and queues
            particleEnchantmentTableQueue.clear();
            particleCritQueue.clear();
            particleMagicCritQueue.clear();
            particleDripLavaQueue.clear();
        }
    }

    private Waypoint detectResult() {
        if (particleEnchantmentTableQueue.isEmpty()) {
            return null;
        }

        Queue<S2APacketParticles> largestQueue = getLargestQueue();
        if (largestQueue == null || largestQueue.isEmpty() || largestQueue.size() < particleThreshold) {
            return null;
        }

        List<S2APacketParticles> combinedParticles = new ArrayList<>();

        S2APacketParticles enchParticle = particleEnchantmentTableQueue.peek();
        // Combine enchantmenttable particles
        combinedParticles.add(particleEnchantmentTableQueue.poll());
        particleEnchantmentTableQueue.clear(); // Flush queue for receiving new particles

        // Combine particles from the largest queue
        while (!largestQueue.isEmpty()) {
            S2APacketParticles particle = largestQueue.poll();
            if (particle == null) return null; // Ignore null particles
            if (areCoordinatesClose(roundToCoords(particle.getXCoordinate(), particle.getYCoordinate(), particle.getZCoordinate()),
                    roundToCoords(enchParticle.getXCoordinate(), enchParticle.getYCoordinate(), enchParticle.getZCoordinate()),
                    distanceThreshold)) {
                combinedParticles.add(particle);
            }
        }
        // Flush queues
        particleCritQueue.clear();
        particleMagicCritQueue.clear();
        particleDripLavaQueue.clear();

        Waypoint waypoint = classifyGroup(combinedParticles);

        // Check if dev pet is close
        if (isDevPetClose(waypoint.getCoordinates())) return  null;

        // Calculate the average position and classify the group
        return waypoint;
    }

    private Queue<S2APacketParticles> getLargestQueue() {
        Queue<S2APacketParticles> largestQueue = null;
        int maxSize = 0;

        for (Queue<S2APacketParticles> queue : Arrays.asList(particleCritQueue, particleMagicCritQueue, particleDripLavaQueue)) {
            if (queue.size() > maxSize) {
                largestQueue = queue;
                maxSize = queue.size();
            }
        }

        return largestQueue;
    }

    private Waypoint classifyGroup(List<S2APacketParticles> group) {
        if (group == null || group.isEmpty()) return null;
        Set<String> groupTypes = new HashSet<>();
        double sumX = 0, sumY = 0, sumZ = 0;

        for (S2APacketParticles particle : group) {
            if (particle == null || particle.getParticleType() == null || particle.getParticleType().getParticleName() == null) {
                continue; // Ignore invalid particles
            }
            groupTypes.add(particle.getParticleType().getParticleName());
            sumX += particle.getXCoordinate();
            sumY += particle.getYCoordinate();
            sumZ += particle.getZCoordinate();
        }

        if (groupTypes.isEmpty()) {
            return null;
        }

        int size = group.size();
        int[] avgCoordinates = new int[]{
                (int) Math.floor(sumX / size),
                (int) Math.floor(sumY / size),
                (int) Math.floor(sumZ / size)
        };

        if (groupTypes.contains("magicCrit") && groupTypes.contains("enchantmenttable")) {
            return new Waypoint("EMPTY", avgCoordinates);
        }
        if (groupTypes.contains("crit") && groupTypes.contains("enchantmenttable")) {
            return new Waypoint("MOB", avgCoordinates);
        }
        if (groupTypes.contains("dripLava") && groupTypes.contains("enchantmenttable")) {
            return new Waypoint("TREASURE", avgCoordinates);
        }

        return null;
    }

    private boolean isDuplicateResult(Waypoint result) {
        if (result == null) return false;
        try {
            for (Waypoint waypoint : waypoints) {
                if (waypoint == null) continue;
                if (areCoordinatesClose(waypoint.getCoordinates(), result.getCoordinates(), distanceThreshold)) {
                    return true;
                }
            }
        } catch (Exception ignored) {}
        return false;
    }

    public boolean areCoordinatesClose(int[] coords1, int[] coords2, float threshold) {
        double distance = Math.sqrt(
                Math.pow(coords1[0] - coords2[0], 2) +
                        Math.pow(coords1[1] - coords2[1], 2) +
                        Math.pow(coords1[2] - coords2[2], 2)
        );
        return distance < threshold;
    }

    public static float getDistance(int[] coords1, int[] coords2) {
        return (float) Math.sqrt(
                Math.pow(coords1[0] - coords2[0], 2) +
                        Math.pow(coords1[1] - coords2[1], 2) +
                        Math.pow(coords1[2] - coords2[2], 2)
        );
    }

    private int[] roundToCoords(double x, double y, double z) {
        return new int[]{(int)Math.floor(x), (int)Math.floor(y), (int)Math.floor(z)};
    }

    public List<Waypoint> getWaypoints() {
        return waypoints;
    }

    public void addWaypoint(Waypoint waypoint) {
        if (waypoint != null) {
            waypoints.add(waypoint);
        }
    }

    public void deleteWaypoint(Waypoint result) {
        waypoints.remove(result);
    }

    public void clearWaypoints() {
        waypoints.clear();
    }

    public Waypoint getClosestWaypoint(int[] coords) {
        Waypoint result = null;
        float distance = 9999;
        if (waypoints.isEmpty()) return null;
        for (Waypoint res : waypoints) {
            float dist = getDistance(coords, res.getCoordinates());
            if (dist < distance) {
                distance = dist;
                result = res;
            }
        }
        return result;
    }

    private boolean isDevPetClose(int[] cords) {
        boolean found = false;
        for (Entity entity : Minecraft.getMinecraft().theWorld.loadedEntityList) {
            if (entity == null) continue;
            if (entity.getName() == null) continue;
            if (entity instanceof EntityArmorStand) {
                if (!entity.getName().contains("Developer's Pet")) continue;
                if (getDistance(cords, new int[]{entity.getPosition().getX(), entity.getPosition().getY(), entity.getPosition().getZ()}) <= 5) {
                    found = true;
                    break;
                }
            }
        }
        return found;
    }

}
