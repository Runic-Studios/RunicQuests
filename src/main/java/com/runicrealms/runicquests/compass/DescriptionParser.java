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

        // Get the first position of the tip line
        int stopPosition = input.indexOf("Location/Tip");

        // If "at" comes before period or exclamation, or if period or exclamation do not exist, use "at" as the stop position
        if (stopPosition == -1 || atPosition != -1 && atPosition < stopPosition) {
            stopPosition = atPosition;
        }

        if (questPosition != -1 && stopPosition != -1 && questPosition < stopPosition) {
            // Both "quest" and the stop position were found, and "quest" comes before the stop position
            return input.substring(questPosition + "quest".length(), stopPosition).trim();
        } else {
            // Either "quest" or the stop position was not found, or "quest" does not come before the stop position
            // Return a suitable error state.
            // You may need to handle this in your application.
            return null;
        }
    }
}
