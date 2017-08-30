/**
 * ExternalSort comprises an external sorting algorithm. It sorts lists of integer values which are
 * too big to sort in memory.
 * 
 * The program was developed for Tick 0 of the Further Java course of the Cambridge Computer
 * Science Tripos Part 1B.
 * 
 * Lex van der Stoep - 6 July 2017
 */

package uk.ac.cam.lmv34.fjava.tick0;

import java.io.*;
import java.nio.channels.FileChannel;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.nio.IntBuffer;
import java.nio.ByteBuffer;

public class ExternalSort {

	/**
	 * Externally sort the contents of the file f1 and write the results out to f1. The file f2 can
	 * be used to temporarily store values. The algorithm divides the data into blocks of maximum
	 * size (what can be loaded into memory) and sorts these. Then, it will use external merge sort
	 * to combine the several blocks of sorted integers to obtain a fully sorted list.
	 * @param f1	The filename of the file which holds all the integers.
	 * @param f2	The filename of the file which can be used to temporarily store data. F2 is
	 * 				assumed to be of the same size as f1.
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void sort(String f1, String f2) throws FileNotFoundException, IOException {
		
		print("ExternalSort, Part IB: Further Java (Tick 0), Lex van der Stoep\n");
		
		long maxMemory = Runtime.getRuntime().maxMemory();	// Memory available to JVM
		int maxNumbers = (int) (0.27 * maxMemory / 4);		// Maximum number of integers we shall load
															// into memory at once
		boolean currentIsF1 = true;							// The working list is in f1
		int countingMax = (int) (0.1 * maxNumbers);			// The maximum range of numbers for which
															// we shall use counting sort
		int k = 16;											// parameter for k-way merge sort
		
		boolean print = false;
		
		// Open file handlers to the input files
		RandomAccessFile handler1 = new RandomAccessFile(f1, "rw");
		RandomAccessFile handler2 = new RandomAccessFile(f2, "rw");
		
		int totalNumbers = (int) (handler1.length() / 4);			// Total numbers to be sorted
		print("Numbers to sort: " + totalNumbers + "\n");
		
		
		if (print) {
			print("Input:");
			printAllBlocks(f1, maxNumbers);
			print("");
		}
		
		// Check if the data set is legible to sort using counting sort
		Range r = checkRange(handler1, maxNumbers);
		if (r.length() < countingMax) {
			
			// Run the counting sort algorithm on the data set
			print("STARTING COUNTING SORT");
			Timer countingT = new Timer();
			countingT.start();
			countingSort(handler1, r, maxNumbers);
			print("Counting sort took: " + countingT.elapsed() + " ms\n");
			
		} else {
			
			// Run the external merge sort algorithm on the data set
			
			
			// Divide the numbers in f1 into blocks of maximum length, read and
			// sort these blocks individually and write them out to f2
			print("STARTING INITIAL SORT");
			Timer initialSortT = new Timer();
			initialSortT.start();
			boolean sorted = sortBlocks(handler1, handler2, maxNumbers);
			currentIsF1 = false;
			print("Initial sorting took: " + initialSortT.elapsed() + " ms\n");

			if (print) {
				print("Result of initial sort:");
				printAllBlocks(f2, maxNumbers);
				print("");
			}

			// If all numbers fit in memory, there is no need to do external merge sort
			// The following does a bottom-up external merge sort
			if (!sorted) {
				
				print("STARTING EXTERNAL MERGE SORT");
				Timer externalSortT = new Timer();
				externalSortT.start();

				int blockSize = maxNumbers; // size of the sorted blocks which
											// will be merged

				// Start the merge sort, repeat until full list is sorted
				while (blockSize < totalNumbers) {
					Timer mergeT = new Timer();
					mergeT.start();
					
					print("Merging blocks (length: " + blockSize + ")");
					
					// Reset all the file handlers
					handler1.seek(0);
					handler2.seek(0);
					
					// Perform one sweep of external merge sort
					String in;
					String out;
					if (currentIsF1) {
						in = f1;
						out = f2;
					} else {
						in = f2;
						out = f1;
					}
					externalMerge(in, out, blockSize, k, maxNumbers);
					if (print) {
						print("Result of merging blocks (length: " + blockSize + ")");
						printAllBlocks(out, k * blockSize);
						print("");
					}
					
					// Update which file is the working file and double the block size
					currentIsF1 = !currentIsF1;
					blockSize *= k;
					print("Merging took " + mergeT.elapsed() + " ms");

				}
				
				print("External merge sort took: " + externalSortT.elapsed() + " ms\n");
			}
		}
		
		// If the final sorted list of number is in f2, then copy its contents to f1
		if (!currentIsF1) {
			copyContents(handler2, handler1);
		}
		
		if (print) {
			print("Output:");
			printAllBlocks(f1, maxNumbers);
			print("");
		}
		
		// Close all file handlers
		handler1.close();
		handler2.close();
		
		print("DONE");
	}
	
	/**
	 * Checks if the given file is sorted
	 * @param file
	 * @param maxNumbers	The maximum number of integers to be read into memory
	 * @return				True if and only if the file is sorted
	 * @throws IOException
	 */
	private static boolean sorted(RandomAccessFile file, int maxNumbers) throws IOException {
		// Create input buffer
		file.seek(0);
		FileInputStream iS = new FileInputStream(file.getFD());
		int length = (int) (file.length()/4);
		BufferedArray buffer = new BufferedArray(maxNumbers, iS, length);
		
		// Go through the data, checking if it is sorted
		int previous = Integer.MIN_VALUE;
		for (int i = 0; i < length; i++) {
			int temp = buffer.get();
			if (temp < previous) return false;
			previous = temp;
			buffer.skip();
		}
		
		return true;
	}
	
	/**
	 * Determines the range of the data in the file. It finds the minimum and 
	 * maximum value
	 * @param file
	 * @param maxNumbers	The maximum number of integer to be read into memory
	 * @return				The minimum and maximum value of the data
	 * @throws IOException
	 */
	private static Range checkRange(RandomAccessFile file, int maxNumbers) throws IOException {
		// Create input buffer
		file.seek(0);
		FileInputStream iS = new FileInputStream(file.getFD());
		int length = (int) (file.length()/4);
		BufferedArray buffer = new BufferedArray(maxNumbers, iS, length);
		
		// Go through the data and determine the min and max values
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		for (int i = 0; i < length; i++) {
			int temp = buffer.get();
			if (temp < min) min = temp;
			if (temp > max) max = temp;
			buffer.skip();
		}
		
		return new Range(min, max);
	}
	
	/**
	 * Sorts the given file using the counting sort algorithm. The numbers
	 * are within a known range (given as input). The method counts the
	 * number of occurences of each integer in the file. Then, it writes
	 * out a sorted file by placing an appriopriate number of copies of 
	 * each integer in the file.
	 * @param file
	 * @param range			The range of the input data
	 * @param maxNumbers	The maximum number of integer to be read in memory
	 * @throws IOException
	 */
	private static void countingSort(RandomAccessFile file, Range range, int maxNumbers) throws IOException {
		// Create the input buffer
		file.seek(0);
		FileInputStream iS = new FileInputStream(file.getFD());
		int length = (int) (file.length()/4);
		BufferedArray buffer = new BufferedArray(maxNumbers, iS, length);
		
		int[] counts = new int[(int) (range.length() + 1)];
		int low = (int) range.getMin();
		
		// Go through the data to count the occurences of numbers
		for (int i = 0; i < length; i++) {
			int temp = buffer.get();
			counts[temp - low]++;
			buffer.skip();
		}
		
		// Create the output buffer
		file.seek(0);
		FileOutputStream oS = new FileOutputStream(file.getFD());
		OutputBuffer out = new OutputBuffer(oS, maxNumbers);
		
		// Write out the numbers
		for (int i = 0; i < counts.length; i++) {
			int count = counts[i];
			int num = (int) (i + range.getMin());
			for (int j = 0; j < count; j++) {
				out.write(num);
			}
		}
		
		out.flush();
	}
	
	/**
	 * Copies the content of an input file to an output file
	 * @param in
	 * @param out
	 * @param maxNumbers	The maximum number of integers to be read in memory
	 * @throws IOException
	 */
	private static void copyContents(RandomAccessFile in, RandomAccessFile out) 
			throws IOException {
		// Open two data streams (I/O) and then transfer the data from in to out
		in.seek(0);
		out.seek(0);
		FileInputStream inS = new FileInputStream(in.getFD());
		FileOutputStream outS = new FileOutputStream(out.getFD());
		FileChannel inC = inS.getChannel();
		FileChannel outC = outS.getChannel();
		inC.transferTo(0, inC.size(), outC);
	}
	
	/**
	 * Given a list of numbers in currentFile which is subsorted (each block of size blockSize is
	 * sorted), externalMerge will perform a k-way merge on these lists and write the result in newFile.
	 * @param currentFile1	input file, contains the sorted blocks to be merged
	 * @param currentFile2	another handler to the input file, allows reading from two different
	 * 						positions simultaneously
	 * @param newFile		output file, the merged blocks will be written here
	 * @param blockSize		size of the sorted blocks
	 * @param k				the number of blocks to merge in one go
	 * @param maxNumbers	the maximum number of integers to be stored in memory
	 * @throws IOException 
	 */
	private static void externalMerge (String currentFile, String newFile, int blockSize, 
			int k, int maxNumbers) throws IOException {
		
		if (k < 2) throw new IllegalArgumentException("K-way merge requires k to be at least 2");
		
		// Initialise IO file access
		RandomAccessFile[] inputFiles = new RandomAccessFile[k];
		for (int i = 0; i < k; i++) inputFiles[i] = new RandomAccessFile(currentFile, "r");
		RandomAccessFile out = new RandomAccessFile(newFile, "rw");
		
		// Reset file pointers
		for (RandomAccessFile r : inputFiles) r.seek(0);
		out.seek(0);
		
		// Initialise IO streams
		FileInputStream[] inputStreams = new FileInputStream[k];
		for (int i = 0; i < k; i++) inputStreams[i] = new FileInputStream(inputFiles[i].getFD());
		FileOutputStream outputStream = new FileOutputStream(out.getFD());
		
		// Initialise output buffer
		OutputBuffer outputB = new OutputBuffer(outputStream, (int) (0.4 * maxNumbers));
		
		
		int numbersToSort = (int) (inputFiles[0].length()/4); 	// total number of values to be sorted
		int totInputBufferSize = (int) (0.8 * maxNumbers);		// max number of integers to be stored
																// in buffers
		
		
		// This function should not have been called if any of the following asserts fails
		assert (blockSize < numbersToSort);
		assert (maxNumbers <= blockSize);
		
		
		// Set the indices of the start and last elements of the blocks
		int[] startIndices = new int[k];
		int[] endIndices = new int[k];
		for (int i = 0; i < k; i++) {
			startIndices[i] = i * blockSize;
			endIndices[i] = (i+1) * blockSize - 1;
		}

		// Move the handlers to the start of the blocks
		for (int i = 0; i < k; i++) inputStreams[i].skip(4 * startIndices[i]);
		
		// Go through the data, sequentially merging two blocks
		// This outer loop (while) sets up the blocks, the inner loop (while) goes through the data 
		// in the blocks
		while (startIndices[0] < numbersToSort) {
			
			// If there is not more than one block of numbers to merge, simlpy copy the block
			if (startIndices[1] >= numbersToSort) {

				BufferedArray buffer = new BufferedArray(totInputBufferSize, inputStreams[0], 
														 numbersToSort - startIndices[0]);
				for (int i = 0; i < numbersToSort - startIndices[0]; i++) {
					int tmp = buffer.get();
					buffer.skip();
					outputB.write(tmp);
				}
				break;
			}
			
			// Check if any of the blocks are empty and update the indices accordingly
			boolean nonFullBlockFound = false;
			for (int i = 0; i < k; i++) {
				if (nonFullBlockFound) {
					endIndices[i] = startIndices[i] - 1; // This indicates an empty block
				} else if (endIndices[i] > numbersToSort) {
					endIndices[i] = numbersToSort - 1;
					nonFullBlockFound = true;
				}
			}
			
			// Create input buffers
			int readSize = (int) (totInputBufferSize / k);
			BufferedArray[] blocks = new BufferedArray[k];
			for (int i = 0; i < k; i++) {
				blocks[i] = new BufferedArray(readSize, inputStreams[i], 
											  endIndices[i] - startIndices[i] + 1);
			}
			
			// Start the merge sort
			int[] numbers = new int[k];
			int nonEmptyBlocks = 0;
			for (int i = 0; i < k; i++) {
				if (!blocks[i].empty()) {
					numbers[i] = blocks[i].get();
					nonEmptyBlocks++;
				}
			}
			
			while (nonEmptyBlocks > 0) {
				int minIdx = -1;				// index of the min value
				int min = Integer.MAX_VALUE;	// min value
				
				nonEmptyBlocks = 0;
				
				// Find the minimum value of the blocks
				for (int i = 0; i < k; i++) {
					if (!blocks[i].empty()) {
						if (numbers[i] <= min) {
							// Found a new minimum
							minIdx = i;
							min = numbers[i];
						}
						nonEmptyBlocks++;
					}
				}
				
				// Write out the min value and update the corresponding block/pointer
				if (nonEmptyBlocks > 0) {
					outputB.write(min);
					blocks[minIdx].skip();
					if (!blocks[minIdx].empty()) numbers[minIdx] = blocks[minIdx].get();
				}
			}

			// Update the pointers
			int skipNumbers = k * blockSize;
			for (int i = 0; i < k; i++) {
				startIndices[i] += skipNumbers;
				endIndices[i] += skipNumbers;
				inputStreams[i].skip(4 * (skipNumbers - blockSize));
			}
		}
		
		outputB.flush();
	}
	
	private static void print (Object o) {
		System.out.println(o);
	}
	
	/**
	 * Divide the numbers in f1 into blocks of a given length,
	 * sort these blocks seperately and write them out to f2
	 * @param f1			input file, contains the data to block sort
	 * @param f2			output file, the blocks of sorted data will be written here
	 * @param maxNumbers	the maximum number of integers to be stored in memory
	 * @return				True iff sortBlocks has fully sorted the array
	 * @throws IOException
	 */
	private static boolean sortBlocks (RandomAccessFile f1, RandomAccessFile f2, int maxNumbers)
			throws IOException {
		
		Timer IO = new Timer();
		int totIOTime = 0;
		// Initialise IO streams
		f1.seek(0);
		f2.seek(0);
		FileInputStream iS = new FileInputStream(f1.getFD());
		FileOutputStream oS = new FileOutputStream(f2.getFD());
		
		
		long totalNumbers = f1.length()/4;	//total numbers to be read
		long numbersRead = 0;				//total numbers processed (read, sorted, written)
		
		// Start processing the numbers.
		// Each iteration processes one block of numbers.
		int numberOfBlocks = 0;
		
		while (numbersRead < totalNumbers) {
			// Initialise an int array to hold one block of numbers
			int currentBlockSize = maxNumbers;
			if (totalNumbers - numbersRead < maxNumbers) { //less than one block left
				currentBlockSize = (int)(totalNumbers - numbersRead);
			}

			// Read in one block
			IO.start();
			byte[] tempBlock = new byte[4 * currentBlockSize];
			final int[] block = new int[currentBlockSize];
			iS.read(tempBlock);
			IntBuffer intB = ByteBuffer.wrap(tempBlock).asIntBuffer();
			intB.get(block);
			numbersRead += currentBlockSize;
			intB = null;
			tempBlock = null;
			totIOTime += IO.elapsed();

			Arrays.sort(block);
			
			// Write out the block
			IO.start();
			ByteBuffer byteBuffer = ByteBuffer.allocate(block.length * 4);
	        intB = byteBuffer.asIntBuffer();
	        intB.put(block);
			oS.write(byteBuffer.array());
			totIOTime += IO.elapsed();
			

			numberOfBlocks++;
		}
		
		print("IO took " + totIOTime + " ms");
		
		IO.start();
		oS.flush();
		totIOTime += IO.elapsed();
		
		// If there was only one block, all the data will now be completely sorted
		return numberOfBlocks <= 1;
	}
	
	/**
	 * Print out the contents of the file, in blocks
	 * @param file
	 * @param maxNumbers	The maximum number of integers to be read in memory
	 * @throws IOException
	 */
	private static void printAllBlocks(String file, int maxNumbers) throws IOException {
		RandomAccessFile in = new RandomAccessFile(file, "r");
		in.seek(0);
		DataInputStream iS = new DataInputStream(new BufferedInputStream(new FileInputStream(in.getFD())));
		
		long totalNumbers = in.length()/4;
		int blockCount = 0;
		
		for (int i = 0; i < totalNumbers; i++) {
			if (i % maxNumbers == 0) {
				blockCount++;
				System.out.println("Block: " + blockCount);
			}
			
			System.out.println(iS.readInt());
		}
	}
	
	private static String byteToHex(byte b) {
		String r = Integer.toHexString(b);
		if (r.length() == 8) {
			return r.substring(6);
		}
		return r;
	}

	public static String checkSum(String f) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			DigestInputStream ds = new DigestInputStream(
					new FileInputStream(f), md);
			byte[] b = new byte[512];
			while (ds.read(b) != -1)
				;

			String computed = "";
			for(byte v : md.digest()) 
				computed += byteToHex(v);

			return computed;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "<error computing checksum>";
	}

	public static void main(String[] args) throws Exception {
		String f1 = args[0];
		String f2 = args[1];
		sort(f1, f2);
		System.out.println("The checksum is: "+checkSum(f1));
	}
}
