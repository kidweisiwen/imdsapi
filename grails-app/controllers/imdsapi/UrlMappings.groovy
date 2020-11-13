package imdsapi

class UrlMappings {

    static mappings = {
//        delete "/$controller/$id(.$format)?"(action:"delete")
//        get "/$controller(.$format)?"(action:"index")
//        get "/$controller/$id(.$format)?"(action:"show")
//        post "/$controller(.$format)?"(action:"save")
//        put "/$controller/$id(.$format)?"(action:"update")
//        patch "/$controller/$id(.$format)?"(action:"patch")
        "/$controller/$action?/$id?(.$format)?"{
            constraints {
                // apply constraints here
            }
        }

        //users
        get "/api/users/list"(controller: 'users', action:'list')
        post "/api/users/save"(controller: 'users', action:'save')
        post "/api/users/update"(controller: 'users', action:'update')
        post "/api/users/delete"(controller: 'users', action:'delete')

        //project permission
        get "/api/userprojectrole/list"(controller: 'userprojectrole', action:'list')
        post "/api/userprojectrole/save"(controller: 'userprojectrole', action:'save')
        post "/api/userprojectrole/update"(controller: 'userprojectrole', action:'update')
        post "/api/userprojectrole/delete"(controller: 'userprojectrole', action:'delete')

        //test
        get "/api/test/index"(controller: 'test', action:'index')
        get "/api/test/test"(controller: 'test', action:'test')

        //file upload
        post "/api/app/uploadfile"(controller: 'app', action:'uploadfile')

        post "/api/login"(controller: 'app', action:'login')
        get "/api/app/index"(controller: 'app', action:'index')
        get "/api/app/compare"(controller: 'app', action:'compare')

        //project api
        get "/api/project/list"(controller: 'project', action:'list')
        post "/api/project/save"(controller: 'project', action:'save')
        post "/api/project/update"(controller: 'project', action:'update')
        post "/api/project/delete"(controller: 'project', action:'delete')
        get "/api/project/exportexcel"(controller: 'project', action:'exportexcel')

        //subproject api
        get "/api/subproject/list"(controller: 'subproject', action:'list')
        post "/api/subproject/save"(controller: 'subproject', action:'save')
        post "/api/subproject/update"(controller: 'subproject', action:'update')
        post "/api/subproject/update"(controller: 'subproject', action:'update')
        post "/api/subproject/delete"(controller: 'subproject', action:'delete')


        //subprojectversion api
        get "/api/subprojectversion/list"(controller: 'subprojectversion', action:'list')
        get "/api/subprojectversion/verlist"(controller: 'subprojectversion', action:'verlist')
        post "/api/subprojectversion/save"(controller: 'subprojectversion', action:'save')
        post "/api/subprojectversion/update"(controller: 'subprojectversion', action:'update')
        post "/api/subprojectversion/update"(controller: 'subprojectversion', action:'update')
        post "/api/subprojectversion/delete"(controller: 'subprojectversion', action:'delete')
        post "/api/subprojectversion/verupdate"(controller: 'subprojectversion', action:'verupdate')
        post "/api/subprojectversion/verdelete"(controller: 'subprojectversion', action:'verdelete')
        post "/api/subprojectversion/dataupdate"(controller: 'subprojectversion', action:'dataupdate')
        post "/api/subprojectversion/datadelete"(controller: 'subprojectversion', action:'datadelete')
        post "/api/subprojectversion/datasave"(controller: 'subprojectversion', action:'datasave')
        post "/api/subprojectversion/merge"(controller: 'subprojectversion', action:'merge')
        get "/api/subprojectversion/compare"(controller: 'subprojectversion', action:'compare')

        post "/api/app/process"(controller: 'app', action:'process')
        get "/api/app/exportsubprojectversion"(controller: 'app', action:'exportsubprojectversion')

        //user api
        get "/api/user/list"(controller: 'user', action:'list')
        post "/api/user/save"(controller: 'user', action:'save')
        post "/api/user/update"(controller: 'user', action:'update')
        post "/api/user/delete"(controller: 'user', action:'delete')

        //dataversion api
        get "/api/dataversion/list"(controller: 'dataversion', action:'list')
        post "/api/dataversion/upload"(controller: 'dataversion', action:'upload')

        "/"(controller: 'application', action:'index')
        "500"(view: '/error')
        "404"(view: '/notFound')
    }
}
