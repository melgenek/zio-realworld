package realworld.zio.service

import java.time.Instant

import com.softwaremill.quicklens._
import io.scalaland.chimney.dsl._
import realworld.zio.dao.ArticleDao
import realworld.zio.dao.ArticleDao.{FilterCriteria, Pagination}
import realworld.zio.data.{CreateArticleRequest, UpdateArticleRequest}
import realworld.zio.error.NoArticle
import realworld.zio.model.{Article, UserId}
import realworld.zio.module.DetailedAuthentication
import zio.{IO, UIO, URIO}

trait ArticleService {

  def create(request: CreateArticleRequest): URIO[DetailedAuthentication, Article]

  def update(slug: String, request: UpdateArticleRequest): IO[NoArticle, Article]

  def delete(slug: String): IO[NoArticle, Unit]

  def findByTitle(title: String): IO[NoArticle, Article]

  def findBySlug(slug: String): IO[NoArticle, Article]

  def list(criteria: FilterCriteria, pagination: Pagination): UIO[Seq[Article]]

  def feed(followees: Seq[UserId], pagination: Pagination): URIO[DetailedAuthentication, Seq[Article]]

}

class ArticleServiceImpl(articleDao: ArticleDao) extends ArticleService {

  override def create(request: CreateArticleRequest): URIO[DetailedAuthentication, Article] = {
    for {
      user <- URIO.access[DetailedAuthentication](_.user)
      now = Instant.now()
      article = request.into[Article]
        .withFieldConst(_.userId, user.id)
        .withFieldComputed(_.slug, a => titleToSlug(a.title))
        .withFieldConst(_.createdAt, now)
        .withFieldConst(_.updatedAt, now)
        .transform
      storedArticle <- request.tagList
        .map(tagList => articleDao.createWithTags(article, tagList))
        .getOrElse(articleDao.create(article))
    } yield storedArticle
  }

  override def update(slug: String, request: UpdateArticleRequest): IO[NoArticle, Article] = {
    for {
      existingArticle <- articleDao.findBySlug(slug)
      patchedArticle = existingArticle.using(request).ignoreRedundantPatcherFields.patch
      updatedArticle = patchedArticle
        .modify(_.updatedAt).setTo(Instant.now())
        .modify(_.slug).setToIfDefined(request.title.map(titleToSlug))
      storedArticle <- request.tagList
        .map(tagList => articleDao.updateWithTags(updatedArticle, tagList))
        .getOrElse(articleDao.update(updatedArticle))
    } yield storedArticle
  }

  override def delete(slug: String): IO[NoArticle, Unit] = {
    articleDao.delete(slug)
  }

  override def findByTitle(title: String): IO[NoArticle, Article] = {
    articleDao.findBySlug(titleToSlug(title))
  }

  override def findBySlug(slug: String): IO[NoArticle, Article] = {
    articleDao.findBySlug(slug)
  }

  override def list(criteria: FilterCriteria, pagination: Pagination): UIO[Seq[Article]] = {
    articleDao.list(criteria, pagination)
  }

  override def feed(followees: Seq[UserId], pagination: Pagination): UIO[Seq[Article]] = {
    articleDao.listByFollowees(followees, pagination)
  }

  private def titleToSlug(title: String): String = {
    title.toLowerCase.replaceAll("[^a-zA-Z\\s]+", "").split("\\s").mkString("_")
  }

}
