package com.kakaopay.investment.controller

import java.util
import java.util.Date

import com.kakaopay.investment.dto.{MyProductDto, ProductDto, UserInvestDto}
import com.kakaopay.investment.service.InvestmentService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.{HttpStatus, ResponseEntity}
import org.springframework.web.bind.annotation._

import scala.jdk.CollectionConverters._

/**
 * 투자 상품관련 RestController
 */
@RestController
class InvestmentController {

  @Autowired
  val investmentService: InvestmentService = null

  /**
   * 전체 투자 상품 조회 api
   *
   * 상품 모집기간 내의 상품 조회시 다음과 같은 정보를 담은 상품 정보를 반환한다.
   *  - 상품ID
   *  - 상품제목
   *  - 총 모집금액
   *  - 현재 모집금액
   *  - 투자자 수
   *  - 투자모집 상태(모집중, 모집완료)
   *  - 상품 모집기간(상품 모집시작 시간, 상품 모집마감 시간)
   *
   * @param date 조회시점 시간정보
   * @return  유효기간내 투자 상품정보 반환
   */
  @GetMapping(path = Array("/products"))
  def getTotalProducts(@RequestParam("date") @DateTimeFormat(pattern="yyyyMMddHHmmss") date: Date): ResponseEntity[util.List[ProductDto]] = {
    val results = investmentService.getTotalProducts(date)
    if(results.isEmpty) ResponseEntity.noContent().build() else ResponseEntity.ok().body(results.asJava)
  }

  /**
   * 투자하기 api
   *
   * 투자시작 정보가 존재하지 않을 경우 비정상적 투자로 판단하 BAD_REQUEST를 반환
   *
   * @param userId header로 부터 전달받은 userId
   * @param investInfo 상품ID, 투자금액 정보
   * @return  투자 결과 정보
   * @throws IllegalArgumentException require의 조건에 만족하지 못하는 경우
   */
  @PostMapping(path = Array("/invest"))
  def investment(@RequestHeader("X-USER-ID") userId: Long, @RequestBody investInfo: UserInvestDto): ResponseEntity[UserInvestDto] = {
    require(userId > 0 && investInfo.productId > 0 && investInfo.amount > 0)

    investmentService.invest(investInfo.copy(userId=userId)) match {
      case r: UserInvestDto if r.startedAt == null =>
        new ResponseEntity(r, HttpStatus.BAD_REQUEST)
      case r: UserInvestDto =>
        new ResponseEntity(r, HttpStatus.ACCEPTED)
    }
  }

  /**
   * 나의 투자상품 조회
   *
   * UserId를 통해 상품ID, 상품제목, 총 모집금액, 나의 투자금액, 투자일시 등을 담은 투자정보를 반환한다.
   * 투자정보가 없는 경우 아무런 정보를 담지 않고 OK로 반환한다.
   *
   * @param userId  사용자 식별값
   * @return  userId로 조회되는 투자정보 반환
   */
  @GetMapping(path = Array("/myproducts"))
  def getMyInvestingProduct(@RequestHeader("X-USER-ID") userId: Long): ResponseEntity[util.List[MyProductDto]] = {
    val results = investmentService.getMyProducts(userId)
    ResponseEntity.ok().body(results.asJava)
  }
}

