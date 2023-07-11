package product.domain

import derevo.circe.{decoder, encoder}
import derevo.derive
import sttp.tapir.Schema
import tofu.logging.derivation.{hidden, loggable}
import user.domain.{ShoppingCart, ShoppingCartId, UserID}

@derive(loggable, encoder, decoder)
final case class Product(id: ProductID,
                         sellerId: UserID,
                         productName: ProductName,
                         price: Price,
                         category: ProductCategory.Value,
                         description: Description
                        )

object Product {
  implicit val schema: Schema[Product] = Schema.derived
}

@derive(loggable, encoder, decoder)
final case class CartProduct(productID: ProductID,
                             shoppingCartId: ShoppingCartId,
                             quantity: Quantity,
                            )
object CartProduct {
  implicit val schema: Schema[CartProduct] = Schema.derived
}

@derive(loggable, encoder, decoder)
final case class CreateProduct(productName: ProductName,
                               category: ProductCategory.Value,
                               price: Price,
                               description: Description)

object CreateProduct {
  implicit val schema: Schema[CreateProduct] = Schema.derived
}

@derive(loggable, encoder, decoder)
final case class UpdateProduct(id: ProductID,
                               productName: ProductName,
                               category: ProductCategory.Value,
                               price: Price,
                               description: Description)

object UpdateProduct {
  implicit val schema: Schema[UpdateProduct] = Schema.derived
}