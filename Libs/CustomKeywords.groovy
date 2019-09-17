
/**
 * This class is generated automatically by Katalon Studio and should not be modified or deleted.
 */

import java.lang.String

import java.util.ArrayList

import java.util.List

import java.util.HashMap


def static "com.kms.katalon.keyword.testsuite.RerunKeyword.isStringBelongToList"(
    	String text	
     , 	java.util.ArrayList<String> list	) {
    (new com.kms.katalon.keyword.testsuite.RerunKeyword()).isStringBelongToList(
        	text
         , 	list)
}

def static "com.kms.katalon.keyword.testsuite.RerunKeyword.createNameForNewSuite"(
    	String oldTestSuitePath	) {
    (new com.kms.katalon.keyword.testsuite.RerunKeyword()).createNameForNewSuite(
        	oldTestSuitePath)
}

def static "com.kms.katalon.keyword.testsuite.RerunKeyword.removeTCPassedAndReplaceFailedRows"(
    	String oldTSTemplate	
     , 	java.util.List<String> listFailTestCases	
     , 	java.util.HashMap<String, java.util.List<java.lang.Integer>> listFailedRowsBelongToTC	) {
    (new com.kms.katalon.keyword.testsuite.RerunKeyword()).removeTCPassedAndReplaceFailedRows(
        	oldTSTemplate
         , 	listFailTestCases
         , 	listFailedRowsBelongToTC)
}

def static "com.kms.katalon.keyword.testsuite.RerunKeyword.replaceTSTemplateWithListNewTC"(
    	String oldTSTemplate	
     , 	String newTCList	) {
    (new com.kms.katalon.keyword.testsuite.RerunKeyword()).replaceTSTemplateWithListNewTC(
        	oldTSTemplate
         , 	newTCList)
}

def static "com.kms.katalon.keyword.testsuite.RerunKeyword.createFile"(
    	String fullPathWithoutExtension	
     , 	String template	
     , 	String extension	) {
    (new com.kms.katalon.keyword.testsuite.RerunKeyword()).createFile(
        	fullPathWithoutExtension
         , 	template
         , 	extension)
}

def static "com.kms.katalon.keyword.testsuite.RerunKeyword.getExecutedBrowser"() {
    (new com.kms.katalon.keyword.testsuite.RerunKeyword()).getExecutedBrowser()
}

def static "com.kms.katalon.keyword.testsuite.RerunKeyword.getProjectFileName"() {
    (new com.kms.katalon.keyword.testsuite.RerunKeyword()).getProjectFileName()
}

def static "com.kms.katalon.keyword.testsuite.RerunKeyword.createConsoleModeFile"(
    	String relativeTestSuitePath	) {
    (new com.kms.katalon.keyword.testsuite.RerunKeyword()).createConsoleModeFile(
        	relativeTestSuitePath)
}

def static "com.kms.katalon.keyword.testsuite.RerunKeyword.getListExecutedRows"(
    	String testCaseId	
     , 	String iterationType	
     , 	String iterationValue	
     , 	int dataTotalRows	) {
    (new com.kms.katalon.keyword.testsuite.RerunKeyword()).getListExecutedRows(
        	testCaseId
         , 	iterationType
         , 	iterationValue
         , 	dataTotalRows)
}

def static "com.kms.katalon.keyword.testsuite.RerunKeyword.replaceFailedRowsForBindingData"(
    	java.util.HashMap<String, java.util.List<java.lang.Integer>> listFailedRowsBelongToTC	
     , 	String testCaseTemp	) {
    (new com.kms.katalon.keyword.testsuite.RerunKeyword()).replaceFailedRowsForBindingData(
        	listFailedRowsBelongToTC
         , 	testCaseTemp)
}

def static "com.kms.katalon.keyword.testsuite.RerunKeyword.deleteTestSuite"(
    	String testSuiteName	) {
    (new com.kms.katalon.keyword.testsuite.RerunKeyword()).deleteTestSuite(
        	testSuiteName)
}
