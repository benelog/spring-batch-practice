package net.benelog.batch;

public class PrintTask {
	private String message;

	public void setMessage(String message) {
		this.message = message;
	}
	
	public void run(){
		System.out.println(message);
	}

}
