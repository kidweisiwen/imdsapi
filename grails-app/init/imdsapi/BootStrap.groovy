package imdsapi

import grails.converters.JSON

import java.text.SimpleDateFormat

class BootStrap {

    def init = { servletContext ->

        JSON.registerObjectMarshaller(Date) {
            //println it
            if (it!=null) {
                def df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                return (df.format(it))
            } else {
                return ""
            }

            //return it.format("yyyy-MM-dd HH:mm")
        }
    }
    def destroy = {
    }
}
