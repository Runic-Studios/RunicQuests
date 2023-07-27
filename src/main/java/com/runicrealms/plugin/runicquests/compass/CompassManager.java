package com.runicrealms.plugin.runicquests.compass;

import com.runicrealms.plugin.rdb.event.CharacterSelectEvent;
import com.runicrealms.plugin.runicquests.api.QuestCompleteEvent;
import com.runicrealms.plugin.runicquests.api.QuestStartEvent;
import com.runicrealms.plugin.runicquests.api.QuestCompleteObjectiveEvent;
import com.runicrealms.plugin.runicquests.listeners.JournalListener;
import com.runicrealms.plugin.runicquests.quests.objective.QuestObjective;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CompassManager implements Listener {

    private static final Map<Player, CompassLocation> compasses = new HashMap<>(); // Null location means no compass

    public static Map<Player, CompassLocation> getCompasses() {
        return compasses;
    }

    public static Optional<Location> parseLocation(String goalLocationMessage, World world) {
        String[] words = goalLocationMessage.split(" ");
        for (int i = 0; i < words.length - 1; i++) {
            Double first = getDoubleOrNull(formatForCoord(words[i]));
            if (first == null) continue;
            Double second = getDoubleOrNull(formatForCoord(words[i + 1]));
            if (second == null) continue;
            Double third = null;
            if (i + 2 < words.length) third = getDoubleOrNull(formatForCoord(words[i + 2]));
            if (third == null) return Optional.of(new Location(world, first, 0, second));
            return Optional.of(new Location(world, first, second, third));
        }
        return Optional.empty();
    }

    private static String formatForCoord(String string) {
        if (string.endsWith(",")) string = string.replaceAll(",", "");
        if (string.endsWith(".")) string = string.replaceAll("\\.", "");
        string = string.replaceAll(" ", "");
        return string;
    }

    public static void setCompass(Player player, CompassLocation location) {
        compasses.put(player, location);
        player.getInventory().setItem(7, createTracker(location.getLocation()));
        player.updateInventory();
    }

    public static void revertCompass(Player player) {
        compasses.put(player, null);
        player.getInventory().setItem(7, JournalListener.getQuestJournal());
        player.updateInventory();
    }

    private static ItemStack createTracker(Location location) {
        ItemStack compass = JournalListener.getQuestJournal().clone();
        compass.setType(Material.COMPASS);
        CompassMeta meta = (CompassMeta) compass.getItemMeta();
        meta.setLodestone(location);
        meta.setLodestoneTracked(false);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true);
        compass.setItemMeta(meta);
        return compass;
    }

    private static Double getDoubleOrNull(String string) {
        try {
            return Double.parseDouble(string);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    @EventHandler
    public void onJoin(CharacterSelectEvent event) {
        compasses.put(event.getPlayer(), null);
    }

    @EventHandler
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        if (!event.getPlayer().getLocation().getWorld().getName().equalsIgnoreCase("alterra")) {
            if (compasses.get(event.getPlayer()) != null) revertCompass(event.getPlayer());
        }
    }

    @EventHandler
    public void onQuestComplete(QuestCompleteEvent event) {
        if (compasses.get(event.getPlayer()) != null) revertCompass(event.getPlayer());
    }

    @EventHandler
    public void onQuestCompleteObjective(QuestCompleteObjectiveEvent event) {
        if (!event.getPlayer().getWorld().getName().equalsIgnoreCase("alterra")) return;
        QuestObjective nextObjective = QuestObjective.getObjective(event.getQuest().getObjectives(), event.getObjectiveCompleted().getObjectiveNumber() + 1);
        if (nextObjective == null || (nextObjective.getGoalMessage() == null && nextObjective.getGoalLocation() == null)) {
            revertCompass(event.getPlayer());
            return;
        }
        Optional<Location> parsed = Optional.empty();
        String message = null;
        if (nextObjective.getGoalMessage() != null) {
            parsed = parseLocation(nextObjective.getGoalMessage(), event.getPlayer().getWorld());
            if (parsed.isPresent()) message = nextObjective.getGoalMessage();
        }
        if (!parsed.isPresent() && nextObjective.getGoalLocation() != null) {
            parsed = parseLocation(nextObjective.getGoalLocation(), event.getPlayer().getWorld());
            message = nextObjective.getGoalLocation();
        }
        if (!parsed.isPresent()) {
            revertCompass(event.getPlayer());
            return;
        }
        CompassLocation comp = new CompassLocation(parsed.get(), event.getQuest().getQuestName(), message);
        setCompass(event.getPlayer(), comp);
        comp.send(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onQuestStart(QuestStartEvent event) {
        if (!event.getPlayer().getWorld().getName().equalsIgnoreCase("alterra")) return;
        QuestObjective nextObjective = event.getQuest().getObjectives().get(0);
        if (nextObjective == null || (nextObjective.getGoalMessage() == null && nextObjective.getGoalLocation() == null)) {
            revertCompass(event.getPlayer());
            return;
        }
        Optional<Location> parsed = Optional.empty();
        String message = null;
        if (nextObjective.getGoalMessage() != null) {
            parsed = parseLocation(nextObjective.getGoalMessage(), event.getPlayer().getWorld());
            if (parsed.isPresent()) message = nextObjective.getGoalMessage();
        }
        if (!parsed.isPresent() && nextObjective.getGoalLocation() != null) {
            parsed = parseLocation(nextObjective.getGoalLocation(), event.getPlayer().getWorld());
            message = nextObjective.getGoalLocation();
        }
        if (!parsed.isPresent()) {
            revertCompass(event.getPlayer());
            return;
        }
        CompassLocation comp = new CompassLocation(parsed.get(), event.getQuest().getQuestName(), message);
        setCompass(event.getPlayer(), comp);
        comp.send(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        compasses.remove(event.getPlayer());
    }

    public static class CompassLocation {

        private final Location location;
        private final List<String> message = new ArrayList<>();

        public CompassLocation(Location location, String questName, String message) {
            this.location = location;
            this.message.add(ChatColor.DARK_GREEN + String.valueOf(ChatColor.BOLD) + "Now Tracking: " + ChatColor.RESET + ChatColor.GREEN + questName);
            this.message.add(ChatColor.GRAY + ChatColor.translateAlternateColorCodes('&', message));
        }

        public Location getLocation() {
            return this.location;
        }

        public List<String> getMessage() {
            return this.message;
        }

        public void send(Player player) {
            message.forEach(player::sendMessage);
            double distance = Math.sqrt(Math.pow(player.getLocation().getX() - location.getX(), 2) + Math.pow(player.getLocation().getZ() - location.getZ(), 2));
            player.sendMessage(ChatColor.GRAY + "Distance: " + ((int) distance) + " blocks");
        }

    }

}
