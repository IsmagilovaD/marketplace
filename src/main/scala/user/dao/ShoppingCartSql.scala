//package user.dao
//
//import domain.CartProduct
//import doobie.implicits.toSqlInterpolator
//import doobie.{ConnectionIO, Update0}
//import user.domain.ShoppingCart
//
//trait ShoppingCartSql {
//  //  def create(buyer: Buyer): ConnectionIO[Either[ShoppingCartAlreadyExists, ShoppingCart]]
//  def addProduct(cartProduct: CartProduct): ConnectionIO[Option[ShoppingCart]]
//
//  def removeProduct(cartProduct: CartProduct): ConnectionIO[Option[ShoppingCart]]
//}
//
//object ShoppingCartSql {
//  object sqls {
//    //    def insertSql(buyer: Buyer): Update0 =
//    //      sql"insert into shopping_carts".update
//
//    def addProductToCartSql(cartProduct: CartProduct): Update0 =
//      sql"insert into cart_products (shopping_cart_id, product_id, quantity,price) values(${cartProduct.shoppingCart.id.value}, ${cartProduct.product.id.value},${cartProduct.quantity.value},${cartProduct.price.value}) "
//        .update
//
//    def removeProductFromCartSql(cartProduct: CartProduct): Update0 =
//      sql"delete from cart_products where shopping_cart_id=${cartProduct.shoppingCart.id.value} and product_id=${cartProduct.product.id.value}".update
//  }
//
//  private final class Impl extends ShoppingCartSql {
//    override def addProduct(cartProduct: CartProduct):
//    ConnectionIO[Option[ShoppingCart]] = ???
//
//    override def removeProduct(cartProduct: CartProduct):
//    ConnectionIO[Option[ShoppingCart]] = ???
//  }
//}
