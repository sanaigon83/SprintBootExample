package com.kakaopay.investment.dto

import java.time.LocalDateTime

import com.fasterxml.jackson.annotation.JsonProperty

import scala.beans.BeanProperty

case class MyProductDto(@BeanProperty @JsonProperty("pid") pId: Int,
                        @BeanProperty @JsonProperty("title") title: String,
                        @BeanProperty @JsonProperty("totalAmount") totalAmount: Long,
                        @BeanProperty @JsonProperty("myAmount") myAmount: Long,
                        @BeanProperty @JsonProperty("startedAt") startedAt: LocalDateTime){
  def this() = this(0, "", 0L, 0L, null)
}