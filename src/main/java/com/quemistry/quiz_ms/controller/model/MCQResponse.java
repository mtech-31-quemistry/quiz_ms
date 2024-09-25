package com.quemistry.quiz_ms.controller.model;

import com.quemistry.quiz_ms.client.model.MCQDto;
import com.quemistry.quiz_ms.model.QuizAttempt;
import java.util.Date;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
public class MCQResponse extends MCQDto {
  private Integer attemptOption;
  private Date attemptOn;

  public static MCQResponse from(QuizAttempt attempt, MCQDto mcqDto) {
    MCQResponseBuilder<?, ?> builder = MCQResponse.builder();
    builder
        .id(attempt.getMcqId())
        .attemptOption(attempt.getOptionNo())
        .attemptOn(attempt.getAttemptTime());

    if (mcqDto != null) {
      builder
          .stem(mcqDto.getStem())
          .options(mcqDto.getOptions())
          .topics(mcqDto.getTopics())
          .skills(mcqDto.getSkills())
          .status(mcqDto.getStatus())
          .publishedOn(mcqDto.getPublishedOn())
          .publishedBy(mcqDto.getPublishedBy())
          .closedOn(mcqDto.getClosedOn())
          .closedBy(mcqDto.getClosedBy())
          .createdOn(mcqDto.getCreatedOn())
          .createdBy(mcqDto.getCreatedBy());
    }
    return builder.build();
  }
}
