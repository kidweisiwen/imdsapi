package me

import grails.gorm.transactions.Transactional
import org.apache.poi.ss.usermodel.BorderStyle
import org.apache.poi.ss.usermodel.HorizontalAlignment

@Transactional
class SubProjectVersionService {

    def renderRow = {sheet,data,index,headStyle,rowStyle->
        def row = sheet.createRow(index);
        data.eachWithIndex{it,i->
            def cell = row.createCell(i);
            cell.setCellValue(it.value)
            cell.setCellStyle(index==0?headStyle:rowStyle)
            //sheet.setColumnWidth(start, it.getBytes().length*2*256);
            sheet.setColumnWidth(i, 256 * it.width + 184);
        }
    }


    @Transactional(readOnly = true)
    def createSheet(workbook,sheetName,sheetData) {

        def headFont = workbook.createFont();
        headFont.setFontHeightInPoints((short) 15);
        headFont.setFontName("Arial");
        headFont.setBold(true);

        def headFont1 = workbook.createFont();
        headFont1.setFontHeightInPoints((short) 15);
        headFont1.setFontName("微软雅黑");
        headFont1.setBold(true);

        def headCellStyle = workbook.createCellStyle();
        headCellStyle.setFont(headFont);
        headCellStyle.setAlignment(HorizontalAlignment.CENTER);
        headCellStyle.setWrapText(true);
        headCellStyle.setBorderTop(BorderStyle.DOUBLE);
        headCellStyle.setBorderLeft(BorderStyle.DOUBLE);
        headCellStyle.setBorderRight(BorderStyle.DOUBLE);
        headCellStyle.setBorderBottom(BorderStyle.DOUBLE);


        def rowFont = workbook.createFont();
        def rowCellStyle = workbook.createCellStyle();
        rowCellStyle.setAlignment(HorizontalAlignment.CENTER);
        rowCellStyle.setVerticalAlignment(rowCellStyle.getVerticalAlignmentEnum().CENTER);
        rowCellStyle.setFont(rowFont);
        rowCellStyle.setWrapText(true);
        rowCellStyle.setBorderTop(BorderStyle.DOUBLE);
        rowCellStyle.setBorderLeft(BorderStyle.DOUBLE);
        rowCellStyle.setBorderRight(BorderStyle.DOUBLE);
        rowCellStyle.setBorderBottom(BorderStyle.DOUBLE);

        def sheet = workbook.createSheet(sheetName);


        sheetData.eachWithIndex{it,i->
            renderRow(sheet,it,i,headCellStyle,rowCellStyle)
        }
    }
}
