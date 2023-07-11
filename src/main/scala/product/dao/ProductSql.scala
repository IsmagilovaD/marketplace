package product.dao

import cats.implicits.{catsSyntaxApplicativeId, catsSyntaxEitherId}
import domain.errors._
import doobie.implicits.toSqlInterpolator
import doobie.{ConnectionIO, Query0, Update0}
import product.domain.{CreateProduct, Product, ProductID, ProductName, Quantity, UpdateProduct}
import user.domain.{ShoppingCart, ShoppingCartId, UserID}

trait ProductSql {
  def listAll: ConnectionIO[List[Product]]

  def create(userID: UserID, product: CreateProduct): ConnectionIO[Either[ProductAlreadyExists, ProductID]]

  def updateProduct(product: UpdateProduct): ConnectionIO[Either[NoSuchProduct, Unit]]

  def findById(productID: ProductID): ConnectionIO[Option[Product]]

  def findSellerByUserId(userID: UserID): ConnectionIO[Option[UserID]]

  def addProductToCart(productId: ProductID, userID: UserID): ConnectionIO[Either[NoSuchShoppingCart, Quantity]]

  def removeProductFromCart(productId: ProductID, userID: UserID): ConnectionIO[Either[NoSuchShoppingCart, Quantity]]

  def deleteProductFromCart(productId: ProductID, userID: UserID): ConnectionIO[Either[NoSuchCartProduct, Unit]]

}

object ProductSql {
  object sqls {

    val listAllSql: Query0[Product] = sql"select * from products".query[Product]

    def findSellerByUserIdSql(userID: UserID): Query0[UserID] =
      sql"select id from sellers where user_id=${userID.value}".query[UserID]

    def findShoppingCartIdByUserIdSql(userID: UserID): Query0[ShoppingCartId] =
      sql"select sc.id from users left join buyers b on users.id = b.user_id left join shopping_carts sc on b.id = sc.buyer_id where users.id = ${userID.value}".query[ShoppingCartId]

    def insertSql(sellerId: UserID, product: CreateProduct): Update0 =
      sql"insert into products (seller_id, name, price,category, description) values (${sellerId.value},${product.productName.value},${product.price.value}, ${product.category}, ${product.description.value})".update

    def findByNameAndSellerSql(name: ProductName, sellerId: UserID): doobie.Query0[Product] =
      sql"select * from products where name=${name.value} and seller_id=${sellerId.value}"
        .query[Product]

    def findByIdSql(id: ProductID): Query0[Product] =
      sql"select * from products where id=${id.value}".query[Product]

    def updateProductSql(product: UpdateProduct): Update0 =
      sql"update products set name=${product.productName.value}, price=${product.price.value}, description=${product.description.value}, category=${product.category} where id=${product.id.value}".update

    def findCartByIdSql(shoppingCartId: ShoppingCartId): Query0[ShoppingCart] =
      sql"select * from shopping_carts where id=${shoppingCartId.value}".query[ShoppingCart]

    def addProductToCartSql(productId: ProductID, shoppingCartId: ShoppingCartId): Update0 =
      sql"insert into cart_products (shopping_cart_id, product_id, quantity) values (${shoppingCartId.value}, ${productId.value}, 1) on conflict (shopping_cart_id, product_id) do update set quantity = cart_products.quantity + 1".update

    def removeProductFromCartSql(productId: ProductID, shoppingCartId: ShoppingCartId): Update0 =
      sql"UPDATE cart_products SET quantity = quantity - 1 WHERE shopping_cart_id = ${shoppingCartId.value} AND product_id = ${productId.value}".update

    def deleteProductFromCartSql(productId: ProductID, shoppingCartId: ShoppingCartId): Update0 =
      sql"DELETE FROM cart_products WHERE shopping_cart_id = ${shoppingCartId.value} AND product_id = ${productId.value}".update

    def getQuantityOfProductCartSql(productId: ProductID, shoppingCartId: ShoppingCartId): Query0[Quantity] =
      sql"SELECT quantity FROM cart_products WHERE shopping_cart_id = ${shoppingCartId.value} AND product_id = ${productId.value}".query[Quantity]
  }

  private final class Impl extends ProductSql {

    import sqls._

    override def listAll: ConnectionIO[List[Product]] =
      listAllSql.to[List]

    override def create(sellerId: UserID,
                        product: CreateProduct)
    : ConnectionIO[Either[ProductAlreadyExists, ProductID]] =
      findByNameAndSellerSql(product.productName, sellerId).option.flatMap {
        case None =>
          insertSql(sellerId, product)
            .withUniqueGeneratedKeys[ProductID]("id")
            .map(id =>
              id.asRight[ProductAlreadyExists]
            )
        case Some(_) => ProductAlreadyExists().asLeft[ProductID].pure[ConnectionIO]
      }


    override def updateProduct(product: UpdateProduct): ConnectionIO[Either[NoSuchProduct, Unit]] =
      updateProductSql(product).run.map {
        case 0 => NoSuchProduct(product.id).asLeft[Unit]
        case _ => ().asRight[NoSuchProduct]
      }

    override def addProductToCart(productId: ProductID, userID: UserID)
    : ConnectionIO[Either[NoSuchShoppingCart, Quantity]] = {
      for {
        shoppingCartId <- findShoppingCartIdByUserIdSql(userID).option
        result <- shoppingCartId match {
          case Some(scID) =>
            addProductToCartSql(productId, scID)
              .withUniqueGeneratedKeys[Quantity]("quantity")
              .map(quantity =>
                quantity.asRight[NoSuchShoppingCart]
              )
          case None =>
            NoSuchShoppingCart(userID).asLeft[Quantity].pure[ConnectionIO]
        }
      } yield result
    }

    override def findById(productID: ProductID): ConnectionIO[Option[Product]] =
      findByIdSql(productID).option

    override def removeProductFromCart(productId: ProductID, userID: UserID)
    : ConnectionIO[Either[NoSuchShoppingCart, Quantity]] =
      for {
        shoppingCartId <- findShoppingCartIdByUserIdSql(userID).option
        result <- shoppingCartId match {
          case Some(scID) =>
            for {
              quantity <- getQuantityOfProductCartSql(productId, scID).unique
              updatedQuantity = (quantity.value - 1).toByte
              _ <- if (updatedQuantity > 0)
                removeProductFromCartSql(productId, scID).run
              else
                deleteProductFromCartSql(productId, scID).run
            } yield Right(Quantity(updatedQuantity))
          case None =>
            NoSuchShoppingCart(userID).asLeft[Quantity].pure[ConnectionIO]
        }
      } yield result

    override def deleteProductFromCart(productId: ProductID, userID: UserID)
    : ConnectionIO[Either[NoSuchCartProduct, Unit]] =
      for {
        shoppingCartId <- findShoppingCartIdByUserIdSql(userID).option
        res <- shoppingCartId match {
          case Some(scID) =>
            for {
              _ <- deleteProductFromCartSql(productId, scID).run
            } yield ().asRight[NoSuchCartProduct]
          case None => NoSuchCartProduct(productId, userID).asLeft[Unit].pure[ConnectionIO]
        }
      } yield res

    override def findSellerByUserId(userID: UserID): ConnectionIO[Option[UserID]] =
      findSellerByUserIdSql(userID).option
  }


  def make: ProductSql = new Impl
}
