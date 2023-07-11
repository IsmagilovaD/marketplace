package product.domain

import cats.tagless.Derive
import derevo.derive
import doobie.postgres.implicits.pgEnum
import io.circe._
import tofu.logging.Loggable


object OrderStatus extends Enumeration {
  type OrderStatus = Value
  val New, InDelivery, Received, Cancelled = Value

  implicit val MyEnumMeta = pgEnum(OrderStatus, "order_status")
  implicit val productCategoryLoggable: Loggable[OrderStatus.Value] = Loggable.empty


  implicit val genderDecoder: Decoder[OrderStatus.Value] = Decoder.decodeEnumeration(OrderStatus)
  implicit val genderEncoder: Encoder[OrderStatus.Value] = Encoder.encodeEnumeration(OrderStatus)
}
