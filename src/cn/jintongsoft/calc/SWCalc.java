package cn.jintongsoft.calc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

public class SWCalc {
	
	private int PER = 7;
	private ArrayList<ArrayList<String>> set = new ArrayList<>();
	

	/**
	 * 
	 * @param input
	 *   The name of the file containing the corpus
	 * @throws Exception
	 */
	public void getAllFeatureVectors (String input) throws Exception{
		BufferedReader br = new BufferedReader(new FileReader(new File(input)));
		String s = null;
		while ((s = br.readLine()) != null){
			String t = br.readLine();
			extractFeature(s,t);
		}
		br.close();
	}
	
	/**
	 * 
	 * @param train
	 *   Name of the train file
	 * @param test
	 *   Name of the test file
	 * @throws Exception
	 */
	public void split(String train, String test) throws Exception {
		split(train, test, PER);
	}
	
	/**
	 * Training process using the CRF tool
	 * @param trainFile
	 *   The train file
	 * @param templateFile
	 *   The template file
	 * @param modelFile
	 *   The model file
	 * @throws Exception
	 */
	public void train(String trainFile, String templateFile, String modelFile) throws Exception{
		String[] s = {"./CRF/crf_learn.exe",templateFile,trainFile,modelFile};
		//System.out.println(s);
		Runtime runtime = Runtime.getRuntime();
		Process process = runtime.exec(s);
		InputStreamReader isr = new InputStreamReader(process.getInputStream());
		BufferedReader br = new BufferedReader(isr);
		String line = null;
		while ((line = br.readLine()) != null){
			//System.out.println(line);
		}
		br.close();
		
		int exitval = process.waitFor();
		//System.out.println("exitValue:" + exitval);
		
		process.destroy();
	}
	
	/**
	 * Predict the labels of the sentences
	 * @param modelFile
	 *   The model file trained by the training function
	 * @param testFile
	 *   The test file
	 * @param targetFile
	 *   The result file predicted by the test function
	 * @throws Exception
	 */
	public void test (String modelFile, String testFile, String targetFile) throws Exception {
		String[] s = {"cmd", "/c", ".\\CRF\\crf_test.exe -v2 -m " + modelFile + " " + testFile + " -o " + targetFile};
		//System.out.println(s[2]);
		Runtime runtime = Runtime.getRuntime();
		Process process = runtime.exec(s);
		InputStreamReader isr = new InputStreamReader(process.getInputStream());
		BufferedReader br = new BufferedReader(isr);
		String line = null;
		while ((line = br.readLine()) != null){
			//System.out.println(line);
		}
		br.close();
		
		int exitval = process.waitFor();
		//System.out.println("exitValue:" + exitval);
		
		process.destroy();
		evaluation(targetFile);
	}
	
	public HashMap<Integer,Double> predictSentence(String s) throws Exception{
		
		ArrayList<ArrayList<String>> list = new ArrayList<ArrayList<String>>();
		String input = "singleSentInput";
		String output = "singleSentOutput";
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(input)));
		bw.write(s);
		bw.newLine();
		bw.flush();
		bw.close();
		getTestFeatureVectors(input, output, list);
		
		String modelFile = "./data/model";
		String targetFile = "singleSentTarget";
		
		test(modelFile, output, targetFile);
		return parseResult(targetFile);
	}
	

	/**
	 * 
	 * @param input
	 *   Name of the input file, the sentences of the file comes singularly
	 * @param output
	 *   Name of the test file, the default flag is false
	 */
	public void getTestFeatureVectors(String input, String output, ArrayList<ArrayList<String>> list){
		try{
			BufferedReader br = new BufferedReader(new FileReader(new File(input)));
			String s = null;
			while ((s = br.readLine()) != null){
				extractFeature(s, list);
			}
			br.close();
			writeToFile(output, list);
		}
		catch(Exception e){
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	
	private HashMap<Integer,Double> parseResult(String inputFile) throws Exception{
		HashMap<Integer,Double> map = new HashMap<Integer,Double>();
		BufferedReader br = new BufferedReader(new FileReader(new File(inputFile)));
		String s = null;
		int tag = 0;
		while ((s = br.readLine()) != null){
			String[] results = s.split("\\s+");
			if (results.length != 15) continue;
			String labelAndProb = results[results.length - 1];
			int index = labelAndProb.indexOf("/");
			String prob = labelAndProb.substring(index + 1, labelAndProb.length());
			double probability = Double.parseDouble(prob);
			map.put(tag, probability);
			tag = tag + 1;
		}
		return map;
	}
	
	private void evaluation(String resultFile) throws Exception{
		BufferedReader br = new BufferedReader(new FileReader(resultFile));
		String s = null;
		double total = 0;
		double pre = 0;
		double recall = 0;
		while ((s = br.readLine()) != null){
			String[] str = s.split("\\s+");
			if (str.length == 15){
				String result = str[11];
				String test = str[12];
				test = test.substring(0, test.indexOf("/"));
				if (test.equals("true") && result.equals("true")){
					total++;
				}
				if (test.equals("true")){
					pre++;
				}
				if (result.equals("true")){
					recall++;
				}
			}
		}
		br.close();
		//System.out.println(total+" "+pre+" "+recall);
		double p = total / pre;
		double r = total / recall;
		System.out.println("precision:"+p);
		System.out.println("recall:"+r);
		double f = 2*p*r/(p+r);
		System.out.println("F-score:"+f);
	}
	
	private void writeToFile(String output, ArrayList<ArrayList<String>> list){
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(output)));
			for (int i = 0; i < list.size(); i++){
				ArrayList<String> arrlist = list.get(i);
				for (String vector : arrlist){
					bw.write(vector);
					bw.newLine();
					bw.flush();
				}
				bw.newLine();
				bw.flush();
			}
		}
		catch (Exception e){
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	private void split(String train, String test, int k) throws Exception{
		setPER(k);
		try{
			File file = new File(train);
			if (!file.exists()){
				file.createNewFile();
			}
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(train)));
			int size = set.size();
			int th = size * PER / 10;
			for (int i = 0; i < th; i++){
				ArrayList<String> a = set.get(i);
				for (String vec : a){
					bw.write(vec);
					bw.newLine();
					bw.flush();
				}
				bw.newLine();
				bw.flush();
			}
			bw.close();
			
			file = new File(test);
			if (!file.exists()){
				file.createNewFile();
			}
			bw = new BufferedWriter(new FileWriter(new File(test)));
			for (int i = th; i < size; i++){
				ArrayList<String> a = set.get(i);
				for (String vec : a){
					bw.write(vec);
					bw.newLine();
					bw.flush();
				}
				bw.newLine();
				bw.flush();
			}
			bw.close();
		}
		catch (Exception e){
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	private void extractFeature(String s, ArrayList<ArrayList<String>> list){
		
		String[] tokens = s.split("\\s+");
		
		ArrayList<String> vectors = new ArrayList<String>();
		
		ArrayList<String> words = new ArrayList<>();
		ArrayList<String> poses = new ArrayList<>();
		for (String token : tokens){
			int index = token.lastIndexOf("/");
			if (index == -1) continue;
			String word = token.substring(0, index);
			String pos = token.substring(index+1);
			if (pos.endsWith(",")){
				pos = pos.substring(0, pos.length() - 1);
			}
			words.add(word);
			poses.add(pos);
		}
		for (int i = 0; i < words.size(); i++){
			String vector = getFeature(words, poses, i);
			vector = vector + " " + false;
			vectors.add(vector);
		}
		
		list.add(vectors);
	}
	
	private void extractFeature(String s, String t){
		String[] tokens = s.split("\\s+");
		String[] contents = t.split("\\s+");
		
		ArrayList<String> vectors = new ArrayList<String>();
		
		ArrayList<String> words = new ArrayList<>();
		ArrayList<String> poses = new ArrayList<>();
		for (String token : tokens){
			int index = token.lastIndexOf("/");
			if (index == -1) continue;
			String word = token.substring(0, index);
			String pos = token.substring(index+1);
			if (pos.endsWith(",")){
				pos = pos.substring(0, pos.length() - 1);
			}
			words.add(word);
			poses.add(pos);
		}
		for (int i = 0; i < words.size(); i++){
			String vector = getFeature(words, poses, i);
			boolean flag = getFlag(words, poses, contents, i);
			vector = vector + " " + flag;
			vectors.add(vector);
		}
		
		set.add(vectors);
	}
	
	private String getFeature(ArrayList<String> words, ArrayList<String> poses, int i){
		String vector = words.get(i) + " " + poses.get(i);
		if (i - 2 < 0){
			vector = vector + " word_-2";
		}
		else {
			vector = vector + " " + words.get(i-2);
		}
		if (i - 1 < 0){
			vector = vector + " word_-1";
		}
		else{ 
			vector = vector + " " + words.get(i-1);
		}
		if (i + 1 >= words.size()){
			vector = vector + " word_+1";
		}
		else{
			vector = vector + " " + words.get(i+1);
		}
		if (i + 2 >= words.size()){
			vector = vector + " word_+2";
		}
		else{
			vector = vector + " " + words.get(i+2);
		}
		if (i - 2 < 0){
			vector = vector + " pos_-2";
		}
		else {
			vector = vector + " " + poses.get(i-2);
		}
		if (i - 1 < 0){
			vector = vector + " pos_-1";
		}
		else{
			vector = vector + " " + poses.get(i-1);
		}
		if (i + 1 >= poses.size()){
			vector = vector + " pos_+1";
		}
		else{
			vector = vector + " " + poses.get(i+1);
		}
		if (i + 2 >= poses.size()){
			vector = vector + " pos_+2";
		}
		else{
			vector = vector + " " + poses.get(i+2);
		}
		
		//get features for relative position
		double position = i * 1.0 / (words.size() - 1);
		vector = vector + " " + position;
		return vector;
	}

	private boolean getFlag(ArrayList<String> words, ArrayList<String> poses, String[] contents, int i){
		boolean flag = false;
		String token = words.get(i) + "/" + poses.get(i);
		if (!isReal(poses.get(i))){
			flag = false;
		}
		else{
			for (String content :contents){
				if (content.equals(token)){
					flag = true;
					break;
				}
				String otherToken = token + ",";
				if (content.equals(otherToken)){
					flag = true;
					break;
				}
			}
		}
		return flag;
	}
		
	private boolean isReal(String s){
		if (s.startsWith("a") || s.startsWith("g") || s.startsWith("n") ||s.startsWith("v") || s.startsWith("m") || s.startsWith("r")){
			return true;
		}
		return false;
	}
	
	private void setPER(int i) throws Exception{
		if (i < 0 || i > 10){
			throw new Exception("Illegal Argument Error");
		}
		PER = i;
	}
	
	
	public static void main(String[] args){
		try{
			long start = System.currentTimeMillis();
			SWCalc sw = new SWCalc();
			sw.getAllFeatureVectors("./data/corpus");
			sw.split("./data/train", "./data/develop", 8);
			//sw.train("./data/train","./CRF/template","./data/model");
			//sw.test("./data/model", "./data/test", "./data/result");
			long end = System.currentTimeMillis();
			
			long starts = System.currentTimeMillis();
			String s = "五一/m 想去/v 西双版纳/ns 不算/v 路程/n 玩儿/v 四天/mq ，/w 求/v 推荐/v 路线/n 及/cc 住宿/v";
			HashMap<Integer,Double> map = sw.predictSentence(s);
			for (int i : map.keySet()){
				System.out.println(i + ":" + map.get(i));
			}
			long ends = System.currentTimeMillis();
			System.out.println(end - start);
			System.out.println(ends - starts);
			/*SWCalc sw = new SWCalc();
			sw.getTestFeatureVectors("./data/test", "./data/tester", new ArrayList<ArrayList<String>>());*/
		}
		catch(Exception e){
			e.printStackTrace();
			System.exit(0);
		}
	}
}
