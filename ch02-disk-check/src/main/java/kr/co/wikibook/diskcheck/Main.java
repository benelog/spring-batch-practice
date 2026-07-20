package kr.co.wikibook.diskcheck;

public class Main {
  public static void main(String[] args) {
    if (args.length < 2) {
      System.out.println("검사할 디렉터리와 남은 용량의 기대치(%)를 차례로 입력해야 합니다.");
      return;
    }
    new CheckDiskSpaceTask().run(args[0], Integer.parseInt(args[1]));
  }
}
