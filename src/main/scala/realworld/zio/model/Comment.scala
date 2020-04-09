package realworld.zio.model

import java.time.Instant

case class Comment(id: Long,
                   body: String,
                   createdAt: Instant,
                   userId: Long,
                   articleId: Long)
