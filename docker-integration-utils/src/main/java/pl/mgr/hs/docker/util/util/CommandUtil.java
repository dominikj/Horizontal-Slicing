package pl.mgr.hs.docker.util.util;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** Created by dominik on 23.12.18. */
public class CommandUtil {

  private static final String SPACE_SPLITTER = " ";

  public static List<String> parseCommand(String command, Map<String, String> replacements) {
    if (StringUtils.isNotBlank(command)) {
      String replacedCommand = replaceVariablesInCommand(command, replacements);
      return splitCommand(replacedCommand);
    }
    return Collections.emptyList();
  }

  public static String replaceVariablesInCommand(String command, Map<String, String> replacements) {
    String replacedCommand = command;

    if (StringUtils.isBlank(command)) {
      return replacedCommand;
    }

    for (Map.Entry<String, String> replacement : replacements.entrySet()) {
      replacedCommand = replacedCommand.replace(replacement.getKey(), replacement.getValue());
    }
    return replacedCommand;
  }

  private static List<String> splitCommand(String command) {
    List<String> splittedCommand = new ArrayList<>();

    String[] splitted = command.split(SPACE_SPLITTER);
    for (int i = 0; splitted.length > i; ++i) {
      if (splitted[i].matches("^\".*")) {
        StringBuilder singleCommandAccumulator = new StringBuilder();
        do {
          singleCommandAccumulator.append(" ").append(splitted[i]);
          ++i;
        } while (splitted.length <= i || !splitted[i].matches(".*\"$"));

        singleCommandAccumulator.append(" ").append(splitted[i]);
        splittedCommand.add(singleCommandAccumulator.toString().replace("\"", ""));
        continue;
      }
      splittedCommand.add(splitted[i]);
    }
    return splittedCommand;
  }
}
