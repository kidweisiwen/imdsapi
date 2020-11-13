package me

class SubProjectVersion {
    long dataCount
    String remark
    String ver
    long userId
    long subProjectId
    Date dateCreated
    Date lastUpdated
    static constraints = {
        remark nullable: true
        dataCount nullable: true
        ver nullable: true
        userId nullable: true
        subProjectId nullable: true
    }
}
