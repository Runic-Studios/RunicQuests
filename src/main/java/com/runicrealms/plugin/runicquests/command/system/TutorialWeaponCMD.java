package com.runicrealms.plugin.runicquests.command.system;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Default;
import com.runicrealms.plugin.rdb.RunicDatabase;
import com.runicrealms.plugin.runicitems.RunicItemsAPI;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@CommandAlias("tutorialweapon")
@CommandPermission("runic.op")
public class TutorialWeaponCMD extends BaseCommand {
    private static final String BOW_TEMPLATE_ID = "tutorial-archer-weapon";
    private static final String MACE_TEMPLATE_ID = "tutorial-cleric-weapon";
    private static final String STAFF_TEMPLATE_ID = "tutorial-mage-weapon";
    private static final String SWORD_TEMPLATE_ID = "tutorial-rogue-weapon";
    private static final String AXE_TEMPLATE_ID = "tutorial-warrior-weapon";

    private ItemStack determineStarterWeapon(Player player) {
        String className = RunicDatabase.getAPI().getCharacterAPI().getPlayerClass(player);
        String templateID = switch (className.toLowerCase()) {
            case "archer" -> BOW_TEMPLATE_ID;
            case "cleric" -> MACE_TEMPLATE_ID;
            case "mage" -> STAFF_TEMPLATE_ID;
            case "rogue" -> SWORD_TEMPLATE_ID;
            case "warrior" -> AXE_TEMPLATE_ID;
            default -> "";
        };
        return RunicItemsAPI.generateItemFromTemplate(templateID).generateItem();
    }

    // tutorialweapon [player]

    @CatchUnknown
    @Default
    @CommandCompletion("@players")
    @Conditions("is-console-or-op")
    public void onCommand(String[] args) {
        Player player = Bukkit.getPlayer(args[0]);
        if (player == null) return;
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 0.5f, 1.0f);
        player.spawnParticle(Particle.REDSTONE, player.getEyeLocation(),
                10, 0.5f, 0.5f, 0.5f, new Particle.DustOptions(Color.fromRGB(210, 180, 140), 4));
        RunicItemsAPI.addItem(player.getInventory(), determineStarterWeapon(player), player.getLocation());
    }
}
