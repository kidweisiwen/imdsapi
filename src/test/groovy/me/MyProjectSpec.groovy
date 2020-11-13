package me

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class MyProjectSpec extends Specification implements DomainUnitTest<MyProject> {

    def setup() {
    }

    def cleanup() {
    }

    void "test something"() {
        expect:"fix me"
            true == false
    }
}
