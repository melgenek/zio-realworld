package realworld.zio.model

import slick.dbio.DBIO
import slick.jdbc.{JdbcBackend, JdbcProfile}
import zio.{IO, Task, UIO}

trait SlickModel {

  val profile: JdbcProfile

  val db: JdbcBackend.Database

  implicit class DBIOOps[A](dbio: DBIO[A]) {
    def asUIO: UIO[A] = {
      Task
        .fromFuture { _ => db.run(dbio) }
        .refineToOrDie
    }
  }

  implicit class DBIOOptionOps[A](dbio: DBIO[Option[A]]) {
    def asIO[E](e: E): IO[E, A] = {
      Task
        .fromFuture { _ => db.run(dbio) }
        .refineToOrDie
        .map(_.toRight(e))
        .absolve
    }

  }

}
