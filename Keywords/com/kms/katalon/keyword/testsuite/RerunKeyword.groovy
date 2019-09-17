package com.kms.katalon.keyword.testsuite

import static com.kms.katalon.core.checkpoint.CheckpointFactory.findCheckpoint
import static com.kms.katalon.core.testcase.TestCaseFactory.findTestCase
import static com.kms.katalon.core.testdata.TestDataFactory.findTestData
import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject

import com.kms.katalon.core.annotation.Keyword
import com.kms.katalon.core.checkpoint.Checkpoint
import com.kms.katalon.core.cucumber.keyword.CucumberBuiltinKeywords as CucumberKW
import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile
import com.kms.katalon.core.model.FailureHandling
import com.kms.katalon.core.testcase.TestCase
import com.kms.katalon.core.testdata.TestData
import com.kms.katalon.core.testobject.TestObject
import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI

import internal.GlobalVariable
import org.apache.commons.lang.StringUtils
import com.kms.katalon.core.configuration.RunConfiguration
import com.kms.katalon.core.webui.driver.DriverFactory

public class RerunKeyword {
	/**
	 * Check if text is one of items in array list
	 * @param text text needs to be checked
	 * @param list array list with type String
	 * @return true or false
	 */
	@Keyword
	def isStringBelongToList(String text, ArrayList<String> list) {
		boolean status = false
		if(!list.empty) {
			for(String item: list) {
				if(text.equalsIgnoreCase(item)) {
					status = true
				}
			}
		}
		return status
	}

	/**
	 * Create name for new test suite with format = old suite name + "_yyyyMMdd_HHmmss"
	 * @param oldTestSuitePath old test suite name
	 * @return new test suite with format = old suite name + "_yyyyMMdd_HHmmss"
	 */
	@Keyword
	def createNameForNewSuite(String oldTestSuitePath) {
		String currentDate = new Date().format("yyyyMMdd_HHmmss")
		return oldTestSuitePath + "_" + currentDate
	}

	/**
	 * Remove list TCs passed in ts file and remove passed rows if data binding is using 
	 * @param oldTSTemplate text in ts file
	 * @param listFailTestCases list failed test cases
	 * @param listFailedRowsBelongToTC list failed rows is Test Case uses data binding
	 * @return new test case list after removing passed test cases
	 */
	@Keyword
	def removeTCPassedAndReplaceFailedRows(String oldTSTemplate, List<String> listFailTestCases, HashMap<String, List<Integer>> listFailedRowsBelongToTC) {
		String newTCList = ""
		String[] listTCs =  StringUtils.substringsBetween(oldTSTemplate, "<testCaseLink>", "</testCaseLink>")
		ArrayList<String> list = new ArrayList<>(Arrays.asList(listTCs))
		List copyList = new ArrayList(list)
		List<String> listFailedBindingTC = new ArrayList(listFailedRowsBelongToTC.keySet())
		for(String tc: list) {
			String testCaseId = StringUtils.substringBetween(tc, "<testCaseId>", "</testCaseId>")
			if(isStringBelongToList(testCaseId, listFailedBindingTC)) {
				String replaceValue = replaceFailedRowsForBindingData(listFailedRowsBelongToTC, tc)
				copyList.set(copyList.indexOf(tc), replaceValue)
			}
			if(!isStringBelongToList(testCaseId, listFailTestCases)) {
				int index = copyList.indexOf(tc)
				copyList.remove(index)
			}
		}
		for(String each: copyList) {
			each = "<testCaseLink>" + each + "</testCaseLink>"
			newTCList = newTCList + "\n" + each
		}
		return newTCList
	}

	/**
	 * Replace TS file text with list failed TCs
	 * @param oldTSTemplate text in ts file
	 * @param newTCList list failed test cases
	 * @return new TS file text with list failed TCs
	 */
	@Keyword
	def replaceTSTemplateWithListNewTC(String oldTSTemplate, String newTCList) {
		String listOldTC = StringUtils.substringBeforeLast(oldTSTemplate.split("<testCaseLink>", 2)[1], "</testCaseLink>")
		String newTSTemplate = oldTSTemplate.replace("<testCaseLink>" + listOldTC + "</testCaseLink>", newTCList)
		return newTSTemplate
	}

	/**
	 * Create test suite file
	 * @param fullPathWithoutExtension name of new test suite with the relative path
	 * @param template file template
	 * @param extension extension between ts and groovy
	 */
	@Keyword
	def createFile(String fullPathWithoutExtension, String template, String extension) {
		String filePath = String.format("./%s.%s", fullPathWithoutExtension, extension)
		def file = new File(filePath)
		file.write(template)
	}

	/**
	 * Get executed browser in current test suite
	 * @return browser name
	 */
	@Keyword
	def getExecutedBrowser() {
		String browserName = DriverFactory.getExecutedBrowser().getName().replace("_DRIVER", "")
		switch(browserName) {
			case "HEADLESS":
				browserName = "Chrome (headless)"
				break
			case "FIREFOX_HEADLESS":
				browserName = "Firefox (headless)"
				break
			case "IE":
				break
			default:
				browserName = browserName.substring(0, 1).toUpperCase() + browserName.substring(1).toLowerCase()
				break
		}
		return browserName
	}

	/**
	 * Get name of prj file
	 * @return prj file name
	 */
	@Keyword
	def getProjectFileName() {
		String projectFileName = ""
		File projectDir = new File(RunConfiguration.getProjectDir())
		for(File file : projectDir.listFiles()) {
			if(file.getName().endsWith(".prj")) {
				projectFileName = file.getName()
				break
			}
		}
		return projectFileName
	}

	/**
	 * Create console mode file (bat file)
	 * @param relativeTestSuitePath current test suite path
	 */
	@Keyword
	def createConsoleModeFile(String relativeTestSuitePath) {
		String newFileName = RunConfiguration.getExecutionSourceId().split("_[\\d]{8}_[\\d]{6}", 2)[0] + "_FailTCUpdated.bat"
		File file = new File(newFileName)
		String newContent = ""
		if(!file.exists()) {
			String content = 'katalon -noSplash  -runMode=console -projectPath="%s" -retry=0 -testSuitePath="%s" -executionProfile="%s" -browserType="%s"'
			String projectPath = String.format("%s/%s", RunConfiguration.getProjectDir(), getProjectFileName()).replace("/", "\\")
			String profile = RunConfiguration.getExecutionProfile()
			String browser = getExecutedBrowser()
			newContent =  String.format(content, projectPath, relativeTestSuitePath, profile, browser)
		} else {
			String existingContent = file.text
			file.delete()
			String oldSuiteName = StringUtils.substringAfter(existingContent, "-testSuitePath=").split("\"", 2)[0]
			newContent = existingContent.replace(oldSuiteName, relativeTestSuitePath)
		}
		createFile(newFileName, newContent, "bat")
	}

	/**
	 * Get list executed rows in ts file
	 * @param testCaseId test case id
	 * @param iterationType iteration type
	 * @param iterationValue iteration value
	 * @param dataTotalRows total rows in data file
	 * @return list executed rows belong to test case
	 */
	@Keyword
	def getListExecutedRows(String testCaseId, String iterationType, String iterationValue, int dataTotalRows) {
		Map<String, List<Integer>> listExecutedRowsAndTC = new HashMap<>()
		List<Integer> listExecutedRows = new ArrayList<>()
		if(dataTotalRows > 0) {
			if(iterationType.equalsIgnoreCase("ALL")) {
				for(int i = 1; i <= dataTotalRows; i++) {
					listExecutedRows.add(i)
				}
			}
			else if(iterationType.equalsIgnoreCase("RANGE")) {
				int startRow = Integer.valueOf(iterationValue.split("-")[0])
				int endRow = Integer.valueOf(iterationValue.split("-")[1])
				for(int i = startRow; i <= endRow; i++){
					listExecutedRows.add(i)
				}
			}
			else{
				String[] items = iterationValue.split(",")
				List<Integer> tempRowList = new ArrayList<>()
				for(String item:items) {
					if(item.contains("-")) {
						int startRow = Integer.valueOf(item.split("-")[0])
						int endRow = Integer.valueOf(item.split("-")[1])
						for(int i = startRow; i <= endRow; i++) {
							listExecutedRows.add(i)
						}
					}
					else {
						listExecutedRows.add(Integer.valueOf(item))
					}
				}
			}
		}
		listExecutedRowsAndTC.put(testCaseId, listExecutedRows)
		return listExecutedRowsAndTC
	}

	/**
	 * Replace failed rows for test case using binding data
	 * @param listFailedRowsBelongToTC list failed rows belong to test case
	 * @param testCaseTemp test case template in ts file
	 * @return new test case template
	 */
	@Keyword
	def replaceFailedRowsForBindingData(HashMap<String, List<Integer>> listFailedRowsBelongToTC, String testCaseTemp) {
		String oldIterationType = StringUtils.substringBetween(testCaseTemp, "<iterationType>", "</iterationType>")
		String oldIterationValue = StringUtils.substringBetween(testCaseTemp, "<value>", "</value>")
		String testCaseId = StringUtils.substringBetween(testCaseTemp, "<testCaseId>", "</testCaseId>")

		String failedRows = StringUtils.join(listFailedRowsBelongToTC.get(testCaseId), ",")
		String newTestCaseTemp = testCaseTemp.replace("<iterationType>"+oldIterationType+"</iterationType>", "<iterationType>SPECIFIC</iterationType>")
		newTestCaseTemp = newTestCaseTemp.replaceFirst("<value>"+oldIterationValue+"</value>", "<value>"+failedRows+"</value>")
		return newTestCaseTemp
	}

	/**
	 * Delete test suite file
	 * @param testSuiteName test suite name should be deleted
	 */
	@Keyword
	def deleteTestSuite(String testSuiteName) {
		String tsFile = testSuiteName + ".ts"
		File fileTS = new File(tsFile)
		fileTS.delete()
		String groovyFile = testSuiteName + ".groovy"
		File fileGroovy = new File(groovyFile)
		fileGroovy.delete()
		String batFileName = testSuiteName.split("_[\\d]{8}_[\\d]{6}", 2)[0] + "_FailTCUpdated.bat"
		File batFile = new File(batFileName)
		batFile.delete()
	}
}
