package ko.co.wikibook.retry;

public class FragileService extends UnstableNotificationService implements NotificationCircuitService {
	public FragileService(int failures) {
		super(failures);
	}

	@Override
	public boolean sendOnCircuit(String message) {
		return super.send(message);
	}
}
