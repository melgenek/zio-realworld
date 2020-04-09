package realworld.zio.error

sealed trait ArticleError

case class NoArticle() extends ArticleError

sealed trait ArticleValidationError extends ArticleError
case class InvalidTitle(min: Int, max: Int) extends ArticleValidationError
case class TitleExists() extends ArticleValidationError
case class InvalidDescription(min: Int, max: Int) extends ArticleValidationError
case class InvalidArticleBody() extends ArticleValidationError
