package me

class Users {
    String cnName
    String enName
    String userLoginId
    String email
    String tel
    String role
    static constraints = {
        cnName nullable: true
        enName nullable: true
        userLoginId nullable: true
        email nullable: true
        tel nullable: true
        role nullable: true
    }
}
