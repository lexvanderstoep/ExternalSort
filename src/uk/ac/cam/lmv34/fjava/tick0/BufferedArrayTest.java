package uk.ac.cam.lmv34.fjava.tick0;

import static org.junit.Assert.*;

import java.io.*;

import org.junit.Test;

public class BufferedArrayTest {

	@Test
	public void test() throws IOException {
		/*
		 * Tests the entire BufferedArray class.
		 * It fills a text file with a number sequence. A BufferedArray then reads out the text file and
		 * if the class works correctly, should read the initial number sequence.
		 */
		
		// Set up parameters
		final int numbersToSort = 29;
		final int blockSize = 7;
		final RandomAccessFile file = new RandomAccessFile("/Users/Lex/Desktop/testFile1.txt", "rw");
		
		// Fill up test array
		int[] testNumbers = new int[numbersToSort];
		for (int i = 0; i < numbersToSort; i++) {testNumbers[i] = i;}
		
		// Write out test array to file
		DataOutputStream oS = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(
				file.getFD())));
		for (int i = 0; i < numbersToSort; i++) {oS.writeInt(testNumbers[i]);}
		oS.flush();
		
		// Create a BufferedArray and read in the data again (checking if it is correct)
		file.seek(0);
		FileInputStream iS = new FileInputStream(file.getFD());
		BufferedArray testArray = new BufferedArray(blockSize, iS, numbersToSort);
		for (int i = 0; i < numbersToSort; i++) {
			assert testArray.get() == testNumbers[i];
			testArray.skip();
		}
		
		// The BufferedArray should be empty now, as the whole file has been read
		assert testArray.empty();
	}
}
