package user.domain

import derevo.circe.{decoder, encoder}
import derevo.derive
import sttp.tapir.Schema
import tofu.logging.derivation.loggable

@derive( encoder, decoder)
case class BonusAccount(user: UserID,
                        accountNumber: AccountNumber,
                        bonusAmount: BonusAmount)
object BonusAccount{
  implicit val schema: Schema[BonusAccount] = Schema.derived

}
