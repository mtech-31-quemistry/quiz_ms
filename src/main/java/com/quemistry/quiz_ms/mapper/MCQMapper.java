package com.quemistry.quiz_ms.mapper;

import com.quemistry.quiz_ms.client.model.MCQDto;
import com.quemistry.quiz_ms.controller.model.MCQResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface MCQMapper {
  MCQMapper INSTANCE = Mappers.getMapper(MCQMapper.class);

  @Mapping(target = "attemptOption", expression = "java(null)")
  @Mapping(target = "attemptOn", expression = "java(null)")
  MCQResponse toMCQResponse(MCQDto mcqDto);
}
