package product.service

import cats.data.Kleisli
import cats.effect.IO
import cats.syntax.applicativeError._
import cats.syntax.either._
import domain.{IOWithRequestContext, RequestContext}
import doobie._
import doobie.implicits._
import tofu.logging.Logging
import domain.errors.{AppError, InternalError, NoSuchCartProduct, NoSuchUserWithId}
import product.dao.ProductSql
import product.domain.{CreateProduct, Price, Product, ProductCategory, ProductID, Quantity, UpdateProduct}
import user.domain.{JwtToken, ShoppingCartId, UserID}

trait ProductStorage {


  def list(minPrice: Option[Price],
           maxPrice: Option[Price],
           category: Option[ProductCategory.Value],
           sellerId: Option[UserID]): IOWithRequestContext[Either[InternalError, List[Product]]]

  def create(userID: UserID, product: CreateProduct): IOWithRequestContext[Either[AppError, ProductID]]

  def findById(productID: ProductID): IOWithRequestContext[Either[InternalError, Option[Product]]]

  def update(product: UpdateProduct): IOWithRequestContext[Either[AppError, Unit]]

  def addProductToCart(productID: ProductID, userID: UserID): IOWithRequestContext[Either[AppError, Quantity]]

  def removeProductFromCart(productID: ProductID, userID: UserID): IOWithRequestContext[Either[AppError, Quantity]]

  def deleteProductFromCart(productID: ProductID, userID: UserID): IOWithRequestContext[Either[AppError, Unit]]
}

object ProductStorage {
  private final class Impl(
                            sql: ProductSql,
                            transactor: Transactor[IOWithRequestContext]
                          ) extends ProductStorage {

    override def list(minPrice: Option[Price],
                      maxPrice: Option[Price],
                      category: Option[ProductCategory.Value],
                      sellerId: Option[UserID])
    : IOWithRequestContext[Either[InternalError, List[Product]]] = {
      sql.listAll.transact(transactor).attempt.map {
        case Left(th) => InternalError(th).asLeft[List[Product]]
        case Right(value) => filterProducts(value, minPrice, maxPrice, category, sellerId).asRight[InternalError]
      }
    }

    def filterProducts(products: List[Product],
                       minPrice: Option[Price],
                       maxPrice: Option[Price],
                       category: Option[ProductCategory.Value],
                       sellerId: Option[UserID])
    : List[Product] = {
      products.filter {
        product =>
          (minPrice.isEmpty || minPrice.forall(_.value <= product.price.value)) &&
            (maxPrice.isEmpty || maxPrice.forall(_.value >= product.price.value)) &&
            (category.isEmpty || category.contains(product.category)) &&
            (sellerId.isEmpty || sellerId.contains(product.sellerId))
      }
    }


    override def create(userId: UserID, product: CreateProduct)
    : IOWithRequestContext[Either[AppError, ProductID]] = {
      sql.findSellerByUserId(userId).transact(transactor).flatMap {
        case Some(sellerId) => createProduct(sellerId, product)
        case None => Kleisli.pure(NoSuchUserWithId(userId).asLeft[ProductID])
      }
    }

    def createProduct(sellerId: UserID, product: CreateProduct)
    : IOWithRequestContext[Either[AppError, ProductID]] = {
      sql.create(sellerId, product).transact(transactor).attempt.map {
        case Left(th) => InternalError(th).asLeft[ProductID]
        case Right(Left(error)) => error.asLeft[ProductID]
        case Right(Right(productId)) => productId.asRight[AppError]
      }
    }

    override def update(product: UpdateProduct): IOWithRequestContext[Either[AppError, Unit]] =
      sql.updateProduct(product).transact(transactor).attempt.map {
        case Left(th) => InternalError(th).asLeft[Unit]
        case Right(Left(error)) => error.asLeft[Unit]
        case Right(_) => Right(())
      }

    def findById(productID: ProductID): IOWithRequestContext[Either[InternalError, Option[Product]]] =
      sql.findById(productID).transact(transactor).attempt.map(_.leftMap(InternalError))


    override def addProductToCart(productId: ProductID, userID: UserID)
    : IOWithRequestContext[Either[AppError, Quantity]] =
      sql.addProductToCart(productId, userID).transact(transactor).attempt.map {
        case Left(th) => InternalError(th).asLeft[Quantity]
        case Right(Left(error)) => error.asLeft[Quantity]
        case Right(quantity) => quantity
      }

    override def removeProductFromCart(productID: ProductID, userID: UserID)
    : IOWithRequestContext[Either[AppError, Quantity]] =
      sql.removeProductFromCart(productID, userID).transact(transactor).attempt.map {
        case Left(th) => InternalError(th).asLeft[Quantity]
        case Right(Left(err)) => err.asLeft[Quantity]
        case Right(Right(value)) => value.asRight[AppError]
      }

    override def deleteProductFromCart(productID: ProductID, userID: UserID)
    : IOWithRequestContext[Either[AppError, Unit]] =
      sql.deleteProductFromCart(productID, userID).transact(transactor).attempt.map {
        case Left(th) => InternalError(th).asLeft[Unit]
        case Right(Left(error)) => error.asLeft[Unit]
        case Right(_) => Right(())
      }


  }

  private final class LoggingImpl(storage: ProductStorage)
                                 (implicit logging: Logging[IOWithRequestContext]) extends ProductStorage {

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

    override def create(sellerId: UserID, product: CreateProduct): IOWithRequestContext[Either[AppError, ProductID]] =
      surroundWithLogs[AppError, ProductID](s"Creating product with params $product") {
        error => (s"Error while creating product: ${error.message}", error.cause)
      } { product =>
        s"Created product $product"
      }(storage.create(sellerId, product))

    override def list(minPrice: Option[Price],
                      maxPrice: Option[Price],
                      category: Option[ProductCategory.Value],
                      sellerId: Option[UserID]): IOWithRequestContext[Either[InternalError, List[Product]]] =
      surroundWithLogs[InternalError, List[Product]]("Getting all products") {
        error =>
          (s"Error while getting all products: ${error.message}", error.cause)
      } { result =>
        s"All products: ${result.mkString}"
      }(storage.list(minPrice, maxPrice, category, sellerId))

    override def update(product: UpdateProduct): IOWithRequestContext[Either[AppError, Unit]] =
      surroundWithLogs[AppError, Unit](s"Updating product with params $product") {
        error => (s"Error while creating product: ${error.message}", error.cause)
      } { product =>
        s"Update product $product"
      }(storage.update(product))

    override def findById(productID: ProductID): IOWithRequestContext[Either[InternalError, Option[Product]]] =
      surroundWithLogs[InternalError, Option[Product]](
        s"Getting product by id ${productID.value}"
      ) { error =>
        (s"Error while getting product: ${error.message}\n", error.cause)
      } { result =>
        s"Found product: ${result.toString}"
      }(storage.findById(productID))

    override def addProductToCart(productID: ProductID, userID: UserID)
    : IOWithRequestContext[Either[AppError, Quantity]] =
      surroundWithLogs[AppError, Quantity](s"Adding product ${productID} to cart of user $userID") {
        error => (s"Error while adding product: ${error.message}", error.cause)
      } { product =>
        s"Created cart product $product"
      }(storage.addProductToCart(productID, userID))

    override def removeProductFromCart(productID: ProductID, userID: UserID)
    : IOWithRequestContext[Either[AppError, Quantity]] =
      surroundWithLogs[AppError, Quantity](s"Removing product ${productID} to cart of user $userID") {
        error => (s"Error while removing product: ${error.message}", error.cause)
      } { product =>
        s"Removed cart product $product"
      }(storage.removeProductFromCart(productID, userID))

    override def deleteProductFromCart(productID: ProductID, userID: UserID)
    : IOWithRequestContext[Either[AppError, Unit]] =
      surroundWithLogs[AppError, Unit](s"Deleting product ${productID} to cart of user $userID") {
        error => (s"Error while deleting product: ${error.message}", error.cause)
      } { product =>
        s"Deleted cart product $product"
      }(storage.deleteProductFromCart(productID, userID))
  }

  def make(
            sql: ProductSql,
            transactor: Transactor[IOWithRequestContext]
          ): ProductStorage = {
    implicit val logs =
      Logging.Make
        .contextual[IOWithRequestContext, RequestContext]
        .forService[ProductStorage]
    val storage = new Impl(sql, transactor)
    new LoggingImpl(storage)
  }
}
