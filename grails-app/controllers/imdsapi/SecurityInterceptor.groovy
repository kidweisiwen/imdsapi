package imdsapi


class SecurityInterceptor {

    SecurityInterceptor() {
        matchAll().except(controller:'security', action:'login')
        .excludes(uri:"/api/test")
        .except(uri:"/api/test")

    }

    boolean before() {

//        println controllerName
//        println actionName
//        if(controllerName=="app" && actionName=="test"){
//            true
//        }
//        println 777777
        if (controllerName != "stomp") {
            response.setHeader("Access-Control-Allow-Origin", "*")
            response.setHeader("Access-Control-Allow-Credentials", "false")
            response.setHeader("Access-Control-Allow-Headers","*")
            response.setHeader("Access-Control-Allow-Methods", "*")
            //response.setHeader("Access-Control-Max-Age", "3600")

            response.status = 200
        } else {
//            response.setHeader("Access-Control-Allow-Origin", "*")
//            response.status = 200
        }


        true
    }

    boolean after() { true }

    void afterView() {
        // no-op
    }
}
