package product.dao

import doobie.implicits.toSqlInterpolator
import doobie.{ConnectionIO, Query0}
import product.domain.{Order, OrderID, OrderItem}

trait OrderSql {
  def listAllOrderItems: ConnectionIO[List[OrderItem]]
  def findOrderById(orderId: OrderID): ConnectionIO[Option[Order]]

}

object OrderSql {
  object sqls {


    def findOrderByIdSql(id: OrderID): Query0[Order] =
      sql"select * from orders where orders.id=${id.value}"
        .query[Order]

    val allOrderItemsSql: Query0[OrderItem] =
      sql"select * from order_items".query[OrderItem]

  }

  private final class Impl extends OrderSql {

    import sqls._

    override def findOrderById(orderId: OrderID): ConnectionIO[Option[Order]] = {
      findOrderByIdSql(orderId).option
    }

    override def listAllOrderItems: ConnectionIO[List[OrderItem]] =
      allOrderItemsSql.to[List]
  }

  def make: OrderSql = new Impl
}
