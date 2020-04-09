package realworld.zio.validation

import realworld.zio.data.CreateCommentRequest
import realworld.zio.error.InvalidCommentBody
import zio.IO

trait CommentValidator {

  def validateCreateCommentRequest(request: CreateCommentRequest): IO[InvalidCommentBody, CreateCommentRequest]

}

class CommentValidatorImpl extends CommentValidator {

  def validateCreateCommentRequest(request: CreateCommentRequest): IO[InvalidCommentBody, CreateCommentRequest] = {
    IO.fail(InvalidCommentBody())
      .when(request.body.isEmpty)
      .as(request)
  }

}
