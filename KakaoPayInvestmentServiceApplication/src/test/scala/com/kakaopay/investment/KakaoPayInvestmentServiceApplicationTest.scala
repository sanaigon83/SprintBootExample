package com.kakaopay.investment

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.{DefaultScalaModule, ScalaObjectMapper}
import com.kakaopay.investment.dto.{MyProductDto, ProductDto}
import com.kakaopay.investment.repository.{InvestProductRepository, UserInvestRepository}
import com.kakaopay.investment.utils.{SampleProduct, TestRequest}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.embedded.LocalServerPort
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.{HttpEntity, HttpHeaders, HttpMethod, HttpStatus}
import org.springframework.test.context.TestContextManager

import scala.collection.immutable.HashMap
import scala.jdk.CollectionConverters._
import scala.util.Random

@SpringBootTest(webEnvironment = RANDOM_PORT)
class KakaoPayInvestmentServiceApplicationTest extends AnyFlatSpec
  with Matchers
  with BeforeAndAfterEach
  with SampleProduct
  with TestRequest{

  @Autowired
  implicit var testRestTemplate: TestRestTemplate = _

  @Autowired
  implicit var productRepo: InvestProductRepository = _

  @Autowired
  var userRepo: UserInvestRepository = _

  @LocalServerPort
  var randomServerPort: Integer = _

  new TestContextManager(this.getClass).prepareTestInstance(this)

  lazy val baseUrl = s"http://localhost:$randomServerPort"
  lazy val totalProductUrl = s"$baseUrl/myproducts"
  lazy val investUrl = s"$baseUrl/invest"
  lazy val myProductUrl = s"$baseUrl/myproducts"

  val mapper = new ObjectMapper() with ScalaObjectMapper
  mapper.registerModule(DefaultScalaModule)

  override def beforeEach(): Unit = {
    productRepo.deleteAll()
    userRepo.deleteAll()
  }

  "KakaoInvestmentServiceApplicationTest" should "상품 조회시 모집기간 이내의 상품만 조회되어야 한다." in {
    /**
     * 테스트 시나리오
     * 1. 샘플 데이터 획득 및 DB에 저장
     * 2. 샘플 데이터내 유효한 시간을 임의로 선택하고 이를 이용해 get request 를 전달한다.
     * 3. 조회된 데이터가 샘플 데이터내 유효한 범위의 데이터와 일치하는지 확인한다.
     */
    withSampleProducts(){ products: Seq[ProductDto] =>
      // 샘플내 유효한 모집기간을 얻고 이를 통한 query를 수행할 URL을 만든다.
      val dateTime: LocalDateTime = getValidDateTime(products)
      val qryDate: String = dateTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
      val url = s"$baseUrl/products?date=$qryDate"
      val erProducts: Seq[ProductDto] = validProducts(dateTime)
      
      // 상품조회 및 결과확인
      val rep = requestQueryProducts(url)
      rep.getStatusCode should be(HttpStatus.OK)

      val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

      val arProducts: Seq[ProductDto] =
        mapper.convertValue(rep.getBody, classOf[List[HashMap[String, Object]]]) map { o =>
          ProductDto(
            o("productId").asInstanceOf[Int],
            o("title").asInstanceOf[String],
            o("totalAmount").asInstanceOf[Int],
            o("curAmount").asInstanceOf[Int],
            o("investorCount").asInstanceOf[Int],
            LocalDateTime.parse(o("startedAt").asInstanceOf[String], formatter),
            LocalDateTime.parse(o("finishedAt").asInstanceOf[String], formatter),
            o("soldOut").asInstanceOf[Boolean])
        }

      arProducts should contain theSameElementsAs erProducts
    }
  }

  it should "모집기간 이외의 기간에 대한 조회시 응답을 하지 않고 에러를 내보낸다" in {
    /**
     * 테스트 시나리오
     * 1. 샘플 데이터 획득 및 DB에 저장
     * 2. 샘플 데이터내 유효하지 않은 시간을 선택하고 이를 이용해 get request를 전달한다.
     * 3. NO CONTENT 에러를 내보내는지 확인한다.
     */
    withSampleProducts(){ products: Seq[ProductDto] =>
      val dateTime: LocalDateTime = getValidDateTime(products).plusYears(1000)
      val qryDate: String = dateTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
      val url = s"$baseUrl/products?date=$qryDate"

      requestQueryProducts(url).getStatusCode should be(HttpStatus.NO_CONTENT)
    }
  }

  it should "투자하기 수행시 정상적으로 투자정보가 저장되어야 한다." in {
    /**
     * 테스트 시나리오
     * 1. 샘플 데이터 획득 및 DB에 저장
     * 2. UserId를 할당하고 하나의 상품을 랜덤 선택해 임의의 수량만큼 투자하기 API를 호출한다.
     * 3. UserId로 투자한 상품 및 금액을 확인한다.
     * 4. 동일한 UserId로 다른 상품을 선택해 임의의 수량만큼 투자하기 API를 호출한다.
     * 5. UserId로 투자한 상품들을 조회하고 투자한 정보와 동일한지 확인한다.
     */
    withSampleProducts(){ products: Seq[ProductDto] =>
      val erUserId = Random.nextInt(10000) + 1
      val erProductId1 = products.head.productId
      val erProductId2 = products.last.productId
      val erAmount  = Random.nextInt(10000) + 1

      //사용가 상품에 투자하는 경우 투자정보 정상 확인
      val response1 = requestInvesting(investUrl, erUserId, erProductId1, erAmount)
      response1.getStatusCode should be(HttpStatus.ACCEPTED)

      val user = userRepo.findByUserId(erUserId.toLong).asScala.toList
      user.length should be(1)
      user.head.userId should be(erUserId)
      user.head.productId should be(erProductId1)
      user.head.amount should be(erAmount)
      user.head.startedAt should not be null

      //동일한 사용가 다른 상품에 하나 더 투자하는 경우
      val response2 = requestInvesting(investUrl, erUserId, erProductId2, erAmount)
      response2.getStatusCode should be(HttpStatus.ACCEPTED)

      val users = userRepo.findByUserId(erUserId.toLong).asScala.toList
      users.length should be(2)
      users.forall(_.userId == erUserId) should be(true)
      users.forall(_.amount == erAmount) should be(true)
      users.map(_.productId) should contain theSameElementsAs Seq(erProductId1, erProductId2)
    }
  }

  it should "존재하지 않는 상품 ID를 입력하는 경우 투자하기 기능이 동작하지 않아야 한다." in {
    val invalidProductId = 1
    val response1 = requestInvesting(investUrl, 1, invalidProductId, 1000)
    response1.getStatusCode should be(HttpStatus.BAD_REQUEST)
    response1.getBody.status should be(s"invalid productId=$invalidProductId")
  }

  it should "0보다 작은 금액으로 투자하는 경우 투자하기 기능이 동작하지 않아야 한다." in {
    withSampleProducts(1){ products: Seq[ProductDto] =>
      val productId = products.head.productId
      val response1 = requestInvesting(investUrl, 0, productId, -1000)
      response1.getStatusCode should be(HttpStatus.BAD_REQUEST)
    }
  }

  it should "같은 유저가 같은 상품에 중복투자 할 수 없어야 한다." in {
    /**
     * 테스트 시나리오
     * 1. 샘플 데이터 획득 및 DB에 저장
     * 2. 투자하기 API를 호출한다.
     * 3. 같은 상품에 대해 한번 더 투자하기 API를 호출한다.
     * 4. 중복투자 여부를 확인한다.
     */
    withSampleProducts(1){ products: Seq[ProductDto] =>
      val userId = Random.nextInt(10000) + 1
      val productId = products.head.productId
      val amount  = Random.nextInt(10000) + 1
      requestInvesting(investUrl, userId, productId, amount).getStatusCode should be (HttpStatus.ACCEPTED)
      // 같은 투자를 한 번더 시도한다.
      val rep = requestInvesting(investUrl, userId, productId, amount)
      rep.getBody.status should be ("Duplicated")
    }
  }

  it should "투자금액이 총 모집금액을 넘는 경우 잔여한도 만큼만 투자되고 상품은 sold-out 상태로 변경한다." in {
    /**
     * 테스트 시나리오
     * 1. 샘플 데이터 획득 및 h2 DB에 저장
     * 2. 하나의 상품을 랜덤 선택해 총 모집금액에 가까운 상태로 만든다
     * 3. 모집 잔여한도 보다 더 큰 금액을 투자한다.
     * 4. 유저의 투자 금액은 모집 잔여한도액인지 확인한다.
     * 5. 상품의 상태가 sold-out 상태로 변경되었는지 확인한다.
     */
    withSampleProducts(1){ products: Seq[ProductDto] =>
      val erLimitAmount = 10000
      val sampleProduct = products.head.copy(curAmount = products.head.totalAmount - erLimitAmount)
      productRepo.save(sampleProduct.toEntity)

      val userId = Random.nextInt(10000) + 1
      val productId = sampleProduct.productId
      val amount  = erLimitAmount + Random.nextInt(10000) + 1
      val rep  = requestInvesting(investUrl, userId, productId, amount)

      rep.getStatusCode should be (HttpStatus.ACCEPTED)
      rep.getBody.amount should be (erLimitAmount)

      productRepo.findByProductId(sampleProduct.productId).soldOut should be (true)
    }
  }

  it should "Sold-out 된 제품을 투자하려 하는 경우 투자하기 기능이 동작하지 않아야 하며 Sold-out 상태를 확인할 수 있어야 한다." in {
    /**
     * 테스트 시나리오
     * 1. 샘플 데이터 획득 및 DB에 저장
     * 2. 모집 한도액을 변경하고, 모집 완료 상태로 변경하여 DB에 반영한다.
     * 3. 투자하기 API를 호출한다.
     * 3. sold-out 상태로 응답한걸 확인한다.
     */
    withSampleProducts(1) { products: Seq[ProductDto] =>
      val sampleProduct = products.head.copy(curAmount = products.head.totalAmount, soldOut = true)
      productRepo.save(sampleProduct.toEntity)

      val userId = Random.nextInt(10000) + 1
      val productId = sampleProduct.productId
      val amount  = Random.nextInt(10000) + 1
      val rep  = requestInvesting(investUrl, userId, productId, amount)

      rep.getStatusCode should be (HttpStatus.BAD_REQUEST)
      rep.getBody.status should be ("SoldOut")
    }
  }

  it should "투자한 이력이 없는 사용자의 나의 투자상품 조회시 반환된 결과값이 없어야 한다." in {
    /**
     * 테스트 시나리오
     * 1. 투자정보가 존재하지 않는 사용자의 식별값으로 투자상품 조회를 시도한다.
     * 2. 결과값이 존재하지 않음을 확인한다.
     */
    val url = s"$baseUrl/myproducts"
    val headers = new HttpHeaders()
    headers.add("X-USER-ID", (Random.nextInt(1000)+1).toString)
    val req = new HttpEntity(null, headers)
    val rep = testRestTemplate.exchange(url, HttpMethod.GET, req, classOf[util.List[MyProductDto]])
    rep.getStatusCode should be(HttpStatus.OK)
    rep.getBody.size() should be(0)
  }

  it should "나의 투자상품 조회시 해당 사용자에 대한 투자정보가 정상적으로 조회되어야 한다." in {
    /**
     * 테스트 시나리오
     * 1. 한 명의 사용자가 임의의 수의 상품에 투자된 상태로 설정한다.
     * 2. 나의 투자상품 조회 api 호출 시 투자된 정보와 일치하는지 확인한다.
     */
    val erProductCount = 3
    val erUserId = Random.nextInt(10) + 1
    withSampleProducts(erProductCount){ products: Seq[ProductDto] =>
      val erMyProductMap = products.map{ p =>
        val amount = Random.nextInt(1000000) + 1
        requestInvesting(investUrl, erUserId, p.productId, amount).getStatusCode should be(HttpStatus.ACCEPTED)
        (p.productId, (p.title, p.totalAmount, amount))
      }.toMap

      val rep = requestMyProducts(myProductUrl, erUserId)
      rep.getStatusCode should be(HttpStatus.OK)

      val myProducts = mapper.convertValue(rep.getBody, classOf[List[HashMap[String, Object]]])
      myProducts.length should be(erProductCount)
      myProducts foreach { p =>
        (p("title").asInstanceOf[String], 
         p("totalAmount").asInstanceOf[Int], 
         p("myAmount").asInstanceOf[Int]) should be (erMyProductMap(p("pid").asInstanceOf[Integer]))
      }
    }
  }
}
