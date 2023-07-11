package product.domain

import derevo.circe.{decoder, encoder}
import derevo.derive
import doobie.Read
import sttp.tapir.Schema
import tofu.logging.derivation.loggable
import user.domain.UserID

@derive(loggable, encoder, decoder)
final case class Order(id: OrderID,
                       buyerId: UserID,
                       totalPrice: Price,
                       orderDate: OrderDate,
                       shippingAddress: ShippingAddress)

object Order {
  implicit val schema: Schema[Order] = Schema.derived

}

@derive(loggable, encoder, decoder)
final case class OrderItem(id: OrderItemID,
                           productId: ProductID,
                           orderId: OrderID,
                           orderStatus: OrderStatus.Value,
                           price: Price,
                           quantity: Quantity)

object OrderItem {
  implicit val schema: Schema[OrderItem] = Schema.derived
  implicit val listSchema: Schema[List[OrderItem]] = Schema.derived

}