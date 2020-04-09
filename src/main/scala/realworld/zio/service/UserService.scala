package realworld.zio.service

import com.softwaremill.quicklens._
import io.scalaland.chimney.dsl._
import org.mindrot.jbcrypt.BCrypt
import realworld.zio.dao.UserDao
import realworld.zio.data.{CreateUserRequest, UpdateUserRequest}
import realworld.zio.error.{InvalidCredentials, NoUser}
import realworld.zio.model.User
import realworld.zio.module.Authentication
import slick.jdbc.JdbcBackend
import zio.{IO, UIO, ZIO}

trait UserService {

  def create(request: CreateUserRequest): UIO[User]

  def update(request: UpdateUserRequest): ZIO[Authentication, NoUser, User]

  def findCurrentUser: ZIO[Authentication, NoUser, User]

  def login(email: String, password: String): IO[InvalidCredentials, User]

  def findByEmail(email: String): IO[NoUser, User]

  def findByUsername(email: String): IO[NoUser, User]

}

class UserServiceImpl(userDao: UserDao, db: JdbcBackend.Database) extends UserService {

  override def create(request: CreateUserRequest): UIO[User] = {
    val user = request.into[User]
      .withFieldComputed(_.password, r => hashPassword(r.password))
      .transform

    userDao.create(user)
  }

  override def update(request: UpdateUserRequest): ZIO[Authentication, NoUser, User] = {
    for {
      currentUserEmail <- ZIO.access[Authentication](_.currentUserEmail)
      existingUser <- userDao.findByEmail(currentUserEmail)
      patchedUser = existingUser.patchUsing(request)
      updatedUser = patchedUser.modify(_.password).setToIfDefined(request.password.map(hashPassword))
      storedUser <- userDao.update(updatedUser)
    } yield storedUser
  }

  override def findCurrentUser: ZIO[Authentication, NoUser, User] = {
    for {
      currentUserEmail <- ZIO.access[Authentication](_.currentUserEmail)
      user <- userDao.findByEmail(currentUserEmail)
    } yield user
  }

  override def login(email: String, password: String): IO[InvalidCredentials, User] = {
    userDao.findByEmail(email)
      .orElseFail(InvalidCredentials())
      .filterOrFail(user => BCrypt.checkpw(password, user.password))(InvalidCredentials())
  }

  override def findByEmail(email: String): IO[NoUser, User] = {
    userDao.findByEmail(email)
  }

  override def findByUsername(username: String): IO[NoUser, User] = {
    userDao.findByUsername(username)
  }

  private def hashPassword(password: String): String = {
    BCrypt.hashpw(password, BCrypt.gensalt())
  }

}
