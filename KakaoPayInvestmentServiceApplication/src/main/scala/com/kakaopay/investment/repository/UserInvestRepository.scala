package com.kakaopay.investment.repository

import com.kakaopay.investment.entity.UserInvestInfo
import org.springframework.data.repository.CrudRepository

/**
 * UserInvestInfo Repository
 */
trait UserInvestRepository extends CrudRepository[UserInvestInfo, java.lang.Long]{
  def findByUserId(userId: java.lang.Long): java.util.List[UserInvestInfo]
}