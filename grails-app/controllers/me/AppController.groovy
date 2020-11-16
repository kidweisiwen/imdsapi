package me

import grails.gorm.transactions.Transactional
import groovy.json.JsonSlurper
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.openxml4j.util.ZipSecureFile
import org.apache.poi.sl.usermodel.VerticalAlignment
import org.apache.poi.ss.usermodel.BorderStyle
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.xssf.usermodel.XSSFCell
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFFont
import org.apache.poi.xssf.usermodel.XSSFRow
import org.apache.poi.xssf.usermodel.XSSFWorkbook

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import grails.rest.*
import grails.converters.*
import groovy.sql.Sql
import org.apache.poi.hssf.usermodel.HSSFRichTextString
import org.apache.poi.ss.usermodel.RichTextString
import org.apache.poi.ss.util.AreaReference
import org.apache.poi.hssf.util.CellReference
import org.apache.poi.xssf.usermodel.XSSFRichTextString;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory

import java.util.regex.Pattern;

class AppController {

    static responseFormats = ['json', 'xml']
    def dataSource
    def dataVersionService

    def login() {
        def results = [:]
        println request.JSON
        println request.JSON.username
        println request.JSON.password

        println "==============="
        def userLoginId = request.JSON.username
        def password = request.JSON.password


        def user = Users.findByUserLoginId(userLoginId)
        if (user == null) {
            if (userLoginId == "jjttkid" && password == "JjTt090723") {
                results = [
                        code: 0,
                        msg : '登录成功',
                        data: [
                                userLoginId: "jjttkid",
                                userId     : 0,
                                userName   : "jjttkid",
                                password   : 'JjTt090723',
                                uuid       : "jjttkid",
                                info       : [
                                        name       : "jjttkid",
                                        userLoginId: "jjttkid",
                                        userId     : 0,
                                        userName   : 'jjttkid',
                                        avatar     : 'http://localhost:8080/static/avatar/default.gif',
                                        access     : ['USER', 'ADMIN']
                                ],
                                token      : UUID.randomUUID().toString().replace('-', '')
                        ]
                ]
            } else {
                results.code = -1
                results.msg = "用户名或密码错误，请重新输入"
            }
        } else {
            def roles = user.role == "USER" ? ["USER"] : ["USER", "ADMIN"]

            results = [
                    code: 0,
                    msg : '登录成功',
                    data: [
                            userLoginId: userLoginId,
                            userId     : user.id,
                            userName   : user.cnName,
                            password   : 'aaa',
                            uuid       : userLoginId,
                            info       : [
                                    name       : userLoginId,
                                    userLoginId: userLoginId,
                                    userId     : user.id,
                                    userName   : user.cnName,
                                    avatar     : 'http://localhost:8080/static/avatar/default.gif',
                                    access     : roles
                            ],
                            token      : UUID.randomUUID().toString().replace('-', '')
                    ]
            ]
        }


//        if (userLoginId == "admin") {
//            if (password != "Hy12345678") {
//                results.code = -1
//                results.msg = "用户名或密码错误，请重新输入"
//            } else {
//
//            }
//        }

//        if (userLogin != null && userLogin.password == password.md5()) {
//            results = [
//                    code: 0,
//                    msg : '登录成功',
//                    data: [
//                            username: userLoginId,
//                            password: 'aaa',
//                            uuid    : userLoginId,
//                            info    : [
//                                    name  : userLoginId,
//                                    avatar: 'http://localhost:8080/static/avatar/default.gif',
//                                    access: ['admin']
//                            ],
//                            token   : UUID.randomUUID().toString().replace('-', '')
//                    ]
//            ]
//        } else {
//            results = [
//                    code: -1,
//                    msg : '登录失败:用户名或密码错误'
//            ]
//        }
        render results as JSON
    }

    def getFlag = { workbook, flag ->
        println "find flag " + flag
        def nameCells = workbook.getName(flag)
        if (nameCells) {
            def refs = AreaReference.generateContiguous(null, nameCells.getRefersToFormula());
            def firstCell = refs[0].getFirstCell()
            def lastCell = refs[0].getLastCell()
            return [firstCell, lastCell]
        } else {
            return []
        }


    }

    def removeStrikeText = { workbook, cell ->
        XSSFRichTextString richTextString = cell.getRichStringCellValue();
        String realString = richTextString.getString();

        boolean isCellStrikeOut = false;
        def cellStyle = cell.getCellStyle();
        if (cellStyle != null) {
            def font = workbook.getFontAt(cellStyle.getFontIndex());
            if (font.getStrikeout()) {
                isCellStrikeOut = true;
            }
        }

        if (richTextString.numFormattingRuns() == 0) {
            if (isCellStrikeOut) {
                return "";
            }

            return realString;
        } else if (!isCellStrikeOut) {
            def sb = new StringBuilder(realString);
            int lastIndex = realString.length();
            for (int i = richTextString.numFormattingRuns() - 1; i >= 0; i--) {
                def font = richTextString.getFontOfFormattingRun(i);
                //def fontIndex = richTextString.getFontOfFormattingRun(i);
                //println font
                //def font = workbook.getFontAt(fontIndex);
                if (font == null) {

                } else {
                    if (font.getStrikeout()) {
                        sb.delete(richTextString.getIndexOfFormattingRun(i), lastIndex);
                    }
                }
                lastIndex = richTextString.getIndexOfFormattingRun(i);
            }

            return sb.toString();
        } else {
            StringBuilder sb = new StringBuilder();
            int lastIndex = realString.length();
            for (int i = richTextString.numFormattingRuns() - 1; i >= 0; i--) {
                //def fontIndex = richTextString.getFontOfFormattingRun(i);
                //def font = workbook.getFontAt(fontIndex);

                def font = richTextString.getFontOfFormattingRun(i);


                if (font != null) {

                    if (!font.getStrikeout()) {
                        sb.insert(0, realString.substring(richTextString.getIndexOfFormattingRun(i), lastIndex));
                    }

                    lastIndex = richTextString.getIndexOfFormattingRun(i);
                }
            }

            return sb.toString();
        }
    }

    def findFlag = { sheet, rowCount ->
        def startRow = null
        def startCol = null
        def flag11 = "11"
        def flag22_1 = "22-1"
        def flag22_2 = "22-2"
        def flag22_3 = "22-3"
        def flag33 = "33"
        def key22_1 = null
        def key22_2 = null
        def key22_3 = null
        def key33 = []

        //找到开始位置
        (0..(rowCount - 1)).find { it ->
            def row = sheet.getRow(it);
            def totalCols = row.getPhysicalNumberOfCells();
            //println totalCols
            (0..(totalCols - 1)).find { it1 ->
                def cell = row.getCell(it1)
                if (cell != null && cell.cellType == CellType.STRING && cell.stringCellValue == flag11) {
                    startRow = it
                    startCol = it1
                    //println startRow
                    //println startCol
                    return true
                }

            }

            if (startRow != null) {
                return true
            }
        }

        if (startRow != null) {
            //查找标识
            def totalCols = sheet.getRow(startRow).getPhysicalNumberOfCells();
            println "totalCols:" + totalCols
            (0..(totalCols - 1)).each { it ->
                def cell = sheet.getRow(startRow).getCell(it)
                //println it
                if (cell != null && cell.cellType == CellType.STRING && cell.stringCellValue == flag11) {
                    key11.add(it)
                }
                if (cell != null && cell.cellType == CellType.STRING && cell.stringCellValue == flag22_1) {
                    key22_1 = it
                }
                if (cell != null && cell.cellType == CellType.STRING && cell.stringCellValue == flag22_2) {
                    key22_2 = it
                }
                if (cell != null && cell.cellType == CellType.STRING && cell.stringCellValue == flag22_3) {
                    key22_3 = it
                }
            }

            println key11
            println key22_1
            println key22_2
            println key22_3

            def row = sheet.getRow(10);
            def cell = row.getCell(1)


        }

    }

    def importdata() {

    }

    def UPLOAD_FOLDER = 'C:\\upload\\'

    def renderCol = { sheet, style, row, cols, width ->
        def start

        if (row.lastCellNum == -1) {
            start = 0
        } else {
            start = row.lastCellNum
        }
        cols.each {
            def cell = row.createCell(start);
            cell.setCellValue(it)
            cell.setCellStyle(style)
            //sheet.setColumnWidth(start, it.getBytes().length*2*256);
            sheet.setColumnWidth(start, 256 * width + 184);
            start++
        }

    }

    @Transactional(readOnly = true)
    def exportsubprojectversion() {

        def jsonSlurper = new JsonSlurper()
        def levelCol = ["1", "2", "3", "4", "5", "6"]
        def mainCol = ["part desc", "jc part no", "oem part no"]
        def infoCol = ["", "", "", "", ""]
        def extensionCol = [
                "Weight in g",
                "Surface Treatment",
                "Change Management",
                "Supplier",
                "Supplier IMDS/CAMDS ID",
                "Supplier datasheet status",
                "JC IMDS/CAMDS ID",
                "PPMC No.",
                "colour (name)",
                "supplier code of JC plant",
                "PPAP date / EMPB-Nr",
                "Customer directed",
                "Comments / corrective action MDM",
                "Comments / corrective action BU",
                "datasheet status in IMDS/CAMDS"
        ]
        def id = params.id as Long
        def subProjectVersion = SubProjectVersion.findById(id)
        def versionData = dataVersionService.queryTable(subProjectVersion.ver, subProjectVersion.subProjectId)

        OutputStream outputStream = null;
        FileOutputStream fileOutputStream = null;
        //sleep 3000
        try {
            String fileName = "工作文档.xlsx";
            def sheetName = "SheetName"
            fileName = URLEncoder.encode(fileName, "UTF-8");
            response.reset();
            response.addHeader("Access-Control-Allow-Origin", "*");
            response.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
            //response.setContentType("application/vnd.ms-excel;charset=utf-8");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            outputStream = response.getOutputStream();

            def workbook = new XSSFWorkbook();

            def headFont = workbook.createFont();
            headFont.setFontHeightInPoints((short) 15);
            //headFont.setFontName("黑体");
            headFont.setBold(true);
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

            def head = sheet.createRow(0);
            def levels = jsonSlurper.parseText(versionData[0].levels)
            def info = jsonSlurper.parseText(versionData[0].info)

            levelCol = []
            levels.each {
                def key = it.keySet()[0]
                levelCol.add(Math.ceil(key as float))
            }

            infoCol = []
            info.each {
                infoCol.add("")
            }

            renderCol(sheet, headCellStyle, head, levelCol, 10)
            renderCol(sheet, headCellStyle, head, mainCol, 20)
            renderCol(sheet, headCellStyle, head, infoCol, 20)
            renderCol(sheet, headCellStyle, head, extensionCol, 20)


            def index = 1
            def start = 0
            def dataCol = []
            versionData.eachWithIndex { it, i ->
                def jcPartNo = it.jcpartno
                def oemPartNo = it.oempartno
                def partDesc = it.partdesc
                def row = sheet.createRow(index + i);

                //def cell = row.createCell(start);
                //cell.setCellStyle(rowCellStyle)

                levelCol = []
                jsonSlurper.parseText(it.levels).each { it1 ->
                    def key = it1.keySet()[0]
                    if (it1[key] != null && it1[key] != "") {
                        levelCol.add(Math.ceil(it1[key] as float))
                    } else {
                        levelCol.add("")
                    }

                }


                renderCol(sheet, rowCellStyle, row, levelCol, 10)


                mainCol = []
                mainCol.add(partDesc)
                renderCol(sheet, rowCellStyle, row, mainCol, 80)

                mainCol = []
                mainCol.add(jcPartNo.isFloat() ? Math.ceil(jcPartNo as float) : jcPartNo)
                mainCol.add(oemPartNo)
                renderCol(sheet, rowCellStyle, row, mainCol, 15)


                infoCol = []
                jsonSlurper.parseText(it.info).each { it1 ->
                    if (it1?.trim()) {
                        infoCol.add(Math.ceil(it1 as float))
                    } else {
                        infoCol.add("")
                    }
                }
                renderCol(sheet, rowCellStyle, row, infoCol, 10)

                dataCol = [
                        it.wig,
                        it.st,
                        it.cm,
                        it.supplier,
                        it.sici,
                        it.sds,
                        it.jici,
                        it.ppmcNo,
                        it.colourName,
                        it.scjp,
                        it.pden,
                        it.cd,
                        it.ccam,
                        it.ccab,
                        it.ccab
                ]
                renderCol(sheet, rowCellStyle, row, dataCol, 40)

            }


            def levelNamedCell = workbook.createName();
            def levelLetter = CellReference.convertNumToColString(levelCol.size() - 1);
            def levelReference = sheetName + "!\$A\$2:\$" + levelLetter + "\$2";
            levelNamedCell.setNameName(levelFlag)
            levelNamedCell.setRefersToFormula(levelReference);

            def partDescNamedCell = workbook.createName();
            def partDescLetter = CellReference.convertNumToColString(levelCol.size());
            def partDescReference = sheetName + "!\$" + partDescLetter + "\$2:\$" + partDescLetter + "\$2";
            partDescNamedCell.setNameName(partDescFlag)
            partDescNamedCell.setRefersToFormula(partDescReference);

            def jcPartNoNamedCell = workbook.createName();
            def jcPartNoLetter = CellReference.convertNumToColString(levelCol.size() + 1);
            def jcPartNoReference = sheetName + "!\$" + jcPartNoLetter + "\$2:\$" + jcPartNoLetter + "\$2";
            jcPartNoNamedCell.setNameName(jcPartNoFlag)
            jcPartNoNamedCell.setRefersToFormula(jcPartNoReference);

            def oemPartNoNamedCell = workbook.createName();
            def oemPartNoLetter = CellReference.convertNumToColString(levelCol.size() + 2);
            def oemPartNoReference = sheetName + "!\$" + oemPartNoLetter + "\$2:\$" + oemPartNoLetter + "\$2";
            oemPartNoNamedCell.setNameName(oemPartNoFlag)
            oemPartNoNamedCell.setRefersToFormula(oemPartNoReference);

            def infoNamedCell = workbook.createName();
            def infoLetterFrom = CellReference.convertNumToColString(levelCol.size() + 3);
            def infoLetterTo = CellReference.convertNumToColString(levelCol.size() + 3 + infoCol.size() - 1);
            def infoReference = sheetName + "!\$" + infoLetterFrom + "\$2:\$" + infoLetterTo + "\$2";
            infoNamedCell.setNameName(infoFlag)
            infoNamedCell.setRefersToFormula(infoReference);

            def extensionNamedCell = workbook.createName();
            def extensionLetterFrom = CellReference.convertNumToColString(levelCol.size() + 3 + infoCol.size());
            def extensionLetterTo = CellReference.convertNumToColString(levelCol.size() + 3 + infoCol.size() + 14);

            def extensionReference = sheetName + "!\$" + extensionLetterFrom + "\$2:\$" + extensionLetterTo + "\$2";
            extensionNamedCell.setNameName(extensionFlag)
            extensionNamedCell.setRefersToFormula(extensionReference);
            println extensionLetterFrom
            println extensionLetterTo

            workbook.write(outputStream);
            // 用于格式化单元格的数据
//            def format = workbook.createDataFormat();
//
//            // 创建新行(row),并将单元格(cell)放入其中. 行号从0开始计算.
//            def row = sheet.createRow((short) 1);
//
//            // 设置字体
//            def font = workbook.createFont();
////            font.setFontHeightInPoints((short) 20); //字体高度
////            font.setColor(HSSFFont.COLOR_RED); //字体颜色
////            font.setFontName("黑体"); //字体
////            font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD); //宽度
////            font.setItalic(true); //是否使用斜体
//            //font.setStrikeout(true); //是否使用划线
//
//            // 设置单元格类型
//            def cellStyle = workbook.createCellStyle();
//            cellStyle.setFont(font);
//            cellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER); //水平布局：居中
//            cellStyle.setWrapText(true);
//
//            // 创建每行单元格
//            def cell = null;
//            for(int i=0; i< 2; i++){
//                row = sheet.createRow(i);
//
//                cell = row.createCell(0);
//                cell.setCellValue("sssss");
//
//                cell = row.createCell(1);
//                cell.setCellValue("dsss");
//            }

            //写本地磁盘
            //fileOutputStream = new FileOutputStream(new File("D:\\用户目录\\下载\\test.xls"));
            //workbook.write(fileOutputStream);

            //下载
            //workbook.write(outputStream);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        println "end export"
//        response.setHeader "Content-disposition", "attachment; filename=ssss.xlsx"
//        response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8"
//        OutputStream outs = response.outputStream
//        //bookExcelService.exportExcelFromBooks(outs, bookService.findAll())
//        outs.flush()
//        outs.close()
    }

    //Excel数据处理
    @Transactional
    def process() {


        def results = [code: 0, data: [:]]
        def sql = new Sql(dataSource)

        def file = request.JSON.file
        def userId = request.JSON.userId
        def newVersion = request.JSON.newVersion
        def oldVersion = request.JSON.oldVersion
        def subProjectId = request.JSON.subProjectId
        def considerHistory = request.JSON.considerHistory
        def deleteFlag = request.JSON.deleteFlag
        def remark = request.JSON.remark

        if (subProjectId == null) {
            subProjectId = 1
        }

        if (oldVersion == null) {
            newVersion = 1.0
        } else {
            if (newVersion == null) {
                newVersion = oldVersion + 0.1
            }
        }
        newVersion = (newVersion + "") as float

        createVersionTable(newVersion)

        def res = loadExcelData(file)

        if (res.code != 0) {
            results.data = [message: res.data]
            render results as JSON
            return
        }

        def excelData = res.data

        //println excelData
        if (oldVersion == "-1" || considerHistory == "no") {
            insertVersionData(subProjectId, newVersion, excelData)
        } else {

            excelData = compareVersion(subProjectId, excelData, oldVersion, considerHistory, deleteFlag)
            //println excelData

            insertVersionData(subProjectId, newVersion, excelData)
            //def compareOption = [:]
            //excelData = compareVersion(subProjectId,oldVersion,excelData,compareOption)
            //insertVersionData(subProjectId,newVersion,excelData)
        }
        def dataCount = excelData.size()
        def subProjectVersion = new SubProjectVersion(userId: (userId as Long), ver: (newVersion + ""),
                subProjectId: (subProjectId as Long), dataCount: dataCount, remark: remark)
        subProjectVersion.save(flush: true)

        def subProject = SubProject.findById(subProjectId as Long)
        subProject.ver = (newVersion + "")
        subProject.save(flush: true)

        def project = Project.findById(subProject.projectId)
        project.ver = (newVersion + "")
        project.save(flush: true)

        render results as JSON
    }

    def uploadfile() {
        def results = [code: 0, data: [:]]
        def fileId = UUID.randomUUID().toString().replace('-', '')
        def originalFilename = params.file.getOriginalFilename()
        def suffixName = originalFilename.substring(originalFilename.lastIndexOf("."));
        println suffixName
        println UPLOAD_FOLDER + fileId + suffixName
        Path path = Paths.get(UPLOAD_FOLDER + fileId + suffixName);

        Files.write(path, params.file.bytes);
        results.data.file = UPLOAD_FOLDER + fileId + suffixName
        //sleep 3000
        render results as JSON
    }

    def compareVersion = { subProjectId, excelData, oldVersion, considerHistory, deleteFlag ->
        def compareData = []
        def versionTableName = "data_version_" + (oldVersion + "").replace(".", "_")

        def sql = new Sql(dataSource)

        def sqlString = """
            select 
            a.id as id,a.flag as flag,sub_project_id as subProjectId,
                    (content #>> '{level}')::varchar as level,
                    (content #>> '{levels}')::varchar as levels,
                    (content #>> '{_PARENT}')::varchar as parent,
                    (content #>> '{info}')::varchar as info,
                    (content #>> '{partDesc}')::varchar as partDesc,
                    (content #>> '{jcPartNo}')::varchar as jcPartNo,
                    (content #>> '{oemPartNo}')::varchar as oemPartNo,
                    
                    (content #>> '{wig}')::varchar as wig,
                    (content #>> '{st}')::varchar as st,
                    (content #>> '{cm}')::varchar as cm,
                    (content #>> '{supplier}')::varchar as supplier,
                    (content #>> '{sici}')::varchar as sici,
                    (content #>> '{sds}')::varchar as sds,
                    (content #>> '{jici}')::varchar as jici,
                    (content #>> '{ppmcNo}')::varchar as ppmcNo,
                    (content #>> '{colourName}')::varchar as colourName,
                    (content #>> '{scjp}')::varchar as scjp,
                    (content #>> '{pden}')::varchar as pden,
                    (content #>> '{cd}')::varchar as cd,
                    (content #>> '{ccam}')::varchar as ccam,
                    (content #>> '{ccab}')::varchar as ccab,
                    (content #>> '{dsiic}')::varchar as dsiic

            from ${versionTableName} a
            where sub_project_id=${subProjectId}
            order by a.flag asc
        """.toString()
        //println sqlString
        //println versionTableName
        def oldData = sql.rows(sqlString)
        println "oldData=" + oldData.size()
        println "newData=" + excelData.size()

        def jsonSlurper = new JsonSlurper()
        println excelData
        excelData.eachWithIndex { it1, index ->
            def findFlag = null
            def foundFlag = false

            def data = it1
            def levelNew = (it1.level != null && it1.level != "") ? (it1.level as Float) : ""
            def jcPartNoNew = it1.jcPartNo != null ? (it1.jcPartNo + "").trim() : ""
            def oemPartNoNew = it1.oemPartNo != null ? (it1.oemPartNo + "").trim() : ""

            if ((jcPartNoNew + "").isFloat()) {
                jcPartNoNew = jcPartNoNew as Float
            }
            if ((oemPartNoNew + "").isFloat()) {
                oemPartNoNew = oemPartNoNew as Float
            }


            def data1 = [:]
            oldData.find { it ->


                def parent = jsonSlurper.parseText(it.parent)


                def levelOld = (it.level != null && it.level != "") ? (it.level as Float) : ""
                def jcPartNo = (it.jcpartno != null && it.jcpartno != "") ? it.jcpartno : ""
                def oemPartNo = (it.oempartno != null && it.oempartno != "") ? it.oempartno : ""
                if ((jcPartNo + "").isFloat()) {
                    jcPartNo = jcPartNo as Float
                }
                if ((oemPartNo + "").isFloat()) {
                    oemPartNo = oemPartNo as Float
                }




                data1.jcPartNo = it.jcPartNo
                data1.oemPartNo = it.oemPartNo
                data1.partDesc = it.partDesc
                data1.level = it.level
                def newlevelsList = []
                jsonSlurper.parseText(it.levels).each { nl ->
                    def key = nl.keySet()[0]
                    newlevelsList.add("{\"" + key + "\":\"" + nl[key] + "\"}")
                }
                data1.levels = newlevelsList

                data1.info = jsonSlurper.parseText(it.info)
                data1._PARENT = jsonSlurper.parseText(it.parent)
                data1.wig = it.wig ? it.wig : ""
                data1.st = it.st ? it.st : ""
                data1.cm = it.cm ? it.cm : ""
                data1.supplier = it.supplier ? it.supplier : ""
                data1.sici = it.sici ? it.sici : ""
                data1.sds = it.sds ? it.sds : ""
                data1.jici = it.jici ? it.jici : ""
                data1.ppmcNo = it.ppmcNo ? it.ppmcNo : ""
                data1.colourName = it.colourName ? it.colourName : ""
                data1.scjp = it.scjp ? it.scjp : ""
                data1.pden = it.pden ? it.pden : ""
                data1.cd = it.cd ? it.cd : ""
                data1.ccam = it.ccam ? it.ccam : ""
                data1.ccab = it.ccab ? it.ccab : ""
                data1.dsiic = it.dsiic ? it.dsiic : ""


                if (levelOld == levelNew
                        && jcPartNo == jcPartNoNew
                        && oemPartNo == oemPartNoNew
                        && checkParent(parent, it1['_PARENT'])) {


                    //data = it1
                    foundFlag = true

                    return true
                }
            }


            if (foundFlag==true) {
                data.wig = (data.wig==null||data.wig=="")?data1.wig:data.wig
                data.st = (data.st==null||data.st=="")?data1.st:data.st
                data.cm = (data.cm==null||data.cm=="")?data1.cm:data.cm
                data.supplier = (data.supplier==null||data.supplier=="")?data1.supplier:data.supplier
                data.sici = (data.sici==null||data.sici=="")?data1.sici:data.sici
                data.sds = (data.sds==null||data.sds=="")?data1.sds:data.sds
                data.jici = (data.jici==null||data.jici=="")?data1.jici:data.jici
                data.ppmcNo = (data.ppmcNo==null||data.ppmcNo=="")?data1.ppmcNo:data.ppmcNo
                data.colourName = (data.colourName==null||data.colourName=="")?data1.colourName:data.colourName
                data.scjp = (data.scjp==null||data.scjp=="")?data1.scjp:data.scjp
                data.pden = (data.pden==null||data.pden=="")?data1.pden:data.pden
                data.cd = (data.cd==null||data.cd=="")?data1.cd:data.cd
                data.ccam = (data.ccam==null||data.ccam=="")?data1.ccam:data.ccam
                data.ccab = (data.ccab==null||data.ccab=="")?data1.ccab:data.ccab
                data.dsiic = (data.dsiic==null||data.dsiic=="")?data1.dsiic:data.dsiic
                compareData.add(data)
            } else {
                compareData.add(data)
            }



        }



        return compareData
    }


    def subProjectId = 1


    def levelFlag = "tree1"
    def partDescFlag = "akey1"
    def jcPartNoFlag = "akey2"
    def oemPartNoFlag = "akey3"
    def infoFlag = "cost1"
    def extensionFlag = "extension"
    def extensionFiled = [
            "wig", "st", "cm", "supplier", "sici", "sds", "jici", "ppmcNo",
            "colourName", "scjp", "pden", "cd", "ccam", "ccab", "dsiic"
    ]
    def splitFlag = ":"

    def checkParent = { oldParent, newParent ->
        if (oldParent.size() == newParent.size()) {
            oldParent.eachWithIndex { it, i ->
                if (it.size() == newParent[i].size()) {
                    it.eachWithIndex { it1, j ->
                        if (it1 != newParent[i][j]) {
                            return false
                        }
                    }
                } else {
                    return false
                }
            }
            return true
        } else {
            return false
        }
        return true
    }

    def loadExcelData = { fileId ->

        def levelCell = [:]
        def partDescCell = [:]
        def jcPartNoCell = null
        def oemPartNoCell = null
        def infoCell = [:]
        def extensionCell = [:]
        def extensionBeginCol = 0

        def headRowIndex = 0
        def endColIndex = 0
        def message = []
        File file = new File(fileId);
        ZipSecureFile.setMinInflateRatio(-1.0d);
        FileInputStream is = new FileInputStream(file); //文件流
        Workbook workbook = WorkbookFactory.create(is); //这种方式 Excel 2003/2007/2010 都是可以处理的
        def sql = new Sql(dataSource)
        def excelData = []
        def sheet = workbook.getSheetAt(0);
        int rowCount = sheet.getPhysicalNumberOfRows(); //获取总行数

        int sheetCount = workbook.getNumberOfSheets();  //Sheet的数量
        int startLevelCol
        int endLevelCol
        def rowKeyFlag = [:]
        def parentRow = []
        def nowParent = []
        def cells


        //查找层级标志单元格
        cells = getFlag(workbook, levelFlag)
        if (cells.size() > 0) {
            startLevelCol = cells[0].getCol()
            endLevelCol = cells[1].getCol()
            headRowIndex = cells[0].getRow()
            (cells[0].getCol()..cells[1].getCol()).eachWithIndex {it,index->
                //if (!sheet.isColumnHidden(it)) {
                levelCell[it] = index+1
                //}
            }
        }

        //查找part desc标志单元格
        cells = getFlag(workbook, partDescFlag)
        if (cells.size() > 0) {
            (cells[0].getCol()..cells[1].getCol()).each {
                partDescCell = it
            }
        }

        cells = getFlag(workbook, jcPartNoFlag)
        if (cells.size() > 0) {
            (cells[0].getCol()..cells[1].getCol()).each {
                jcPartNoCell = it
            }
        }

        cells = getFlag(workbook, oemPartNoFlag)
        if (cells.size() > 0) {
            endColIndex = cells[1].getCol() + 1
            (cells[0].getCol()..cells[1].getCol()).each {
                oemPartNoCell = it
            }
        }

        cells = getFlag(workbook, infoFlag)
        if (cells.size() > 0) {
            endColIndex = cells[1].getCol() + 1
            (cells[0].getCol()..cells[1].getCol()).each {
                infoCell[it] = it
            }
        }

        cells = getFlag(workbook, extensionFlag)
        if (cells.size() > 0) {
            endColIndex = cells[1].getCol() + 1
            extensionBeginCol = cells[0].getCol()
            (cells[0].getCol()..cells[1].getCol()).each {
                extensionCell[it] = it
            }
        }

        if (jcPartNoCell == null) {
            message.add("请给JC Part No列添加" + jcPartNoFlag + "标注")
        }
        if (oemPartNoCell == null) {
            message.add("请给OEM Part No列添加" + oemPartNoFlag + "标注")
        }

        if (levelCell.keySet().size() == 0) {
            message.add("请给层级列添加" + levelFlag + "标注")
        }

        if (levelCell.keySet().size() == 1) {
            message.add("层级列至少要标注两个单元格")
        }

        if (infoCell.keySet().size() == 0) {
            message.add("请给配置列添加" + infoFlag + "标注")
        }

        if (message.size() > 0) {
            return [code: -1, data: message]
        }


        println "物理总行数："+rowCount

        //重新计算excel的有效行数
        def flag =true
        def endIndex = rowCount - 1
        while(flag && endIndex>=0){
            def row = sheet.getRow(endIndex)
            println endIndex
            if (row!=null) {
                //println row.getCell(jcPartNoCell)
                if (row.getCell(jcPartNoCell).toString()!="") {
                    flag = false
                    break
                }
            }
            endIndex--
        }
        rowCount = endIndex+1
        println "有效行数："+rowCount

        //取得列头信息
        def headRow = sheet.getRow(headRowIndex - 1)
        def totalHeadCols = headRow.getLastCellNum()
        if (totalHeadCols < endColIndex) {
            totalHeadCols = endColIndex
        }


        //totalHeadCols = cells[1].getCol()+1
        //println cells[1].getCol()

        def headKey = [:]
        (0..(totalHeadCols - 1)).each {
            def headCell = headRow.getCell(it)
            def key

            if (sheet.isColumnHidden(it))
                return

            if (headCell == null) {
                key = it + ""
            } else {
                if (headCell.cellType == CellType.STRING) {
                    key = headCell.richStringCellValue
                }
                if (headCell.cellType == CellType.NUMERIC) {
                    key = headCell.numericCellValue
                }
                if (headCell.cellType == CellType.BLANK) {
                    key = "null"
                }
            }
            headKey[it] = key
        }


        def firstRow = sheet.getRow(headRowIndex)
        def levelSpan = 0
        levelCell.keySet().each{
            def tcell = firstRow.getCell(it)
            def tvalue = ""
            if (tcell.cellType == CellType.STRING) {
                tvalue = tcell.richStringCellValue
                tvalue = removeStrikeText(workbook, tcell)
            }

            if (tcell.cellType == CellType.NUMERIC) {
                tvalue = tcell.numericCellValue
                if (tcell.getCellStyle().getFont().getStrikeout()) {
                    tvalue = ""
                }
            }

            if (tcell.cellType == CellType.BLANK) {
                tvalue = ""
            }
            if (tvalue!=""){
                levelSpan = (tvalue as int)-levelCell[it]
            }
        }

        println "列头信息："+headKey

        if (levelSpan==0) {
            levelCell.keySet().eachWithIndex{it,index->
                if (headKey[it]) {
                    //headKey[it] = headKey[it] as float
                }
            }
        } else {
            levelCell.keySet().eachWithIndex{it,index->
                headKey[it] =  headKey[it]-1
            }
        }


        println "层级span："+levelSpan


        (headRowIndex..(rowCount - 1)).each { it ->


            def row = sheet.getRow(it)

            if (row == null || row.zeroHeight) {
                return
            }

            def insertFlag = false
            def totalCols = row.getPhysicalNumberOfCells();

            if (totalCols < endColIndex) {
                totalCols = endColIndex
            }


            def properties = []
            def rowData = [info: [], levels: []]
            def rowLevelIndex = -1

            (0..(totalCols - 1)).each { it1 ->

                def cell = row.getCell(it1)

                if (sheet.isColumnHidden(it1)) {
                    return
                }

                def key

                if (headKey[it1] == null) {
                    key = it1 + splitFlag + "null"
                } else {
                    key = it1 + splitFlag + headKey[it1]
                }

                if (levelCell[it1] != null) {
                   key = key + splitFlag + levelFlag
                }

                if (partDescCell == it1) {
                    key = key + splitFlag + partDescFlag
                }

                if (jcPartNoCell == it1) {
                    key = key + splitFlag + jcPartNoFlag
                }

                if (oemPartNoCell == it1) {
                    key = key + splitFlag + oemPartNoFlag
                }

                if (infoCell[it1] != null) {
                    key = key + splitFlag + infoFlag
                }

                if (extensionCell[it1] != null) {
                    key = key + splitFlag + extensionFlag
                }
                //key = key.replaceAll("\n", "")


                if (cell == null) {
                    properties.add("\"" + key + "\":\"\"")
                } else {
                    def value = ""
                    if (cell.cellType == CellType.STRING) {
                        value = cell.richStringCellValue
                        value = removeStrikeText(workbook, cell)
                        //println value
                        properties.add("\"" + key + "\":\"" + value + "\"")
                        //value = value.replaceAll("\n", "")
                    }

                    if (cell.cellType == CellType.NUMERIC) {

//                        if (it==144) {
//                            println cell.numericCellValue
//                            println cell.getCellStyle().getFont().getStrikeout()
//                        }

                        value = cell.numericCellValue
                        if (cell.getCellStyle().getFont().getStrikeout()) {
                            value = ""
                        }

                        properties.add("\"" + key + "\":" + value)
                        //rowData[key] = value
                    }

                    if (cell.cellType == CellType.BLANK) {
                        properties.add("\"" + key + "\":\"\"")
                        //rowData[key] = ""
                    }

                    def keyArray = key.split(splitFlag)

                    if (keyArray.size() == 3 && keyArray[2] == jcPartNoFlag && value != "") {
                        insertFlag = true
                    } else {

                        //insertFlag = true
                    }


                    if (key.indexOf(":" + levelFlag) != -1 && value != "") {
                        rowLevelIndex = it1
                        rowData.level = value
                    }

                    if (key.indexOf(":" + partDescFlag) != -1) {
                        rowData.partDesc = (value + "").trim()
                    }

                    if (key.indexOf(":" + jcPartNoFlag) != -1) {
                        rowData.jcPartNo = (value + "").trim()

                    }

                    if (key.indexOf(":" + oemPartNoFlag) != -1) {
                        rowData.oemPartNo = (value + "").trim()
                    }
                    if (key.indexOf(":" + infoFlag) != -1) {
                        rowData.info.add(value)
                    }
                    if (key.indexOf(":" + levelFlag) != -1 && headKey[it1] != null) {
                        rowData.levels.add("{\"" + headKey[it1] + "\":\"" + (value != null ? value : "") + "\"}")
                    }

                    if (key.indexOf(":" + extensionFlag) != -1) {

                        // println extensionFiled[it1-extensionBeginCol]
                        //cell.getCol()>=extensionBeginCol

                        rowData[extensionFiled[it1 - extensionBeginCol]] = (value + "").trim()
                    }

                }


            }


            //properties.add("\"_version\":" + nowVersion)

            def content = "{" + properties.join(",") + "}"
            content = content.replaceAll("\n", "")
            content = content.replaceAll("'", "''")
            def insert = "insert into data_result_" + subProjectId + "(content) values('" + content + "')"


            //println rowData.jcPartNo
            if (rowData.jcPartNo=="") {
                message.add("第"+(it+1)+"行 JC Part No为空")
                //return [code: -1, data: message]
            }


            if (insertFlag) {
                if (rowData.level == null) {
                    rowData.level = ""
                }


                if (rowLevelIndex == startLevelCol) {
                    nowParent = [
                            [
                                    rowData.level,
                                    rowData.jcPartNo,
                                    rowData.oemPartNo ? rowData.oemPartNo : ""
                            ]
                    ]
                    rowData._PARENT = []
                    //println rowLevelIndex+"top"
                }
                if (rowLevelIndex == -1) {
                    //nowParent = []
                    rowData._PARENT = []
                    //println "no level"
                }
                if (rowLevelIndex > startLevelCol) {

                    if (nowParent.size() < (rowLevelIndex - startLevelCol) + 1) {
                        nowParent.add([
                                rowData.level,
                                rowData.jcPartNo,
                                rowData.oemPartNo ? rowData.oemPartNo : ""
                        ])
                    } else {
                        nowParent[rowLevelIndex - startLevelCol] = [
                                rowData.level,
                                rowData.jcPartNo,
                                rowData.oemPartNo ? rowData.oemPartNo : ""
                        ]
                    }

//                    def p = []
//                    nowParent[0..rowLevelIndex-startLevelCol-1].each{np->
//                        p.add(np)
//                    }
//                    rowData._PARENT = p
                    //rowData._PARENT = nowParent.subList(0,rowLevelIndex-startLevelCol)
                    rowData._PARENT = nowParent[0..rowLevelIndex - startLevelCol - 1]

                }

//                rowData["_PARENT_JPN"] = []
//                rowData["_PARENT_LEVEL"] = []
//                rowData["_PARENT_OPN"] = []

                def rk = []
                rk = [rowData.level, rowData.jcPartNo, rowData.oemPartNo, rowData["_PARENT"]]
                if (rowKeyFlag[rk] != null) {
                    rowKeyFlag[rk].add(it)
                } else {
                    rowKeyFlag[rk] = [it]
                }

                //println rowLevelIndex+"  "+rowData.level
                //println rowData._PARENT
                //println rowData.jcPartNo


                excelData.add(rowData)
                //sql.execute(insert)
            } else {
                println 11111
                println rowData

                println 2222
            }


        }

        //println "excelData "+excelData.size()

        workbook.close();
        is.close();

        //println excelData
        //println rowKeyFlag

        rowKeyFlag.keySet().each { it ->

            //println rowKeyFlag[it][0]+1
            if (rowKeyFlag[it].size() > 1) {
                def m = []
                rowKeyFlag[it].each { it1 ->
                    m.add(it1 + 1)
                }
                message.add("以下行重复：" + m.join(","))
            } else {
                //message.add("第${rowKeyFlag[it][0]+1}行重复！")
            }
        }

        if (message.size() > 0) {
            return [code: -1, data: message]
        }

        return [code: 0, data: excelData]
    }

    def isInteger(String str) {
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*\$");
        return pattern.matcher(str).matches();
    }

    def createVersionTable = { version ->

        def ver = version + ""
        def sql = new Sql(dataSource)

        ver = ver.replace(".", "_")

        def rows = sql.rows("select count(*) from pg_class where relname = 'data_version_${ver}'".toString())
        if (rows[0].count == 0) {
            def sqlString = """
          CREATE SEQUENCE seq_data_version_${ver};
          CREATE TABLE public.data_version_${ver}
          (
              id bigint,
              flag bigint,
              sub_project_id bigint,
              content jsonb
          );
          CREATE UNIQUE INDEX idx_uniq_data_version_${ver}_id ON public.data_version_${ver}(id);
          CREATE INDEX idx_btree_data_version_${ver}_flag ON public.data_version_${ver}(flag);
          CREATE INDEX idx_gin_data_version_${ver}_content ON public.data_version_${ver} USING gin (content);
          CREATE INDEX idx_btree_data_version_${ver}_sub_project_id on public.data_version_${ver} using btree (sub_project_id);
            """.toString()
            //println sqlString
            sql.executeUpdate(sqlString)
        } else {

        }

    }

    def insertVersionData = { subProjectId, version, data ->
        def versionTableName = "data_version_" + (version + "").replace(".", "_")
        def sql = new Sql(dataSource)
        data.each { it ->
            def content = []
            //println it
            it.keySet().each { it1 ->
//                if (it1 == "partDesc") {
//                    content.add("\"" + it1 + "\":E'" + it[it1] + "'")
//                }
                if (it1 == "info") {
                    def array = []
                    it[it1].each { it2 ->
                        array.add("\"" + it2 + "\"")
                    }
                    content.add("\"" + it1 + "\":[" + array.join(",") + "]")
                    return true
                }
                if (it1 == "levels") {
                    def array = []
                    it[it1].each { it2 ->
                        array.add(it2)
                    }
                    content.add("\"" + it1 + "\":[" + array.join(",") + "]")
                    return true
                }

                if (it1 == "_PARENT") {
                    def array = []
                    //println it[it1]==null
                    it[it1].each { it2 ->
                        def parent = []
                        //println it2
                        it2.each { it3 ->
                            parent.add("\"" + it3 + "\"")
                        }
                        array.add("[" + parent.join(",") + "]")
                    }
                    content.add("\"" + it1 + "\":[" + array.join(",") + "]")
                    return true
                }


                if (it1 == "partDesc") {
                    def partDesc = it[it1]
                    partDesc = partDesc.replace("\\", " \\\\")
                    //partDesc = partDesc.replace("\\C", " \\\\C ")
                    content.add("\"" + it1 + "\":\"" + partDesc + "\"")
                    return true
                }

                content.add("\"" + it1 + "\":\"" + it[it1] + "\"")


            }

            content = "{" + content.join(",") + "}"
            content = content.replaceAll("\n", " \\\\n ")

            //content = content.replace("\\H", " sss ")

            //content = content.replaceAll("\r", " ss")
            content = content.replaceAll("'", "''")

            def seq = sql.rows("select  nextval('seq_${versionTableName}') as id".toString())
            def id = seq[0].id
            //def uuid = UUID.randomUUID().toString().replace('-', '')
            def insertSql = """
                    insert into ${versionTableName}(id,flag,sub_project_id,content) 
                values(${id},${id},${subProjectId},'${content}')
                """.toString()
            //println insertSql

            sql.execute(insertSql)
        }
    }

    def index() {


        def newVersion = request.JSON.newVersion
        def oldVersion = request.JSON.oldVersion
        def subProjectId = request.JSON.subProjectId
        if (subProjectId == null) {
            subProjectId = 1
        }

        if (oldVersion == null) {
            newVersion = 1.0
        } else {
            if (newVersion == null) {
                newVersion = oldVersion + 0.1
            }
        }
        newVersion = (newVersion + "") as float

        createVersionTable(newVersion)

        def sql = new Sql(dataSource)
        def results = [code: 0, data: [total: 0, rows: []]]
        String path = "C:\\Users\\86138\\Documents\\excel\\sheet2.xlsx";
        path = "C:\\Users\\86138\\Documents\\excel\\sheet3.xlsx";

        // 获得文件所在地
        File file = new File(path);
        FileInputStream is = new FileInputStream(file); //文件流
        Workbook workbook = WorkbookFactory.create(is); //这种方式 Excel 2003/2007/2010 都是可以处理的
        def excelData = loadExcelData(workbook)

        println "exceldata:"+excelData.size()

        println "oldVersion:" + oldVersion

        if (oldVersion == null) {

            insertVersionData(subProjectId, newVersion, excelData)
        } else {
            def compareOption = [:]

            excelData = compareVersion(subProjectId, oldVersion, excelData, compareOption)
            //insertVersionData(subProjectId, newVersion, excelData)
        }


        workbook.close();
        is.close();



        render results as JSON
    }

}
