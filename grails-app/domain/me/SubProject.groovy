package me

class SubProject {
    String name
    String no
    String description
    String customer
    String remark
    String ver
    long createUserId
    long updateUserId
    long projectId
    Date dateCreated
    Date lastUpdated
    static constraints = {
        name nullable: true
        no nullable: true
        description nullable: true
        customer nullable: true
        remark nullable: true
        ver nullable: true
        createUserId nullable: true
        updateUserId nullable: true
    }
}
