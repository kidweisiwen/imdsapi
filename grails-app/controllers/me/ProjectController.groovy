package me

import grails.gorm.transactions.Transactional
import grails.converters.*
import groovy.json.JsonSlurper
import groovy.sql.Sql
import org.apache.poi.ss.usermodel.BorderStyle
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.xssf.usermodel.XSSFColor
import org.apache.poi.xssf.usermodel.XSSFWorkbook

import java.awt.Color

class ProjectController {
    static responseFormats = ['json', 'xml']
    def dataSource
    def weatherService
    def subProjectVersionService
    def dataVersionService

    def index() {
        def results = [:]
        def data = weatherService.query()
        render data as JSON
    }

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
    def exportexcel() {



        def subProjects = SubProject.findAllByProjectId(params.id)
        def jsonSlurper = new JsonSlurper()
        def levelCol = ["1", "2", "3", "4", "5", "6"]
        def mainCol = ["part description", "YFAS Part No.","YFAS Part Rev.",
                       "OEM Part No."
        ]
        def mainCol1 = [
                "OEM Part Rev."
        ]
        def mainCol2 = [
                "Space for Level 1 parts"
        ]
        def mainCol3 = [
                "COP","ＹFAS Drawing Number","ＹFAS Drawing Level","ＹFAS Drawing Desc",
                "Originating Program","Status","ECO","Release Date","ECR","DA","DA Short Description"
        ]
        def mainCol4 = [
                "Weight in g (for Adient sourced materials only)",
                "Material (for Adient sourced materials only)"
        ]
        def mainCol5 = [
                "Surface Treatment",
                "Change Management"
        ]
        def mainCol6 = [
                "Colour Key"
        ]
        def mainCol7 = [
                "Colour Code"
        ]
        def mainCol8 = [
                "D-Part",
                "E-Part"
        ]
        def mainCol9 = [
                "OEM Drawing Number",
                "OEM Drawing Level",
        ]
        def mainCol10 = [
                "Supplier"
        ]
        def mainCol11 = [
                "Supplier Part Number",
                "Supplier Part Level",
                "Supplier Drawing Number",
                "Supplier Drawing Level"
        ]
        def mainCol12 = [
                "Supplier IMDS/CAMDS ID",
                "Supplier datasheet status",
                "YFAS IMDS/CAMDS ID",
                "PPMC No.",
                "colour (name)",
                "supplier code  of JC plant (e.g. DUNS)",
                "PPAP date / EMPB-Nr",
                "Customer directed Part (Y/N)"
        ]

        def mainCol13 = [
                "Comments / corrective action MDM",
                "Comments / corrective action BU",
                "datasheet status in IMDS/CAMDS\n(RYG)"
        ]

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

        OutputStream outputStream = null;
        FileOutputStream fileOutputStream = null;
        //sleep 3000
        try {
            String fileName = "download.xlsx";

            fileName = URLEncoder.encode(fileName, "UTF-8");

            response.reset();
            response.addHeader("Access-Control-Allow-Origin", "*");
            response.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
            //response.setContentType("application/vnd.ms-excel;charset=utf-8");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            outputStream = response.getOutputStream();

            //def workbook = new XSSFWorkbook();
            def template = new FileInputStream(new File("template/newtemplate1.xlsx"));
            println template
            def workbook = new XSSFWorkbook(new BufferedInputStream(template));

            def headFont = workbook.createFont();
            headFont.setFontHeightInPoints((short) 10);
            headFont.setFontName("Arial");
            headFont.setBold(false);

            def headFont1 = workbook.createFont();
            headFont.setFontHeightInPoints((short) 10);
            headFont.setFontName("微软雅黑");
            headFont.setBold(false);


            def color = new XSSFColor(new java.awt.Color(0, 255, 255));

            def dataCellStyle = workbook.createCellStyle();
            dataCellStyle.setFont(headFont);
            dataCellStyle.setAlignment(HorizontalAlignment.LEFT);
            dataCellStyle.setVerticalAlignment(dataCellStyle.getVerticalAlignmentEnum().BOTTOM);
            dataCellStyle.setWrapText(true);
            dataCellStyle.setBorderTop(BorderStyle.THIN);
            dataCellStyle.setBorderLeft(BorderStyle.THIN);
            dataCellStyle.setBorderRight(BorderStyle.THIN);
            dataCellStyle.setBorderBottom(BorderStyle.THIN);

            def headCellStyle0 = workbook.createCellStyle();
            headCellStyle0.setFont(headFont);
            headCellStyle0.setAlignment(HorizontalAlignment.LEFT);
            headCellStyle0.setVerticalAlignment(headCellStyle0.getVerticalAlignmentEnum().BOTTOM);
            headCellStyle0.setWrapText(true);
            headCellStyle0.setBorderTop(BorderStyle.THIN);
            headCellStyle0.setBorderLeft(BorderStyle.THIN);
            headCellStyle0.setBorderRight(BorderStyle.THIN);
            headCellStyle0.setBorderBottom(BorderStyle.THIN);


            def headCellStyle = workbook.createCellStyle();
            headCellStyle.setFont(headFont);
            headCellStyle.setAlignment(HorizontalAlignment.LEFT);
            headCellStyle.setVerticalAlignment(headCellStyle.getVerticalAlignmentEnum().BOTTOM);
            headCellStyle.setWrapText(true);
            headCellStyle.setBorderTop(BorderStyle.THIN);
            headCellStyle.setBorderLeft(BorderStyle.THIN);
            headCellStyle.setBorderRight(BorderStyle.THIN);
            headCellStyle.setBorderBottom(BorderStyle.THIN);
            headCellStyle.setFillForegroundColor(new XSSFColor(new Color(0, 255, 255)))
            headCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND)

            def headCellStyle1 = workbook.createCellStyle();
            headCellStyle1.setFont(headFont);
            headCellStyle1.setAlignment(HorizontalAlignment.LEFT);
            headCellStyle1.setVerticalAlignment(headCellStyle1.getVerticalAlignmentEnum().BOTTOM);
            headCellStyle1.setWrapText(true);
            headCellStyle1.setBorderTop(BorderStyle.THIN);
            headCellStyle1.setBorderLeft(BorderStyle.THIN);
            headCellStyle1.setBorderRight(BorderStyle.THIN);
            headCellStyle1.setBorderBottom(BorderStyle.THIN);
            headCellStyle1.setFillForegroundColor(new XSSFColor(new Color(255, 255, 0)))
            headCellStyle1.setFillPattern(FillPatternType.SOLID_FOREGROUND)

            def headCellStyle2 = workbook.createCellStyle();
            headCellStyle2.setFont(headFont);
            headCellStyle2.setAlignment(HorizontalAlignment.LEFT);
            headCellStyle2.setVerticalAlignment(headCellStyle2.getVerticalAlignmentEnum().BOTTOM);
            headCellStyle2.setWrapText(true);
            headCellStyle2.setBorderTop(BorderStyle.THIN);
            headCellStyle2.setBorderLeft(BorderStyle.THIN);
            headCellStyle2.setBorderRight(BorderStyle.THIN);
            headCellStyle2.setBorderBottom(BorderStyle.THIN);
            headCellStyle2.setFillForegroundColor(new XSSFColor(new Color(255, 204, 153)))
            headCellStyle2.setFillPattern(FillPatternType.SOLID_FOREGROUND)

            def headCellStyle3 = workbook.createCellStyle();
            headCellStyle3.setFont(headFont1);
            headCellStyle3.setAlignment(HorizontalAlignment.LEFT);
            headCellStyle3.setVerticalAlignment(headCellStyle3.getVerticalAlignmentEnum().BOTTOM);
            headCellStyle3.setWrapText(true);
            headCellStyle3.setBorderTop(BorderStyle.THIN);
            headCellStyle3.setBorderLeft(BorderStyle.THIN);
            headCellStyle3.setBorderRight(BorderStyle.THIN);
            headCellStyle3.setBorderBottom(BorderStyle.THIN);
            headCellStyle3.setFillForegroundColor(new XSSFColor(new Color(192, 192, 192)))
            headCellStyle3.setFillPattern(FillPatternType.SOLID_FOREGROUND)

            def headCellStyle4 = workbook.createCellStyle();
            headCellStyle4.setFont(headFont);
            headCellStyle4.setAlignment(HorizontalAlignment.LEFT);
            headCellStyle4.setVerticalAlignment(headCellStyle4.getVerticalAlignmentEnum().BOTTOM);
            headCellStyle4.setWrapText(true);
            headCellStyle4.setBorderTop(BorderStyle.THIN);
            headCellStyle4.setBorderLeft(BorderStyle.THIN);
            headCellStyle4.setBorderRight(BorderStyle.THIN);
            headCellStyle4.setBorderBottom(BorderStyle.THIN);
            headCellStyle4.setFillForegroundColor(new XSSFColor(new Color(255, 0, 0)))
            headCellStyle4.setFillPattern(FillPatternType.SOLID_FOREGROUND)

            def rowFont = workbook.createFont();
            def rowCellStyle = workbook.createCellStyle();
            rowCellStyle.setAlignment(HorizontalAlignment.CENTER);
            rowCellStyle.setVerticalAlignment(rowCellStyle.getVerticalAlignmentEnum().CENTER);
            rowCellStyle.setFont(rowFont);
            rowCellStyle.setWrapText(true);
            rowCellStyle.setBorderTop(BorderStyle.THIN);
            rowCellStyle.setBorderLeft(BorderStyle.THIN);
            rowCellStyle.setBorderRight(BorderStyle.THIN);
            rowCellStyle.setBorderBottom(BorderStyle.THIN);


            subProjects.each {
                def sheetName = it.name


                def versionData = []
                try {
                    if (it.ver!=null && it.ver!="") {
                        versionData = dataVersionService.queryTable(it.ver, it.id)
                    }
                } catch(e){
                    println e
                }


                if (versionData.size()>0) {
                    def sheet = workbook.createSheet(sheetName);
                    def head = sheet.createRow(0);
                    head.setHeight((short) 1520)
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

                    renderCol(sheet, headCellStyle, head, ["Pos",""], 2)
                    renderCol(sheet, headCellStyle, head, levelCol, 1)
                    renderCol(sheet, headCellStyle, head, mainCol, 10)
                    renderCol(sheet, headCellStyle1, head, mainCol1, 10)
                    renderCol(sheet, headCellStyle2, head, mainCol2, 10)
                    renderCol(sheet, headCellStyle3, head, mainCol3, 10)
                    renderCol(sheet, headCellStyle, head, mainCol4, 10)
                    renderCol(sheet, headCellStyle4, head, mainCol5, 10)
                    renderCol(sheet, headCellStyle3, head, mainCol6, 10)
                    renderCol(sheet, headCellStyle, head, mainCol7, 10)
                    renderCol(sheet, headCellStyle3, head, mainCol8, 10)
                    renderCol(sheet, headCellStyle1, head, mainCol9, 10)
                    renderCol(sheet, headCellStyle, head, mainCol10, 10)
                    renderCol(sheet, headCellStyle3, head, mainCol11, 10)
                    renderCol(sheet, headCellStyle, head, mainCol12, 10)
                    renderCol(sheet, headCellStyle0, head, mainCol13, 10)

                    def index = 1
                    def start = 0
                    def dataCol = []
                    versionData.eachWithIndex { it1, i ->
                        def jcPartNo = it1.jcpartno
                        def oemPartNo = it1.oempartno
                        def partDesc = it1.partdesc
                        def row = sheet.createRow(index + i);

                        //def cell = row.createCell(start);
                        //cell.setCellStyle(rowCellStyle)

                        levelCol = []
                        jsonSlurper.parseText(it1.levels).each { it2 ->
                            def key = it2.keySet()[0]
                            if (it2[key] != null && it2[key] != "") {
                                levelCol.add(Math.ceil(it2[key] as float))
                            } else {
                                levelCol.add("")
                            }

                        }

                        renderCol(sheet, dataCellStyle, row, ['',''], 3)

                        renderCol(sheet, dataCellStyle, row, levelCol, 3)


                        mainCol = []
                        mainCol.add(partDesc)
                        renderCol(sheet, dataCellStyle, row, mainCol, 30)

                        mainCol = []
                        mainCol.add(jcPartNo.isFloat() ? Math.ceil(jcPartNo as float) : jcPartNo)
                        mainCol.add("")
                        mainCol.add(oemPartNo)
                        renderCol(sheet, dataCellStyle, row, mainCol, 10)


                        renderCol(sheet, dataCellStyle, row, [""], 10)
                        renderCol(sheet, dataCellStyle, row, [""], 10)
                        renderCol(sheet, dataCellStyle, row, ["","","","","","","","","","",""], 10)

                        renderCol(sheet, dataCellStyle, row, [it1.wig,""], 12)
                        renderCol(sheet, dataCellStyle, row, [it1.st,it1.cm], 12)

                        renderCol(sheet, dataCellStyle, row, [""], 10)
                        renderCol(sheet, dataCellStyle, row, [""], 10)
                        renderCol(sheet, dataCellStyle, row, ["",""], 10)
                        renderCol(sheet, dataCellStyle, row, ["",""], 10)
                        renderCol(sheet, dataCellStyle, row, [it1.supplier], 10)
                        renderCol(sheet, dataCellStyle, row, ["","","",""], 10)
                        renderCol(sheet, dataCellStyle, row, [it1.sici,it1.sds,it1.jici,it1.ppmcNo,it1.colourName,it1.scjp,it1.pden,it1.cd], 10)
                        renderCol(sheet, dataCellStyle, row, [it1.ccam,it1.ccab,it1.dsiic], 10)
                        infoCol = []
                        jsonSlurper.parseText(it1.info).each { it2 ->
                            if (it2?.trim()) {
                                infoCol.add(Math.ceil(it2 as float))
                            } else {
                                infoCol.add("")
                            }
                        }
                        //renderCol(sheet, rowCellStyle, row, infoCol, 10)

                        dataCol = [
                                it1.wig,
                                it1.st,
                                it1.cm,
                                it1.supplier,
                                it1.sici,
                                it1.sds,
                                it1.jici,
                                it1.ppmcNo,
                                it1.colourName,
                                it1.scjp,
                                it1.pden,
                                it1.cd,
                                it1.ccam,
                                it1.ccab,
                                it1.dsiic
                        ]
                        //renderCol(sheet, rowCellStyle, row, dataCol, 40)
                    }


                }
            }

            def totalSheet = workbook.getNumberOfSheets()
            workbook.setSheetOrder("Checklist Change Management",totalSheet-1)
            workbook.setSheetOrder("Change Log",totalSheet-1)
            workbook.setActiveSheet(0)
            workbook.write(outputStream);


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

    }

    def query() {


    }

    //项目信息修改
    @Transactional
    def update() {
        def results = [code: 0, data: [:]]
        def id = request.JSON.id
        def no = request.JSON.no
        def name = request.JSON.name
        def description = request.JSON.description
        def customer = request.JSON.customer
        def remark = request.JSON.remark
        def userId = request.JSON.userId


        def project = Project.findById(id as Long)
        project.no = no
        project.name = name
        project.description = description
        project.customer = customer
        project.remark = remark
        project.updateUserId = userId as long
        project.save(flush: true)

        render results as JSON
    }

    //项目信息增加
    @Transactional
    def save() {
        def results = [code: 0, data: [:]]
        def no = request.JSON.no
        def name = request.JSON.name
        def desc = request.JSON.desc
        def customer = request.JSON.customer
        def remark = request.JSON.remark
        def userId = request.JSON.userId

        def project = new Project(no: no, name: name, description: desc, customer: customer,
                remark: remark, createUserId: userId as long)
        project.save(flush: true)

        render results as JSON
    }

    @Transactional
    def delete() {
        def results = [code: 0, data: [:]]
        def sql = new Sql(dataSource)
        request.JSON.each { it ->
            def projectId = it.id
            UserProjectRole.where {
                projectId == projectId
            }.deleteAll()
            def project = Project.findById(it.id)
            if (project != null) {
                project.delete()
            }
            //查找子项目并删除
            def subProjects = SubProject.findAllByProjectId(it.id)
            subProjects.each { it1 ->
                def subProjectId = it1.id
                def subProject = SubProject.findById(it1.id)
                UserProjectRole.where {
                    subProjectId == subProjectId
                }.deleteAll()
                def subProjectVersions = SubProjectVersion.findAllBySubProjectId(it1.id)
                subProjectVersions.each { it2 ->
                    def subProjectVersion = SubProjectVersion.findById(it2.id)
                    def ver = subProjectVersion.ver
                    subProjectId = subProjectVersion.subProjectId
                    def delSql = """
                        delete from data_version_${ver.replace(".", "_")} where sub_project_id=${subProjectId}
                    """.toString()
                    println delSql
                    sql.execute(delSql)
                    subProjectVersion.delete()
                }
                subProject.delete()
            }
        }
        render results as JSON

        //        SubProject.where {
        //            id == id
        //        }.deleteAll()

    }

    @Transactional(readOnly = true)
    def list() {
        def results = [code: 0, data: [total: 0, rows: []]]
        def page = params.page ? params.page as Integer : 1
        def size = params.size ? params.size as Integer : 20
        def no = params.no ? params.no.trim() : ""
        def id = params.id ? params.id : ""
        def name = params.name ? params.name.trim() : ""
        def description = params.description ? params.description.trim() : ""
        def customer = params.customer ? params.customer.trim() : ""
        def ver = params.ver ? params.ver.trim() : ""
        def userId = params.userId ? (params.userId as long) : ""


        def p = [:]
        def orderSql = " order by a.id desc"

        def permissionIds = []
        def allFlag = false
        def allAdminFlag = false
        def userProjectRoles = []
        def userProjectPermission = [:]
        if (userId != "") {
            userProjectRoles = UserProjectRole.findAllByUserId(userId)
            userProjectRoles.each {
                permissionIds.add(it.projectId)
                if (it.projectId == -1) {
                    allFlag = true
                }
                if (it.projectId == -1 && it.role == "ADMIN") {
                    allAdminFlag = true
                }
                if (it.projectId != -1 && it.role == "ADMIN") {
                    userProjectPermission[it.projectId] = true
                }
            }
        }


        //判断项目权限
        def whereSql = ""
        if (allFlag || userId == "") {
            whereSql = "where 1=1 "
        } else {
            whereSql = "where (a.createUserId=:userId or a.id in :permissionIds)  "
            p.userId = userId
            p.permissionIds = permissionIds
        }


        //项目id
        if (id != "") {
            whereSql += " and a.id = :id";
            p.id = id as Long;
        }

        //项目名称
        if (name != "") {
            whereSql += " and a.name like :name";
            p.name = "%" + name + "%";
        }

        //项目编号
        if (no != "") {
            whereSql += " and a.no like :no";
            p.no = "%" + no + "%";
        }

        //项目描述
        if (description != "") {
            whereSql += " and a.description like :description";
            p.description = "%" + description + "%";
        }

        //客户名称
        if (customer != "") {
            whereSql += " and a.customer like :customer";
            p.customer = "%" + customer + "%";
        }

        //版本号
        if (ver != "") {
            whereSql += " and a.ver like :ver";
            p.ver = "%" + ver + "%";
        }

        def totalStr = """
            select count(a.id) 
            from Project a 
        """ + whereSql.toString()
        println totalStr

        def total = Project.executeQuery(totalStr, p)

        def sqlString = """
            select new map(a.createUserId as createUserId,a.id as id,a.name as name,a.no as no,a.description as description,
            a.ver as ver,a.customer as customer,a.remark as remark)
            from Project a
        """ + whereSql + orderSql.toString()
        //println sqlString
        def rows = Project.findAll(sqlString, p, [max: size, offset: (page - 1) * size])

        def data = []
        rows.each {
            def hm = [:]
            hm.putAll(it)
            hm.adminFlag = allAdminFlag || (userId == it.createUserId) || (userProjectPermission[it.id] ? userProjectPermission[it.id] : false)
            data.add(hm)
        }

        //println data
        results.data.total = total[0]
        results.data.rows = data;
        //sleep 2000
        render results as JSON
    }


}
