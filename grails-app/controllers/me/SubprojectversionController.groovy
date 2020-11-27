package me

import grails.converters.JSON
import grails.gorm.transactions.Transactional
import groovy.json.JsonSlurper
import groovy.sql.Sql

class SubprojectversionController {
    static responseFormats = ['json', 'xml']
    def dataSource
    def dataVersionService

    def index() {


    }

    @Transactional(readOnly = true)
    def verlist() {
        def sql = new Sql(dataSource)
        def results = [code: 0, data: [total: 0, rows: []]]
        def page = params.page ? params.page as Integer : 1
        def size = params.size ? params.size as Integer : 20
        def id = params.id ? params.id : ""
        def flag = params.flag ? params.flag : ""
        def ver = params.ver ? params.ver.trim() : ""
        def jcPartNo = params.jcPartNo ? params.jcPartNo.trim() : ""
        def oemPartNo = params.oemPartNo ? params.oemPartNo.trim() : ""
        def partDescription = params.partDescription ? params.partDescription.trim() : ""
        def subProjectId = params.subProjectId ? params.subProjectId : ""
        def whereSql = "where sub_project_id = ${subProjectId} "
        ver = ver.replace(".", "_")


        def p = [:]
        def orderSql = "order by a.flag asc "

        //根据参数构建查询条件
        //根据标志位来查询当前和上一条数据
        if (flag != "") {
            whereSql += " and a.flag <= :flag";
            p.flag = flag as Long;
            size = 2
            orderSql = "order by a.flag desc "
        }


        //id
        if (id != "") {
            whereSql += " and a.id = :id";
            p.id = id;
        }

        //jcPartNo
        if (jcPartNo != "") {
            whereSql += " and (content #>> '{jcPartNo}')::varchar like :jcPartNo";
            p.jcPartNo = '%' + jcPartNo + '%';
        }

        //partDescription
        if (partDescription != "") {
            whereSql += " and (content #>> '{partDesc}')::varchar like :partDescription";
            p.partDescription = '%' + partDescription + '%';
        }

        //oemPartNo
        if (oemPartNo != "") {
            whereSql += " and (content #>> '{oemPartNo}')::varchar like :oemPartNo";
            p.oemPartNo = '%' + oemPartNo + '%';
        }

        def totalSql = ("""
            select count(a.id) as total
            from data_version_${ver} a 
        """ + whereSql).toString()
        println totalSql
        def total = sql.rows(totalSql, p)


        def detailSql = """
        select a.id as id,a.flag as flag,sub_project_id as subProjectId,
        (content #>> '{level}')::varchar as level,
        (content #>> '{levels}')::varchar as levels,

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
        from data_version_${ver} a 
        ${whereSql} ${orderSql}
        limit ${size} offset ${(page - 1) * size}"""
        println detailSql
        def rows = sql.rows(detailSql, p)

        //def rows = sql.rows(detailSql, p, [max: size, offset: (page - 1) * size])

        results.data.total = total[0].total
        results.data.rows = rows;
        render results as JSON
    }

    @Transactional(readOnly = true)
    def list() {

        def results = [code: 0, data: [total: 0, rows: []]]
        def page = params.page ? params.page as Integer : 1
        def size = params.size ? params.size as Integer : 20
        def id = params.id ? params.id : ""
        def ver = params.ver ? params.ver.trim() : ""
        def userName = params.userName ? params.userName.trim() : ""
        def remark = params.remark ? params.remark.trim() : ""
        def subProjectId = params.subProjectId ? params.subProjectId : ""
        def whereSql = "where 1=1 "


        def p = [:]
        def orderSql = " order by a.id desc"

        //根据参数构建查询条件
        //子项目id
        if (subProjectId != "") {
            whereSql += " and a.subProjectId = :subProjectId";
            p.subProjectId = subProjectId as Long;
        }

        //子项目版本id
        if (id != "") {
            whereSql += " and a.id = :id";
            p.id = id as Long;
        }

        //子项目版本备注
        if (remark != "") {
            whereSql += " and a.remark like :remark";
            p.remark = "%" + remark + "%";
        }

        remark
        //操作者姓名
        if (userName != "") {
            whereSql += " and b.cnName like :userName";
            p.userName = "%" + userName + "%";
        }

        //版本号
        if (ver != "") {
            whereSql += " and a.ver like :ver";
            p.ver = "%" + ver + "%";
        }

        def total = SubProjectVersion.executeQuery("""
            select count(a.id) 
            from SubProjectVersion a 
            left join Users b on a.userId = b.id
        """ + whereSql.toString(), p)

        def rows = SubProjectVersion.findAll("""
            select new map(c.name as projectName,b.name as subProjectName,a.id as id,
            a.subProjectId as subProjectId,a.userId as userId,
            a.id as id,a.dataCount as dataCount,a.remark as remark,a.ver as ver,
            a.lastUpdated as lastUpdated,d.cnName as userName)
            from SubProjectVersion a
            left join SubProject b on a.subProjectId = b.id
            left join Project c on c.id = b.projectId
            left join Users d on a.userId = d.id
        """ + whereSql + orderSql.toString(), p, [max: size, offset: (page - 1) * size])

        results.data.total = total[0]
        results.data.rows = rows;
        render results as JSON

    }

    //子项目版本信息修改
    @Transactional
    def update() {
        def results = [code: 0, data: [:]]
        def id = request.JSON.id

        def remark = request.JSON.remark
        def subProject = SubProjectVersion.findById(id as Long)

        subProject.remark = remark
        subProject.save(flush: true)

        render results as JSON
    }

    //子项目版本数据条目修改
    @Transactional
    def verupdate() {
        def results = [code: 0, data: [:]]
        def id = request.JSON.id

        def remark = request.JSON.remark
        def subProject = SubProjectVersion.findById(id as Long)

        subProject.remark = remark
        subProject.save(flush: true)

        render results as JSON
    }

    //子项目版本数据条目删除
    @Transactional
    def verdelete() {
        def results = [code: 0, data: [:]]
        def sql = new Sql(dataSource)
        def ver = request.JSON.ver.replace(".", "_")
        def ids = request.JSON.data
        def subProjectId = request.JSON.subProjectId

        ids.each {
            def delSql = """
                delete from data_version_${ver} where sub_Project_Id=${subProjectId} and id = '${it.id}'
            """.toString()
            println delSql
            sql.execute(delSql)

        }

        sql.execute("""
            update sub_project_version 
            set data_count=data_count-${ids.size()}
            where ver=? and sub_project_id=?
        """.toString(), [request.JSON.ver, subProjectId as Long])

        render results as JSON
    }

    //子项目版本数据增加
    @Transactional
    def datasave() {

        def results = [code: 0, data: [:]]
        def sql = new Sql(dataSource)
        def levels = []
        def ver = request.JSON.ver
        def subProjectId = request.JSON.subProjectId

        def level = request.JSON.level
        def partDesc = request.JSON.partDescription
        def jcPartNo = request.JSON.jcPartNo
        def oemPartNo = request.JSON.oemPartNo

        def infos = request.JSON.infos
        def lastData = request.JSON.lastData
        def lastDataFlag = lastData.flag as Integer
        def wig = request.JSON.wig
        def st = request.JSON.st
        def cm = request.JSON.cm
        def supplier = request.JSON.supplier
        def sici = request.JSON.sici
        def sds = request.JSON.sds
        def jici = request.JSON.jici
        def ppmcNo = request.JSON.ppmcNo
        def colourName = request.JSON.colourName
        def scjp = request.JSON.scjp
        def pden = request.JSON.pden
        def cd = request.JSON.cd
        def ccam = request.JSON.ccam
        def ccab = request.JSON.ccab
        def dsiic = request.JSON.dsiic
        def myLevel = request.JSON.myLevel

        def lastDataLevels = request.JSON.lastData.levels
        def jsonSlurper = new JsonSlurper()
        lastDataLevels = jsonSlurper.parseText(lastDataLevels)

        lastDataLevels.each {
            def key = it.keySet()[0]
            if ((myLevel as Float) == (key as Float)) {
                levels.add("{\"" + key + "\":\"" + myLevel + "\"}")
            } else {
                levels.add("{\"" + key + "\":\"\"}")
            }
        }


        def content = []
        //常规字段
        content.add("\"levels\":[" + levels.join(",") + "]")
        content.add("\"level\":\"" + (myLevel != null ? myLevel : "") + "\"")
        content.add("\"partDesc\":\"" + (partDesc ? partDesc : "") + "\"")
        content.add("\"jcPartNo\":\"" + (jcPartNo ? jcPartNo : "") + "\"")
        content.add("\"oemPartNo\":\"" + (oemPartNo ? oemPartNo : "") + "\"")

        //扩展字段
        content.add("\"wig\":\"" + (wig ? wig : "") + "\"")
        content.add("\"st\":\"" + (st ? st : "") + "\"")
        content.add("\"cm\":\"" + (cm ? cm : "") + "\"")
        content.add("\"supplier\":\"" + (supplier ? supplier : "") + "\"")
        content.add("\"sici\":\"" + (sici ? sici : "") + "\"")
        content.add("\"sds\":\"" + (sds ? sds : "") + "\"")
        content.add("\"jici\":\"" + (jici ? jici : "") + "\"")
        content.add("\"ppmcNo\":\"" + (ppmcNo ? ppmcNo : "") + "\"")
        content.add("\"colourName\":\"" + (colourName ? colourName : "") + "\"")
        content.add("\"scjp\":\"" + (scjp ? scjp : "") + "\"")
        content.add("\"pden\":\"" + (pden ? pden : "") + "\"")
        content.add("\"cd\":\"" + (cd ? cd : "") + "\"")
        content.add("\"ccam\":\"" + (ccam ? ccam : "") + "\"")
        content.add("\"ccab\":\"" + (ccab ? ccab : "") + "\"")
        content.add("\"dsiic\":\"" + (dsiic ? dsiic : "") + "\"")


        def infoArray = []
        infos.eachWithIndex { it, i ->
            infoArray.add("\"" + (request.JSON["infos" + i] ? request.JSON["infos" + i] : "") + "\"")

        }
        content.add("\"info\":[" + infoArray.join(",") + "]")


        content.add("\"_PARENT_JPN\":[]")
        content.add("\"_PARENT_LEVEL\":[]")

        def versionTableName = "data_version_" + (ver + "").replace(".", "_")
        def seq = sql.rows("select  nextval('seq_${versionTableName}') as id".toString())
        def id = seq[0].id

        sql.execute("""update ${versionTableName} set flag=flag+1 
        where flag>=${lastDataFlag + 1} and sub_project_id=${subProjectId}""".toString())

//        def rows = sql.rows("""select * from ${versionTableName}
//        where sub_project_id=${subProjectId} and flag>${lastDataFlag} order by flag asc""".toString())
//        rows.each{
//            sql.execute("update ${versionTableName} set flag=${lastDataFlag+2} where id=${it.id}")
//        }

        content = "{" + content.join(",") + "}"
        def insertSql = """
                    insert into ${versionTableName}(id,flag,sub_project_id,content)
                values(${id},${lastDataFlag + 1},${subProjectId},'${content}')
                """.toString()
        println insertSql
        sql.execute(insertSql)
        sql.execute("""
            update sub_project_version 
            set data_count=data_count+1 
            where ver=? and sub_project_id=?
        """.toString(), [ver, subProjectId as Long])
        render results as JSON
    }


    //子项目版本数据修改
    @Transactional
    def dataupdate() {
        def results = [code: 0, data: [:]]
        def sql = new Sql(dataSource)
        def levels = []
        def ver = request.JSON.ver
        def subProjectId = request.JSON.subProjectId
        def id = request.JSON.id
        def level = request.JSON.level
        def myLevel = request.JSON.myLevel
        myLevel = myLevel.isFloat() ? (myLevel as float) : myLevel

        def partDesc = request.JSON.partDescription
        def jcPartNo = request.JSON.jcPartNo
        jcPartNo = jcPartNo.isBigDecimal() ? (jcPartNo as BigDecimal) : jcPartNo
        def oemPartNo = request.JSON.oemPartNo

        def infos = request.JSON.infos

        def wig = request.JSON.wig
        def st = request.JSON.st
        def cm = request.JSON.cm
        def supplier = request.JSON.supplier
        def sici = request.JSON.sici
        def sds = request.JSON.sds
        def jici = request.JSON.jici
        def ppmcNo = request.JSON.ppmcNo
        def colourName = request.JSON.colourName
        def scjp = request.JSON.scjp
        def pden = request.JSON.pden
        def cd = request.JSON.cd
        def ccam = request.JSON.ccam
        def ccab = request.JSON.ccab
        def dsiic = request.JSON.dsiic

        def mylevels = request.JSON.mylevels

        def content = []
        def newParent = []
        def versionTableName = "data_version_" + (ver + "").replace(".", "_")
        def jsonSlurper = new JsonSlurper()

        def old = sql.rows("""
        select * 
        from ${versionTableName} 
        where id =:id""", [id: id])[0]

        def oldContent = jsonSlurper.parseText(old.content.toString())
        def oldJcPartNo = oldContent.jcPartNo.isBigDecimal() ? (oldContent.jcPartNo as BigDecimal) : oldContent.jcPartNo
        def oldLevel = oldContent.level.isFloat() ? (oldContent.level as float) : oldContent.level
        def oldParent = oldContent._PARENT?oldContent._PARENT:[]
        def oldOemPartNo = oldContent.oemPartNo



        if (myLevel == oldLevel && jcPartNo == oldJcPartNo && oemPartNo == oldOemPartNo) {
        } else {

            def findFlag = null
            def toData = sql.rows("""
                select * 
                from ${versionTableName} 
                where sub_project_id =:subProjectId order by flag asc
            """.toString(), [subProjectId: old.sub_project_id])
            toData.find { it ->
                def toContent = jsonSlurper.parseText(it.content.toString())
                def toLevel = toContent.level.isFloat() ? (toContent.level as float) : toContent.level
                def toJcPartNo = toContent.jcPartNo.isBigDecimal() ? (toContent.jcPartNo as BigDecimal) : toContent.jcPartNo
                def toOemPartNo = toContent.oemPartNo
                def toParent = toContent._PARENT?toContent._PARENT:[]


                if (it.id!=id && myLevel == toLevel
                        && jcPartNo == toJcPartNo
                        && oemPartNo == toOemPartNo
                        && checkParent(oldParent, toParent)
                ) {
                    findFlag = toData.indexOf(it)

                    return true
                }
            }

            if (findFlag != null) {
                results = [
                        code: -1,
                        msg : '该条数据与第'+(findFlag+1)+'行数据重复！'
                ]
                render results as JSON
                return
            }
        }


        mylevels.each {
            def value = it.value
            def label = it.label
            if ((myLevel != null && myLevel != "") && (myLevel as Float) == (value as Float)) {
                levels.add("{\"" + label + "\":\"" + myLevel + "\"}")
            } else {
                levels.add("{\"" + label + "\":\"\"}")
            }
        }

        partDesc = partDesc.replaceAll("\\", " \\\\")
        partDesc = partDesc.replaceAll("\n", " \\\\n")

        //常规字段
        content.add("\"levels\":[" + levels.join(",") + "]")
        content.add("\"level\":\"" + (myLevel != null ? myLevel : "") + "\"")
        content.add("\"partDesc\":\"" + (partDesc ? partDesc : "") + "\"")
        content.add("\"jcPartNo\":\"" + (jcPartNo ? jcPartNo : "") + "\"")
        content.add("\"oemPartNo\":\"" + (oemPartNo ? oemPartNo : "") + "\"")

        //扩展字段
        content.add("\"wig\":\"" + (wig ? wig : "") + "\"")
        content.add("\"st\":\"" + (st ? st : "") + "\"")
        content.add("\"cm\":\"" + (cm ? cm : "") + "\"")
        content.add("\"supplier\":\"" + (supplier ? supplier : "") + "\"")
        content.add("\"sici\":\"" + (sici ? sici : "") + "\"")
        content.add("\"sds\":\"" + (sds ? sds : "") + "\"")
        content.add("\"jici\":\"" + (jici ? jici : "") + "\"")
        content.add("\"ppmcNo\":\"" + (ppmcNo ? ppmcNo : "") + "\"")
        content.add("\"colourName\":\"" + (colourName ? colourName : "") + "\"")
        content.add("\"scjp\":\"" + (scjp ? scjp : "") + "\"")
        content.add("\"pden\":\"" + (pden ? pden : "") + "\"")
        content.add("\"cd\":\"" + (cd ? cd : "") + "\"")
        content.add("\"ccam\":\"" + (ccam ? ccam : "") + "\"")
        content.add("\"ccab\":\"" + (ccab ? ccab : "") + "\"")
        content.add("\"dsiic\":\"" + (dsiic ? dsiic : "") + "\"")


        def infoArray = []
        infos.eachWithIndex { it, i ->
            infoArray.add("\"" + (request.JSON["infos" + i] ? request.JSON["infos" + i] : "") + "\"")

        }
        content.add("\"info\":[" + infoArray.join(",") + "]")

        def parent = []
        oldContent._PARENT.each{
            parent.add("[\"" + it[0] + "\",\""+ it[1] +"\",\"" + it[2] + "\"]")
             
        }
        content.add("\"_PARENT\":[" + parent.join(",") + "]")

        content = "{" + content.join(",") + "}"
        def insertSql = """
        update ${versionTableName} set content='${content}'
        where id=${id}
        """.toString()

        println insertSql
        sql.execute(insertSql)


        def rows = sql.rows("""
                select * 
                from ${versionTableName} 
                where sub_project_id =:subProjectId order by flag asc
            """.toString(), [subProjectId: old.sub_project_id])
        rows.each{

        }

        render results as JSON
    }

    //子项目版本数据删除
    @Transactional
    def datadelete() {
        def results = [code: 0, data: [:]]
        def sql = new Sql(dataSource)
        def ver = request.JSON.ver.replace(".", "_")
        def ids = request.JSON.ids
        def subProjectId = request.JSON.ids

        ids.each {
            def delSql = """
                delete from data_version_${ver} where subProjectId=${subProjectId} uuid = '${it}'
            """.toString()
            sql.execute(delSql)

        }

        render results as JSON
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

    @Transactional
    def merge() {
        def jsonSlurper = new JsonSlurper()
        def results = [code: 0, data: [:]]
        def sql = new Sql(dataSource)
        def userId = request.JSON.userId
        def ver = (request.JSON.ver + "") as float
        ver = ver + ''
        def baseVer = request.JSON.baseVer
        def newVer = request.JSON.newVer
        def remark = request.JSON.remark
        def deleteFlag = request.JSON.deleteFlag
        def subProjectId = request.JSON.subProjectId
        subProjectId = subProjectId as long

        createVersionTable(ver)

        def baseData = dataVersionService.queryTable(baseVer, subProjectId)
        def newData = dataVersionService.queryTable(newVer, subProjectId)

        def dataCount = 0
        baseData.each { it ->
            def findFlag = null
            def id = it.id
            def parent = it.parent ? it.parent : "[]"
            def pi = jsonSlurper.parseText(parent)

            def level = it.level
            if (level.isFloat()) {
                level = level as float
            }


            def levels = it.levels

            levels = jsonSlurper.parseText(levels)
            def levelsList = []
            levels.each { nl ->
                def key = nl.keySet()[0]
                levelsList.add("{\"" + key + "\":\"" + nl[key] + "\"}")
            }
            levels = levelsList.join(",")

            def info = it.info
            info = jsonSlurper.parseText(info)
            def infoList = []
            info.each { p ->
                infoList.add("\"" + p + "\"")
            }
            info = infoList.join(",")

            def jcPartNo = it.jcPartNo
            if (jcPartNo.isFloat()) {
                jcPartNo = jcPartNo as float
            }

            def oemPartNo = it.oemPartNo?it.oemPartNo:""
            if (oemPartNo=="null"||oemPartNo==null){
                oemPartNo = ""
            }



            def partDesc = it.partDesc
            //partDesc = partDesc.replaceAll("\n", " \\\\n ")
            partDesc = partDesc.replaceAll("\n", " \\\\n")
            def wig = it.wig ? it.wig : ""
            def st = it.st ? it.st : ""
            def cm = it.cm ? it.cm : ""
            def supplier = it.supplier ? it.supplier : ""
            def sici = it.sici ? it.sici : ""
            def sds = it.sds ? it.sds : ""
            def jici = it.jici ? it.jici : ""
            def ppmcNo = it.ppmcNo ? it.ppmcNo : ""
            def colourName = it.colourName ? it.colourName : ""
            def scjp = it.scjp ? it.scjp : ""
            def pden = it.pden ? it.pden : ""
            def cd = it.cd ? it.cd : ""
            def ccam = it.ccam ? it.ccam : ""
            def ccab = it.ccab ? it.ccab : ""
            def dsiic = it.dsiic ? it.dsiic : ""


            def newId = null

            def newlevel = null
            def newlevels = null
            def newinfo = null

            def newJcPartNo = null
            def newOemPartNo = null
            def newPartDesc = null

            def newwig = null
            def newst = null
            def newcm = null
            def newsupplier = null
            def newsici = null
            def newsds = null
            def newjici = null
            def newppmcNo = null
            def newcolourName = null
            def newscjp = null
            def newpden = null
            def newcd = null
            def newccam = null
            def newccab = null
            def newdsiic = null

            def newParent = null
            def npi = null
            newData.find { it1 ->
                newParent = it1.parent ? it1.parent : "[]"

                npi = jsonSlurper.parseText(newParent)

                newId = it1.id

                newlevel = it1.level ? it1.level : ""
                if (newlevel.isFloat()) {
                    newlevel = newlevel as float
                }

                newlevels = it1.levels

                newlevels = jsonSlurper.parseText(newlevels)
                def newlevelsList = []
                newlevels.each { nl ->
                    def key = nl.keySet()[0]
                    newlevelsList.add("{\"" + key + "\":\"" + nl[key] + "\"}")
                }
                newlevels = newlevelsList.join(",")


                newinfo = it1.info
                newinfo = jsonSlurper.parseText(newinfo)
                def newinfoList = []
                newinfo.each { p ->
                    newinfoList.add("\"" + p + "\"")
                }
                newinfo = newinfoList.join(",")

                newJcPartNo = it1.jcPartNo
                if (newJcPartNo.isFloat()) {
                    newJcPartNo = newJcPartNo as float
                }

                newOemPartNo = it1.oemPartNo?it1.oemPartNo:""
                if (newOemPartNo=="null"||newOemPartNo==null){
                    newOemPartNo = ""
                }


                newPartDesc = it1.partDesc
                newPartDesc = newPartDesc.replaceAll("\n", " \\\\n")

                newwig = it1.wig ? it1.wig : ""
                newst = it1.st ? it1.st : ""
                newcm = it1.cm ? it1.cm : ""
                newsupplier = it1.supplier ? it1.supplier : ""
                newsici = it1.sici ? it1.sici : ""
                newsds = it1.sds ? it1.sds : ""
                newjici = it1.jici ? it1.jici : ""
                newppmcNo = it1.ppmcNo ? it1.ppmcNo : ""
                newcolourName = it1.colourName ? it1.colourName : ""
                newscjp = it1.scjp ? it1.scjp : ""
                newpden = it1.pden ? it1.pden : ""
                newcd = it1.cd ? it1.cd : ""
                newccam = it1.ccam ? it1.ccam : ""
                newccab = it1.ccab ? it1.ccab : ""
                newdsiic = it1.dsiic ? it1.dsiic : ""


                if (
                level == newlevel
                        && jcPartNo == newJcPartNo
                        && oemPartNo == newOemPartNo
                        && checkParent(pi, npi)
                ) {

                    findFlag = newData.indexOf(it1)
                    newData.removeAt(findFlag)
                    return true
                }
            }


            def content = []


            if (findFlag != null) {

                content.add("\"info\":[" + newinfo + "]")
                content.add("\"levels\":[" + newlevels + "]")
                content.add("\"level\":\"" + newlevel + "\"")

                content.add("\"oemPartNo\":\"" + newOemPartNo + "\"")
                content.add("\"jcPartNo\":\"" + newJcPartNo + "\"")
                content.add("\"partDesc\":\"" + newPartDesc + "\"")

                content.add("\"wig\":\"" + newwig + "\"")
                content.add("\"st\":\"" + newst + "\"")
                content.add("\"cm\":\"" + newcm + "\"")
                content.add("\"supplier\":\"" + newsupplier + "\"")
                content.add("\"sici\":\"" + newsici + "\"")
                content.add("\"sds\":\"" + newsds + "\"")
                content.add("\"jici\":\"" + newjici + "\"")
                content.add("\"ppmcNo\":\"" + newppmcNo + "\"")
                content.add("\"colourName\":\"" + newcolourName + "\"")
                content.add("\"scjp\":\"" + newscjp + "\"")
                content.add("\"pden\":\"" + newpden + "\"")
                content.add("\"cd\":\"" + newcd + "\"")
                content.add("\"ccam\":\"" + newccam + "\"")
                content.add("\"ccab\":\"" + newccab + "\"")
                content.add("\"dsiic\":\"" + newdsiic + "\"")
                content.add("\"_PARENT\":" + newParent + "")

            } else {
                content.add("\"info\":[" + info + "]")
                content.add("\"levels\":[" + levels + "]")
                content.add("\"level\":\"" + level + "\"")

                content.add("\"oemPartNo\":\"" + oemPartNo + "\"")
                content.add("\"jcPartNo\":\"" + jcPartNo + "\"")
                content.add("\"partDesc\":\"" + partDesc + "\"")

                content.add("\"wig\":\"" + wig + "\"")
                content.add("\"st\":\"" + st + "\"")
                content.add("\"cm\":\"" + cm + "\"")
                content.add("\"supplier\":\"" + supplier + "\"")
                content.add("\"sici\":\"" + sici + "\"")
                content.add("\"sds\":\"" + sds + "\"")
                content.add("\"jici\":\"" + jici + "\"")
                content.add("\"ppmcNo\":\"" + ppmcNo + "\"")
                content.add("\"colourName\":\"" + colourName + "\"")
                content.add("\"scjp\":\"" + scjp + "\"")
                content.add("\"pden\":\"" + pden + "\"")
                content.add("\"cd\":\"" + cd + "\"")
                content.add("\"ccam\":\"" + ccam + "\"")
                content.add("\"ccab\":\"" + ccab + "\"")
                content.add("\"dsiic\":\"" + dsiic + "\"")

                content.add("\"_PARENT\":" + parent + "")

            }
            //println content.size()+" "+findFlag
            if (content.size() != 0) {


                dataCount++
                content = "{" + content.join(",") + "}"
                def seq = sql.rows("select  nextval('seq_data_version_${ver.replace(".", "_")}') as id".toString())
                def insertSql = """
                        insert into data_version_${ver.replace(".", "_")}(id,flag,sub_project_id,content) 
                        values(${seq[0].id},${seq[0].id},${subProjectId},'${content}')
                    """.toString()

                //println insertSql
                sql.execute(insertSql)
            }
        }

        println "newData after size:"+newData.size()
        newData.eachWithIndex { it, i ->


            def levels = it.levels
            def oemPartNo = it.oemPartNo?it.oemPartNo:""
            if (oemPartNo=="null"||oemPartNo==null){
                oemPartNo = ""
            }
            levels = jsonSlurper.parseText(levels)
            def levelsList = []
            levels.each { nl ->
                def key = nl.keySet()[0]
                levelsList.add("{\"" + key + "\":\"" + nl[key] + "\"}")
            }
            levels = levelsList.join(",")

            dataCount++
            def partDesc = it.partDesc ? it.partDesc : ""
            partDesc = partDesc.replaceAll("\n", " \\\\n")
            def content = []
            content.add("\"info\":[" + it.info + "]")
            content.add("\"levels\":[" + levels + "]")
            content.add("\"level\":\"" + it.level + "\"")

            content.add("\"oemPartNo\":\"" + oemPartNo + "\"")
            content.add("\"jcPartNo\":\"" + it.jcPartNo + "\"")

            content.add("\"partDesc\":\"" + partDesc + "\"")

            def wig = it.wig ? it.wig : ""
            def st = it.st ? it.st : ""
            def cm = it.cm ? it.cm : ""
            def supplier = it.supplier ? it.supplier : ""
            def sici = it.sici ? it.sici : ""
            def sds = it.sds ? it1.sds : ""
            def jici = it.jici ? it.jici : ""
            def ppmcNo = it.ppmcNo ? it.ppmcNo : ""
            def colourName = it.colourName ? it.colourName : ""
            def scjp = it.scjp ? it.scjp : ""
            def pden = it.pden ? it.pden : ""
            def cd = it.cd ? it.cd : ""
            def ccam = it.ccam ? it.ccam : ""
            def ccab = it.ccab ? it.ccab : ""
            def dsiic = it.dsiic ? it.dsiic : ""

            content.add("\"wig\":\"" + wig + "\"")
            content.add("\"st\":\"" + st + "\"")
            content.add("\"cm\":\"" + cm + "\"")
            content.add("\"supplier\":\"" + supplier + "\"")
            content.add("\"sici\":\"" + sici + "\"")
            content.add("\"sds\":\"" + sds + "\"")
            content.add("\"jici\":\"" + jici + "\"")
            content.add("\"ppmcNo\":\"" + ppmcNo + "\"")
            content.add("\"colourName\":\"" + colourName + "\"")
            content.add("\"scjp\":\"" + scjp + "\"")
            content.add("\"pden\":\"" + pden + "\"")
            content.add("\"cd\":\"" + cd + "\"")
            content.add("\"ccam\":\"" + ccam + "\"")
            content.add("\"ccab\":\"" + ccab + "\"")
            content.add("\"dsiic\":\"" + dsiic + "\"")


            content.add("\"_PARENT\":" + it.parent + "")

            content = "{" + content.join(",") + "}"
            def seq = sql.rows("select  nextval('seq_data_version_${ver.replace(".", "_")}') as id".toString())
            def insertSql = """
                        insert into data_version_${ver.replace(".", "_")}(id,flag,sub_project_id,content) 
                        values(${seq[0].id},${seq[0].id},${subProjectId},'${content}')
                    """.toString()

            //println insertSql
            sql.execute(insertSql)
        }

        //sql.execute("delete from data_version_${baseVer.replace(".", "_")} where sub_project_id=?".toString(), [subProjectId])
        //sql.execute("delete from data_version_${newVer.replace(".", "_")} where sub_project_id=?".toString(), [subProjectId])

        def spv = SubProjectVersion.findByVerAndSubProjectId(baseVer, subProjectId)
        //spv.delete()
        spv = SubProjectVersion.findByVerAndSubProjectId(newVer, subProjectId)
        //spv.delete()

        def subProjectVersion = new SubProjectVersion(userId: (userId as Long), ver: (ver + ""),
                subProjectId: (subProjectId as Long), dataCount: dataCount, remark: remark)
        subProjectVersion.save(flush: true)
        def maxVer = sql.rows("select max(ver) ver from sub_project_version where sub_project_id=?", [subProjectId])[0].ver
        def subProject = SubProject.findById(subProjectId as Long)

        subProject.ver = maxVer + ""
        subProject.save(flush: true)
        maxVer = sql.rows("select max(ver) ver from sub_project where project_id=?", [subProject.projectId])[0].ver
        def project = Project.findById(subProject.projectId)
        project.ver = maxVer + ""
        project.save(flush: true)
        render results as JSON
    }

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

    @Transactional(readOnly = true)
    def compare() {
        def results = [code: 0, data: [total: 0, rows: []]]
        def fromVer = params.fromVer
        def toVer = params.toVer
        def subProjectId = params.subProjectId as Long

        def fromData = dataVersionService.queryTable(fromVer, subProjectId)
        def toData = dataVersionService.queryTable(toVer, subProjectId)
        def jsonSlurper = new JsonSlurper()

        def rows = []
        toData.eachWithIndex { it, i ->
            def row = it

            def findFlag = null
            def parent = it.parent ? it.parent : "[]"
            parent = jsonSlurper.parseText(parent)

            def jcPartNo = it.jcPartNo
            if (jcPartNo.isFloat()) {
                jcPartNo = jcPartNo as float
            }

            def level = it.level ? it.level : ""
            if (level.isFloat()) {
                level = level as float
            }

            def oemPartNo = it.oemPartNo
            def partDesc = it.partDesc
            partDesc = partDesc.replaceAll("\n", " \\\\n")

            row.level = level
            row.jcpartno = jcPartNo
            row.oempartno = oemPartNo
            row.partdesc = partDesc
            row.id = it.id
            row.levels = it.levels
            row.info = it.info
            row.wig = it.wig
            row.st = it.st
            row.cm = it.cm
            row.supplier = it.supplier
            row.sici = it.sici
            row.sds = it.sds
            row.jici = it.jici
            row.ppmcNo = it.ppmcNo
            row.colourName = it.colourName
            row.scjp = it.scjp
            row.pden = it.pden
            row.cd = it.cd
            row.ccam = it.ccam
            row.ccab = it.ccab
            row.dsiic = it.dsiic
            row.parentJpn = it.parentJpn
            row.parentLevel = it.parentLevel

            fromData.find { it1 ->
                def newParent = it1.parent ? it1.parent : ""
                newParent = jsonSlurper.parseText(newParent)

                def newlevel = it1.level ? it1.level : ""
                if (newlevel.isFloat()) {
                    newlevel = newlevel as float
                }

                def newJcPartNo = it1.jcPartNo
                if (newJcPartNo.isFloat()) {
                    newJcPartNo = newJcPartNo as float
                }

                def newOemPartNo = it1.oemPartNo
                def newPartDesc = it1.partDesc
                newPartDesc = newPartDesc.replaceAll("\n", " \\\\n")
                if (level == newlevel
                        && jcPartNo == newJcPartNo
                        && oemPartNo == newOemPartNo
                        && checkParent(parent, newParent)
                ) {
                    findFlag = fromData.indexOf(it1)

                    row.newlevel = newlevel
                    row.newOemPartNo = newOemPartNo
                    row.newJcPartNo = newJcPartNo
                    row.newPartDesc = newPartDesc

                    row.newId = it1.id
                    row.newlevels = it1.levels
                    row.newinfo = it1.info
                    row.newwig = it1.wig
                    row.newst = it1.st
                    row.newcm = it1.cm
                    row.newsupplier = it1.supplier
                    row.newsici = it1.sici
                    row.newsds = it1.sds
                    row.newjici = it1.jici
                    row.newppmcNo = it1.ppmcNo
                    row.newcolourName = it1.colourName
                    row.newscjp = it1.scjp
                    row.newpden = it1.pden
                    row.newcd = it1.cd
                    row.newccam = it1.ccam
                    row.newccab = it1.ccab
                    row.newdsiic = it1.dsiic
                    row.newparentJpn = it1.parentJpn
                    row.newparentLevel = it1.parentJpn


                    fromData.removeAt(findFlag)
                    return true
                }

            }


            def jcpartnoStyle = []

            row.findFlag = findFlag
            def style = ""
            def levelStyle = ""
            if (findFlag != null) {
                if (row.level != row.newlevel) {
                    row.levelStyle = "color:red"
                } else {
                    row.levelStyle = ""
                }
                if (row.oemPartNo != row.newOemPartNo) {
                    row.oempartnoStyle = "color:red"
                } else {
                    row.oempartnoStyle = ""
                }

                if (row.jcPartNo != row.jcPartNo) {
                    row.jcpartnoStyle = "color:red"
                } else {
                    row.jcpartnoStyle = ""
                }

                if (row.partDesc != row.newPartDesc) {
                    row.partdescStyle = "color:red"
                } else {
                    row.partdescStyle = ""
                }

                if (row.info != row.newinfo) {
                    row.infoStyle = "color:red"
                } else {
                    row.infoStyle = ""
                }


            } else {
                row.levelStyle = "color:red"
                row.infoStyle = "color:red"
                row.oempartnoStyle = "color:red"
                row.jcpartnoStyle = "color:red"
                row.partdescStyle = "color:red"
            }


            //row.jcpartnoStyle = jcpartnoStyle.join(";")
            row.style = style

            rows.add(row)
        }

        fromData.eachWithIndex { it, i ->
            def row = it

            def findFlag = -1

            def jcPartNo = it.jcPartNo
            if (jcPartNo.isFloat()) {
                jcPartNo = jcPartNo as float
            }

            def level = it.level ? it.level : ""
            if (level.isFloat()) {
                level = level as float
            }

            def oemPartNo = it.oemPartNo
            def partDesc = it.partDesc
            partDesc = partDesc.replaceAll("\n", " \\\\n")

            row.level = level
            row.newJcPartNo = jcPartNo
            row.oempartno = oemPartNo
            row.newPartDesc = partDesc
            row.id = it.id
            row.levels = it.levels
            row.info = it.info
            row.wig = it.wig
            row.st = it.st
            row.cm = it.cm
            row.supplier = it.supplier
            row.sici = it.sici
            row.sds = it.sds
            row.jici = it.jici
            row.ppmcNo = it.ppmcNo
            row.colourName = it.colourName
            row.scjp = it.scjp
            row.pden = it.pden
            row.cd = it.cd
            row.ccam = it.ccam
            row.ccab = it.ccab
            row.dsiic = it.dsiic
            row.parentJpn = it.parentJpn
            row.parentLevel = it.parentLevel
            row.findFlag = findFlag
            row.style = "color:red;text-decoration:line-through;"
            row.levelStyle = "color:red;text-decoration:line-through;"
            row.oempartnoStyle = "color:red;text-decoration:line-through;"
            row.partdescStyle = "color:red;text-decoration:line-through;"
            row.jcpartnoStyle = "color:red;text-decoration:line-through;"
            row.infoStyle = "color:red;text-decoration:line-through;"
            println row
            rows.add(row)
        }

        results.data.total = rows.size()
        results.data.rows = rows

        render results as JSON
    }

    //子项目版本增加
    @Transactional
    def save() {

        def results = [code: 0, data: [:]]
        def sql = new Sql(dataSource)
        def no = request.JSON.no
        def name = request.JSON.name
        def desc = request.JSON.desc
        def customer = request.JSON.customer
        def remark = request.JSON.remark
        def projectId = request.JSON.projectId
        def subProject = new SubProject(no: no, name: name, description: desc, projectId: projectId,
                customer: customer, remark: remark)
        subProject.save(flush: true)

        render results as JSON
    }

    //查找子项目版本信息并删除
    @Transactional
    def delete() {
        def results = [code: 0, data: [:]]
        def sql = new Sql(dataSource)
        def subProjectId = null
        request.JSON.each {
            def id = it.id
            def subProjectVersion = SubProjectVersion.findById(id)
            def ver = subProjectVersion.ver
            subProjectId = subProjectVersion.subProjectId
            subProjectVersion.delete(flush: true)
//            subProjectVersion.each {
//                SubProjectVersion.findById(it.id).delete()
//
//            }

            def delSql = """
                delete from data_version_${ver.replace(".", "_")} where sub_project_id=${subProjectId}
            """.toString()

            sql.execute(delSql)

        }

        if (subProjectId != null) {
            def maxVer = sql.rows("select max(ver) ver from sub_project_version where sub_project_id=?", [subProjectId])[0].ver
            def subProject = SubProject.findById(subProjectId as Long)

            subProject.ver = maxVer == null ? "" : (maxVer + "")
            subProject.save(flush: true)
            maxVer = sql.rows("select max(ver) ver from sub_project where project_id=?", [subProject.projectId])[0].ver
            def project = Project.findById(subProject.projectId)
            project.ver = maxVer == null ? "" : (maxVer + "")
            project.save(flush: true)
        }

        render results as JSON

    }

}
