package com.kakaopay.investment.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

import com.kakaopay.investment.dto.ProductDto
import com.kakaopay.investment.repository.InvestProductRepository

import scala.util.Random

trait SampleProduct{

  val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
  val sampleProducts =
    List(
      ProductDto(
        productId = 1,
        title = "아파트담보대출",
        totalAmount = 10000000,
        startedAt = LocalDateTime.parse("2020-03-01 00:00:00", formatter),
        finishedAt = LocalDateTime.parse("2020-05-09 23:59:59", formatter)),
      ProductDto(
        productId = 2,
        title = "빌라담보대출",
        totalAmount = 10000000,
        startedAt = LocalDateTime.parse("2020-05-01 00:00:00", formatter),
        finishedAt = LocalDateTime.parse("2020-08-31 23:59:59", formatter)),
      ProductDto(
        productId = 3,
        title = "개인신용대출",
        totalAmount = 10000000,
        startedAt = LocalDateTime.parse("2020-06-01 00:00:00", formatter),
        finishedAt = LocalDateTime.parse("2020-12-15 23:59:59", formatter)),
      ProductDto(
        productId = 4,
        title = "공장담보대출",
        totalAmount = 10000000,
        startedAt = LocalDateTime.parse("2020-04-01 00:00:00", formatter),
        finishedAt = LocalDateTime.parse("2020-06-30 23:59:59", formatter)),
      ProductDto(
        productId = 5,
        title = "아파트담보대출2",
        totalAmount = 10000000,
        startedAt = LocalDateTime.parse("2020-07-01 00:00:00", formatter),
        finishedAt = LocalDateTime.parse("2020-08-31 23:59:59", formatter)),
      ProductDto(
        productId = 6,
        title = "빌라담보대출2",
        totalAmount = 10000000,
        startedAt = LocalDateTime.parse("2020-08-01 00:00:00", formatter),
        finishedAt = LocalDateTime.parse("2020-11-31 23:59:59", formatter)),
      ProductDto(
        productId = 7,
        title = "개인신용대출2",
        totalAmount = 10000000,
        startedAt = LocalDateTime.parse("2020-09-01 00:00:00", formatter),
        finishedAt = LocalDateTime.parse("2020-12-30 23:59:59", formatter)),
      ProductDto(
        productId = 8,
        title = "공장담보대출2",
        totalAmount = 10000000,
        startedAt = LocalDateTime.parse("2020-03-01 00:00:00", formatter),
        finishedAt = LocalDateTime.parse("2020-12-31 23:59:59", formatter)),
      ProductDto(
        productId = 9,
        title = "아파트담보대출3",
        totalAmount = 10000000,
        startedAt = LocalDateTime.parse("2020-04-01 00:00:00", formatter),
        finishedAt = LocalDateTime.parse("2020-11-30 23:59:59", formatter)),
      ProductDto(
        productId = 10,
        title = "빌라담보대출3",
        totalAmount = 10000000,
        startedAt = LocalDateTime.parse("2020-05-01 00:00:00", formatter),
        finishedAt = LocalDateTime.parse("2020-12-15 23:59:59", formatter)),
    )

  /**
   * 모집기한이 유효한 상품 목록을 가져온다.
   * @param date 상품조회 시간
   * @return 유효한 상품 목록
   */
  def validProducts(date: LocalDateTime): Seq[ProductDto] =
    sampleProducts.filter(p => p.startedAt.isEqual(date) || (p.startedAt.isBefore(date) && p.finishedAt.isAfter(date)))

  def getValidDateTime(products: Seq[ProductDto]): LocalDateTime = {
    val dateTimeMin: LocalDateTime = products.map(_.startedAt).min
    val dateTImeMax: LocalDateTime = products.map(_.finishedAt).max
    val days: Long = dateTimeMin.until(dateTImeMax, ChronoUnit.DAYS)
    dateTimeMin.plus(Random.nextLong(days) + 1, ChronoUnit.DAYS)
  }

  def withSampleProducts(take: Int = 0)(testFunction: List[ProductDto] => Unit)(implicit repo: InvestProductRepository): Unit = {
    val products =
      if(take == 0)
        Random.shuffle(sampleProducts)
      else
        Random.shuffle(sampleProducts).take(take)

    products foreach (s => repo.save(s.toEntity))

    testFunction(products)
  }
}
