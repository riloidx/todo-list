package com.riloidx.todolist.repository;

import com.riloidx.todolist.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findAllByUserIdAndCompletedOrderByUpdatedAtDesc(String userId, boolean completed);

    List<Task> findAllByUserIdAndCompletedFalseOrderByPositionAsc(String userId);

    @Query("SELECT MAX(t.position) " +
            "FROM Task t " +
            "WHERE t.userId = :userId")
    Integer findMaxPositionByUserId(@Param("userId") String userId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Task t " +
            "SET t.position = t.position + 1 " +
            "WHERE t.position >= :position " +
            "AND t.userId = :userId " +
            "AND t.completed = false")
    void incrementPositionsFrom(@Param("position") int position, @Param("userId") String userId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Task t " +
            "SET t.position = t.position - 1 " +
            "WHERE t.position > :position " +
            "AND t.userId = :userId " +
            "AND t.completed = false")
    void decrementPositionsFrom(@Param("position") int position, @Param("userId") String userId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Task t " +
            "SET t.position = t.position + 1 " +
            "WHERE t.position >= :startPosition " +
            "AND t.position < :endPosition " +
            "AND t.userId = :userId " +
            "AND t.completed = false")
    void incrementPositionsInRange(@Param("startPosition") int startPosition,
                                   @Param("endPosition") int endPosition,
                                   @Param("userId") String userId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Task t " +
            "SET t.position = t.position - 1 " +
            "WHERE t.position > :startPosition " +
            "AND t.position <= :endPosition " +
            "AND t.userId = :userId " +
            "AND t.completed = false")
    void decrementPositionsInRange(@Param("startPosition") int startPosition,
                                   @Param("endPosition") int endPosition,
                                   @Param("userId") String userId);
}
