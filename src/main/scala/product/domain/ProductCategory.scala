package product.domain

import derevo.derive
import doobie.Read
import doobie.postgres.implicits.pgEnum
import io.circe._
import tofu.logging.Loggable
import tofu.logging.derivation.loggable

@derive(loggable)
object ProductCategory extends Enumeration {
  type ProductCategory = Value
  val Electronics, Clothing, Footwear,
  BabyProducts, BeautyAndHealth, Appliances,
  SportsAndLeisure, PetSupplies, Books = Value

  implicit val MyEnumMeta = pgEnum(ProductCategory, "product_category")
  implicit val productCategoryLoggable: Loggable[ProductCategory.Value] = Loggable.empty

  implicit val genderDecoder: Decoder[ProductCategory.Value] = Decoder.decodeEnumeration(ProductCategory)
  implicit val genderEncoder: Encoder[ProductCategory.Value] = Encoder.encodeEnumeration(ProductCategory)
}
