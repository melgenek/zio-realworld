package realworld.zio.service

import realworld.zio.dao.ArticleRelationDao
import realworld.zio.error.NoArticle
import realworld.zio.model.{ArticleId, UserId}
import realworld.zio.module.DetailedAuthentication
import zio.{UIO, URIO, ZIO}

trait FavoritingService {

  def favorite(articleId: ArticleId): ZIO[DetailedAuthentication, NoArticle, Unit]

  def unfavorite(articleId: ArticleId): ZIO[DetailedAuthentication, NoArticle, Unit]

  def favoritesCount(articleId: ArticleId): UIO[Int]

  def isFavorited(articleId: ArticleId): URIO[DetailedAuthentication, Boolean]

}

class FavoritingServiceImpl(articleRelationDao: ArticleRelationDao) extends FavoritingService {

  override def favorite(articleId: ArticleId): URIO[DetailedAuthentication, Unit] = {
    changeRelation(articleId, articleRelationDao.favorite)
  }

  override def unfavorite(articleId: ArticleId): URIO[DetailedAuthentication, Unit] = {
    changeRelation(articleId, articleRelationDao.unfavorite)
  }

  override def favoritesCount(articleId: ArticleId): UIO[Int] = {
    articleRelationDao.favoritesCount(articleId)
  }

  override def isFavorited(articleId: ArticleId): URIO[DetailedAuthentication, Boolean] = {
    for {
      user <- URIO.access[DetailedAuthentication](_.user)
      isFavorited <- articleRelationDao.isFavorited(user.id, articleId)
    } yield isFavorited
  }

  private def changeRelation(articleId: ArticleId,
                             change: (UserId, ArticleId) => UIO[Unit]): URIO[DetailedAuthentication, Unit] = {
    for {
      user <- URIO.access[DetailedAuthentication](_.user)
      _ <- change(user.id, articleId)
    } yield ()
  }

}
