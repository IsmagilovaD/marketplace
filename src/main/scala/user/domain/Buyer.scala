package user.domain

import derevo.circe.{decoder, encoder}
import derevo.derive
import product.domain.Order
import sttp.tapir.Schema
import tofu.logging.derivation.loggable

@derive(loggable, encoder, decoder)
final case class Buyer(id: UserID,
                       username: Username,
                       password: UserPassword,
                       phoneNumber: Option[PhoneNumber],
                       email: Option[Email],
                       bonusAccount: AccountNumber,
                       shoppingCart: ShoppingCartId)
  extends User(id, username, password, phoneNumber, email, bonusAccount)

@derive(loggable, encoder, decoder)
final case class CreateBuyer(username: Username,
                             password: UserPassword,
                             phoneNumber: PhoneNumber,
                             email: Email)
object CreateBuyer {
  implicit val schema: Schema[CreateBuyer] = Schema.derived
}

