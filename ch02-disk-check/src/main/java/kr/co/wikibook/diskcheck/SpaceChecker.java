package kr.co.wikibook.diskcheck;

import java.io.File;

public class SpaceChecker {

  public int run(String directory, int minUsablePercentage) {
    var file = new File(directory);
    int actualUsablePercentage = (int) (file.getUsableSpace() * 100 / file.getTotalSpace());
    if (actualUsablePercentage < minUsablePercentage) {
      throw new IllegalStateException("디스크 용량이 기대치보다 작습니다 : " + actualUsablePercentage + "% 사용 가능");
    }
    return actualUsablePercentage;
  }
}
