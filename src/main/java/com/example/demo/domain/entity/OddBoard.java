package com.example.demo.domain.entity;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OddBoard {

  @Id
  private long id;

  private String title;

  private String content;

  private LocalDateTime createdAt;
}
