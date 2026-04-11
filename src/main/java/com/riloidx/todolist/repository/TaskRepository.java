package com.riloidx.todolist.repository;

import com.riloidx.todolist.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findAllByUserIdAndCompletedOrderByUpdatedAtDesc(String userId, boolean completed);

    List<Task> findAllByUserIdAndCompletedFalseOrderByPositionDesc(String userId);

    @Query("SELECT MAX(t.position) FROM Task t WHERE t.userId = :userId")
    Integer findMaxPositionByUserId(@Param("userId")String userId);
}
