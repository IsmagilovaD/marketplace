package user.controller

import cats.effect.IO
import sttp.tapir.server.ServerEndpoint
import user.service.UserStorage

trait UserController {
  def userRegistration: ServerEndpoint[Any, IO]

  def userAuthorization: ServerEndpoint[Any, IO]

  def allUserEndpoints: List[ServerEndpoint[Any, IO]]
}

object UserController {
  final case class Impl(storage: UserStorage) extends UserController {
    override def userRegistration: ServerEndpoint[Any, IO] =
      endpoints.registration.serverLogic {
      case (userRegistration, ctx) => storage.userRegistration(userRegistration).run(ctx)
    }

    override def userAuthorization: ServerEndpoint[Any, IO] =
      endpoints.authorization.serverLogic{
        case (userAuth, ctx) => storage.userAuthorization(userAuth).run(ctx)
      }

    override def allUserEndpoints: List[ServerEndpoint[Any, IO]] = List(
      userRegistration,
      userAuthorization
    )

  }
  def make(storage: UserStorage): UserController = new Impl(storage)
}
