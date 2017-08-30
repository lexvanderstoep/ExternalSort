package uk.ac.cam.lmv34.fjava.tick0;

import java.io.*;

public class TestClass2 {
	public static void main(String args[]) throws IOException {
		RandomAccessFile correct = new RandomAccessFile("/Users/Lex/OneDrive/Documenten/Cambridge/Further Java/Tick 0/src/testLex correct.txt", "rw");
		RandomAccessFile file = new RandomAccessFile("/Users/Lex/OneDrive/Documenten/Cambridge/Further Java/Tick 0/src/testLex.txt", "rw");
		
		FileWriter out = new FileWriter("/Users/Lex/OneDrive/Documenten/Cambridge/Further Java/Tick 0/src/out.txt");
		PrintWriter printer = new PrintWriter(out);
		
		DataInputStream iS = new DataInputStream(new BufferedInputStream(new FileInputStream(correct.getFD())));
		DataInputStream iS2 = new DataInputStream(new BufferedInputStream(new FileInputStream(file.getFD())));
		
		
		int length = (int) (correct.length()/4);
		
		int prev1 = 0;
		int prev2 = 0;
		
		for (int i = 0; i < length; i++) {
			int one = iS.readInt();
			int two = iS2.readInt();

			//printer.print(one + " " + two + " " + i + "\n");
			if (one != prev1) {
				System.out.println(one + " " + i);
				prev1 = one;
			}
			if (two != prev2) {
				System.out.println(two + " " + i);
				prev2 = two;
			}
		}
		
		printer.flush();
		printer.close();
		
		System.out.println("DONE!");
	}
}
