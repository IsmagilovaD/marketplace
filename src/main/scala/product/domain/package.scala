package product


import derevo.circe.{decoder, encoder}
import derevo.derive
import doobie.Read
import io.estatico.newtype.macros.newtype
import sttp.tapir.CodecFormat.TextPlain
import sttp.tapir.{Codec, Schema}
import tofu.logging.derivation.loggable

import java.time.{Instant}

package object domain {
  @derive(loggable, encoder, decoder)
  @newtype
  case class ProductID(value: Int)
  object ProductID {
    implicit val doobieRead: Read[ProductID] = Read[Int].map(ProductID(_))
    implicit val schema: Schema[ProductID] =
      Schema.schemaForInt.map(l => Some(ProductID(l)))(_.value)
    implicit val codec: Codec[String, ProductID, TextPlain] =
      Codec.int.map(ProductID(_))(_.value)
  }

  @derive(loggable, encoder, decoder)
  @newtype
  case class OrderID(value: Long)
  object OrderID {
    implicit val doobieRead: Read[OrderID] = Read[Long].map(OrderID(_))
    implicit val schema: Schema[OrderID] =
      Schema.schemaForLong.map(l => Some(OrderID(l)))(_.value)
    implicit val codec: Codec[String, OrderID, TextPlain] =
      Codec.long.map(OrderID(_))(_.value)
  }

  @derive(loggable, encoder, decoder)
  @newtype
  case class OrderItemID(value: Long)
  object OrderItemID {
    implicit val doobieRead: Read[OrderItemID] = Read[Long].map(OrderItemID(_))
    implicit val schema: Schema[OrderItemID] =
      Schema.schemaForLong.map(l => Some(OrderItemID(l)))(_.value)
    implicit val codec: Codec[String, OrderItemID, TextPlain] =
      Codec.long.map(OrderItemID(_))(_.value)
  }

  @derive(loggable, encoder, decoder)
  @newtype
  case class ProductName(value: String)
  object ProductName {
    implicit val doobieRead: Read[ProductName] = Read[String].map(ProductName(_))
    implicit val schema: Schema[ProductName] =
      Schema.schemaForString.map(n => Some(ProductName(n)))(_.value)
    implicit val codec: Codec[String, ProductName, TextPlain] =
      Codec.string.map(ProductName(_))(_.value)
  }

  @derive(loggable, encoder, decoder)
  @newtype
  case class Quantity(value: Byte)
  object Quantity {
    implicit val doobieRead: Read[Quantity] = Read[Byte].map(Quantity(_))
    implicit val schema: Schema[Quantity] =
      Schema.schemaForByte.map(l => Some(Quantity(l)))(_.value)
  }

  @derive(loggable, encoder, decoder)
  @newtype
  case class ShippingAddress(value: String)
  object ShippingAddress {
    implicit val doobieRead: Read[ShippingAddress] = Read[String].map(ShippingAddress(_))
    implicit val schema: Schema[ShippingAddress] =
      Schema.schemaForString.map(n => Some(ShippingAddress(n)))(_.value)
  }

  @derive(loggable, encoder, decoder)
  @newtype
  case class OrderDate(value: Instant)
  object OrderDate {
    implicit val doobieRead: Read[OrderDate] =
      Read[Long].map(ts => OrderDate(Instant.ofEpochMilli(ts)))
    implicit val schema: Schema[OrderDate] = Schema.schemaForString.map(n =>
      Some(OrderDate(Instant.parse(n)))
    )(_.value.toString)
  }

  @derive(loggable, encoder, decoder)
  @newtype
  case class Description(value: String)
  object Description {
    implicit val doobieRead: Read[Description] = Read[String].map(Description(_))
    implicit val schema: Schema[Description] =
      Schema.schemaForString.map(n => Some(Description(n)))(_.value)
  }

  @derive(loggable, encoder, decoder)
  @newtype
  case class Price(value: Int)
  object Price {
    implicit val doobieRead: Read[Price] = Read[Int].map(Price(_))
    implicit val schema: Schema[Price] =
      Schema.schemaForInt.map(l => Some(Price(l)))(_.value)
    implicit val codec: Codec[String, Price, TextPlain] =
      Codec.int.map(Price(_))(_.value)
  }

  @derive(loggable, encoder, decoder)
  case class PriceRange(minPrice: Price, maxPrice: Price)
}
