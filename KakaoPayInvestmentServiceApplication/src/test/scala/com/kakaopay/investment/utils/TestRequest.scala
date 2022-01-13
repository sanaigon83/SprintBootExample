package com.kakaopay.investment.utils

import java.util

import com.kakaopay.investment.dto.{MyProductDto, ProductDto, UserInvestDto}
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.{HttpEntity, HttpHeaders, HttpMethod, ResponseEntity}

trait TestRequest {

  def requestQueryProducts(url: String)(implicit testRestTemplate: TestRestTemplate): ResponseEntity[util.List[ProductDto]] = {
    testRestTemplate.getForEntity(url, classOf[util.List[ProductDto]])
  }

  def requestInvesting(url: String, userId: Long, productId: Int, amount: Long)(implicit testRestTemplate: TestRestTemplate): ResponseEntity[UserInvestDto] = {
    val headers = new HttpHeaders()
    headers.add("X-USER-ID", userId.toString)
    val req = new HttpEntity(UserInvestDto(userId, productId, amount, null, null), headers)
    testRestTemplate.postForEntity(url, req, classOf[UserInvestDto])
  }

  def requestMyProducts(url: String, userId: Long)(implicit testRestTemplate: TestRestTemplate): ResponseEntity[util.List[MyProductDto]] = {
    val headers = new HttpHeaders()
    headers.add("X-USER-ID", userId.toString)
    val req = new HttpEntity(null, headers)
    testRestTemplate.exchange(url, HttpMethod.GET, req, classOf[util.List[MyProductDto]])
  }
}
