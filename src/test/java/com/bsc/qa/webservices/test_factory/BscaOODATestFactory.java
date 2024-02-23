package com.bsc.qa.webservices.test_factory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;

import com.bsc.qa.facets.tests.BscaOODATest;

public class BscaOODATestFactory {
	   
	/**
	 * Factory method to trigger simple parameter tests from Factory
	 * @return
	 */
	@Factory(dataProvider = "data")
    public Object[] factoryMethod(String inputFileName) {
        return new Object[] { new BscaOODATest(inputFileName) };
    }
	
	@DataProvider(name = "data")
	public Object[] getData() {
		Object[] tableData = null;

		try (Stream<Path> walk = Files.walk(Paths.get(System.getenv("XML_835_INPUT")))) {

			List<String> result = walk.filter(Files::isRegularFile)
					.map(x -> x.toString()).collect(Collectors.toList());
			
			tableData = (Object[]) result.toArray();

		} catch (IOException e) {
			e.printStackTrace();
		}
		return tableData;
	}

}
