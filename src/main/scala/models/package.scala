package object models {
  /** Created by Shashi Gireddy (https://github.com/sgireddy) on 1/2/17 */
  case class Activity(
                       timeStamp: Long,
                       productId: Int,
                       userId: Int,
                       referrer: String,
                       retailPrice: Int,
                       productDiscountPct: Int,
                       cartDiscountPct: Int,
                       actionCode: Int,
                       marginPct: Int
                     )
}
