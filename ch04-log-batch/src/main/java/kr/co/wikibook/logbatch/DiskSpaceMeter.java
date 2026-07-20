package kr.co.wikibook.logbatch;

import java.io.File;

public class DiskSpaceMeter {

  public int getUsablePercentage(String directory) {
    var file = new File(directory);
    return (int) (file.getUsableSpace() * 100 / file.getTotalSpace());
  }
}
