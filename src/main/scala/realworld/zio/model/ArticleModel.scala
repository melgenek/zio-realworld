package realworld.zio.model

import java.time.Instant

import slick.lifted.MappedTo

case class ArticleId(value: Long) extends AnyVal with MappedTo[Long]

case class Article(id: ArticleId = ArticleId(-1),
                   slug: String,
                   title: String,
                   description: String,
                   body: String,
                   createdAt: Instant,
                   updatedAt: Instant,
                   userId: UserId)

trait ArticleModel extends UserModel {

  import profile.api._

  class Articles(t: Tag) extends Table[Article](t, "articles") {
    def id = column[ArticleId]("id", O.PrimaryKey, O.AutoInc)

    def slug = column[String]("slug", O.Unique)

    def title = column[String]("title")

    def description = column[String]("description")

    def body = column[String]("body")

    def createAt = column[Instant]("create_at")

    def updatedAt = column[Instant]("create_at")

    def userId = column[UserId]("user_id")

    def user = foreignKey("user_fk", userId, users)(_.id)

    //    def * = (id, slug, title, description, body, createAt, updatedAt, userId).mapTo[Article]
    def * = (id, slug, title, description, body, createAt, updatedAt, userId) <> (Article.tupled, Article.unapply)
  }

  val articles = TableQuery[Articles]

}
