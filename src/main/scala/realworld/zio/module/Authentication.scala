package realworld.zio.module

import realworld.zio.model.User

trait Authentication {
  val currentUserEmail: String
}

trait DetailedAuthentication extends Authentication {
  val user: User
}
