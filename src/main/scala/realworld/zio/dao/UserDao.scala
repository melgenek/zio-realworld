package realworld.zio.dao

import realworld.zio.error.NoUser
import realworld.zio.model.{User, UserId, UserModel}
import slick.jdbc.{JdbcBackend, JdbcProfile}
import zio.{IO, UIO}

trait UserDao {

  def create(user: User): UIO[User]

  def update(user: User): UIO[User]

  def findById(id: UserId): IO[NoUser, User]

  def findByEmail(email: String): IO[NoUser, User]

  def findByUsername(username: String): IO[NoUser, User]

}

class UserDaoImpl(val profile: JdbcProfile, val db: JdbcBackend.Database) extends UserDao with UserModel {

  import profile.api._

  override def create(user: User): UIO[User] = {
    (users returning users.map(_.id) into ((record, id) => record.copy(id = id)) += user).asUIO
  }

  override def update(user: User): UIO[User] = {
    users
      .filter(_.id === user.id.bind)
      .update(user)
      .asUIO
      .as(user)
  }

  override def findById(id: UserId): IO[NoUser, User] = {
    users.filter(_.id === id.bind).result.headOption.asIO(NoUser())
  }

  override def findByEmail(email: String): IO[NoUser, User] = {
    users.filter(_.email === email.bind).result.headOption.asIO(NoUser())
  }

  override def findByUsername(username: String): IO[NoUser, User] = {
    users.filter(_.username === username.bind).result.headOption.asIO(NoUser())
  }

}
