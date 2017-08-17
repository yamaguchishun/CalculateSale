package jp.alhinc.yamaguchi_shun.calculate_sales;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class CalculateSales {

	public static void main(String[] args) {
		String branch = "branch.lst";
		String commodity = "commodity.lst";

		int nLen = args.length;
		if(nLen != 1){
			System.out.println("予期せぬエラーが発生しました");
			return;
		}

		String branchlst = args[0] + File.separator + branch;
		String commoditylst = args[0] + File.separator + commodity;

		File branchfile  = new File(branchlst);
		File commodityfile = new File(commoditylst);

		if(branchfile.exists() == false){
			System.out.println("支店定義ファイルが存在しません");
			return;
		}

		if(commodityfile.exists() == false){
			System.out.println("商品定義ファイルが存在しません");
			return;
		}

		HashMap<String, String> branchMap = new HashMap<String, String>();
		HashMap<String, String> commodityMap = new HashMap<String, String>();
		HashMap<String, Long> branchSaleMap = new HashMap<String, Long>();
		HashMap<String, Long> comSaleMap = new HashMap<String, Long>();

		String regexThird = "^\\d{3}";
		String regexEight = "^[0-9a-zA-Z]{8}";

		boolean mapResult;

		mapResult = definitionReader(branchlst,regexThird,branchMap,branchSaleMap,"支店定義ファイル");
		if(mapResult == false){
			return;
		}
		mapResult = definitionReader(commoditylst,regexEight,commodityMap,comSaleMap,"商品定義ファイル");
		if(mapResult == false){
			return;
		}

		FileReader fr = null;
		BufferedReader br = null;

		try{
			File readFile = null;
			readFile = new File(args[0]);
			File[] filelist = readFile.listFiles();
			ArrayList<String> directorySaleList = new ArrayList<String>();
			String regexRcd = "^[0-9]{8}[.][rcd]{3}$";
			String regexw = "^\\d{1,11}";

			for(int i = 0; i < filelist.length; i++){
				if(filelist[i].isFile() && filelist[i].getName().matches(regexRcd)){
					directorySaleList.add(filelist[i].getName());
				}
			}

			long saleNumber[] = new long[directorySaleList.size()];

			for(int i =0; i < directorySaleList.size(); i++){
				saleNumber[i] = Long.parseLong(directorySaleList.get(i).replace(".rcd", ""));

			}

			for(int i = 0; i < directorySaleList.size() - 1; i++){
				int ic = i + 1;
				if(saleNumber[ic] - saleNumber[i] != 1){
					System.out.println("売上ファイル名が連番になっていません");
					return;
				}
			}

			for(int i = 0; i < directorySaleList.size(); i++){
				String exefile = args[0] +  File.separator + directorySaleList.get(i);
				fr = new FileReader(exefile);
				br = new BufferedReader(fr);
				ArrayList<String> saleList = new ArrayList<String>();
				String s,saleValue,brCord,iCord = null;
				long iSaleTotal,slValue,brSaleTotal = 0;

				while((s = br.readLine()) != null){
					saleList.add(s);
				}

				if(saleList.size() != 3){
					System.out.println(directorySaleList.get(i) + "のフォーマットが不正です");
					return;
				}

				brCord = saleList.get(0).toString();
				if(branchMap.get(brCord) == null){
					System.out.println(directorySaleList.get(i) + "の支店コードが不正です");
					return;
				}

				iCord = saleList.get(1).toString();
				if(commodityMap.get(iCord) == null){
					System.out.println(directorySaleList.get(i) + "の商品コードが不正です");
					return;
				}

				saleValue = saleList.get(2).toString();

				if(saleValue.matches(regexw) == false){
					System.out.println("予期せぬエラーが発生しました");
					return;
				}

				slValue = Long.parseLong(saleValue);

				brSaleTotal = slValue + branchSaleMap.get(brCord);
				if(brSaleTotal > 9999999999L){
					System.out.println("合計金額が10桁を超えました");
					return;
				}
				branchSaleMap.put(brCord,brSaleTotal);

				iSaleTotal = slValue + comSaleMap.get(iCord);
				if(iSaleTotal > 9999999999L){
					System.out.println("合計金額が10桁を超えました");
					return;
				}
				comSaleMap.put(iCord,iSaleTotal);

			}
		}catch(IOException e){
			System.out.println("予期せぬエラーが発生しました");
			return;

		} finally {
			try {
				if (br != null) {
					br.close();
				}
				if (fr != null) {
					fr.close();
				}
			}catch (IOException e) {
				System.out.println("予期せぬエラーが発生しました");
				return;
			}
		}
		String outBPath = args[0] + File.separator + "branch.out";
		String outCPath = args[0] + File.separator + "commodity.out";

		boolean outPutresult;
		outPutresult = output(branchSaleMap,branchMap,branchlst,outBPath);
		if(outPutresult == false){
			return;
		}
		outPutresult = output(comSaleMap,commodityMap,commoditylst,outCPath);
		if(outPutresult == false){
			return;
		}
	}

	public static boolean definitionReader(String path,String regexThird,HashMap<String, String>definitionMap,HashMap<String, Long>saleMap,String fileName){

		String cord;
		String name;
		File file = new File(path);
		FileReader fr = null;

		try {
			fr = new FileReader(file);
		} catch (FileNotFoundException e2) {
			System.out.println("予期せぬエラーが発生しました");
			return false;
		}

		BufferedReader br = new BufferedReader(fr);

		try{
			String s;
			while((s = br.readLine()) != null){

				if(s.split(",",0).length == 2){
					String[] hlf = s.split(",",0);
					cord = hlf[0];
					name = hlf[1];

					if(cord.matches(regexThird)){
						definitionMap.put(cord,name);
						saleMap.put(cord,0L);
					}else{
						System.out.println( fileName +"のフォーマットが不正です");
						return false;
					}

				}else{
					System.out.println(fileName + "のフォーマットが不正です");
					return false;
				}
			}

		}catch(IOException e){
			System.out.println("予期せぬエラーが発生しました");
			return false;

		}finally {
			try {
				if (br != null) {
					br.close();
				}
			}catch (IOException e) {
				System.out.println("予期せぬエラーが発生しました");
				return false;
			}
		}
		return true;
	}

	public static boolean output(HashMap<String,Long> saleMap,HashMap<String,String> definition,String filePath,String outPath){
		List<Map.Entry<String,Long>> entries = new ArrayList<Map.Entry<String,Long>>(saleMap.entrySet());
		Collections.sort(entries, new Comparator<Map.Entry<String,Long>>() {
			@Override
			public int compare(Entry<String,Long> entry1, Entry<String,Long> entry2) {
				return ((Long)entry2.getValue()).compareTo((Long)entry1.getValue());
			}
		});

		File file = new File(filePath);
		FileWriter Writer = null;

		try{
			file.createNewFile();
			Writer = new FileWriter(outPath);
			for (Entry<String, Long> s :entries) {
				Writer.write(s.getKey() + "," + definition.get(s.getKey()) + "," + s.getValue() + System.getProperty("line.separator"));
			}

		}catch(IOException e){
			System.out.println("予期せぬエラーが発生しました");
			return false;
		}finally {
			try {
				if (Writer != null) {
					Writer.close();
				}
			}catch (IOException e) {
				System.out.println("予期せぬエラーが発生しました");
				return false ;
			}
		}
		return true;
	}
}