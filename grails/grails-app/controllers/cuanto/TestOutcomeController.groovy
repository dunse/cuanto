/*
 Copyright (c) 2008 thePlatform, Inc.

This file is part of Cuanto, a test results repository and analysis program.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.

*/



package cuanto

import cuanto.TestCase
import cuanto.TestOutcome
import grails.converters.JSON
import com.thoughtworks.xstream.XStream
import cuanto.testapi.TestOutcome as TestOutcomeApi

class TestOutcomeController {
	def parsingService
	def dataService
	def testOutcomeService

	XStream xstream = new XStream()

	// the delete, save and update actions only accept POST requests
	static def allowedMethods = [delete: 'POST', save: 'POST', update: 'POST', applyAnalysis: 'POST']

	def index = { redirect(action: list, params: params) }

	def get = {
		TestOutcome outcome = TestOutcome.get(params.id)
		def formatter = testOutcomeService.getTestCaseFormatter(params.tcFormat)
		def myJson = [
			id: outcome.id,
			bug: [title: outcome.bug?.title, url: outcome.bug?.url, 'id': outcome.bug?.id],
			analysisState: [name: outcome.analysisState?.name, 'id': outcome.analysisState?.id],
			testCase: [name: formatter.getTestName(outcome?.testCase),'id': outcome.testCase?.id,
				'testType': outcome.testCase?.testType?.name],
			result: outcome.testResult?.name,
			owner: outcome.owner,
			note: outcome.note,
			testOutput: outcome.testOutput,
			duration: outcome.duration]
		render myJson as JSON
	}


	def findForTestRun = {
		TestCase testCase = TestCase.get(params.testCase)
		if (!testCase) {
			response.status = response.SC_NOT_FOUND
			render "Test case ${params.testCase} not found"
		}

		TestRun testRun = TestRun.get(params.testRun)
		if (!testRun) {
			response.status = response.SC_NOT_FOUND
			render "Test run ${params.testRun} not found"
		}

		List<TestOutcome> out = dataService.getTestOutcomes(testCase, testRun)
		if (!out) {
			response.status = response.SC_NOT_FOUND
			render "Test outcome not found for test case ${params.testCase} and test run ${params.testRun}"
		} else {
			def outsToReturn = out.collect { it.toTestOutcomeApi() }
			render xstream.toXML(outsToReturn)
		}
	}


	def getXml = {
		TestOutcome outcome = TestOutcome.get(params.id)
		def outString = ""
		if (outcome) {
			outString = xstream.toXML(outcome.toTestOutcomeApi())
		} else {
		    response.status = response.SC_NOT_FOUND
		}
		render outString
	}

	def outcome = {   //todo: is this and the corresponding GSP used anymore? investigate
		['testOutcome': TestOutcome.get(params.id)]
	}

	def saveDetails = {
		testOutcomeService.updateTestOutcome(params)
		render "OK"
	}

	def latest = {
		def tc = TestCase.get(params.id)
		List outs = dataService.getTestOutcomeHistory(tc, 0, 1, "testRun.dateExecuted", "desc")
		['testOutcome': outs[0], 'testRun': outs[0].testRun, 'project': outs[0].testRun.project]
	}


	def applyAnalysis = {
		def targets = []
		if (params.target) {
			def targetParamList
			if (params.target instanceof String[]) {
				targetParamList = params.target as List
			} else {
				targetParamList = [params.target]
			}
			targetParamList.each { targets << TestOutcome.get(it)	}
		}

		def fieldUpdateMap = [:]

		if (params.src) {
			def source = TestOutcome.get(params.src)
			def fields
			if (params.field instanceof String[]) {
				fields = params.field as List
			} else if (params.field) {
				fields = [params.field]
			} else {
				fields = testOutcomeService.getValidAnalysisFields()
			}

			fields.each {myField ->
				fieldUpdateMap[myField] = source.getProperty(myField)
			}
			testOutcomeService.applyAnalysis(source, targets, fields)
		}

		def myJson = [updatedRecords: targets.collect {it.id}, fields: fieldUpdateMap]
		render myJson as JSON
	}


	def outputData = {
		def outcomeIds = [params.id].flatten().collect {
			Long.valueOf(it)
		}
		def testOutcomes = dataService.getTestOutcomes(outcomeIds)
		def outputJson = []
		def formatter = testOutcomeService.getTestCaseFormatter("methodonly")

		testOutcomes.each { testOutcome ->
			def output
			if (testOutcome && testOutcome.testOutput) {
				output = testOutcome.testOutput.encodeAsHTML().replaceAll("\n", "<br/>")
			} else {
				output = "(No output available)"
			}
			outputJson += ['output': output, 'id': testOutcome.id, 'testName': testOutcome.testCase.fullName,
			'shortName': formatter.getTestName(testOutcome.testCase)]
		}
		withFormat {
			json {
				def myJson = ['testOutputs': outputJson]
				render myJson as JSON
			}
		}
	}


	def update = {
		def testOutcome = (TestOutcomeApi) xstream.fromXML(request.inputStream)
		try {
			testOutcomeService.updateTestOutcome(testOutcome)
			render ""
		} catch (CuantoException e) {
			response.status = response.SC_INTERNAL_SERVER_ERROR
			render e.message
		}
	}


	def delete = {
		def testOutcome = TestOutcome.get(params.id)
		if (testOutcome) {
			testOutcomeService.deleteTestOutcome(testOutcome)
		}
		render ""
	}
}