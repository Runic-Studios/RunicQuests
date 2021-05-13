package com.runicrealms.runicquests.command;

import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.runicitems.RunicItemsAPI;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class TutorialWeaponCommand implements CommandExecutor {

    private static final String BOW_TEMPLATE_ID = "oaken-shortbow";
    private static final String MACE_TEMPLATE_ID = "oaken-mace";
    private static final String STAFF_TEMPLATE_ID = "oaken-branch";
    private static final String SWORD_TEMPLATE_ID = "oaken-sparring-sword";
    private static final String AXE_TEMPLATE_ID = "oaken-axe";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            sender.sendMessage(ChatColor.RED + "Players cannot execute this command!");
            return true;
        }
        Player player = Bukkit.getPlayer(args[1]);
        if (player == null) return true;
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 0.5f, 1.0f);
        player.spawnParticle(Particle.REDSTONE, player.getEyeLocation(),
                10, 0.5f, 0.5f, 0.5f, new Particle.DustOptions(Color.FUCHSIA, 4));
        player.getInventory().addItem(determineStarterWeapon(player));
        return true;
    }

    private ItemStack determineStarterWeapon(Player player) {
        String className = RunicCoreAPI.getPlayerClass(player);
        String templateID = "";
        switch (className) {
            case "archer":
                templateID = BOW_TEMPLATE_ID;
                break;
            case "cleric":
                templateID = MACE_TEMPLATE_ID;
                break;
            case "mage":
                templateID = STAFF_TEMPLATE_ID;
                break;
            case "rogue":
                templateID = SWORD_TEMPLATE_ID;
                break;
            case "warrior":
                templateID = AXE_TEMPLATE_ID;
                break;

        }
        return RunicItemsAPI.generateItemFromTemplate(templateID).generateItem();
    }
}
