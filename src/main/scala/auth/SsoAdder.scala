package auth

import cats.Monad
import cats.implicits.toFunctorOps
import io.circe.{Decoder, Encoder}
import sttp.model.StatusCode
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.{Endpoint, oneOf, oneOfVariant}
import user.domain.{JwtToken, UserJwtInfo, UserType}

trait SsoAdder[F[_], SECURITY_INPUT] {
  def add[INPUT, ERROR_OUTPUT: Encoder : Decoder, OUTPUT, R]
  (
    endpoint: Endpoint[SECURITY_INPUT, INPUT, ERROR_OUTPUT, OUTPUT, R],
    serverLogic: UserJwtInfo => INPUT => F[Either[ERROR_OUTPUT, OUTPUT]],
    roles: Seq[UserType.Value]
  ): ServerEndpoint[R, F]
}

class SsoAdderImpl[F[_] : Monad](auth: Auth[F])
  extends SsoAdder[F, JwtToken] {
  def add[INPUT, ERROR_OUTPUT: Encoder : Decoder, OUTPUT, R]
  (
    endpoint: Endpoint[JwtToken, INPUT, ERROR_OUTPUT, OUTPUT, R],
    serverLogic: UserJwtInfo => INPUT => F[Either[ERROR_OUTPUT, OUTPUT]],
    roles: Seq[UserType.Value]
  ): ServerEndpoint[R, F] = endpoint
    .errorOutEither[AuthError](
      oneOf[AuthError](
        oneOfVariant(
          StatusCode.Forbidden,
          jsonBody[AuthenticationError].description("Authentication failed")
        ),
        oneOfVariant(
          StatusCode.Unauthorized,
          jsonBody[AuthorizationError].description("Authorization failed")
        )
      )
    )
    .serverSecurityLogic[UserJwtInfo, F](token =>
      auth
        .validateJwtToken(token)
        .flatMap[AuthError, UserJwtInfo](user => auth.authorizeWithRoles(user, roles))
        .leftMap[Either[ERROR_OUTPUT, AuthError]](err => Right(err))
        .value
    )
    .serverLogic(user =>
      input =>
        serverLogic(user)(input).map {
          case Left(err) => Left(Left(err))
          case Right(res) => Right(res)
        }
    )

}
