package me

class UserProjectRole {
    long userId
    long projectId
    long subProjectId
    String role
    Date dateCreated
    Date lastUpdated
    static constraints = {
        userId nullable: true
        projectId nullable: true
        subProjectId nullable: true
        role nullable: true
    }
}
