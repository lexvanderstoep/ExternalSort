package uk.ac.cam.lmv34.fjava.tick0;

class NumberEntry implements Comparable<NumberEntry>{
	int value;
	int index;
	
	public NumberEntry(int v, int i) {
		value = v;
		index = i;
	}
	
	public int compareTo(NumberEntry e) {
		if (value < e.value) return -1;
		return 1;
	}
}