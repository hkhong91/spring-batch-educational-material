package com.example.demo.domain.repository;

import com.example.demo.domain.entity.DeletedBoard;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeletedBoardRepository extends JpaRepository<DeletedBoard, Long> {
}
