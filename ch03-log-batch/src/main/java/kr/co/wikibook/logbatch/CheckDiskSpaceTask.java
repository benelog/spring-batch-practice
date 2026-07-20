package kr.co.wikibook.logbatch;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class CheckDiskSpaceTask implements CommandLineRunner {

  private final SpaceChecker spaceChecker = new SpaceChecker();
  private final NotificationService notificationService;
  private final int minUsablePercentage;

  public CheckDiskSpaceTask(
      NotificationService notificationService,
      @Value("${disk.min-usable-percentage:10}") int minUsablePercentage) {
    this.notificationService = notificationService;
    this.minUsablePercentage = minUsablePercentage;
  }

  @Override
  public void run(String... args) {
    if (args.length < 1) {
      return;
    }
    String directory = args[0];
    int usablePercentage = spaceChecker.run(directory, minUsablePercentage);
    this.notificationService.send("남은 용량 " + usablePercentage + "%");
  }
}
