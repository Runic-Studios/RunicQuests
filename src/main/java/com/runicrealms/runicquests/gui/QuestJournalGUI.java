//package com.runicrealms.runicquests.gui;
//
//import com.runicrealms.plugin.RunicCore;
//import com.runicrealms.plugin.item.GUIMenu.ItemGUI;
//import com.runicrealms.runicquests.config.QuestLoader;
//import com.runicrealms.runicquests.quests.Quest;
//import org.bukkit.ChatColor;
//import org.bukkit.Material;
//import org.bukkit.entity.Player;
//import org.bukkit.inventory.ItemStack;
//
//import java.util.Comparator;
//import java.util.HashMap;
//import java.util.List;
//
//public class QuestJournalGUI {
//
//    private ItemGUI questJournalMenu(Player player, HashMap<String, String> goalMessages) {
//
//        ItemGUI questJournal = new ItemGUI("&f" + player.getName() + "'s &6Quest Journal", 54, event -> {}, RunicCore.getInstance());
//        questJournal.setOption(4, new ItemStack(Material.WRITABLE_BOOK), "&6Quest Journal",
//                "\n&7Here you can view every quest!" +
//                        "\n&7Quest Color Key:" +
//                        "\n&6[Gold] &7--> &6Main Storyline" +
//                        "\n&e[Yellow] &7 --> &eSide Quest" +
//                        "\n&b[Blue] &7--> &bRepeatable Quest" +
//                        "\n&c[Red] &7 --> &cMissing Requirement" +
//                        "\n&a[Green] &7 --> &aCompleted Quest!", 0, false);
//
//        // set the handler
//        questJournal.setHandler(event -> {
//            event.setWillClose(false);
//            event.setWillDestroy(true);
//        });
//
//        List<Quest> sortedList = QuestLoader.getUnusedQuestList();
//        sortedList.sort(Comparator.comparing(o -> o.getRequirements().getClassLvReq()));
//
//        int count = 9;
//
//        // in-progress quests
//        for (Quest q : sortedList) {
//
////            // skip not started quests
////            if (!quester.getCurrentQuests().containsKey(q)) continue;
////
////            // skip completed quests
////            if (quester.getCompletedQuests().contains(q.getName())) continue;
//
//            // setup item
//            ItemStack displayItem = new ItemStack(Material.LEGACY_BOOK_AND_QUILL);
//            String lore = "";
//
//            // add type-specific stuff
////            if (q.getPlanner().getCooldown() > 0) {
////                displayItem.setType(Material.LIGHT_BLUE_DYE);
////                lore += "&b[Repeatable Quest]";
////            } else if (q.getIsSide()) {
////                displayItem.setType(Material.DANDELION_YELLOW);
////                lore += "&e[Side Quest]";
////            } else {
//                displayItem.setType(Material.ORANGE_DYE);
//                lore += "&6[Main Story]";
//            //}
//
//            // setup item lore display
//            String dispName = ChatColor.YELLOW + "" + ChatColor.BOLD + q.getQuestName();
//            lore += ("\n\n&7Required Lv: " + ChatColor.GREEN + "[" + q.getRequirements().getClassLvReq() + "]" +
//                    "\n\n&7Quest Start:" +
//                    "\n&e" + q.getDescription().split(";")[0]) + " " +
//                    "&7at [" + (int) q.getFirstNPC().getStoredLocation().getX() + ", " +
//                    (int) q.getFirstNPC().getStoredLocation().getY() + ", " +
//                    (int) q.getFirstNPC().getStoredLocation().getZ() + "]";
//
////            // display current objective
////            lore +=  "\n\n&7Objective:";
////            lore += "\n&7" + quester.getCurrentStage(q).getObjectiveOverride();
////            if (quester.getCurrentStage(q).getCitizensToInteract().size() >= 1) {
////                int npcID = quester.getCurrentStage(q).getCitizensToInteract().getLast();
////                double x = CitizensAPI.getNPCRegistry().getById(npcID).getStoredLocation().getX();
////                double y = CitizensAPI.getNPCRegistry().getById(npcID).getStoredLocation().getY();
////                double z = CitizensAPI.getNPCRegistry().getById(npcID).getStoredLocation().getZ();
////                lore += " &7at [" + (int) x + ", " + (int) y + ", " + (int) z + "]";
////            }
////
////            // calculate percentage
////            double num = 0;
////            for (int i = 0; i < q.getStages().size(); i++) {
////                if (q.getStage(i) == quester.getCurrentStage(q)) {
////                    num = i;
////                }
////            }
////            int percentage = (int) Math.rint((num / q.getStages().size()) * 100);
////            dispName += (" &e" + percentage + "%");
////
//            questJournal.setOption(count, displayItem, dispName, lore, 0, false);
////            count++;
//        }
//
//        // not started quests
//        for (Quest q : sortedList) {
//
//            // skip started quests
//            if (quester.getCurrentQuests().containsKey(q)) continue;
//
//            // skip completed quests
//            if (quester.getCompletedQuests().contains(q.getName())) continue;
//
//            ChatColor levelColor;
//            if (player.getLevel() >= q.getRequirements().getLevel()) {
//                levelColor = ChatColor.GREEN;
//            } else {
//                levelColor = ChatColor.RED;
//            }
//
//            ChatColor questReqColor;
//            if (q.getRequirements().getNeededQuests().size() > 0 && quester.getCompletedQuests().size() > 0
//                    && quester.getCompletedQuests().contains(q.getRequirements().getNeededQuests().get(0))) {
//                questReqColor = ChatColor.GREEN;
//            } else {
//                questReqColor = ChatColor.RED;
//            }
//
//            // setup item
//            ItemStack displayItem = new ItemStack(Material.LEGACY_BOOK_AND_QUILL);
//            String lore = "";
//
//            // add type-specific stuff
//            if (q.getPlanner().getCooldown() > 0) {
//                displayItem.setType(Material.LIGHT_BLUE_DYE);
//                lore += "&b[Repeatable Quest]";
//            } else if (q.testRequirements(quester)) {
//                if (q.getIsSide()) {
//                    displayItem.setType(Material.DANDELION_YELLOW);
//                    lore += "&e[Side Quest]";
//                } else {
//                    displayItem.setType(Material.ORANGE_DYE);
//                    lore += "&6[Main Story]";
//                }
//            } else {
//                displayItem.setType(Material.ROSE_RED);
//                lore += "&c[Missing Requirement]";
//            }
//
//            // setup item lore display
//            String dispName = levelColor + "" + ChatColor.BOLD + q.getName();
//
//            lore += "\n\n&7Required Lv: " + levelColor + "[" + q.getRequirements().getLevel() + "]";
//            if (q.getRequirements().getNeededQuests().size() > 0) {
//                lore += "\n&7Required Quest(s): " + questReqColor + q.getRequirements().getNeededQuests().get(0);
//            }
//
//            lore += "\n\n&7Quest Start:" +
//                    "\n&e" + q.getDescription().split(";")[0] + " " +
//                    "&7at [" + (int) q.getFirstNPC().getStoredLocation().getX() + ", " +
//                    (int) q.getFirstNPC().getStoredLocation().getY() + ", " +
//                    (int) q.getFirstNPC().getStoredLocation().getZ() + "]";
//
//            questJournal.setOption(count, displayItem, dispName, lore, 0, false);
//            count++;
//        }
//
////        // completed quests
////        for (Quest q : sortedList) {
////
////            // skip all quests but completed quests
////            if (!quester.getCompletedQuests().contains(q.getName())) continue;
////
////            // setup item
////            ItemStack displayItem = new ItemStack(Material.CACTUS_GREEN);
////            String lore = "";
////
////            if (q.getPlanner().getCooldown() > 0) {
////                displayItem.setType(Material.LIGHT_BLUE_DYE);
////            }
////
////            // setup item lore display
////            String dispName = ChatColor.DARK_GREEN + "" + ChatColor.BOLD + q.getName();
////            lore += "&a[Complete]\n\n&2Rewards:";
////
////            if (q.getRewards().getMoney() > 0) {
////                lore += "\n &6- &f" + q.getRewards().getMoney() + " &6Coins";
////            }
////            if (q.getRewards().getExp() > 0) {
////                lore += "\n &a- &f" + q.getRewards().getExp() + " &aExperience";
////            }
////            if (q.getRewards().getQuestPoints() > 0) {
////                lore += "\n &a- &f" + q.getRewards().getQuestPoints() + " &aQuest Point(s)";
////            }
////
////            questJournal.setOption(count, displayItem, dispName, lore, 0, false);
////            count++;
////        }
//        return questJournal;
//    }
//}
