package realworld.zio.model

trait UserRelationModel extends UserModel {

  import profile.api._

  class UserRelations(t: Tag) extends Table[(UserId, UserId)](t, "relations") {
    def followerId = column[UserId]("follower_id")

    def followeeId = column[UserId]("followee_id")

    def pk = primaryKey("pk", (followerId, followeeId))

    def follower = foreignKey("follower_fk", followerId, users)(_.id)

    def followee = foreignKey("followee_fk", followeeId, users)(_.id)

    def * = (followerId, followeeId)
  }

  val relations = TableQuery[UserRelations]

}
