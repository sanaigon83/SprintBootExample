package com.kakaopay.investment.utils

import java.sql.Timestamp
import java.time.LocalDateTime

import javax.persistence.{AttributeConverter, Converter}

@Converter(autoApply = true)
class LocalDateTimeConverter extends AttributeConverter[LocalDateTime, Timestamp] {
  override def convertToDatabaseColumn(attribute: LocalDateTime): Timestamp = {
    if(attribute == null) null else Timestamp.valueOf(attribute)
  }
  override def convertToEntityAttribute(dbData: Timestamp): LocalDateTime =
    if(dbData == null) null else dbData.toLocalDateTime
}
