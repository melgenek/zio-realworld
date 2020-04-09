package realworld.zio.model

import slick.lifted.MappedTo

case class UserId(value: Long) extends AnyVal with MappedTo[Long]

case class User(id: UserId = UserId(-1),
                email: String,
                username: String,
                password: String,
                bio: Option[String],
                image: Option[String])

trait UserModel extends SlickModel {

  import profile.api._

  class Users(t: Tag) extends Table[User](t, "users") {
    def id = column[UserId]("id", O.PrimaryKey, O.AutoInc)

    def email = column[String]("email", O.Unique)

    def username = column[String]("username", O.Unique)

    def password = column[String]("password")

    def bio = column[String]("bio")

    def image = column[String]("image")

    def * = (id, email, username, password, bio.?, image.?).mapTo[User]
  }

  val users = TableQuery[Users]

}
