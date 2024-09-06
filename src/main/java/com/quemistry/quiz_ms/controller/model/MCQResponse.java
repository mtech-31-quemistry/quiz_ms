package com.quemistry.quiz_ms.controller.model;

import com.quemistry.quiz_ms.client.model.MCQDto;
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
}
