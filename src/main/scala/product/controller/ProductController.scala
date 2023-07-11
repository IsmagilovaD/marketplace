package product.controller

import auth.SsoAdder
import cats.effect.IO
import domain.RequestContext
import product.domain.{CreateProduct, ProductID, UpdateProduct}
import product.service.ProductStorage
import sttp.tapir.server.ServerEndpoint
import user.domain.{JwtToken, UserJwtInfo, UserType}

trait ProductController {
  def createProduct: ServerEndpoint[Any, IO]

  def listProducts: ServerEndpoint[Any, IO]

  def updateProduct: ServerEndpoint[Any, IO]

  def addProductToCart: ServerEndpoint[Any, IO]

  def removeProductFromCart: ServerEndpoint[Any, IO]

  def deleteProductFromCart: ServerEndpoint[Any, IO]

  def allProductEndpoints: List[ServerEndpoint[Any, IO]]

}

object ProductController {
  final private class Impl(storage: ProductStorage,
                           sso: SsoAdder[IO, JwtToken]) extends ProductController {

    override def createProduct: ServerEndpoint[Any, IO] =
      sso.add(
        endpoint = endpoints.createProduct,
        serverLogic = (user: UserJwtInfo) => {
          (tuple: (RequestContext, CreateProduct)) => {
            tuple match {
              case (ctx: RequestContext, product: CreateProduct) =>
                storage.create(user.id, product).run(ctx)
            }
          }
        },
        roles = Seq(UserType.Seller))

    override def listProducts: ServerEndpoint[Any, IO] = {
      endpoints.findProductWithFilters.serverLogic {
        case (minPrice, maxPrice, category, sellerId, ctx) =>
          storage.list(minPrice, maxPrice, category, sellerId).run(ctx)
      }
    }

    override def updateProduct: ServerEndpoint[Any, IO] =
      sso.add(
        endpoint = endpoints.editProduct,
        serverLogic = (_: UserJwtInfo) => {
          (tuple: (RequestContext, UpdateProduct)) => {
            tuple match {
              case (ctx, product) =>
                storage.update(product).run(ctx)
            }
          }
        },
        roles = Seq(UserType.Seller)
      )


    override def addProductToCart: ServerEndpoint[Any, IO] =
      sso.add(
        endpoint = endpoints.addProductToCart,
        serverLogic = (user: UserJwtInfo) => {
          (tuple: (ProductID, RequestContext)) => {
            tuple match {
              case (productId, ctx) =>
                storage.addProductToCart(productId, user.id).run(ctx)
            }
          }
        },
        roles = Seq(UserType.Buyer))

    override def removeProductFromCart: ServerEndpoint[Any, IO] =
      sso.add(
        endpoint = endpoints.removeProductFromCart,
        serverLogic = (user: UserJwtInfo) => {
          (tuple: (ProductID, RequestContext)) => {
            tuple match {
              case (productId, ctx) =>
                storage.removeProductFromCart(productId, user.id).run(ctx)

            }
          }
        },
        roles = Seq(UserType.Buyer)
      )

    override def deleteProductFromCart: ServerEndpoint[Any, IO] =
      sso.add(
        endpoint = endpoints.deleteProductFromCart,
        serverLogic = (user: UserJwtInfo) => {
          (tuple: (ProductID, RequestContext)) => {
            tuple match {
              case (productId, ctx) =>
                storage.deleteProductFromCart(productId, user.id).run(ctx)
            }
          }
        },
        roles = Seq(UserType.Buyer))

    override val allProductEndpoints: List[ServerEndpoint[Any, IO]] = List(
      listProducts,
      createProduct,
      updateProduct,
      addProductToCart,
      removeProductFromCart,
      deleteProductFromCart
    )
  }

  def make(storage: ProductStorage, sso: SsoAdder[IO, JwtToken]): ProductController = new Impl(storage, sso)
}
