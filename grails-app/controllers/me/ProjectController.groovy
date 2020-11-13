package me

import grails.gorm.transactions.Transactional
import grails.converters.*
import groovy.json.JsonSlurper
import groovy.sql.Sql
import org.apache.poi.xssf.usermodel.XSSFWorkbook

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

    @Transactional(readOnly = true)
    def exportexcel() {


        def subProjects = SubProject.findAllByProjectId(params.id)
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

        OutputStream outputStream = null;
        FileOutputStream fileOutputStream = null;
        //sleep 3000
        try {
            String fileName = "工作文档.xlsx";

            fileName = URLEncoder.encode(fileName, "UTF-8");
            response.reset();
            response.addHeader("Access-Control-Allow-Origin", "*");
            response.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
            //response.setContentType("application/vnd.ms-excel;charset=utf-8");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            outputStream = response.getOutputStream();

            def workbook = new XSSFWorkbook();

            subProjects.each {
                def sheetName = it.name
                def sheetData = []

                def versionData = dataVersionService.queryTable(it.ver, it.id)

                def head = []
                def levels = jsonSlurper.parseText(versionData[0].levels)
                def info = jsonSlurper.parseText(versionData[0].info)
                levels.each {
                    def key = it.keySet()[0]
                    head.add([value: Math.ceil(key as float), width: 10])
                }

                mainCol.each {
                    head.add([value: it, width: 20])
                }

                infoCol = []
                info.each {
                    head.add([value: "", width: 20])
                }

                extensionCol.each {
                    head.add([value: it, width: 20])
                }

                sheetData.add(head)
                versionData.each {
                    def row = []

                    def jcPartNo = it.jcpartno
                    def oemPartNo = it.oempartno
                    def partDesc = it.partdesc

                    jsonSlurper.parseText(it.levels).each { it1 ->
                        def key = it1.keySet()[0]
                        if (it1[key] != null && it1[key] != "") {
                            row.add([value:Math.ceil(it1[key] as float),width:10])
                        } else {
                            row.add([value:"",width:10])
                        }

                    }
                    row.add([value:partDesc,width:20])
                    row.add([value:Math.ceil(jcPartNo as float),width:20])
                    row.add([value:oemPartNo,width:20])

                    jsonSlurper.parseText(it.info).each { it1 ->
                        if (it1?.trim()) {
                            row.add([value:Math.ceil(it1 as float),width:10])
                        } else {
                            row.add([value:"",width:10])
                        }
                    }

                    row.add([value:it.wig,width:40])
                    row.add([value:it.st,width:40])
                    row.add([value:it.cm,width:40])
                    row.add([value:it.supplier,width:40])
                    row.add([value:it.sici,width:40])
                    row.add([value:it.sds,width:40])
                    row.add([value:it.jici,width:40])
                    row.add([value:it.ppmcNo,width:40])
                    row.add([value:it.colourName,width:40])
                    row.add([value:it.scjp,width:40])
                    row.add([value:it.pden,width:40])
                    row.add([value:it.cd,width:40])
                    row.add([value:it.ccam,width:40])
                    row.add([value:it.ccab,width:40])
                    row.add([value:it.wig,width:40])



                    sheetData.add(row)

                }

                subProjectVersionService.createSheet(workbook, sheetName, sheetData)
            }

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
