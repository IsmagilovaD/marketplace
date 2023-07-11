package auth

import derevo.circe.{decoder, encoder}
import derevo.derive
import sttp.tapir.derevo.schema
import user.domain.UserType

sealed abstract class AuthError(msg: String) extends Throwable(msg)

case object CredentialsError extends AuthError("Wrong credentials")

@derive(encoder, decoder, schema)
case class AuthenticationError() extends AuthError("Authorization failed")


@derive(encoder, decoder, schema)
case class AuthorizationError(role: UserType.Value, required: Seq[UserType.Value])
  extends AuthError(s"Insufficient roles. Required: $required, got: $role")

case class JwtTokenParseError(errorMessage: String)
  extends AuthError(s"Invalid token value with message $errorMessage")
