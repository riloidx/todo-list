package com.riloidx.todolist.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Task extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @NotNull
    @Column(name = "user_id", nullable = false)
    private String userId;

    @NotNull
    @Column(name = "completed", nullable = false)
    private Boolean completed = false;

    @NotNull
    @Column(name = "position", nullable = false)
    private Integer position;
}
