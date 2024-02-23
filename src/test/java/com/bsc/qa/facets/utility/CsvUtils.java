package com.bsc.qa.facets.utility;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import au.com.bytecode.opencsv.CSVWriter;

public class CsvUtils {
	public static String timestamp= new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
	public static String suiteName=System.getProperty("user.dir").substring(System.getProperty("user.dir").lastIndexOf("\\")+1);
	//Storing the CSV file name
	public static String outputFile = "test-output\\BSC-reports\\"+suiteName+"_"+timestamp+".csv";


	/**
	 * To write the data in to the CSV file
	 * 
	 * @param claimid,fieldName,fieldType,EDI FileOutput,carewareDBOutput,status
	 * @param header fields of the CSV file
	 * @return
	 */
	public static void writeAllData(List<String[]> data) {

		
		 

		try {
			// FileWriter constructor that specifies open for appending
			CSVWriter writer = new CSVWriter(new FileWriter(outputFile, true), ',');
			
			
			writer.writeAll(data);
			

			// closing writer connection
			writer.close();
		} catch (IOException e) {

			e.printStackTrace();
		}
	}
	
	
	/**
	 * To read the data in to the CSV file
	 * 
	 * @param filepath
	 
	 * @return List of data in the csv
	 */
	public static List<String[]> readAllDataAtOnce(String file) 
	{ 
		List<String[]> allData=null;
	    try(FileReader filereader = new FileReader(file)) { 
	        // create csvReader object and skip first Line 
	        CSVReader csvReader = new CSVReaderBuilder(filereader) 
	                                  .withSkipLines(1) 
	                                  .build(); 
	         allData = csvReader.readAll(); 
  
	    } 
		
	    catch (Exception e) { 
	        e.printStackTrace(); 
	    }
	    return allData;
		 
	} 
}