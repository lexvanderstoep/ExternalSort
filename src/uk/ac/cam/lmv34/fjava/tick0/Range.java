package uk.ac.cam.lmv34.fjava.tick0;

public class Range {
	private long mMin;
	private long mMax;
	
	public Range (long min, long max) {
		mMin = min;
		mMax = max;
	}
	
	public boolean contains (long number) {
		return (number >= mMin && number <= mMax);
	}
	
	public long getMin() {
		return mMin;
	}
	public long getMax() {
		return mMax;
	}
	
	public long length() {
		return (mMax - mMin);
	}
}
