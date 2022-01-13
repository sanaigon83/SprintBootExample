package com.kakaopay.investment.entity

import java.beans.Transient
import java.time.LocalDateTime

import com.kakaopay.investment.composite.UserId
import javax.persistence.{Column, Entity, Id, IdClass}


@Entity
@IdClass(classOf[UserId])
class UserInvestInfo(@Transient id: Long,
                     @Transient pId: Int,
                     @Column var amount: Long,
                     @Column var startedAt: LocalDateTime) extends Serializable {
  @Id
  var userId: java.lang.Long = id

  @Id
  var productId: java.lang.Integer = pId

  def this() = this(0L, 0, 0L, null)
}
