package com.kakaopay.investment.entity

import java.beans.Transient
import java.time.LocalDateTime

import javax.persistence.{Column, Entity, Id}

@Entity
class InvestProduct(@Transient var pid: Integer,
                    @Column var title: String,
                    @Column var totalAmount: Long,
                    @Column var curAmount: Long,
                    @Column var investorCount: Long,
                    @Column var startedAt: LocalDateTime,
                    @Column var finishedAt: LocalDateTime,
                    @Column var soldOut: Boolean) extends Serializable {

  @Id
  var productId: Integer = pid

  def this() = this(0, "", 0L, 0L, 0L, null, null, false)
}
