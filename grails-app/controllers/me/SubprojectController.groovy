package me

import grails.gorm.transactions.Transactional
import grails.rest.*
import grails.converters.*
import grails.gorm.transactions.Transactional
import groovy.sql.Sql

class SubprojectController {
    static responseFormats = ['json', 'xml']
    def dataSource

    def index() {


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
        def projectId = params.projectId ? params.projectId : ""
        def userId = params.userId?(params.userId as long):""

        def p = [:]
        def whereSql = ""
        def permissionIds = []
        def allAdminFlag = false
        def allFlag = false
        def userProjectRoles = []
        def userSubProjectPermission = [:]
        if (userId!="") {
            userProjectRoles =  UserProjectRole.findAllByUserId(userId)
            userProjectRoles.each{

                permissionIds.add(it.subProjectId)
                if (it.projectId==-1 || it.subProjectId==-1) {
                    allFlag = true
                }

                if (it.subProjectId==-1 && it.role=="ADMIN") {
                    allAdminFlag = true
                }
                if (it.subProjectId!=-1 && it.role=="ADMIN") {
                    userSubProjectPermission[it.subProjectId] = true
                }

            }
        }


        if (allFlag || userId=="") {
            whereSql = "where 1=1 "
        } else {
            whereSql = "where (a.createUserId=:userId or a.id in :permissionIds)  "
            p.userId = userId
            p.permissionIds = permissionIds
        }



        def orderSql = " order by a.id desc"

        //根据参数构建查询条件
        //项目id
        if (projectId != "") {
            whereSql += " and a.projectId = :projectId";
            p.projectId = projectId as Long;
        }

        //子项目id
        if (id != "") {
            whereSql += " and a.id = :id";
            p.id = id as Long;
        }

        //子项目名称
        if (name != "") {
            whereSql += " and a.name like :name";
            p.name = "%" + name + "%";
        }

        //子项目编号
        if (no != "") {
            whereSql += " and a.no like :no";
            p.no = "%" + no + "%";
        }

        //子项目描述
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

        def total = SubProject.executeQuery("""
            select count(a.id) 
            from SubProject a 
        """ + whereSql.toString(), p)

        def rows = SubProject.findAll("""
            select new map(a.id as id,a.name as name,a.no as no,a.description as description,
            a.ver as ver,a.customer as customer,a.remark as remark,a.projectId as projectId)
            from SubProject a
        """ + whereSql + orderSql.toString(), p, [max: size, offset: (page - 1) * size])

        def data =[]
        rows.each{
            def hm = [:]
            hm.putAll(it)
            hm.adminFlag = allAdminFlag || (userId==it.createUserId) || (userSubProjectPermission[it.id]?userSubProjectPermission[it.id]:false)
            data.push(hm)
        }

        results.data.total = total[0]
        results.data.rows = data;
        render results as JSON

    }

    //子项目信息修改
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

        def subProject = SubProject.findById(id as Long)
        subProject.no = no
        subProject.name = name
        subProject.description = description
        subProject.customer = customer
        subProject.remark = remark
        subProject.updateUserId = userId as long
        subProject.save(flush: true)

        render results as JSON
    }

    //子项目信息增加
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
        def userId = request.JSON.userId

        def subProject = new SubProject(no: no, name: name, description: desc, projectId: projectId,
                customer: customer, remark: remark,createUserId: userId as long)
        subProject.save(flush: true)

//        sql.executeUpdate("""
//            CREATE TABLE public.data_result_${subProject.id}
//            (
//                content jsonb
//            );
//            CREATE INDEX idx_gin_data_result_${subProject.id}
//                ON public.data_${subProject.id} USING gin
//                (content)
//            ;
//        """.toString())
//
//        sql.executeUpdate("""
//            CREATE TABLE public.data_version_${subProject.id}
//            (
//                content jsonb
//            );
//            CREATE INDEX idx_gin_version_${subProject.id}
//                ON public.data_${subProject.id} USING gin
//                (content)
//            ;
//        """.toString())

        render results as JSON
    }


    @Transactional
    def delete() {
        def results = [code: 0, data: [:]]
        def sql = new Sql(dataSource)
        request.JSON.each {it->
            //查找子项目并删除
            def subProjects = SubProject.findAllById(it.id)
            subProjects.each {it1->
                def subProjectId = it1.id
                UserProjectRole.where {
                    subProjectId == subProjectId
                }.deleteAll()
                def subProject = SubProject.findById(it1.id)
                def subProjectVersions = SubProjectVersion.findAllBySubProjectId(it1.id)
                subProjectVersions.each {it2->
                    def subProjectVersion = SubProjectVersion.findById(it2.id)
                    def ver = subProjectVersion.ver
                    subProjectId = subProjectVersion.subProjectId
                    def delSql = """
                        delete from data_version_${ver.replace(".","_")} where sub_project_id=${subProjectId}
                    """.toString()
                    println delSql
                    sql.execute(delSql)
                    subProjectVersion.delete()
                }
                subProject.delete()

            }
        }
        render results as JSON

    }

}
