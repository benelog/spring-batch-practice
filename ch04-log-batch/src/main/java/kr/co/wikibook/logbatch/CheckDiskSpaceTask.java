package kr.co.wikibook.logbatch;

import org.slf4j.Logger;
import org.slf4j.ILoggerFactory;
import org.springframework.boot.CommandLineRunner;

//@Component
public class CheckDiskSpaceTask implements CommandLineRunner {

  private final SpaceChecker spaceChecker = new SpaceChecker();
  private final Logger logger;

  public CheckDiskSpaceTask(ILoggerFactory loggerFactory) {
    this.logger = loggerFactory.getLogger(CheckDiskSpaceTask.class.getName()); // <1>
  }

  @Override
  public void run(String... args) {
    if (args.length < 2) {
      return;
    }
    int usablePercentage = spaceChecker.run(args[0], Integer.parseInt(args[1]));
    logger.info("남은 용량 {}%", usablePercentage);
  }
}
