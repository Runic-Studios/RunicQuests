package com.runicrealms.runicquests.ui;

import com.runicrealms.plugin.character.api.CharacterLoadEvent;
import com.runicrealms.runicquests.api.QuestCompleteObjectiveEvent;
import com.runicrealms.runicquests.api.QuestStartEvent;
import com.runicrealms.runicquests.quests.objective.QuestObjective;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.ItemMeta;

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
        for (int i = 0; i < words.length - 2; i++) {
            Double x = getDoubleOrNull(formatForCoord(words[i]));
            if (x == null) continue;
            Double y = getDoubleOrNull(formatForCoord(words[i + 1]));
            if (y == null) continue;
            Double z = getDoubleOrNull(formatForCoord(words[i + 2]));
            if (z == null) continue;
            return Optional.of(new Location(world, x, y, z));
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

    @EventHandler
    public void onJoin(CharacterLoadEvent event) {
        compasses.put(event.getPlayer(), null);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        compasses.remove(event.getPlayer());
    }

    @EventHandler
    public void onQuestCompleteObjective(QuestCompleteObjectiveEvent event) {
        QuestObjective nextObjective = QuestObjective.getObjective(event.getQuest().getObjectives(), event.getObjectiveCompleted().getObjectiveNumber() + 1);
        if (nextObjective == null || (nextObjective.getGoalMessage() == null && nextObjective.getGoalLocation() == null)) { revertCompass(event.getPlayer()); return; }
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
        if (!parsed.isPresent()) { revertCompass(event.getPlayer()); return; }
        CompassLocation comp = new CompassLocation(parsed.get(), event.getQuest().getQuestName(), message);
        setCompass(event.getPlayer(), comp);
        comp.send(event.getPlayer());
    }

    @EventHandler
    public void onQuestStart(QuestStartEvent event) {
        QuestObjective nextObjective = event.getQuest().getObjectives().get(0);
        if (nextObjective == null || (nextObjective.getGoalMessage() == null && nextObjective.getGoalLocation() == null)) { revertCompass(event.getPlayer()); return; }
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
        if (!parsed.isPresent()) { revertCompass(event.getPlayer()); return; }
        CompassLocation comp = new CompassLocation(parsed.get(), event.getQuest().getQuestName(), message);
        setCompass(event.getPlayer(), comp);
        comp.send(event.getPlayer());
    }

    @EventHandler
    public void onQuestJournalClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null || event.getCurrentItem() == null) return;
        if (!(event.getClickedInventory().getHolder() instanceof QuestMenu)) return;
        if (event.getSlot() < 9 || event.getCurrentItem().getType() == Material.BLACK_STAINED_GLASS_PANE) return;
        ItemStack item = event.getCurrentItem();
        if (!item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        for (String lore : meta.getLore()) {
            String stripped = ChatColor.stripColor(lore);
            if (stripped.toLowerCase().startsWith("location: ")) stripped = stripped.replaceAll("location: ", "");
            Optional<Location> opt = parseLocation(stripped, event.getWhoClicked().getWorld());
            if (opt.isPresent()) {
                event.setCancelled(true);
                Location location = opt.get();
                CompassLocation comp = new CompassLocation(location, ChatColor.stripColor(meta.getDisplayName()), stripped);
                setCompass((Player) event.getWhoClicked(), comp);
                comp.send((Player) event.getWhoClicked());
                event.getWhoClicked().closeInventory();
                return;
            }
        }
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

    public static class CompassLocation {

        private Location location;
        private List<String> message = new ArrayList<>();

        public CompassLocation(Location location, String questName, String message) {
            this.location = location;
            this.message.add(ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "Now Tracking: " + ChatColor.RESET + ChatColor.GREEN + questName);
            this.message.add(ChatColor.GRAY + message);
        }

        public Location getLocation() {
            return this.location;
        }

        public List<String> getMessage() {
            return this.message;
        }

        public void send(Player player) {
            message.forEach(player::sendMessage);
        }

    }

}
