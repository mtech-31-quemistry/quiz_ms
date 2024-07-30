package com.quemistry.quiz_ms.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.quemistry.quiz_ms.client.model.MCQDto;
import com.quemistry.quiz_ms.controller.model.MCQResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class MCQMapperTest {

  private final MCQMapper mcqMapper = Mappers.getMapper(MCQMapper.class);

  @Test
  void toMCQResponse_MapsFieldsCorrectly() {
    MCQDto mcqDto = new MCQDto();
    mcqDto.setId(1L);
    mcqDto.setStem("Sample Question");
    mcqDto.setOptions(
        List.of(
            new MCQDto.OptionDto(1, "Option 1", "Explanation 1", true),
            new MCQDto.OptionDto(2, "Option 2", "Explanation 2", false)));
    mcqDto.setStatus("ACTIVE");

    MCQResponse mcqResponse = mcqMapper.toMCQResponse(mcqDto);

    assertThat(mcqResponse.getId()).isEqualTo(1L);
    assertThat(mcqResponse.getStem()).isEqualTo("Sample Question");
    assertThat(mcqResponse.getOptions()).hasSize(2);
    assertThat(mcqResponse.getOptions().get(0).getText()).isEqualTo("Option 1");
    assertThat(mcqResponse.getStatus()).isEqualTo("ACTIVE");
    assertThat(mcqResponse.getAttemptOption()).isNull();
    assertThat(mcqResponse.getAttemptOn()).isNull();
  }

  @Test
  void toMCQResponse_HandlesNullInput() {
    MCQResponse mcqResponse = mcqMapper.toMCQResponse(null);

    assertThat(mcqResponse).isNull();
  }

  @Test
  void toMCQResponse_HandlesEmptyFields() {
    MCQDto mcqDto = new MCQDto();

    MCQResponse mcqResponse = mcqMapper.toMCQResponse(mcqDto);

    assertThat(mcqResponse.getId()).isNull();
    assertThat(mcqResponse.getStem()).isNull();
    assertThat(mcqResponse.getOptions()).isNull();
    assertThat(mcqResponse.getStatus()).isNull();
    assertThat(mcqResponse.getAttemptOption()).isNull();
    assertThat(mcqResponse.getAttemptOn()).isNull();
  }
}
