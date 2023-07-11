package product.controller

import domain.RequestContext
import domain.errors.AppError
import io.circe.generic.decoding.DerivedDecoder.deriveDecoder
import io.circe.generic.encoding.DerivedAsObjectEncoder.deriveEncoder
import product.domain.{CreateProduct, Order, OrderID, OrderItem, OrderItemID, OrderStatus, Price, Product, ProductCategory, ProductID, Quantity, ShippingAddress, UpdateProduct}
import sttp.model.StatusCode
import sttp.model.headers.WWWAuthenticateChallenge
import sttp.tapir.{PublicEndpoint, endpoint, header, _}
import sttp.tapir.json.circe._
import user.domain.{JwtToken, UserID}

object endpoints {

  val createProduct
  : Endpoint[JwtToken, (RequestContext, CreateProduct), AppError, ProductID, Any] =
    endpoint.post
      .in("product")
      .description("Here the seller can add a new product")
      .securityIn(auth.bearer[JwtToken](WWWAuthenticateChallenge.bearer))
      .in(header[RequestContext]("X-Request-Id"))
      .in(jsonBody[CreateProduct])
      .errorOut(jsonBody[AppError])
      .out(jsonBody[ProductID])

  val editProduct
  : Endpoint[JwtToken, (RequestContext, UpdateProduct), AppError, Unit, Any] =
    endpoint.put
      .in("product")
      .description("Here the seller can change the product")
      .securityIn(auth.bearer[JwtToken](WWWAuthenticateChallenge.bearer))
      .in(header[RequestContext]("X-Request-Id"))
      .in(jsonBody[UpdateProduct])
      .errorOut(jsonBody[AppError])
      .out(statusCode(StatusCode.Ok))

  val findProductWithFilters
  : PublicEndpoint[(Option[Price], Option[Price], Option[ProductCategory.Value],
    Option[UserID], RequestContext), AppError, List[Product], Any] =
    endpoint.get
      .in("products")
      .description("Here you can find products with filters, if there are no filters, the entire list of products is returned")
      .in(query[Option[Price]]("minPrice"))
      .in(query[Option[Price]]("maxPrice"))
      .in(query[Option[ProductCategory.Value]]("category"))
      .in(query[Option[UserID]]("seller"))
      .in(header[RequestContext]("X-Request-Id"))
      .errorOut(jsonBody[AppError])
      .out(jsonBody[List[Product]])


  val addProductToCart
  : Endpoint[JwtToken, (ProductID, RequestContext), AppError, Quantity, Any] =
    endpoint.put
      .in("addToCart" / path[ProductID])
      .description("Add one item of product to cart")
      .securityIn(auth.bearer[JwtToken](WWWAuthenticateChallenge.bearer))
      .in(header[RequestContext]("X-Request-Id"))
      .errorOut(jsonBody[AppError])
      .out(jsonBody[Quantity])

  val removeProductFromCart
  : Endpoint[JwtToken, (ProductID, RequestContext), AppError, Quantity, Any] =
    endpoint.put
      .in("removeFromCart" / path[ProductID])
      .description("Remove one item of product from cart")
      .securityIn(auth.bearer[JwtToken](WWWAuthenticateChallenge.bearer))
      .in(header[RequestContext]("X-Request-Id"))
      .errorOut(jsonBody[AppError])
      .out(jsonBody[Quantity])

  val deleteProductFromCart:
    Endpoint[JwtToken, (ProductID, RequestContext), AppError, Unit, Any] =
    endpoint.put
      .in("removeAllFromCart" / path[ProductID])
      .description("Remove all items of product from cart")
      .securityIn(auth.bearer[JwtToken](WWWAuthenticateChallenge.bearer))
      .in(header[RequestContext]("X-Request-Id"))
      .errorOut(jsonBody[AppError])
      .out(statusCode(StatusCode.Ok))

  val buyShoppingCart
  : PublicEndpoint[(RequestContext, ShippingAddress), AppError, OrderID, Any] =
    endpoint.post
      .in("buyCart")
      .description("Here you can buy your shopping cart")
      .in(header[RequestContext]("X-Request-Id"))
      .in(jsonBody[ShippingAddress])
      .errorOut(jsonBody[AppError])
      .out(jsonBody[OrderID])

  val getOrderByID
  : Endpoint[JwtToken, (OrderID, RequestContext), AppError, Option[Order], Any] =
    endpoint.get
      .in("getOrder" / path[OrderID])
      .description("Here you can get your order")
      .securityIn(auth.bearer[JwtToken](WWWAuthenticateChallenge.bearer))
      .in(header[RequestContext]("X-Request-Id"))
      .errorOut(jsonBody[AppError])
      .out(jsonBody[Option[Order]])

  val getOrderItemsByID
  : Endpoint[JwtToken, (OrderID, RequestContext), AppError, List[OrderItem], Any] =
    endpoint.get
      .in("getOrderItems" / path[OrderID])
      .description("Here you can get your order's items")
      .securityIn(auth.bearer[JwtToken](WWWAuthenticateChallenge.bearer))
      .in(header[RequestContext]("X-Request-Id"))
      .errorOut(jsonBody[AppError])
      .out(jsonBody[List[OrderItem]])

  val cancelOrderItem
  : PublicEndpoint[(OrderItemID, RequestContext), AppError, Unit, Any] =
    endpoint.put
      .in("cancelOrderItem" / path[OrderItemID])
      .description("Here you can cancel one order item")
      .in(header[RequestContext]("X-Request-Id"))
      .errorOut(jsonBody[AppError])
      .out(statusCode(StatusCode.Ok))

  val cancelOrder
  : PublicEndpoint[(OrderID, RequestContext), AppError, Unit, Any] =
    endpoint.put
      .in("cancelOrder" / path[OrderID])
      .description("Here you can cancel your entire order")
      .in(header[RequestContext]("X-Request-Id"))
      .errorOut(jsonBody[AppError])
      .out(statusCode(StatusCode.Ok))

  val getUsersOrders
  : PublicEndpoint[RequestContext, AppError, List[Order], Any] =
    endpoint.get
      .in("getOrders")
      .description("Here buyer can get all his orders")
      .in(header[RequestContext]("X-Request-Id"))
      .errorOut(jsonBody[AppError])
      .out(jsonBody[List[Order]])

  val changeShippingAddress
  : PublicEndpoint[(OrderID, ShippingAddress, RequestContext), AppError, Unit, Any] =
    endpoint.put
      .in("changeShippingAddress" / path[OrderID])
      .description("Here buyer can change shipping address for order")
      .in(jsonBody[ShippingAddress])
      .in(header[RequestContext]("X-Request-Id"))
      .errorOut(jsonBody[AppError])
      .out(statusCode(StatusCode.Ok))

  val getSellersProducts
  : PublicEndpoint[(RequestContext), AppError, List[OrderItem], Any] =
    endpoint.get
      .in("myOrders")
      .in(header[RequestContext]("X-Request-Id"))
      .errorOut(jsonBody[AppError])
      .out(jsonBody[List[OrderItem]])

  val changeOrderStatus
  : PublicEndpoint[(OrderStatus.Value, RequestContext), AppError, Unit, Any] =
    endpoint.put
      .in("changeOrderStatus" / path[OrderStatus.Value])
      .in(header[RequestContext]("X-Request-Id"))
      .errorOut(jsonBody[AppError])
      .out(statusCode(StatusCode.Ok))


}
