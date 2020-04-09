package realworld.zio.error

sealed trait CommentError

case class NoComment() extends CommentError

sealed trait CommentValidationError extends CommentError
case class InvalidCommentBody() extends CommentValidationError
