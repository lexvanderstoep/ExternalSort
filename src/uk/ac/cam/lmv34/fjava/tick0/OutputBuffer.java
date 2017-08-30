package uk.ac.cam.lmv34.fjava.tick0;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class OutputBuffer {
	private int[] mData;
	private int mIndex = 0;
	private FileOutputStream mOutputStream;
	
	public OutputBuffer (FileOutputStream oS, int bufferSize) {
		mData = new int[bufferSize];
		mOutputStream = oS;
	}
	
	/**
	 * Output a number to the output stream
	 * @param num
	 * @throws IOException
	 */
	public void write (int num) throws IOException {
		mData[mIndex] = num;
		mIndex++;
		if (mIndex >= mData.length) flush();
	}
	
	/**
	 * Flushes the content of the buffer
	 * @throws IOException
	 */
	public void flush() throws IOException {
		ByteBuffer byteBuffer = ByteBuffer.allocate(mIndex * 4);
		IntBuffer intBuffer = byteBuffer.asIntBuffer();
		intBuffer.put(mData, 0, mIndex);
		mOutputStream.write(byteBuffer.array(), 0, mIndex * 4);
		mOutputStream.flush();
		
		mIndex = 0;
	}
}
