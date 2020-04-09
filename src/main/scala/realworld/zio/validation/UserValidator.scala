package realworld.zio.validation

import org.hazlewood.connor.bottema.emailaddress.EmailAddressValidator
import realworld.zio.data.{CreateUserRequest, UpdateUserRequest}
import realworld.zio.error.{EmailExists, InvalidEmail, InvalidPassword, InvalidUsername, UserValidationError, UsernameExists}
import realworld.zio.service.UserService
import zio.IO

trait UserValidator {

  def validateCreateUserRequest(request: CreateUserRequest): IO[::[UserValidationError], CreateUserRequest]

  def validateUpdateUserRequest(request: UpdateUserRequest): IO[::[UserValidationError], UpdateUserRequest]

}

class UserValidatorImpl(userService: UserService) extends UserValidator {

  override def validateCreateUserRequest(request: CreateUserRequest): IO[::[UserValidationError], CreateUserRequest] = {
    validate(request)(
      validateEmail(request.email),
      checkEmailIsAvailable(request.email),
      validateUsername(request.username),
      checkUsernameIsAvailable(request.username),
      validatePassword(request.password)
    )
  }

  override def validateUpdateUserRequest(request: UpdateUserRequest): IO[::[UserValidationError], UpdateUserRequest] = {
    validate(request)(
      validateOption(request.email)(validateEmail),
      validateOption(request.email)(checkEmailIsAvailable),
      validateOption(request.username)(validateUsername),
      validateOption(request.username)(checkUsernameIsAvailable),
      validateOption(request.password)(validatePassword)
    )
  }

  private def validateEmail(email: String): IO[InvalidEmail, Unit] = {
    IO.fail(InvalidEmail()).when(!EmailAddressValidator.isValid(email))
  }

  private def checkEmailIsAvailable(email: String): IO[EmailExists, Unit] = {
    userService
      .findByEmail(email).ignore
      .flatMap(_ => IO.fail(EmailExists()))
  }

  private def validatePassword(password: String): IO[InvalidPassword, Unit] = {
    validateLength(password)(8, 20)(InvalidPassword.apply)
  }

  private def validateUsername(username: String): IO[InvalidUsername, Unit] = {
    validateLength(username)(4, 15)(InvalidUsername.apply)
  }

  private def checkUsernameIsAvailable(username: String): IO[UsernameExists, Unit] = {
    userService
      .findByUsername(username).ignore
      .flatMap(_ => IO.fail(UsernameExists()))
  }

}
