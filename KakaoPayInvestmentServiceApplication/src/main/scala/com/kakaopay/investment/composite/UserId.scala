package com.kakaopay.investment.composite

/**
 * UserInvestInfo Entity의 Composite key 구성을 위한 클래스
 * @param userId      userId
 * @param productId   productId
 */
class UserId(var userId: java.lang.Long, var productId: java.lang.Integer) extends Serializable {
  def this() = this(0L, 0)
}

