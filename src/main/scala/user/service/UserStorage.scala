package user.service

import cats.implicits.{catsSyntaxApplicativeId, catsSyntaxEitherId}
import cats.syntax.applicativeError._
import domain.{IOWithRequestContext, RequestContext}
import domain.errors.{AppError, InternalError, InvalidLogin, NoSuchUserWithLogin, WrongPassword}
import doobie.Transactor
import doobie.implicits._
import io.circe.parser._
import io.circe.syntax.EncoderOps
import org.mindrot.jbcrypt.BCrypt
import pdi.jwt.{JwtAlgorithm, JwtCirce, JwtClaim}
import tofu.logging.Logging
import user.dao.UserSql
import user.domain.{CreateUser, Email, JwtToken, PhoneNumber, UserAuthLogin, UserAuthorization, UserInfo, UserJwtInfo, UserPassword, UserRegistration}

import scala.util.{Success, Try}
import scala.util.matching.Regex

trait UserStorage {
  def userRegistration(userRegistration: UserRegistration): IOWithRequestContext[Either[AppError, Unit]]

  def userAuthorization(userAuthorization: UserAuthorization): IOWithRequestContext[Either[AppError, String]]

}

object UserStorage {
  final case class Impl(sql: UserSql,
                        transactor: Transactor[IOWithRequestContext],
                        secretKey: String,
                       ) extends UserStorage {

    val emailRegex: Regex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$".r

    val russianPhoneNumberRegex: Regex = "^\\+7\\d{10}$".r

    def isValidEmail(email: String): Boolean = emailRegex.findFirstMatchIn(email).isDefined

    def isValidPhoneNumber(phoneNumber: String): Boolean = russianPhoneNumberRegex
      .findFirstMatchIn(phoneNumber).isDefined

    override def userRegistration(userRegistration: UserRegistration)
    : IOWithRequestContext[Either[AppError, Unit]] =
      if (!isValidPhoneNumber(userRegistration.login.value) && !isValidEmail(userRegistration.login.value)) {
        val invalidLogin: AppError = InvalidLogin(userRegistration.login.value)
        invalidLogin.asLeft[Unit].pure[IOWithRequestContext]
      }
      else {
        val user = createUserFromUserRegistration(userRegistration)
        sql.addUser(user).transact(transactor).attempt.map {
          case Left(th) => InternalError(th).asLeft[Unit]
          case Right(Left(error)) => error.asLeft[Unit]
          case Right(_) => Right(())
        }
      }

    def createUserFromUserRegistration(userRegistration: UserRegistration): CreateUser = {
      val passwordHash = BCrypt.hashpw(userRegistration.password.value, BCrypt.gensalt())
      if (isValidEmail(userRegistration.login.value)) {
        CreateUser(
          userRegistration.username,
          None,
          Some(Email(userRegistration.login.value)),
          UserPassword(passwordHash),
          userRegistration.userType)
      } else {
        CreateUser(
          userRegistration.username,
          Some(PhoneNumber(userRegistration.login.value)),
          None,
          UserPassword(passwordHash),
          userRegistration.userType)
      }
    }

    def validatedUserAuthLogin(login: String): UserAuthLogin = {
      if (isValidEmail(login)) {
        UserAuthLogin(None, Option(Email(login)))
      } else {
        UserAuthLogin(Option(PhoneNumber(login)), None)
      }
    }

    override def userAuthorization(userAuthorization: UserAuthorization)
    : IOWithRequestContext[Either[AppError, String]] =
      if (!isValidPhoneNumber(userAuthorization.login.value) && !isValidEmail(userAuthorization.login.value)) {
        val invalidLogin: AppError = InvalidLogin(userAuthorization.login.value)
        invalidLogin.asLeft[String].pure[IOWithRequestContext]
      }
      else {
        val validatedLogin = validatedUserAuthLogin(userAuthorization.login.value)
        sql.getUserInfo(validatedLogin).transact(transactor).attempt.map {
          case Left(th) => InternalError(th).asLeft[String]
          case Right(None) => NoSuchUserWithLogin(userAuthorization.login.value).asLeft[String]
          case Right(Some(userInfo)) =>
            if (BCrypt.checkpw(userAuthorization.password.value, userInfo.password.value)) {
              val claim = JwtClaim(content = UserJwtInfo(userInfo.id, userInfo.userType).asJson.noSpaces)
              JwtCirce.encode(claim, secretKey, JwtAlgorithm.HS256).asRight[AppError]
            } else WrongPassword().asLeft[String]
        }
      }

  }

  private final class LoggingImpl(storage: UserStorage)
                                 (implicit logging: Logging[IOWithRequestContext]) extends UserStorage {

    private def surroundWithLogs[Error, Res](
                                              inputLog: String
                                            )(errorOutputLog: Error => (String, Option[Throwable]))(
                                              successOutputLog: Res => String
                                            )(
                                              io: IOWithRequestContext[Either[Error, Res]]
                                            ): IOWithRequestContext[Either[Error, Res]] =
      for {
        _ <- logging.info(inputLog)
        res <- io
        _ <- res match {
          case Left(error) => {
            val (msg, cause) = errorOutputLog(error)
            cause.fold(logging.error(msg))(cause => logging.error(msg, cause))
          }
          case Right(result) => logging.info(successOutputLog(result))
        }
      } yield res

    override def userRegistration(userRegistration: UserRegistration)
    : IOWithRequestContext[Either[AppError, Unit]] =
      surroundWithLogs[AppError, Unit](s"Registration User with params $userRegistration") {
        error => (s"Error while registration User: ${error.message}", error.cause)
      } { product =>
        s"Registered User $product"
      }(storage.userRegistration(userRegistration))

    override def userAuthorization(userAuthorization: UserAuthorization)
    : IOWithRequestContext[Either[AppError, String]] =
      surroundWithLogs[AppError, String](s"Authentication User with params $userAuthorization") {
        error => (s"Error while Authentication User: ${error.message}", error.cause)
      } { user =>
        s"Authenticated User $user"
      }(storage.userAuthorization(userAuthorization))
  }

  def make(
            sql: UserSql,
            transactor: Transactor[IOWithRequestContext],
            secretKey: String
          ): UserStorage = {
    implicit val logs =
      Logging.Make
        .contextual[IOWithRequestContext, RequestContext]
        .forService[UserStorage]
    val storage = new Impl(sql, transactor, secretKey)
    new LoggingImpl(storage)
  }
}
