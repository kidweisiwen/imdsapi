package me


import grails.rest.*
import grails.converters.*
import groovy.sql.Sql
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.messaging.simp.SimpMessageSendingOperations

class TestController {
	static responseFormats = ['json', 'xml']
    def weatherService
    def dataSource
    SimpMessageSendingOperations brokerMessagingTemplate
    def index() {
        def results = [:]

        //results = weatherService.query()
        def sql = new Sql(dataSource)
        def d = sql.rows("select * from project")
        println d

        render results as JSON

    }
    def testService
    def test1Service
    def test(){
        def results = [:]
        //brokerMessagingTemplate.convertAndSend "/topic/channel", "hello from service!"
        def sql = new Sql(dataSource)
        def d = sql.rows("select * from project")
        println d
        //throw new Exception("dfdfdfdf")
        flash.message = "ddddd"
        def sd = [code:-1,msg:'失败']

        //throw new Exception("dfsfsdfsdfs")
        //respond (sd,[status: 400])
        println 4445
        //testService.serviceMethod()
        render sd as JSON
    }

    @MessageMapping("/hello")
    @SendTo("/topic/hello")
    protected String hello(String world) {
        return "hello, ${world}!"
    }


}
