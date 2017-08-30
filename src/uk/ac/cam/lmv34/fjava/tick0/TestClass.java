package uk.ac.cam.lmv34.fjava.tick0;

import java.io.*;
import java.util.Arrays;
import java.util.Random;

public class TestClass {
	public static void main (String args[]) throws IOException {
		int size = 1000;
		
		Random rnd = new Random();
		RandomAccessFile file = new RandomAccessFile("/Users/Lex/OneDrive/Documenten/Cambridge/Further Java/Tick 0/src/testLex.txt", "rw");
		FileOutputStream oS = new FileOutputStream(file.getFD());
		RandomAccessFile file2 = new RandomAccessFile("/Users/Lex/OneDrive/Documenten/Cambridge/Further Java/Tick 0/src/testLex2.txt", "rw");
		FileOutputStream oS2 = new FileOutputStream(file2.getFD());
		file.setLength(0);
		file2.setLength(0);
		OutputBuffer b1 = new OutputBuffer(oS, 1_000_000);
		OutputBuffer b2 = new OutputBuffer(oS2, 1_000_000);
		
		//String checkString = "/Users/Lex/OneDrive/Documenten/Cambridge/Further Java/Tick 0/src/testLexCheck.txt";
		//RandomAccessFile checkFile = new RandomAccessFile(checkString, "rw");
		//DataOutputStream oC = new DataOutputStream(new BufferedOutputStream(
		//		new FileOutputStream(checkFile.getFD())));
		
		for (int i = 0; i < size; i++) {
			int random = rnd.nextInt(10_000_000);
			b1.write(random);
			b2.write(0);
		}
		
		/*for (int i = 0; i < size; i++) {
			//oC.writeInt(arr[i]);
			System.out.println(arr[i]);
		}*/
		
		b1.flush();
		b2.flush();
		oS.flush();
		oS2.flush();
		//oC.flush();
		oS.close();
		oS2.close();
		//oC.close();
		file.close();
		file2.close();
		//checkFile.close();
		
		System.out.println("DONE");
	}
}
