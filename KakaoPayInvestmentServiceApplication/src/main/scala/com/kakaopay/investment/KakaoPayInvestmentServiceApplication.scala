package com.kakaopay.investment

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication


@SpringBootApplication
class KakaoPayInvestmentServiceApplication{}

object KakaoPayInvestmentServiceApplication extends App{
  SpringApplication.run(classOf[KakaoPayInvestmentServiceApplication], args:_ *)
}
