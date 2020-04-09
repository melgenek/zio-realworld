package realworld.zio.dao

import realworld.zio.model.{ArticleId, ArticleRelationModel, UserId}
import slick.jdbc.{JdbcBackend, JdbcProfile}
import zio.UIO

trait ArticleRelationDao {

  def favorite(userId: UserId, articleId: ArticleId): UIO[Unit]

  def unfavorite(userId: UserId, articleId: ArticleId): UIO[Unit]

  def favoritesCount(articleId: ArticleId): UIO[Int]

  def isFavorited(userId: UserId, articleId: ArticleId): UIO[Boolean]

}

class ArticleRelationDaoImpl(val profile: JdbcProfile, val db: JdbcBackend.Database)
  extends ArticleRelationDao with ArticleRelationModel {

  import profile.api._

  override def favorite(userId: UserId, articleId: ArticleId): UIO[Unit] = {
    relations.insertOrUpdate(userId, articleId).asUIO.unit
  }

  override def unfavorite(userId: UserId, articleId: ArticleId): UIO[Unit] = {
    relations.filter(r => r.articleId === articleId.bind && r.userId === userId.bind).delete.asUIO.unit
  }

  override def favoritesCount(articleId: ArticleId): UIO[Int] = {
    relations.filter(_.articleId === articleId.bind).length.result.asUIO
  }

  override def isFavorited(userId: UserId, articleId: ArticleId): UIO[Boolean] = {
    relations.filter(r => r.articleId === articleId.bind && r.userId === userId.bind).exists.result.asUIO
  }

}
