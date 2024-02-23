package com.bsc.qa.facets.utility;

import java.io.FileInputStream;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;



/**
 * ExcelUtilsExtended class that gets test data from excel file to be used for
 * driving tests.
 * 
 *
 */
public class ExcelUtilsExtended {
	private static XSSFSheet excelWSheet;
	private static XSSFWorkbook excelWBook;
	public static XSSFCellStyle style;
	private static XSSFCell cell;

	public String getQueryFromMappingSheet(String nodeName,
			String mappingSheetName) {
		
		 XSSFSheet excelWSheet;
		String excelCellData = null;
		excelWSheet = excelWBook.getSheet(mappingSheetName);
		int totalRows = excelWSheet.getPhysicalNumberOfRows() - 1;
		if (totalRows < 2) {
			System.out.println("Sheet Empty.....!!");
		} else {
			for (int rowNo = 1; rowNo <= totalRows; rowNo++) {
				// GetCellData is not fetching correct data from ExcelUtils, because of that created new method in this ExcelUtilsExtended file
				if (nodeName.equalsIgnoreCase(ExcelUtilsExtended.getCellData(rowNo, 0)
						.trim().toString())) {
				 excelCellData= ExcelUtilsExtended.getCellData(rowNo, 1);
				}
			}
		}
		return excelCellData;
	
	}

	public ExcelUtilsExtended(String path, String sheetName ) {
        try(FileInputStream excelFile = new FileInputStream(path)){
        excelWBook = new XSSFWorkbook(excelFile);
        excelWSheet = excelWBook.getSheet(sheetName);
        }catch(Exception e){
               e.printStackTrace();
        }
        }

	
	// GetCellData method is not fetching data from ExcelUtils class file, because of that created this new method in this ExcelUtilsExtended class file
    public static String getCellData(int rowNum, int colNum) 
    {
    	String cellData = null;
    	try
           {
			//Creating cell
			  cell = excelWSheet.getRow(rowNum).getCell(colNum);
			  
			  //To fetch data from excel cell when cell type as String
			  if (cell.getCellType() == XSSFCell.CELL_TYPE_STRING) {
			        cellData = cell.getStringCellValue();
			  }
			//To fetch data from excel cell when cell type as Numeric
			  else if (cell.getCellType() == XSSFCell.CELL_TYPE_NUMERIC) 
			  {
			        cellData = String.valueOf(cell.getNumericCellValue());
			  }
			//To fetch data from excel cell when cell value as Blank
			  else if (cell.getCellType() == org.apache.poi.ss.usermodel.Cell.CELL_TYPE_BLANK)
			  {
			        cellData = "";
			          }
           }
			  catch (Exception e)
			   {
			         System.out.println("Failed due to Exception : "+e);
			   }
			          return cellData; 
			  
    	}
    
}