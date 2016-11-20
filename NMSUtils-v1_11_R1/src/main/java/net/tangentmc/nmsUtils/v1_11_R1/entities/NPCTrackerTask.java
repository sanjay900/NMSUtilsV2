/**
 * Copyright (C) 2015 Mark Hendriks
 * <p>
 * This file is part of DarkSeraphim's NPC library.
 * <p>
 * DarkSeraphim's NPC library is free software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 * <p>
 * DarkSeraphim's NPC library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with DarkSeraphim's NPC library. If not, see <http://www.gnu.org/licenses/>.
 */
package net.tangentmc.nmsUtils.v1_11_R1.entities;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author DarkSeraphim.
 */
public class NPCTrackerTask extends BukkitRunnable {

    /**
     * Composition of multiple Consumers, allowing you to chain Consumers
     * in one lambda call.
     *
     * @param <T> Generic type of composed consumers
     */
    private static class CompositeConsumer<T> implements Consumer<T> {
        private final List<Consumer<T>> consumers;

        private CompositeConsumer(List<Consumer<T>> consumers) {
            this.consumers = consumers;
        }

        /**
         * Accepts t for all contained Consumers.
         *
         * @param t parameter to accept
         */
        @Override
        public void accept(T t) {
            this.consumers.stream().forEach(consumer -> consumer.accept(t));
        }

        /**
         * Creates a CompositeConsumer of zero or more consumers.
         *
         * @param consumers Consumers to compose.
         * @param <T>       Generic type of the Consumers.
         * @return A composition of the given consumers.
         */
        @SafeVarargs
        private static <T> Consumer<T> of(Consumer<T>... consumers) {
            return new CompositeConsumer<>(Arrays.asList(consumers));
        }
    }

    /**
     * Predicate which only returns true once, and will return only return true once
     * there has been at least one validation which resulted to false.
     *
     * @param <T> Predicate type
     */
    private static class CachedPredicate<T extends Map.Entry<Player, Boolean>> implements Predicate<T> {

        private final Predicate<T> original;

        private final Set<UUID> cache = new HashSet<>();

        private CachedPredicate(Predicate<T> original) {
            this.original = original;
        }

        /**
         * Tests if the predicate holds with the given variable, if and only if it previously returned false
         *
         * @param t variable to test
         * @return {@code result && !this.cache.contains(t.getKey().getUniqueId())}
         * @modifies adds {@code t.getKey().getUniqueId()} to the cache if \result is true,
         * or removes it if \result is false
         */
        @Override
        public boolean test(T t) {
            boolean result = this.original.test(t);
            UUID uuid = t.getKey().getUniqueId();
            if (result) {
                return this.cache.add(uuid);
            } else {
                this.cache.remove(uuid);
            }
            return false;
        }
    }

    // Distance to respawn, customise to your liking
    private static final double DISTANCE = 50;

    private static final double DISTANCE_SQUARED = DISTANCE * DISTANCE;

    private final Function<Player, Map.Entry<Player, Boolean>> inRange;

    private final NPC npc;

    private final Consumer<Player> update;

    private final Predicate<Map.Entry<Player, Boolean>> filter;

    protected NPCTrackerTask(NPC npc) {
        this.npc = npc;
        this.inRange = player -> new AbstractMap.SimpleEntry<>(player, this.npc.getDistanceSquared(player) < DISTANCE_SQUARED);
        Consumer<Player> cleanup = this.npc::cleanup;
        Consumer<Player> spawn = this.npc::spawn;
        this.update = CompositeConsumer.of(cleanup, spawn);
        this.filter = new CachedPredicate<>(Map.Entry::getValue);
    }

    /**
     * Respawns the NPC for any players which moved within {@code DISTANCE} of the NPC.
     */
    @Override
    public void run() {
        this.npc.getTrackedPlayers().map(this.inRange)
                .filter(this.filter)
                .map(Map.Entry::getKey)
                .forEach(this.update);
    }
}