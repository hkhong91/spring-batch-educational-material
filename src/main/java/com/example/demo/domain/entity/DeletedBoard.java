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
public class DeletedBoard {

  @Id
  private long id;

  private String title;

  private String content;

  private boolean isDeleted;

  private LocalDateTime createdAt;

  private LocalDateTime deletedAt;
}
