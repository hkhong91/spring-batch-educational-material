package com.example.demo.util;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.database.JpaItemWriter;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class JpaItemListWriter<T> extends JpaItemWriter<List<T>> {

  private final JpaItemWriter<T> jpaItemWriter;

  @Override
  public void write(List<? extends List<T>> items) {
    List<T> list = new ArrayList<>();
    items.forEach(list::addAll);
    this.jpaItemWriter.write(list);
  }
}
