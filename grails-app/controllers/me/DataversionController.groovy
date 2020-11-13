package me


import grails.rest.*
import grails.converters.*
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory

class DataversionController {
	static responseFormats = ['json', 'xml']
	def dataVersionService
    def index() { }



    def upload() {
        def results = [code: 0, data: [total: 0, rows: []]]
        def data = dataVersionService.uploadExcelData(params.file)
        results.data.total = data.size()
        results.data.rows = data

        render results as JSON
    }

    def list() {
        def results = [code: 0, data: [:]]


        render results as JSON
    }

}
