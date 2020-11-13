package me

import grails.gorm.transactions.Transactional

import java.lang.reflect.UndeclaredThrowableException


@Transactional(rollbackFor = RuntimeException.class)
class Test1Service {
    def testService


    def serviceMethod() {
        1/0
    }
}
