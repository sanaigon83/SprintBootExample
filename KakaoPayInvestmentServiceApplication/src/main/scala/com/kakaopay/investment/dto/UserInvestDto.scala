package com.kakaopay.investment.dto

import java.time.LocalDateTime

import com.kakaopay.investment.entity.UserInvestInfo
import org.springframework.format.annotation.DateTimeFormat

import scala.beans.BeanProperty

case class UserInvestDto(@BeanProperty userId: Long,
                         @BeanProperty productId: Int,
                         @BeanProperty amount: Long,
                         @BeanProperty @DateTimeFormat(pattern="yyyyMMddHHmmss") startedAt: LocalDateTime,
                         @BeanProperty status: String){
  def this() = this(0L, 0, 0L, null, "")
  def toEntity(): UserInvestInfo = {
    new UserInvestInfo(userId, productId, amount, startedAt)
  }
}