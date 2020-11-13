package me

import grails.gorm.transactions.Transactional
import grails.rest.*
import grails.converters.*
import groovy.sql.Sql

class UsersController {
	static responseFormats = ['json', 'xml']
	
    def index() {


    }

    @Transactional(readOnly = true)
    def list(){
        def results = [code: 0, data: [total: 0, rows: []]]
        def page = params.page ? params.page as Integer : 1
        def size = params.size ? params.size as Integer : 20

        def id = params.id ? params.id : ""
        def cnName = params.cnName ? params.cnName.trim() : ""
        def userLoginId = params.userLoginId ? params.userLoginId.trim() : ""
        def email = params.email ? params.email.trim() : ""
        def tel = params.tel ? params.tel.trim() : ""

        def whereSql = "where 1=1 "


        def p = [:]
        def orderSql = " order by a.id desc"

        //根据参数构建查询条件
        //id
        if (id != "") {
            whereSql += " and a.id = :id";
            p.id = id as Long;
        }

        //姓名
        if (cnName != "") {
            whereSql += " and a.cnName like :cnName";
            p.cnName = "%" + cnName + "%";
        }

        //登录名
        if (userLoginId != "") {
            whereSql += " and a.userLoginId like :userLoginId";
            p.userLoginId = "%" + userLoginId + "%";
        }

        //邮箱
        if (email != "") {
            whereSql += " and a.email like :email";
            p.email = "%" + email + "%";
        }

        //电话
        if (tel != "") {
            whereSql += " and a.tel like :tel";
            p.tel = "%" + tel + "%";
        }


        def total = Users.executeQuery("""
            select count(a.id) 
            from Users a 
        """ + whereSql.toString(), p)

        def rows = Users.findAll("""
            select new map(a.id as id,a.cnName as cnName,a.userLoginId as userLoginId,
            a.email as email,a.tel as tel,a.role as role)
            from Users a
        """ + whereSql + orderSql.toString(), p, [max: size, offset: (page - 1) * size])

        results.data.total = total[0]
        results.data.rows = rows;
        render results as JSON


    }

    @Transactional
    def save(){

        def results = [code: 0, data: [:]]

        def cnName = request.JSON.cnName
        def userLoginId = request.JSON.userLoginId
        def email = request.JSON.email
        def tel = request.JSON.tel
        def role = request.JSON.role


        def user = Users.findByUserLoginId(userLoginId)

        if (user) {

            results.code = -1
            results.msg = "登录账号已存在"
            render results as JSON
            return

        }

        def users = new Users(cnName: cnName, userLoginId: userLoginId, email: email,
                tel: tel,role: role)
        users.save(flush: true)
        render results as JSON
    }

    @Transactional
    def update(){
        def results = [code: 0, data: [:]]

        def id = request.JSON.id
        def cnName = request.JSON.cnName
        def userLoginId = request.JSON.userLoginId
        def email = request.JSON.email
        def tel = request.JSON.tel
        def role = request.JSON.role


        def user = Users.findById(id as Long)

        def toUser = Users.findByUserLoginId(userLoginId)

        if (user.userLoginId!=userLoginId && toUser) {

            results.code = -1
            results.msg = "登录账号已存在"
            render results as JSON
            return
        }



        user.cnName = cnName
        user.userLoginId = userLoginId
        user.email = email
        user.tel = tel
        user.role = role

        user.save(flush: true)

        render results as JSON
    }
    @Transactional
    def delete(){
        def results = [code: 0, data: [:]]

        request.JSON.each {it->
            def id =it.id as long
            def user = Users.findById(id)
            if (user != null) {
                user.delete()

                def userProjectRoles = UserProjectRole.findAllByUserId(id)
                UserProjectRole.where {
                    userId == id
                }.deleteAll()
                //UserProjectRole.executeUpdate("delete UserProjectRole r where r.id in :ids", [ids: ids])


            }
        }
        render results as JSON
    }
}
