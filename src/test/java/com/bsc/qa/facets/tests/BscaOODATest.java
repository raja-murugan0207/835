package com.bsc.qa.facets.tests;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.SoftAssertions;
import org.testng.IHookCallBack;
import org.testng.IHookable;
import org.testng.ITestResult;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.bsc.qa.facets.utility.CsvUtils;
import com.bsc.qa.facets.utility.XMLParse;
import com.bsc.qa.framework.base.BaseTest;
import com.bsc.qa.framework.utility.DBUtils;
import com.bsc.qa.framework.utility.ExcelUtils;
import com.relevantcodes.extentreports.LogStatus;

public class BscaOODATest extends BaseTest implements IHookable {

	public String inputFileName = null;
	public XMLParse xMLParse = null;
	private List<String[]> csvDataList = new ArrayList<String[]>();

	public BscaOODATest(String inputFileName) {
		this.inputFileName = inputFileName;
	}
	/**
	 * DataProvider for returning the specific test data based on test method
	 * name
	 * 
	 * @param method
	 * @return
	 */
	@DataProvider(name = "masterDataProvider")
	public Object[][] getData(Method method) {
		Object[][] data = null;
		Map<String, String> dataMap = new HashMap<String, String>();

		String xlsPath = "src/test/resources/"
				+ this.getClass().getSimpleName() + ".xlsx";
		dataMap = ExcelUtils.getTestMethodData(xlsPath, method.getName());

		data = new Object[][] { { dataMap } };

		return data;
	}

	/**
	 * Sample test method
	 * 
	 * @param dataMap
	 */
	@Test(dataProvider = "masterDataProvider")
	public void test835XMLToDBValidation(Map<String, String> dataMap) {

		try {			
			Map<String, String> finalXMLDataMap = new HashMap<String,String>();
			String strClaimIDList = null;
			Map<String, String> dBDataMap = new HashMap<String,String>();
			xMLParse = new XMLParse(this.inputFileName);	
			String xmlTag = dataMap.get("ELEMENT_TAG_NAME").toString().trim();

			// To fetch all Claim ID's from XML, multiple claim ID's data will be retrieved with pipe (|) delimiter	
			strClaimIDList = xMLParse.uniqueDetailsExtraction(xmlTag, "THG835X2_2100_CLP07__PayerClaimControlNumber").substring(1);

			if (!"".equals(strClaimIDList)) {

				String[] strClaimIDArray = strClaimIDList.split("\\|");

				List<String> listOFTags = new ArrayList<String>();
				listOFTags.add("THG835X2_1000A_N1__PayerIdentification");
				listOFTags.add("THG835X2_1000B_N1__PayeeIdentification");
				listOFTags.add("THG835X2_2100");
				listOFTags.add("THG835X2_1000A_N3__PayerAddress");
				listOFTags.add("THG835X2_1000B_N3__PayeeAddress");
				listOFTags.add("THG835X2_1000A_N4__PayerCityStateZIPCode");
				listOFTags.add("THG835X2_1000B_N4__PayeeCityStateZIPCode");

				for (String claimID:strClaimIDArray ) {

					// validating specific claims data
					for (String tag : listOFTags) {						
						finalXMLDataMap.putAll(xMLParse.getXmlDataMap(tag,claimID));
					}		
					addDataFromPreparedStatement(dBDataMap,dataMap.get("THG835X2_1000A_N102__PayerName"), claimID);
					addDataFromPreparedStatement(dBDataMap,dataMap.get("THG835X2_2100_CLP03__TotalClaimChargeAmount"), claimID);
					addDataFromPreparedStatement(dBDataMap,dataMap.get("THG835X2_1000A_N301__PayerAddressLine"),claimID);
					addDataFromPreparedStatement(dBDataMap,dataMap.get("THG835X2_1000A_N401__PayerCityName"), claimID);
					addDataFromPreparedStatement(dBDataMap,dataMap.get("THG835X2_1000A_N402__PayerStateCode"), claimID);
					addDataFromPreparedStatement(dBDataMap,dataMap.get("THG835X2_1000A_N403__PayerPostalZoneorZIPCode"), claimID);
					addDataFromPreparedStatement(dBDataMap,dataMap.get("THG835X2_1000B_N102__PayeeName"), claimID);
					addDataFromPreparedStatement(dBDataMap,dataMap.get("THG835X2_1000B_N301__PayeeAddressLine"), claimID);
					addDataFromPreparedStatement(dBDataMap,dataMap.get("THG835X2_1000B_N401__PayeeCityName"), claimID);
					addDataFromPreparedStatement(dBDataMap,dataMap.get("THG835X2_1000B_N402__PayeeStateCode"), claimID);
					addDataFromPreparedStatement(dBDataMap,dataMap.get("THG835X2_1000B_N403__PayeePostalZoneorZIPCode"), claimID);
					addDataFromPreparedStatement(dBDataMap,dataMap.get("THG835X2_2100_NM103__PatientLastName"), claimID);
					addDataFromPreparedStatement(dBDataMap,dataMap.get("THG835X2_2100_NM104__PatientFirstName"), claimID);
					addDataFromPreparedStatement(dBDataMap,dataMap.get("THG835X2_2100_CLP04__ClaimPaymentAmount"), claimID);
 
					String[] columnArray = { "Patient Claim ID", "Key",
			                "Expected Filevalue", "Actual FacetDBvalue", "Status","TestCase Name","Filename" };
					csvDataList.add(columnArray);
					
					xmlToDbAssertion(dBDataMap, finalXMLDataMap,claimID);					
				}
			}

		} catch (Exception e) {
			System.out.println("Failed due to Exception : " + e);
			e.printStackTrace();
		}
		CsvUtils.writeAllData(csvDataList);	
	}
	/**
	 * Execute a prepared statement and add results to the dataMap used by the
	 * test suite
	 * 
	 * @param dBDataMap
	 *            data map used for the test suite
	 */
	private void addDataFromPreparedStatement(Map<String, String> dBDataMap,
			String preparedStatementQuery, String claimID) {
		DBUtils databaseUtil = new DBUtils();
		Map<String, String> preparedStatementDataMap = databaseUtil
				.getDataFromPreparedQuery("facets", preparedStatementQuery,
						claimID,claimID);
		databaseUtil.tearDown();
		dBDataMap.putAll(preparedStatementDataMap);
	}
	public void xmlToDbAssertion(Map<String, String> dBDataMap, Map<String, String> finalXMLDataMap, String claimID) {
		String status = null;
		softAssertions.assertThat(dBDataMap.size()).isGreaterThan(0);
		for (String key : dBDataMap.keySet()) {
			String dBValue = dBDataMap.get(key);
			String xmlValue=finalXMLDataMap.get(key);

			//removing spaces of db values when not null
			if(dBValue!=null){ dBValue = dBValue.replaceAll("\\s+", ""); }

			try{
				//Checking the Status Value for CSV
                if (dBValue.trim().equalsIgnoreCase(xmlValue)) {
                      status = "Pass";
                } else {
                      status = "Fail";
                }
			}
			catch(Exception e){
				System.out.println("Exception occured!! Database value is :  "+e.getMessage());
			}
            String[] fieldDataArray = { "CLM_"+claimID, key, 
            		xmlValue, dBValue, status,"",inputFileName };
            
            csvDataList.add(fieldDataArray);
			
			softAssertions.assertThat(xmlValue)
			.as(claimID+" Values are not matching for : "+key + " Actual : " + xmlValue + " Expected: " + dBValue)
			.isEqualToIgnoringCase(dBValue);
		}
	}
	/**
	 * //To run test method, this method will initiate the HTML report
	 * 
	 * @Override run is a hook before @Test method
	 */
	@Override
	public void run(IHookCallBack callBack, ITestResult testResult) {
		reportInit(testResult.getTestContext().getName(), testResult.getName());
		softAssertions = new SoftAssertions();
		logger.log(LogStatus.INFO, "Starting test " + testResult.getName());
		// To execute test method multiple times
		callBack.runTestMethod(testResult);
		softAssertions.assertAll();
	}

	public Map<String, String> getData(String testMethodName) {
		Map<String, String> dataMap = new HashMap<String, String>();
		// assigning test data excel file path to a local variable
		String xlsPath = "src/test/resources/" + this.getClass().getSimpleName() + ".xlsx";
		dataMap = ExcelUtils.getTestMethodData(xlsPath, testMethodName);

		return dataMap;

	}
}
