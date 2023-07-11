import auth.{AuthImpl, SsoAdder, SsoAdderImpl}
import cats.data.ReaderT
import cats.effect.kernel.Resource
import cats.effect.{ExitCode, IO, IOApp}
import com.comcast.ip4s._
import config.AppConfig
import domain.{IOWithRequestContext, RequestContext}
import doobie.util.transactor.Transactor
import org.http4s.ember.server._
import org.http4s.implicits._
import org.http4s.server.Router
import product.controller.{OrderController, ProductController}
import product.dao.{OrderSql, ProductSql}
import product.service.{OrderStorage, ProductStorage}
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import tofu.logging.Logging
import user.controller.UserController
import user.dao.UserSql
import user.service.UserStorage

object Main extends IOApp {
  private val mainLogs =
    Logging.Make.plain[IO].byName("Main")

  override def run(args: List[String]): IO[ExitCode] =
    (for {
      _ <- Resource.eval(mainLogs.info("Starting marketplace service..."))
      config <- Resource.eval(AppConfig.load)
      transactor = Transactor
        .fromDriverManager[IO](
          config.db.driver,
          config.db.url,
          config.db.user,
          config.db.password
        )
        .mapK[IOWithRequestContext](ReaderT.liftK[IO, RequestContext])
      auth = new AuthImpl[IO]("secret-key")
      sso = new SsoAdderImpl[IO](auth)
      productSql = ProductSql.make
      productStorage = ProductStorage.make(productSql, transactor)
      productController = ProductController.make(productStorage, sso)
      orderSql = OrderSql.make
      orderStorage = OrderStorage.make(orderSql, transactor)
      orderController = OrderController.make(orderStorage, sso)
      userSql = UserSql.make
      userStorage = UserStorage.make(userSql, transactor, "secret-key")
      userController = UserController.make(userStorage)
      allEndpoints = productController.allProductEndpoints
        .appendedAll(orderController.allOrderEndpoints)
        .appendedAll(userController.allUserEndpoints)
      swaggerEndpoints = SwaggerInterpreter()
        .fromServerEndpoints(allEndpoints, "marketPlace", "1.0")
      routes = Http4sServerInterpreter[IO]()
        .toRoutes(allEndpoints.appendedAll(swaggerEndpoints))

      httpApp = Router("/" -> routes).orNotFound
      _ <- EmberServerBuilder
        .default[IO]
        .withHost(
          Ipv4Address.fromString(config.server.host).getOrElse(ipv4"0.0.0.0")
        )
        .withPort(Port.fromInt(config.server.port).getOrElse(port"80"))
        .withHttpApp(httpApp)
        .build
    } yield ()).useForever.as(ExitCode.Success)
}
