package user.domain

import derevo.circe.{decoder, encoder}
import derevo.derive
import doobie.postgres.implicits.pgEnum
import io.circe._
import product.domain.ProductCategory
import sttp.tapir.Schema
import tofu.logging.Loggable
import tofu.logging.derivation.loggable
import user.domain.UserType.UserType


object UserType extends Enumeration {
  type UserType = Value
  val Buyer, Seller = Value
  implicit val MyEnumMeta = pgEnum(UserType, "user_type")
  implicit val productCategoryLoggable: Loggable[UserType.Value] = Loggable.empty


  implicit val userTypeDecoder: Decoder[UserType.Value] = Decoder.decodeEnumeration(UserType)
  implicit val userTypeEncoder: Encoder[UserType.Value] = Encoder.encodeEnumeration(UserType)
}

@derive(loggable, encoder, decoder)
final case class UserRegistration(username: Username,
                                  login: Login,
                                  password: UserPassword,
                                  userType: UserType.Value)

object UserRegistration {
  implicit val schema: Schema[UserRegistration] = Schema.derived
}

@derive(loggable, encoder, decoder)
final case class CreateUser(username: Username,
                            phoneNumber: Option[PhoneNumber],
                            email: Option[Email],
                            password: UserPassword,
                            userType: UserType.Value)

object CreateUser {
  implicit val schema: Schema[CreateUser] = Schema.derived
}

@derive(loggable, encoder, decoder)
final case class UserAuthorization(login: Login, password: UserPassword)

object UserAuthorization {
  implicit val schema: Schema[UserAuthorization] = Schema.derived
}

@derive(loggable, encoder, decoder)
final case class UserAuthLogin(phoneNumber: Option[PhoneNumber],
                               email: Option[Email])

object UserAuthLogin {
  implicit val schema: Schema[UserAuthLogin] = Schema.derived
}

@derive(loggable, encoder, decoder)
final case class UserInfo(id: UserID,
                          password: UserPassword,
                          userType: UserType.Value)

object UserInfo {
  implicit val schema: Schema[UserInfo] = Schema.derived
}

@derive(loggable, encoder, decoder)
final case class UserJwtInfo(id: UserID,
                             userType: UserType.Value)

object UserJwtInfo {
  implicit val schema: Schema[UserInfo] = Schema.derived
}

abstract class User(id: UserID,
                    username: Username,
                    password: UserPassword,
                    phoneNumber: Option[PhoneNumber],
                    email: Option[Email],
                    bonusAccount: AccountNumber)

