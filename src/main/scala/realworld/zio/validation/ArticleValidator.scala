package realworld.zio.validation

import realworld.zio.data.{CreateArticleRequest, UpdateArticleRequest}
import realworld.zio.error.{ArticleValidationError, InvalidArticleBody, InvalidDescription, InvalidTitle, TitleExists}
import realworld.zio.service.ArticleService
import zio.IO

trait ArticleValidator {

  def validateCreateArticleRequest(request: CreateArticleRequest): IO[::[ArticleValidationError], CreateArticleRequest]

  def validateUpdateArticleRequest(request: UpdateArticleRequest): IO[::[ArticleValidationError], UpdateArticleRequest]

}

class ArticleValidatorImpl(articleService: ArticleService) extends ArticleValidator {

  override def validateCreateArticleRequest(request: CreateArticleRequest): IO[::[ArticleValidationError], CreateArticleRequest] = {
    validate(request)(
      validateTitle(request.title),
      checkTileIsAvailable(request.title),
      validateDescription(request.description),
      validateBody(request.body)
    )
  }

  override def validateUpdateArticleRequest(request: UpdateArticleRequest): IO[::[ArticleValidationError], UpdateArticleRequest] = {
    validate(request)(
      validateOption(request.title)(validateTitle),
      validateOption(request.title)(checkTileIsAvailable),
      validateOption(request.description)(validateDescription),
      validateOption(request.body)(validateBody)
    )
  }

  private def validateTitle(title: String): IO[InvalidTitle, Unit] = {
    validateLength(title)(1, 200)(InvalidTitle.apply)
  }

  private def checkTileIsAvailable(title: String): IO[TitleExists, Unit] = {
    articleService
      .findByTitle(title)
      .catchAll(_ => IO.unit)
      .flatMap(_ => IO.fail(TitleExists()))
  }

  private def validateDescription(description: String): IO[InvalidDescription, Unit] = {
    validateLength(description)(1, 500)(InvalidDescription.apply)
  }

  private def validateBody(body: String): IO[InvalidArticleBody, Unit] = {
    IO.fail(InvalidArticleBody()).when(body.isEmpty)
  }

}
