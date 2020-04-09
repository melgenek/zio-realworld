package realworld.zio.model

trait ArticleRelationModel extends UserModel with ArticleModel {

  import profile.api._

  class ArticleRelations(t: Tag) extends Table[(UserId, ArticleId)](t, "relations") {
    def userId = column[UserId]("user_id")

    def articleId = column[ArticleId]("article_id")

    def pk = primaryKey("pk", (userId, articleId))

    def user = foreignKey("user_fk", userId, users)(_.id, onDelete = ForeignKeyAction.Cascade)

    def article = foreignKey("article_fk", articleId, articles)(_.id, onDelete = ForeignKeyAction.Cascade)

    def * = (userId, articleId)
  }

  val relations = TableQuery[ArticleRelations]

}
