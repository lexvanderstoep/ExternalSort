package uk.ac.cam.lmv34.fjava.tick0;

import static org.junit.Assert.*;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.junit.Test;

public class OutputBufferTest {

	@Test
	public void test() throws IOException {
		/*
		 * Tests the entire OutputBuffer class.
		 * An OutputBufer then writes out a generated number sequence. The file will then be read and 
		 * if the class works correctly, should read the initial number sequence.
		 */
		
		// Set up parameters
		final int numbersToWriteOut = 29;
		final int blockSize = 7;
		final RandomAccessFile file = new RandomAccessFile("/Users/Lex/Desktop/testFile2.txt", "rw");
		
		// Fill up test array
		int[] testNumbers = new int[numbersToWriteOut];
		for (int i = 0; i < numbersToWriteOut; i++) {testNumbers[i] = i;}
		
		// Create an OutputBuffer and write out the data
		FileOutputStream oS = new FileOutputStream(file.getFD());
		OutputBuffer outBuffer = new OutputBuffer(oS, 7);
		for (int i = 0; i < numbersToWriteOut; i++) { outBuffer.write(testNumbers[i]); }
		outBuffer.flush();
		
		// Read in the data again and check if it is correct
		file.seek(0);
		DataInputStream iS = new DataInputStream(new FileInputStream(file.getFD()));
		for (int i = 0; i < numbersToWriteOut; i++) {
			assert iS.readInt() == testNumbers[i];
		}
	}
}
