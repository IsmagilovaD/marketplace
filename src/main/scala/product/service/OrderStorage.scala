package product.service

import cats.syntax.applicativeError._
import cats.syntax.either._
import domain.errors.{InternalError, NoSuchOrder}
import domain.{IOWithRequestContext, RequestContext}
import doobie._
import doobie.implicits._
import product.dao.OrderSql
import product.domain.{Order, OrderID, OrderItem}
import tofu.logging.Logging

trait OrderStorage {
  def getOrderItemsById(id: OrderID): IOWithRequestContext[Either[InternalError,List[OrderItem]]]

  def findById(orderId: OrderID): IOWithRequestContext[Either[InternalError, Option[Order]]]
}

object OrderStorage {
  private final class Impl(sql: OrderSql,
                           transactor: Transactor[IOWithRequestContext]
                          ) extends OrderStorage {

    override def findById(orderId: OrderID): IOWithRequestContext[Either[InternalError, Option[Order]]] =
      sql.findOrderById(orderId).transact(transactor).attempt.map(_.leftMap(InternalError))

    override def getOrderItemsById(id: OrderID): IOWithRequestContext[Either[InternalError, List[OrderItem]]] =
      sql.listAllOrderItems.transact(transactor).attempt.map {
        case Left(th) => InternalError(th).asLeft[List[OrderItem]]
        case Right(value) => value.filter(_.orderId == id).asRight[InternalError]
      }
  }

  private final class LoggingImpl(storage: OrderStorage)
                                 (implicit logging: Logging[IOWithRequestContext]) extends OrderStorage {

    private def surroundWithLogs[Error, Res](
                                              inputLog: String
                                            )(errorOutputLog: Error => (String, Option[Throwable]))(
                                              successOutputLog: Res => String
                                            )(
                                              io: IOWithRequestContext[Either[Error, Res]]
                                            ): IOWithRequestContext[Either[Error, Res]] =
      for {
        _ <- logging.info(inputLog)
        res <- io
        _ <- res match {
          case Left(error) => {
            val (msg, cause) = errorOutputLog(error)
            cause.fold(logging.error(msg))(cause => logging.error(msg, cause))
          }
          case Right(result) => logging.info(successOutputLog(result))
        }
      } yield res

    override def findById(orderId: OrderID): IOWithRequestContext[Either[InternalError, Option[Order]]] =
      surroundWithLogs[InternalError, Option[Order]](
        s"Getting order by id ${orderId.value}"
      ) { error =>
        (s"Error while getting order: ${error.message}\n", error.cause)
      } { result =>
        s"Found order: ${result.toString}"
      }(storage.findById(orderId))

    override def getOrderItemsById(id: OrderID)
    : IOWithRequestContext[Either[InternalError, List[OrderItem]]] =
      surroundWithLogs[InternalError, List[OrderItem]]("Getting all order's items") {
        error =>
          (s"Error while getting all order's items: ${error.message}", error.cause)
      } { result =>
        s"All order's items: ${result.mkString}"
      }(storage.getOrderItemsById(id))
  }

  def make(
            sql: OrderSql,
            transactor: Transactor[IOWithRequestContext]
          ): OrderStorage = {
    implicit val logs =
      Logging.Make
        .contextual[IOWithRequestContext, RequestContext]
        .forService[OrderStorage]
    val storage = new Impl(sql, transactor)
    new LoggingImpl(storage)
  }
}
