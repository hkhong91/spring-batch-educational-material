package com.example.demo.domain.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeletedArticle {

  @Id
  private Long id;

  private String title;

  private String content;

  private LocalDateTime createdAt;

  private LocalDateTime updatedAt;

  @CreationTimestamp
  private LocalDateTime deletedAt;
}
