package com.example.demo.domain.repository;

import com.example.demo.domain.entity.OddBoard;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OddBoardRepository extends JpaRepository<OddBoard, Long> {
}
