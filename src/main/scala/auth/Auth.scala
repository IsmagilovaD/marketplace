package auth

import cats.Applicative
import cats.data.EitherT
import io.circe.parser
import pdi.jwt.{JwtAlgorithm, JwtCirce}
import user.domain.{JwtToken, UserJwtInfo, UserType}

trait Auth[F[_]] {
  def validateJwtToken(token: JwtToken): EitherT[F, AuthError, UserJwtInfo]

  def authorizeWithRoles(user: UserJwtInfo, roles: Seq[UserType.Value]): EitherT[F, AuthError, UserJwtInfo]
}

class AuthImpl[F[_] : Applicative](secretKey: String) extends Auth[F] {
  override def validateJwtToken(token: JwtToken): EitherT[F, AuthError, UserJwtInfo] = {
    val userInfo = for {
      jwt <- JwtCirce.decode(token.value,secretKey, Seq(JwtAlgorithm.HS256)).toEither
      json <- parser.parse(jwt.content)
      userInfo <- json.as[UserJwtInfo]
    } yield userInfo
    userInfo match {
      case Left(value) => EitherT.leftT(JwtTokenParseError(value.getMessage))
      case Right(userInfo) => EitherT.rightT(userInfo)
    }
  }

  override def authorizeWithRoles(user: UserJwtInfo, roles: Seq[UserType.Value]): EitherT[F, AuthError, UserJwtInfo] = {
    if (roles.contains(user.userType))
    EitherT.rightT(user)
    else EitherT.leftT(AuthorizationError(user.userType, roles))
  }
}

