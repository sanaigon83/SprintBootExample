package com.kakaopay.investment.utils

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.{ExceptionHandler, RestControllerAdvice}

@RestControllerAdvice
class ExceptionController {

  val logger = LoggerFactory.getLogger(classOf[ExceptionController])

  @ExceptionHandler(Array(classOf[IllegalArgumentException]))
  def dateFormatParseExceptionHandler(e: IllegalArgumentException): ResponseEntity[String] ={
    logger.error(e.getMessage)
    ResponseEntity.badRequest().body("")
  }
}

