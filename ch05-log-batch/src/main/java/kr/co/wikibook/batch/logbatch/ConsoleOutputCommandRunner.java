package kr.co.wikibook.batch.logbatch;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import org.springframework.batch.core.step.tasklet.CommandRunner;

public class ConsoleOutputCommandRunner implements CommandRunner {
  @Override
  public Process exec(String[] command, String[] environmentParams, File directory)
      throws IOException {
    ProcessBuilder builder = new ProcessBuilder(command)
        .redirectOutput(ProcessBuilder.Redirect.INHERIT)
        .redirectError(ProcessBuilder.Redirect.INHERIT)
        .directory(directory);

    Map<String, String> environment = builder.environment();
    copyKeyValuePairs(environmentParams, environment);

    return builder.start();
  }

  private static void copyKeyValuePairs(String[] source, Map<String, String> destination) {
    if (source == null) {
      return;
    }

    for (String pair : source) {
      String[] keyValue = pair.split("=");
      if (keyValue.length != 2) {
        continue;
      }
      destination.put(keyValue[0], keyValue[1]);
    }
  }
}
