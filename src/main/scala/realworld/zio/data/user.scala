package realworld.zio.data

case class LoginRequest(email: String, password: String)

case class CreateUserRequest(email: String,
                             username: String,
                             password: String,
                             bio: Option[String],
                             image: Option[String])

case class UpdateUserRequest(email: Option[String],
                             username: Option[String],
                             password: Option[String],
                             bio: Option[String],
                             image: Option[String])
