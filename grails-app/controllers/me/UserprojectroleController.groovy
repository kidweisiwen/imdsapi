package me

import grails.converters.JSON
import grails.gorm.transactions.Transactional

class UserprojectroleController {
	static responseFormats = ['json', 'xml']
	
    def index() {


    }

    @Transactional(readOnly = true)
    def list(){
        def results = [code: 0, data: [total: 0, rows: []]]
        def page = params.page ? params.page as Integer : 1
        def size = params.size ? params.size as Integer : 20

        def id = params.id ? params.id : ""
        def projectName = params.projectName ? params.projectName.trim() : ""
        def subProjectName = params.subProjectName ? params.subProjectName.trim() : ""
        def cnName = params.cnName ? params.cnName.trim() : ""
        def userLoginId = params.userLoginId ? params.userLoginId.trim() : ""

        def whereSql = "where 1=1 "


        def p = [:]
        def orderSql = " order by a.id desc"


        if (id != "") {
            whereSql += " and a.id = :id";
            p.id = id as Long;
        }


        if (projectName != "" && projectName != "所有项目") {
            whereSql += " and b.name like :projectName";
            p.projectName = "%" + projectName + "%";
        }


        if (subProjectName != "" && subProjectName != "所有子项目") {
            whereSql += " and c.name like :subProjectName";
            p.subProjectName = "%" + subProjectName + "%";
        }

        if (cnName != "") {
            whereSql += " and d.cnName like :cnName";
            p.cnName = "%" + cnName + "%";
        }


        if (userLoginId != "") {
            whereSql += " and d.userLoginId like :userLoginId";
            p.userLoginId = "%" + userLoginId + "%";
        }


        def total = UserProjectRole.executeQuery("""
            select count(a.id) 
            from UserProjectRole a 
            left join Project b on a.projectId = b.id
            left join SubProject c on a.subProjectId = c.id
            left join Users d on a.userId = d.id
        """ + whereSql.toString(), p)

        def detailSql = """
            select new map(a.projectId as projectId,a.subProjectId as subProjectId,a.id as id,d.cnName as cnName,d.userLoginId as userLoginId,
            b.name as projectName,c.name as subProjectName,a.role as role)
            from UserProjectRole a
            left join Project b on a.projectId = b.id
            left join SubProject c on a.subProjectId = c.id
            left join Users d on a.userId = d.id
        """ + whereSql + orderSql.toString()

        def rows = UserProjectRole.findAll(detailSql, p, [max: size, offset: (page - 1) * size])

        results.data.total = total[0]
        results.data.rows = rows;
        render results as JSON


    }

    @Transactional
    def save(){

        def results = [code: 0, data: [:]]

        def projectId = request.JSON.projectId as long
        def subProjectId = request.JSON.subProjectId as long
        def userLoginId = request.JSON.userLoginId
        def role = request.JSON.role

        def user = Users.findByUserLoginId(userLoginId)
        if (!user) {
            results.code = -1
            results.msg = "登录账号不存在"
            render results as JSON
            return
        }
        def userProjectRole = new UserProjectRole(projectId: projectId, subProjectId: subProjectId,
                userId: user.id,role: role)
        userProjectRole.save(flush: true)
        render results as JSON
    }

    @Transactional
    def update(){
        def results = [code: 0, data: [:]]
        def id = request.JSON.id as long
        def projectId = request.JSON.projectId as long
        def subProjectId = request.JSON.subProjectId as long
        def userLoginId = request.JSON.userLoginId
        def role = request.JSON.role


        def user = Users.findByUserLoginId(userLoginId)
        if (!user) {
            results.code = -1
            results.msg = "登录账号不存在"
            render results as JSON
            return
        }
        def userProjectRole = UserProjectRole.findById(id)

        userProjectRole.projectId = projectId
        userProjectRole.subProjectId = subProjectId
        userProjectRole.userId = user.id
        userProjectRole.role = role


        userProjectRole.save(flush: true)

        render results as JSON
    }
    @Transactional
    def delete(){
        def results = [code: 0, data: [:]]

        request.JSON.each {it->
            def id =it.id as long
            def userProjectRole = UserProjectRole.findById(id)
            if (userProjectRole != null) {
                userProjectRole.delete()


            }
        }
        render results as JSON
    }
}
