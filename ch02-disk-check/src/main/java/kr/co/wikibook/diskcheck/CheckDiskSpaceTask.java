package kr.co.wikibook.diskcheck;

import java.io.File;

public class CheckDiskSpaceTask {

  public void run(String... args) {
    if (args.length < 2) {
      return;
    }
    String directory = args[0];
    int minUsablePercentage = Integer.parseInt(args[1]);
    var file = new File(directory);
    int actualUsablePercentage = (int) (file.getUsableSpace() * 100 / file.getTotalSpace());
    System.out.println("남은 용량 " + actualUsablePercentage + "%");
    if (actualUsablePercentage < minUsablePercentage) {
      throw new IllegalStateException("디스크 용량이 기대치보다 작습니다 : " + actualUsablePercentage + "% 사용 가능");
    }
  }
}
