package com.upsc.ai.entity;

import com.upsc.ai.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "topics", uniqueConstraints = { @UniqueConstraint(columnNames = { "subject_id", "name" }) })
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Topic extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonBackReference
    @lombok.ToString.Exclude
    @lombok.EqualsAndHashCode.Exclude
    private Subject subject;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;
}
