package product.controller

import auth.SsoAdder
import cats.effect.IO
import domain.RequestContext
import product.domain.OrderID
import product.service.OrderStorage
import sttp.tapir.server.ServerEndpoint
import user.domain.{JwtToken, UserJwtInfo, UserType}

trait OrderController {
  def findOrderById: ServerEndpoint[Any, IO]

  def findOrderItemsById: ServerEndpoint[Any, IO]

  def allOrderEndpoints: List[ServerEndpoint[Any, IO]]

}

object OrderController {
  private final class Impl(storage: OrderStorage,
                           sso: SsoAdder[IO, JwtToken]) extends OrderController {

    override def findOrderById: ServerEndpoint[Any, IO] =
      sso.add(endpoint = endpoints.getOrderByID,
        serverLogic = (user: UserJwtInfo) => {
          (tuple: (OrderID, RequestContext)) => {
            tuple match {
              case (id, ctx) =>
                storage.findById(id).run(ctx)
            }
          }
        }, roles = Seq(UserType.Buyer, UserType.Seller))

    def findOrderItemsById: ServerEndpoint[Any, IO] =
      sso.add(endpoint = endpoints.getOrderItemsByID,
        serverLogic = (user: UserJwtInfo) => {
          (tuple: (OrderID, RequestContext)) => {
            tuple match {
              case (id, ctx) =>
                storage.getOrderItemsById(id).run(ctx)
            }
          }
        }, roles = Seq(UserType.Buyer, UserType.Seller))

    override def allOrderEndpoints: List[ServerEndpoint[Any, IO]] = List(
      findOrderById,
      findOrderItemsById
    )
  }

  def make(storage: OrderStorage, ssoAdder: SsoAdder[IO, JwtToken]): OrderController = new Impl(storage,ssoAdder)

}
