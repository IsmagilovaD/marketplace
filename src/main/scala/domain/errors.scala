package domain

import derevo.circe.{decoder, encoder}
import derevo.derive
import io.circe.{Decoder, Encoder, HCursor, Json}
import product.domain.{OrderID, ProductID}
import sttp.tapir.Schema
import tofu.logging.derivation.loggable
import user.domain.{ShoppingCartId, UserID, UserType}

object errors {
  sealed abstract class AppError(
                                  val message: String,
                                  val cause: Option[Throwable] = None
                                )

  object AppError {
    implicit val encoder: Encoder[AppError] = new Encoder[AppError] {
      override def apply(a: AppError): Json = Json.obj(
        ("message", Json.fromString(a.message))
      )
    }

    implicit val decoder: Decoder[AppError] = new Decoder[AppError] {
      override def apply(c: HCursor): Decoder.Result[AppError] =
        c.downField("message").as[String].map(MockError(_))
    }

    implicit val schema: Schema[AppError] = Schema.string[AppError]
  }

  case class ProductAlreadyExists()
    extends AppError("Product with same name and seller already exists")

  case class UserAlreadyExist()
    extends AppError("User with same login already exists")

  case class NoSuchProduct(id: ProductID)
    extends AppError(s"Product with id ${id.value} does not exist")

  case class NoSuchCartProduct(productId: ProductID, userID: UserID)
    extends AppError(s"Cart product with product id ${productId.value} and shoppingCart of user with id ${userID.value} does not exist")

  case class NoSuchShoppingCart(id: UserID)
    extends AppError(s"User with id ${id.value} does not have ShoppingCart")

  case class NoSuchUserWithId(id: UserID)
    extends AppError(s"User with id ${id.value} does not exist")

  case class NoSuchUserWithLogin(login: String)
    extends AppError(s"User with login ${login} does not exist")

  case class NoSuchOrder(id: OrderID)
    extends AppError(s"Order with id ${id.value} does not exist")

  case class ShoppingCartAlreadyExists()
    extends AppError("Shopping cart with same buyer already exists")

  case class InternalError(cause0: Throwable)
    extends AppError("Internal error", Some(cause0))

  case class InvalidLogin(login: String) extends AppError("Invalid login type ${login}")

  case class WrongPassword() extends AppError("Wrong password")


  case class MockError(override val message: String) extends AppError(message)
}
