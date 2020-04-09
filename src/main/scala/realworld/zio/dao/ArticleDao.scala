package realworld.zio.dao

import realworld.zio.dao.ArticleDao.{FilterCriteria, Pagination}
import realworld.zio.error.NoArticle
import realworld.zio.model.{Article, ArticleModel, ArticleRelationModel, TagList, TagModel, UserId}
import slick.jdbc.{JdbcBackend, JdbcProfile}
import zio.{IO, UIO}

import scala.concurrent.ExecutionContext

trait ArticleDao {

  def create(article: Article): UIO[Article]

  def createWithTags(article: Article, articleTags: TagList): UIO[Article]

  def update(article: Article): UIO[Article]

  def updateWithTags(article: Article, tagList: TagList): UIO[Article]

  def delete(slug: String): IO[NoArticle, Unit]

  def findBySlug(slug: String): IO[NoArticle, Article]

  def list(criteria: FilterCriteria, pagination: Pagination): UIO[Seq[Article]]

  def listByFollowees(followees: Seq[UserId], pagination: Pagination): UIO[Seq[Article]]

}

object ArticleDao {

  case class Pagination(limit: Int, offset: Int)

  case class FilterCriteria(tag: Option[String],
                            authorUsername: Option[String],
                            favoritedBy: Option[String])

}

class ArticleDaoImpl(val profile: JdbcProfile, val db: JdbcBackend.Database)(implicit ec: ExecutionContext)
  extends ArticleDao with ArticleModel with TagModel with ArticleRelationModel {

  import profile.api._

  override def create(article: Article): UIO[Article] = {
    createAction(article).asUIO
  }

  override def createWithTags(article: Article, tagList: TagList): UIO[Article] = {
    (for {
      article <- createAction(article)
      _ <- tags ++= tagList.tags.map((article.id, _))
    } yield article)
      .transactionally
      .asUIO
  }

  private def createAction(article: Article): DBIO[Article] = {
    articles returning articles.map(_.id) into ((record, id) => record.copy(id = id)) += article
  }

  override def update(article: Article): UIO[Article] = {
    updateAction(article).asUIO.as(article)
  }

  override def updateWithTags(article: Article, tagList: TagList): UIO[Article] = {
    (for {
      _ <- updateAction(article)
      _ <- tags.filter(_.articleId === article.id.bind).delete
      _ <- tags ++= tagList.tags.map((article.id, _))
    } yield article)
      .transactionally
      .asUIO
  }

  private def updateAction(article: Article): DBIO[Int] = {
    articles
      .filter(_.id === article.id.bind)
      .update(article)
  }

  override def delete(slug: String): IO[NoArticle, Unit] = {
    articles.filter(_.slug === slug.bind).delete.asUIO.unit
  }

  override def findBySlug(slug: String): IO[NoArticle, Article] = {
    articles.filter(_.slug === slug.bind).result.headOption.asIO(NoArticle())
  }

  override def list(criteria: FilterCriteria, pagination: Pagination): UIO[Seq[Article]] = {
    articles
      .filterOpt(criteria.tag) { case (article, tag) =>
        tags.filter(t => t.articleId === article.id && t.tag === tag.bind).exists
      }
      .filterOpt(criteria.authorUsername) { case (article, username) =>
        article.user.filter(_.username === username.bind).exists
      }
      .filterOpt(criteria.favoritedBy) { case (article, favoritedByUsername) =>
        (for {
          relation <- relations if relation.articleId === article.id
          user <- relation.user if user.username === favoritedByUsername.bind
        } yield user).exists
      }
      .take(pagination.limit)
      .drop(pagination.offset)
      .sortBy(_.createAt.desc)
      .result
      .asUIO
  }

  override def listByFollowees(followees: Seq[UserId], pagination: Pagination): UIO[Seq[Article]] = {
    articles
      .filter(_.userId inSet followees)
      .take(pagination.limit)
      .drop(pagination.offset)
      .sortBy(_.createAt.desc)
      .result
      .asUIO
  }

}
