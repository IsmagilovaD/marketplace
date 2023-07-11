package user.domain

import derevo.circe.{decoder, encoder}
import derevo.derive
import product.domain.CartProduct
import sttp.tapir.Schema
import tofu.logging.derivation.loggable

@derive(loggable, encoder, decoder)
final case class ShoppingCart(id: ShoppingCartId,
                              buyerId: UserID
//                              cartProducts: List[CartProduct]
                              )
object ShoppingCart{
  implicit val schema: Schema[ShoppingCart] = Schema.derived

}