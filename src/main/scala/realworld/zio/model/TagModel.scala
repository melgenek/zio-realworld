package realworld.zio.model

case class TagList(tags: Seq[String])

trait TagModel extends ArticleModel {

  import profile.api._

  class Tags(t: Tag) extends Table[(ArticleId, String)](t, "tags") {
    def articleId = column[ArticleId]("article_id")

    def tag = column[String]("tag")

    def pk = primaryKey("pk", (articleId, tag))

    def article = foreignKey("article_fk", articleId, articles)(_.id, onDelete = ForeignKeyAction.Cascade)

    def * = (articleId, tag)
  }

  val tags = TableQuery[Tags]

}
