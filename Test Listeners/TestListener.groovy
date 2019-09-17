import static com.kms.katalon.core.checkpoint.CheckpointFactory.findCheckpoint
import static com.kms.katalon.core.testcase.TestCaseFactory.findTestCase
import static com.kms.katalon.core.testdata.TestDataFactory.findTestData
import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject

import com.kms.katalon.core.checkpoint.Checkpoint as Checkpoint
import com.kms.katalon.core.model.FailureHandling as FailureHandling
import com.kms.katalon.core.testcase.TestCase as TestCase
import com.kms.katalon.core.testdata.TestData as TestData
import com.kms.katalon.core.testobject.TestObject as TestObject

import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile

import internal.GlobalVariable as GlobalVariable

import com.kms.katalon.core.annotation.BeforeTestCase
import com.kms.katalon.core.annotation.BeforeTestSuite
import com.kms.katalon.core.annotation.AfterTestCase
import com.kms.katalon.core.annotation.AfterTestSuite
import com.kms.katalon.core.context.TestCaseContext
import com.kms.katalon.core.context.TestSuiteContext

import org.apache.commons.lang.StringUtils
import com.kms.katalon.core.configuration.RunConfiguration as RunConfiguration
import com.kms.katalon.core.setting.BundleSettingStore as BundleSettingStore
import com.kms.katalon.core.testdata.TestDataFactory
import com.kms.katalon.core.testdata.TestDataInfo
import java.util.List
import com.kms.katalon.core.logging.KeywordLogger

class TestListener {
	BundleSettingStore bundleSetting = new BundleSettingStore(RunConfiguration.getProjectDir(), 'com.kms.katalon.keyword.Create-Suite-Plugin', true)
	boolean isCreate = bundleSetting.getBoolean("Create Suite", true)
	boolean isDelete = bundleSetting.getBoolean("Delete Suite", false)
	List<String> listFailTestCases = new ArrayList<>()
	String newSuiteName = ""
	int numberOfTC = 0
	List<Integer> listExecutedRows = new ArrayList<>()
	List<Integer> listFailedRows = new ArrayList<>()
	Map<String, Integer> startRow = new HashMap<>()
	Map<String, String> typeValueTestDataBelongToTC = new HashMap<>()
	Map<String, List<Integer>> listBindingTCAndExecutedRows = new HashMap<>()
	Map<String, List<Integer>> listFailUsingBindingData = new HashMap<>()
	KeywordLogger log = new KeywordLogger()
	
	/** 
	 * Get list test cases using binding data and iterationType and Value belongs to each test case from ts file
	 * @param testSuiteContext test suite context
	 */
	@BeforeTestSuite
	def getListBindingTC(TestSuiteContext testSuiteContext) {
		if(isCreate) { 
			String oldTSTemplate = new File(String.format("./%s.ts", testSuiteContext.testSuiteId)).text
			String[] listTCs =  StringUtils.substringsBetween(oldTSTemplate, "<testCaseLink>", "</testCaseLink>")
			for(String tc: listTCs) {
				if(tc.contains("<testDataLink>")) {
					String value, testDataName = ""
					String testCaseId = StringUtils.substringBetween(tc, "<testCaseId>", "</testCaseId>") 
					String iterationType = StringUtils.substringBetween(tc, "<iterationType>", "</iterationType>")
					if(!iterationType.equalsIgnoreCase("ALL")) {
						value = StringUtils.substringsBetween(tc, "<value>", "</value>")[0]
					}
					testDataName = StringUtils.substringBetween(tc, "<testDataId>", "</testDataId>").split("/", 2)[1]
					List<String> tempList = new ArrayList<>()
					tempList.add(0, iterationType)
					tempList.add(1, value)
					tempList.add(2, testDataName)
					typeValueTestDataBelongToTC.put(testCaseId, tempList)
					startRow.put(testCaseId, 0)
				}
			}
		}
	} 
	
	/**
	 * Get list executed rows belonged to test case id in ts file
	 * @param testCaseContext test case context
	 */
	@BeforeTestCase
	def executeRows(TestCaseContext testCaseContext) {
		if(isCreate) {
			String testCaseId = testCaseContext.getTestCaseId()
			if(CustomKeywords.'com.kms.katalon.keyword.testsuite.RerunKeyword.isStringBelongToList'(testCaseId, new ArrayList(typeValueTestDataBelongToTC.keySet()))) {		
				List<String> typeValueTestData = typeValueTestDataBelongToTC.get(testCaseId)
				String type = typeValueTestData.get(0)
				String typeValue = typeValueTestData.get(1)
				String testDataName = typeValueTestData.get(2)
				int totalRows = TestDataFactory.findTestData(testDataName).rowNumbers
				listBindingTCAndExecutedRows = CustomKeywords.'com.kms.katalon.keyword.testsuite.RerunKeyword.getListExecutedRows'(testCaseId, type, typeValue, totalRows)
				listExecutedRows = listBindingTCAndExecutedRows.get(testCaseId)
			}
		}
	}
	
	/**
	 * Get list failed test cases after running each
	 * @param testSuiteContext test suite context
	 */
	@AfterTestCase
	def addFailedTCToList(TestCaseContext testCaseContext) {
		if(isCreate) {
			String status = testCaseContext.getTestCaseStatus()
			String testCaseId = testCaseContext.getTestCaseId()
			if(!status.contains("PASSED")) {
				listFailTestCases.add(testCaseContext.getTestCaseId())
				if(CustomKeywords.'com.kms.katalon.keyword.testsuite.RerunKeyword.isStringBelongToList'(testCaseId, new ArrayList(typeValueTestDataBelongToTC.keySet()))) {			
					int i = startRow.get(testCaseId)
					listFailedRows.add(listExecutedRows.get(i))
					List<Integer> tempList = new ArrayList<>(listFailedRows)
					listFailUsingBindingData.put(testCaseId, tempList)
				}
			}
			if(CustomKeywords.'com.kms.katalon.keyword.testsuite.RerunKeyword.isStringBelongToList'(testCaseId, new ArrayList(typeValueTestDataBelongToTC.keySet()))) {
				startRow.replace(testCaseId, startRow.get(testCaseId) + 1)
				if(startRow.get(testCaseId) == listExecutedRows.size()) {
					listFailedRows.clear()
				}
			}
			numberOfTC = numberOfTC + 1
		}
	}
	
	/**
	 * Create groovy file
	 * @param testSuiteContext test suite context
	 */
	@AfterTestSuite
	def createGroovySuiteFile(TestSuiteContext testSuiteContext) {
		if(isCreate) {
			if(numberOfTC == listFailTestCases.size()) return
			if(!listFailTestCases.empty) {
				String oldSuiteName = testSuiteContext.testSuiteId
				newSuiteName = CustomKeywords.'com.kms.katalon.keyword.testsuite.RerunKeyword.createNameForNewSuite'(oldSuiteName)
				String groovyTemplate = new File(String.format("./%s.groovy", oldSuiteName)).text	
				if(groovyTemplate.contains("ExecutionEventManager.getInstance()")) {
					String oldListener = "ExecutionEventManager.getInstance()" + StringUtils.substringAfter(groovyTemplate, "ExecutionEventManager.getInstance()").split("\\r?\\n")[0]				
					String oldName = StringUtils.substringBetween(oldListener, "ReportPortalListener(", "))")
					String newListener = oldListener.replace(oldName, String.format('"%s","%s"', newSuiteName, newSuiteName))
					groovyTemplate = groovyTemplate.replace(oldListener, newListener)
				}
				CustomKeywords.'com.kms.katalon.keyword.testsuite.RerunKeyword.createFile'(newSuiteName, groovyTemplate, "groovy")
			}
		}
	}
	
	/**
	 * Create ts file
	 * @param testSuiteContext test suite context
	 */
	@AfterTestSuite
	def createTSSuiteFile(TestSuiteContext testSuiteContext) {
		String oldSuiteName = testSuiteContext.testSuiteId
		if(isCreate) {
			if(numberOfTC == listFailTestCases.size()) {
				log.logInfo(String.format("All Test Cases in %s are failed. Please re-run again.", oldSuiteName))
				return
			}
			if(listFailTestCases.empty) {
				log.logInfo("No suite is created because there's no failed TC.")
				return
			}
			if(!listFailTestCases.empty) {
				//get old suite name and old suite guid
				String oldTSTemplate = new File(String.format("./%s.ts", oldSuiteName)).text
				String oldSuiteGuid = StringUtils.substringBetween(oldTSTemplate, "<testSuiteGuid>", "</testSuiteGuid>")
				
				//replace with new suite name and new suite guid
				String[] oldPath = oldSuiteName.split("/")
				String[] newPath = newSuiteName.split("/")
				String newTSTemplate = oldTSTemplate.replaceFirst(String.format("<name>%s</name>", oldPath[oldPath.length - 1]), String.format("<name>%s</name>", newPath[newPath.length - 1]))
				newTSTemplate = newTSTemplate.replaceFirst(oldSuiteGuid, UUID.randomUUID().toString())
				
				//prepare list TCs for new suite
				//remove TCs passed
				String listTCAfterRemovePassed = CustomKeywords.'com.kms.katalon.keyword.testsuite.RerunKeyword.removeTCPassedAndReplaceFailedRows'(newTSTemplate, listFailTestCases, listFailUsingBindingData)
				//replace list old tc in old suite with list new test case
				newTSTemplate = CustomKeywords.'com.kms.katalon.keyword.testsuite.RerunKeyword.replaceTSTemplateWithListNewTC'(newTSTemplate, listTCAfterRemovePassed)
				//create file ts with new suite name
				CustomKeywords.'com.kms.katalon.keyword.testsuite.RerunKeyword.createFile'(newSuiteName, newTSTemplate, "ts")
				log.logInfo(String.format("%s is created with %s test cases.", newSuiteName, String.valueOf(new HashSet(listFailTestCases).size())))
				if(!listFailUsingBindingData.keySet().empty) {
					for(String testCase: listFailUsingBindingData.keySet()) {
						log.logInfo(String.format("%s is failed at row %s", testCase, StringUtils.join(listFailUsingBindingData.get(testCase), ",")))
					}
				}
				CustomKeywords.'com.kms.katalon.keyword.testsuite.RerunKeyword.createConsoleModeFile'(newSuiteName)
			}
		}
		if(isDelete) {
			if(oldSuiteName.matches(".*_[\\d]{8}_[\\d]{6}")) {
				CustomKeywords.'com.kms.katalon.keyword.testsuite.RerunKeyword.deleteTestSuite'(oldSuiteName)
				log.logInfo(String.format("%s is deleted.", oldSuiteName))
			}
		}
	}
}