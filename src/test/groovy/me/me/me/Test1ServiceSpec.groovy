package me.me.me

import grails.testing.services.ServiceUnitTest
import me.Test1Service
import spock.lang.Specification

class Test1ServiceSpec extends Specification implements ServiceUnitTest<Test1Service>{

    def setup() {
    }

    def cleanup() {
    }

    void "test something"() {
        expect:"fix me"
            true == false
    }
}
