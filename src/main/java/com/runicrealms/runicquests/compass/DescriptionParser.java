package com.runicrealms.runicquests.compass;

import org.bukkit.ChatColor;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DescriptionParser {
    private static final Pattern pattern = Pattern.compile("(-?\\d+),\\s*?(-?\\d+),\\s*?(-?\\d+)");

    public static int[] parseCoordinates(List<String> inputList) {
        // Strip color codes and combine the list into a single string
        String input = inputList.stream()
                .map(ChatColor::stripColor)
                .collect(Collectors.joining(" "));
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            int x = Integer.parseInt(matcher.group(1));
            int y = Integer.parseInt(matcher.group(2));
            int z = Integer.parseInt(matcher.group(3));
            return new int[]{x, y, z};
        } else {
            // No match found
            return null;
        }
    }

    public static String parseQuestDescription(List<String> inputList) {
        // Strip color codes and combine the list into a single string
        String input = inputList.stream()
                .map(ChatColor::stripColor)
                .collect(Collectors.joining(" "));

        int questPosition = input.indexOf("QUEST");
        int atPosition = input.indexOf(" at ");

        if (questPosition != -1 && atPosition != -1 && questPosition < atPosition) {
            // Both "quest" and " at " were found, and "quest" comes before " at "
            return input.substring(questPosition + "quest".length(), atPosition).trim();
        } else {
            // Either "quest" or " at " was not found, or "quest" does not come before " at "
            // Return a suitable error state.
            // You may need to handle this in your application.
            return null;
        }
    }
}
