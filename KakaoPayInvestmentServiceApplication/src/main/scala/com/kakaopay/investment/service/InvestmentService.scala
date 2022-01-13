package com.kakaopay.investment.service

import java.time.LocalDateTime
import java.util.Date

import com.kakaopay.investment.dto.{MyProductDto, ProductDto, UserInvestDto}
import com.kakaopay.investment.entity.{InvestProduct, UserInvestInfo}
import com.kakaopay.investment.repository.{InvestProductRepository, UserInvestRepository}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import scala.jdk.CollectionConverters._
import scala.beans.BeanProperty

@Service
class InvestmentService {
  @Autowired
  @BeanProperty
  val productRepo: InvestProductRepository = null

  @Autowired
  @BeanProperty
  val userRepo: UserInvestRepository = null

  /**
   * 전체 투자 상품 리스트를 반환한다.
   *
   * @param date 조회시점 시간정보
   * @return 상품 모집기간 내 투자 상품 리스트 반환
   */
  def getTotalProducts(date: Date): List[ProductDto] = {
    productRepo.findByValidRange(date).asScala.toList.map { p: InvestProduct =>
      ProductDto(p.productId,
        p.title,
        p.totalAmount,
        p.curAmount,
        p.investorCount,
        p.startedAt,
        p.finishedAt,
        p.soldOut)
    }
  }

  /**
   * 사용자의 투자정보를 입력받아 투자정보를 저장한다.
   *
   * 다음과 같은 제한이 존재한다.
   *  - 같은 상품에 대한 중복투자는 허용하지 않는다.
   *  - 상품 정보를 찾을 수 없는 경우 투자를 허용하지 않는다.
   *  - 이미 판매가 완료된 경우 투자를 허용하지 않는다.
   *  - 잔여 투자 한도액이 투자금 보다 작은경우 잔여 투자 한도액만큼만 투자된다.
   *
   * @param uii 사용자 투자정보
   * @return 투자 결과 정보 반환
   */
  def invest(uii: UserInvestDto): UserInvestDto = {
    if(userRepo.findByUserId(uii.userId).asScala.toList.map(_.productId).contains(uii.productId)){
      // 중복 투자인 경우 중복 여부를 알린다.
      UserInvestDto(uii.userId, uii.productId, 0, null, "Duplicated")
    }
    else{
      productRepo.findByProductId(uii.productId) match{
        case null =>
          // 상품 정보를 찾을 수 없는 경우
          UserInvestDto(uii.userId, uii.productId, 0L, null, s"invalid productId=${uii.productId}")
        case product: InvestProduct if !product.soldOut =>
          // 판매가 완료되지 않은 경우 잔여투자액에 따라 투자금액을 설정한다.
          if(uii.amount + product.curAmount >= product.totalAmount){
            // 잔여투자액을 초과할 경우 잔여 투자액 만큼만 투자하고 sold-out 상태로 변경한다.
            val amount = product.totalAmount - product.curAmount
            processingInvest(uii.userId, product, amount, true)
          }
          else
            processingInvest(uii.userId, product, uii.amount, false)
        case product: InvestProduct if product.soldOut =>
          // 이미 판매가 완료된 경우 판매가 완료되었음을 알린다.
          UserInvestDto(uii.userId, uii.productId, 0L, null, "SoldOut")
      }
    }
  }

  /**
   * 나의 투자상품 조회
   * @param userId 사용자 식별값
   * @return 사용자가 투자한 상품 정보
   */
  def getMyProducts(userId: Long): List[MyProductDto] = {
    userRepo.findByUserId(userId).asScala.toList map { user: UserInvestInfo =>
      val product: InvestProduct = productRepo.findByProductId(user.productId)
      MyProductDto(user.productId, product.title, product.totalAmount, user.amount, user.startedAt)
    }
  }

  /**
   * 투자결과 저장
   * @param userId    사용자 식별값
   * @param product   투자상품정보
   * @param amount    투자금액
   * @param isSoldOut 투자모집상태 (true = 모집완료, false = 모집중)
   * @return  투자결과정보
   */
  private def processingInvest(userId: Long, product: InvestProduct, amount: Long, isSoldOut: Boolean): UserInvestDto = {
    val startedAt = LocalDateTime.now()
    userRepo.save(new UserInvestInfo(userId, product.productId, amount, startedAt))
    product.curAmount = product.curAmount + amount
    product.investorCount = product.investorCount + 1
    product.soldOut = isSoldOut
    productRepo.save(product)
    UserInvestDto(userId, product.productId, amount, startedAt, "Accepted")
  }
}