package user.controller

import io.circe.generic.decoding.DerivedDecoder.deriveDecoder
import io.circe.generic.encoding.DerivedAsObjectEncoder.deriveEncoder
import domain.RequestContext
import domain.errors.AppError
import sttp.model.StatusCode
import sttp.tapir._
import sttp.tapir.json.circe._
import sttp.tapir.{PublicEndpoint, endpoint, header, statusCode}
import user.domain.{BonusAmount, Seller, UserAuthorization, UserRegistration, UserType}

object endpoints {
  val registration: PublicEndpoint[(UserRegistration, RequestContext), AppError, Unit, Any] =
    endpoint.post
      .in("signUp")
      .in(jsonBody[UserRegistration])
      .in(header[RequestContext]("X-Request-Id"))
      .errorOut(jsonBody[AppError])
      .out(statusCode(StatusCode.Ok))

  val authorization: PublicEndpoint[(UserAuthorization, RequestContext), AppError, String, Any] =
    endpoint.post
      .in("signIn")
      .in(jsonBody[UserAuthorization])
      .in(header[RequestContext]("X-Request-Id"))
      .errorOut(jsonBody[AppError])
      .out(plainBody[String])

  val topUpBalance: PublicEndpoint[(BonusAmount, RequestContext), AppError, Unit, Any] =
    endpoint.put
      .in("topUp")
      .in(query[BonusAmount]("amount"))
      .in(header[RequestContext]("X-Request-Id"))
      .errorOut(jsonBody[AppError])
      .out(statusCode(StatusCode.Ok))

  val getBalance: PublicEndpoint[RequestContext, AppError, BonusAmount, Any] =
    endpoint.get
      .in("balance")
      .in(header[RequestContext]("X-Request-Id"))
      .errorOut(jsonBody[AppError])
      .out(jsonBody[BonusAmount])

  val getAllSellers: PublicEndpoint[RequestContext, AppError, List[Seller], Any] =
    endpoint.get
      .in("sellers")
      .in(header[RequestContext]("X-Request-Id"))
      .errorOut(jsonBody[AppError])
      .out(jsonBody[List[Seller]])
}
