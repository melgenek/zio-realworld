package realworld.zio.dao

import realworld.zio.model.{UserId, UserRelationModel}
import slick.jdbc.{JdbcBackend, JdbcProfile}
import zio.UIO

trait UserRelationDao {

  def isFollowing(followerId: UserId, followeeId: UserId): UIO[Boolean]

  def follow(followerId: UserId, followeeId: UserId): UIO[Unit]

  def unfollow(followerId: UserId, followeeId: UserId): UIO[Unit]

  def followees(followerId: UserId): UIO[Seq[UserId]]

}


class UserRelationDaoImpl(val profile: JdbcProfile, val db: JdbcBackend.Database)
  extends UserRelationDao with UserRelationModel {

  import profile.api._

  def isFollowing(followerId: UserId, followeeId: UserId): UIO[Boolean] = {
    relations
      .filter(r => r.followerId === followerId.bind && r.followeeId === followeeId.bind)
      .exists
      .result
      .asUIO
  }

  override def follow(followerId: UserId, followeeId: UserId): UIO[Unit] = {
    (relations += (followerId, followeeId)).asUIO.unit
  }

  override def unfollow(followerId: UserId, followeeId: UserId): UIO[Unit] = {
    relations
      .filter(r => r.followerId === followerId.bind && r.followeeId === followeeId.bind)
      .delete
      .asUIO
      .unit
  }

  override def followees(followerId: UserId): UIO[Seq[UserId]] = {
    relations
      .filter(_.followerId === followerId.bind)
      .map(_.followeeId)
      .result
      .asUIO
  }

}
