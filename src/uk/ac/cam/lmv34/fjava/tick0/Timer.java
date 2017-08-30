package uk.ac.cam.lmv34.fjava.tick0;

public class Timer {
	private long startTime;
	
	public void start() {
		//startTime = System.currentTimeMillis();
		startTime = System.nanoTime();
	}
	
	public long elapsed() {
		return (System.nanoTime() - startTime)/1_000_000;
	}
}