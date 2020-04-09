package realworld.zio

import zio.IO

package object validation {

  def validate[E, A](a: A)(validator: IO[E, _], anotherValidators: IO[E, _]*): IO[::[E], A] = {
    IO.validate(validator +: anotherValidators)(identity).as(a)
  }

  def validateOption[E, A](a: Option[A])(validator: A => IO[E, _]): IO[E, _] = {
    a.map(validator).getOrElse(IO.unit)
  }

  def validateLength[E](a: String)(min: Int, max: Int)(createError: (Int, Int) => E): IO[E, Unit] = {
    IO.fail(createError(min, max)).when(a.length < min && a.length > max)
  }

}
