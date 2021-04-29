package com.example.demo.domain.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ArticleFile {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  private String path;

  private LocalDate createdDate;

  @CreationTimestamp
  private LocalDateTime createdAt;
}
