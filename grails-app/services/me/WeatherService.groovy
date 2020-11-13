package me

import grails.gorm.transactions.Transactional
//import com.fasterxml.jackson.databind.DeserializationFeature
//import com.fasterxml.jackson.databind.ObjectMapper
//import groovy.json.JsonSlurper
//import groovy.transform.CompileStatic
//import io.micronaut.http.HttpRequest
//import io.micronaut.http.HttpResponse
//import io.micronaut.http.client.HttpClient
//import io.micronaut.http.uri.UriBuilder
//import org.springframework.beans.factory.annotation.Autowired


@Transactional
class WeatherService {

    @Transactional(readOnly = true)
    def query() {
//        String baseUrl = "https://api.jisuapi.com/"
//        def client = HttpClient.create(baseUrl.toURL())
//        def request = HttpRequest.GET(UriBuilder.of('/weather/query')
//                .queryParam('appkey', 'c6f0e76d382e32cb')
//                .queryParam('city', '松江')
//                .build())
//        def resp = client.toBlocking().exchange(request, String)
//        def json = resp.body()
//        def jsonSlurper = new JsonSlurper()
//        def results = jsonSlurper.parseText(json)
//        println results
//        results
    }
}
