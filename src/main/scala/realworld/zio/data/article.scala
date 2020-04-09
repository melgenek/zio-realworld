package realworld.zio.data

import realworld.zio.model.TagList

case class CreateArticleRequest(title: String,
                                description: String,
                                body: String,
                                tagList: Option[TagList])

case class UpdateArticleRequest(title: Option[String],
                                description: Option[String],
                                body: Option[String],
                                tagList: Option[TagList])
