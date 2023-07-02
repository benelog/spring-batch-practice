package kr.co.wikibook.batch.logbatch;

public class MockNotificationService implements NotificationService {

  private String lastMessage;

  @Override
  public void send(String message) {
    lastMessage = message;
  }

  public String getLastMessage() {
    return lastMessage;
  }
}
