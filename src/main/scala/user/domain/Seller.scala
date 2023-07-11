package user.domain

import derevo.circe.{decoder, encoder}
import derevo.derive
import product.domain.{Product,OrderItem}
import sttp.tapir.Schema
import tofu.logging.derivation.loggable

@derive( encoder, decoder)
final case class Seller(id: UserID,
                        username: Username,
                        password: UserPassword,
                        phoneNumber: Option[PhoneNumber],
                        email: Option[Email],
                        bonusAccount: AccountNumber,
                        orders: List[OrderItem],
                        products: List[Product])
  extends User(id, username, password, phoneNumber, email, bonusAccount)
object Seller {
  implicit val schema: Schema[Seller] = Schema.derived
}


final case class CreateSeller(username: Username,
                              password: UserPassword,
                              phoneNumber: PhoneNumber,
                              email: Email)
object CreateSeller {
  implicit val schema: Schema[CreateSeller] = Schema.derived
}
