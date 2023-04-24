package com.runicrealms.runicquests.command.system;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.runicitems.RunicItemsAPI;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@CommandAlias("tutorialweapon")
public class TutorialWeaponCMD extends BaseCommand {
    private static final String BOW_TEMPLATE_ID = "oaken-shortbow";
    private static final String MACE_TEMPLATE_ID = "oaken-mace";
    private static final String STAFF_TEMPLATE_ID = "oaken-branch";
    private static final String SWORD_TEMPLATE_ID = "oaken-sparring-sword";
    private static final String AXE_TEMPLATE_ID = "oaken-axe";

    private ItemStack determineStarterWeapon(Player player) {
        String className = RunicCore.getCharacterAPI().getPlayerClass(player);
        String templateID = "";
        switch (className.toLowerCase()) {
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
