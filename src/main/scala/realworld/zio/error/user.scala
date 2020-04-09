package realworld.zio.error

sealed trait UserError

case class InvalidCredentials() extends UserError
case class NoUser() extends UserError

sealed trait UserValidationError extends UserError
case class InvalidEmail() extends UserValidationError
case class EmailExists() extends UserValidationError
case class InvalidUsername(min: Int, max: Int) extends UserValidationError
case class UsernameExists() extends UserValidationError
case class InvalidPassword(min: Int, max: Int) extends UserValidationError
