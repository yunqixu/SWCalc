package cn.jintongsoft.calc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class ConvertFile {
	public static void convert(String input, String output) throws Exception{
		BufferedReader br = new BufferedReader(new FileReader(new File(input)));
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(output)));
		
		String s = null;
		
		while ((s = br.readLine()) != null){
			
			String[] array = s.split("\\s+");
			
			if (s.length() > 10){
				String vector = array[array.length - 1] + "\t";
				vector = vector + "word|" + array[0] + "\t";
				vector = vector + "pos|" + array[1] + "\t";
				vector = vector + "prev2Word|" + array[2] + "\t";
				vector = vector + "prev1Word|" + array[3] + "\t";
				vector = vector + "next1Word|" + array[4] + "\t";
				vector = vector + "next2Word|" + array[5] + "\t";
				vector = vector + "prev2Pos|" + array[6] + "\t";
				vector = vector + "prev1Pos|" + array[7] + "\t";
				vector = vector + "next1Pos|" + array[8] + "\t";
				vector = vector + "next2Pos|" + array[9] + "\t";
				vector = vector + "position=" + array[10];
				
				bw.write(vector);
				bw.newLine();
				bw.flush();
			}
			
			else {
				bw.write(s);
				bw.newLine();
				bw.flush();
			}
		}
		br.close();
		bw.close();
	}
	
	public static void main(String[] args){
		
		try{
			String input = "./data/train";
			String output = "newTrain";
			ConvertFile.convert(input, output);
			
			input = "./data/develop";
			output = "newDevelop";
			ConvertFile.convert(input, output);
			
			input = "./data/tester";
			output = "newTest";
			ConvertFile.convert(input, output);
		}
		catch (Exception e){
			e.printStackTrace();
			System.exit(0);
		}
		
		
	}
}
