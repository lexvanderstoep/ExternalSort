package uk.ac.cam.lmv34.fjava.tick0;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class BufferedArray {
	private int[] mData;
	private int mTotalLength;
	private int mIndex = 0;
	private int mOverallIndex = 0;
	private FileInputStream mInputStream;
	
	public BufferedArray (int bufferSize, FileInputStream inputStream, int totalLength) throws IOException {
		mData = new int[bufferSize];
		mTotalLength = totalLength;
		mInputStream = inputStream;
		
		reloadBuffer();
	}
	
	public boolean empty() {
		return mOverallIndex >= mTotalLength;
	}
	
	/**
	 * Gets the number at the current position in the array
	 * @throws IOException
	 */
	public int get() throws IOException {
		if (mOverallIndex >= mTotalLength) throw new IOException("End of buffer reached");
		
		return mData[mIndex];
	}
	
	/**
	 * Increments the current position by one (and refills the array if needed)
	 * @throws IOException
	 */
	public void skip() throws IOException {
		if (empty()) return;
		
		mIndex++;
		mOverallIndex++;
		
		if (mIndex >= mData.length) reloadBuffer();
	}
	
	/**
	 * The internal buffer will be refilled as far as possible with data
	 * @throws IOException
	 */
	private void reloadBuffer() throws IOException {
		int numbersToRead = Math.min(mData.length, mTotalLength - mOverallIndex);
		
		byte[] tempBlock = new byte[4 * numbersToRead];
		mInputStream.read(tempBlock, 0, 4 * numbersToRead);
		IntBuffer intB = ByteBuffer.wrap(tempBlock).asIntBuffer();
		intB.get(mData, 0, numbersToRead);
		
		mIndex = 0;
	}
}
