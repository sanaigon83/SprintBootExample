package com.kakaopay.investment.dto

import java.time.LocalDateTime

import com.kakaopay.investment.entity.InvestProduct

import scala.beans.BeanProperty

case class ProductDto(@BeanProperty productId: Int,
                      @BeanProperty title: String,
                      @BeanProperty totalAmount: Long,
                      @BeanProperty curAmount: Long = 0,
                      @BeanProperty investorCount: Long = 0,
                      @BeanProperty startedAt: LocalDateTime,
                      @BeanProperty finishedAt: LocalDateTime,
                      @BeanProperty soldOut: Boolean = false){

  def this() = this(0, "", 0L, 0L, 0L, null, null, false)

  def toEntity: InvestProduct =
    new InvestProduct(productId,
      title,
      totalAmount,
      curAmount,
      investorCount,
      startedAt,
      finishedAt,
      soldOut)
}
