package com.kakaopay.investment.repository

import java.util.Date

import com.kakaopay.investment.entity.InvestProduct
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

/**
 * Product Repository
 */
trait InvestProductRepository extends CrudRepository[InvestProduct, java.lang.Integer]{
  @Query(nativeQuery = true, value = "select * from invest_product where started_at <= ?1 AND ?1 < finished_at")
  def findByValidRange(date: Date): java.util.List[InvestProduct]
  def findByProductId(pId: java.lang.Integer): InvestProduct
}


