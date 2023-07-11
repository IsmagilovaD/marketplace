package user

import derevo.circe.{decoder, encoder}
import derevo.derive
import doobie.Read
import io.estatico.newtype.macros.newtype
import sttp.tapir.CodecFormat.TextPlain
import sttp.tapir.{Codec, Schema}
import tofu.logging.derivation.loggable

package object domain {
  @derive( loggable, encoder, decoder)
  @newtype
  case class UserID(value: Int)

  object UserID {
    implicit val doobieRead: Read[UserID] = Read[Int].map(UserID(_))
    implicit val schema: Schema[UserID] =
      Schema.schemaForInt.map(l => Some(UserID(l)))(_.value)
    implicit val codec: Codec[String, UserID, TextPlain] =
      Codec.int.map(UserID(_))(_.value)
  }

  @derive(loggable,  encoder, decoder)
  @newtype
  case class Username(value: String)

  object Username {
    implicit val doobieRead: Read[Username] = Read[String].map(Username(_))
    implicit val schema: Schema[Username] =
      Schema.schemaForString.map(n => Some(Username(n)))(_.value)
    implicit val codec: Codec[String, Username, TextPlain] =
      Codec.string.map(Username(_))(_.value)
  }

  @derive(loggable, encoder, decoder)
  @newtype
  case class Login(value: String)
  object Login {
    implicit val doobieRead: Read[Login] = Read[String].map(Login(_))
    implicit val schema: Schema[Login] =
      Schema.schemaForString.map(n => Some(Login(n)))(_.value)
  }

  @derive(loggable, encoder, decoder)
  @newtype
  case class PhoneNumber(value: String)
  object PhoneNumber {
    implicit val doobieRead: Read[PhoneNumber] = Read[String].map(PhoneNumber(_))
    implicit val schema: Schema[PhoneNumber] =
      Schema.schemaForString.map(n => Some(PhoneNumber(n)))(_.value)
  }

  @derive(loggable, encoder, decoder)
  @newtype
  case class Email(value: String)
  object Email {
    implicit val doobieRead: Read[Email] = Read[String].map(Email(_))
    implicit val schema: Schema[Email] =
      Schema.schemaForString.map(n => Some(Email(n)))(_.value)
  }
  @derive(loggable, encoder, decoder)
  @newtype
  case class UserPassword(value: String)
  object UserPassword {
    implicit val doobieRead: Read[UserPassword] = Read[String].map(UserPassword(_))
    implicit val schema: Schema[UserPassword] =
      Schema.schemaForString.map(n => Some(UserPassword(n)))(_.value)
  }
  @derive(loggable,encoder, decoder)
  @newtype
  case class AccountNumber(value: Long)
  object AccountNumber {
    implicit val doobieRead: Read[AccountNumber] = Read[Long].map(AccountNumber(_))
    implicit val schema: Schema[AccountNumber] =
      Schema.schemaForLong.map(l => Some(AccountNumber(l)))(_.value)
  }

  @derive(loggable,encoder, decoder)
  @newtype
  case class BonusAmount(value: Int)

  object BonusAmount {
    implicit val doobieRead: Read[AccountNumber] = Read[Int].map(AccountNumber(_))
    implicit val codec: Codec[String, BonusAmount, TextPlain] =
      Codec.int.map(BonusAmount(_))(_.value)
    implicit val schema: Schema[BonusAmount] =
      Schema.schemaForInt.map(n => Some(BonusAmount(n)))(_.value)
  }

  @derive(loggable, encoder, decoder)
  @newtype
  case class ShoppingCartId(value: Long)

  object ShoppingCartId {
    implicit val doobieRead: Read[ShoppingCartId] = Read[Long].map(ShoppingCartId(_))
    implicit val schema: Schema[ShoppingCartId] =
      Schema.schemaForLong.map(l => Some(ShoppingCartId(l)))(_.value)
  }

  @derive(loggable, encoder, decoder)
  @newtype
  case class JwtToken(value: String)
  object JwtToken{
    implicit val codec: Codec[String, JwtToken, TextPlain] =
      Codec.string.map(JwtToken(_))(_.value)
  }

}
