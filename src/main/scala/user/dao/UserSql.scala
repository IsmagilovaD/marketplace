package user.dao

import cats.implicits.{catsSyntaxApplicativeId, catsSyntaxEitherId}
import domain.errors.{NoSuchUserWithId, UserAlreadyExist}
import doobie.implicits._
import doobie.{ConnectionIO, Query0, Update0}
import user.domain.{AccountNumber, CreateUser, Email, PhoneNumber, ShoppingCartId, UserAuthLogin, UserID, UserInfo, UserPassword, UserType, Username}

trait UserSql {
  def addUser(user: CreateUser): ConnectionIO[Either[UserAlreadyExist, Unit]]

  def getUserInfo(userAuth: UserAuthLogin): ConnectionIO[Option[UserInfo]]
}

object UserSql {
  object sqls {
    def insertUserWithEmailSql(user: CreateUser, email: Email): Update0 =
      sql"insert into users(username, email,password, user_type) values (${user.username.value},${email.value},${user.password.value},${user.userType})".update

    def insertUserWithPhoneNumberSql(user: CreateUser, phoneNumber: PhoneNumber): Update0 =
      sql"insert into users(username,phone_number,password, user_type) values (${user.username.value},${phoneNumber.value},${user.password.value},${user.userType})".update

    def insertBuyerSql(userId: UserID): Update0 =
      sql"insert into buyers(user_id) values (${userId.value})".update

    def insertShoppingCartSql(buyerId: UserID): Update0 =
      sql"insert into shopping_carts(buyer_id) values(${buyerId.value})".update

    def insertSellerSql(userId: UserID): Update0 =
      sql"insert into sellers(user_id) values (${userId.value})".update

    def findByIdSql(userID: UserID): Query0[Username] =
      sql"select username from users where id=${userID.value}".query[Username]

    def findByEmailSql(email: Email): Query0[UserInfo] =
      sql"select id, password, user_type from users where email=${email.value}".query[UserInfo]

    def findByPhoneNumberSql(phoneNumber: PhoneNumber): Query0[UserInfo] =
      sql"select id, password, user_type from users where phone_number=${phoneNumber.value}".query[UserInfo]

    def insertBonusAccountSql(userId: UserID): Update0 =
      sql"insert into bonus_accounts (bonus_amount, user_id) values (1000, ${userId.value})".update
  }

  private final class Impl extends UserSql {

    import sqls._

    override def addUser(user: CreateUser)
    : ConnectionIO[Either[UserAlreadyExist, Unit]] =
      user.email match {
        case Some(email) => findByEmailSql(email).option.flatMap {
          case None => AddUserByEmail(user, email)
          case Some(_)
          => UserAlreadyExist().asLeft[Unit].pure[ConnectionIO]
        }
        case None => findByPhoneNumberSql(user.phoneNumber.get).option.flatMap {
          case None => AddUserByPhoneNumber(user, user.phoneNumber.get)
          case Some(_)
          => UserAlreadyExist().asLeft[Unit].pure[ConnectionIO]
        }
      }


    def AddUserByEmail(user: CreateUser, email: Email): ConnectionIO[Either[UserAlreadyExist, Unit]] =
      for {
        id <- insertUserWithEmailSql(user, email)
          .withUniqueGeneratedKeys[UserID]("id")
        _ <- insertBonusAccountSql(id)
          .withUniqueGeneratedKeys[AccountNumber]("account_number")
        _ <- addUserByType(user.userType, id)
      } yield Right(())


    def AddUserByPhoneNumber(user: CreateUser, phoneNumber: PhoneNumber): ConnectionIO[Either[UserAlreadyExist, Unit]] =
      for {
        id <- insertUserWithPhoneNumberSql(user, phoneNumber)
          .withUniqueGeneratedKeys[UserID]("id")
        _ <- insertBonusAccountSql(id)
          .withUniqueGeneratedKeys[AccountNumber]("account_number")
        _ <- addUserByType(user.userType, id)
      } yield Right(())


    def addUserByType(userType: UserType.Value, id: UserID): ConnectionIO[Either[NoSuchUserWithId, Unit]] =
      userType match {
        case UserType.Buyer => addBuyer(id)
        case UserType.Seller => addSeller(id)
      }


    def addBuyer(userID: UserID): ConnectionIO[Either[NoSuchUserWithId, Unit]] =
      findByIdSql(userID).option.flatMap {
        case Some(_) => for {
          buyerId <- insertBuyerSql(userID).withUniqueGeneratedKeys[UserID]("id")
          _ <- insertShoppingCartSql(buyerId).withUniqueGeneratedKeys[ShoppingCartId]("id")
        } yield Right(())
        case None => NoSuchUserWithId(userID).asLeft[Unit].pure[ConnectionIO]
      }


    def addSeller(userID: UserID): ConnectionIO[Either[NoSuchUserWithId, Unit]] =
      findByIdSql(userID).option.flatMap {
        case Some(_) => insertSellerSql(userID).withUniqueGeneratedKeys[UserID]("id")
          .map(_ => Right(()))
        case None => NoSuchUserWithId(userID).asLeft[Unit].pure[ConnectionIO]
      }

    override def getUserInfo(userAuth: UserAuthLogin): ConnectionIO[Option[UserInfo]] =
      userAuth.email match {
        case Some(email) => findByEmailSql(email).option
        case None => findByPhoneNumberSql(userAuth.phoneNumber.get).option
      }
  }

  def make: UserSql = new Impl
}
