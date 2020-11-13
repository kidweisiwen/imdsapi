package me

import grails.gorm.transactions.Transactional

import java.lang.reflect.UndeclaredThrowableException


@Transactional(rollbackFor = RuntimeException.class)
class TestService {
    def test1Service


    def serviceMethod() {
        try{
            test1Service.serviceMethod()
        } catch(e){
            println e
        }
        def project = new Project(name:"dfsfds565656")
        project.save(flush:true,failOnError:true)
        println 666
    }
}
