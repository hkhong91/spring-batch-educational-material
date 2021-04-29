package com.example.demo.util.batch;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.database.JpaItemWriter;

@RequiredArgsConstructor
public class JpaItemListWriterBuilder<T> {

  private final JpaItemWriter<T> jpaItemWriter;

  public JpaItemListWriter<T> build() {
    return new JpaItemListWriter<>(this.jpaItemWriter);
  }
}
