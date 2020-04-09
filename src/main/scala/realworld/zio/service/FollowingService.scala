package realworld.zio.service

import realworld.zio.dao.{UserDao, UserRelationDao}
import realworld.zio.error.NoUser
import realworld.zio.model.UserId
import realworld.zio.module.DetailedAuthentication
import zio.{UIO, URIO, ZIO}

trait FollowingService {

  def following(userId: UserId): ZIO[DetailedAuthentication, NoUser, Boolean]

  def followees(): URIO[DetailedAuthentication, Seq[UserId]]

  def follow(userId: UserId): ZIO[DetailedAuthentication, NoUser, Unit]

  def unfollow(userId: UserId): ZIO[DetailedAuthentication, NoUser, Unit]

}

class FollowingServiceImpl(userDao: UserDao, relationDao: UserRelationDao) extends FollowingService {

  override def following(userId: UserId): ZIO[DetailedAuthentication, NoUser, Boolean] = {
    for {
      currentUser <- ZIO.access[DetailedAuthentication](_.user)
      following <- relationDao.isFollowing(currentUser.id, userId)
    } yield following
  }

  override def followees(): URIO[DetailedAuthentication, Seq[UserId]] = {
    for {
      currentUser <- ZIO.access[DetailedAuthentication](_.user)
      followees <- relationDao.followees(currentUser.id)
    } yield followees
  }

  override def follow(userId: UserId): ZIO[DetailedAuthentication, NoUser, Unit] = {
    changeRelation(userId, relationDao.follow)
  }

  override def unfollow(userId: UserId): ZIO[DetailedAuthentication, NoUser, Unit] = {
    changeRelation(userId, relationDao.unfollow)
  }

  private def changeRelation(userId: UserId,
                             change: (UserId, UserId) => UIO[Unit]): ZIO[DetailedAuthentication, NoUser, Unit] = {
    for {
      currentUser <- ZIO.access[DetailedAuthentication](_.user)
      _ <- change(currentUser.id, userId)
    } yield ()
  }

}
