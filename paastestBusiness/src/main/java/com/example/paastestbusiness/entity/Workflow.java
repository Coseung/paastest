package com.example.paastestbusiness.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class Workflow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String workSpaceName;

    @Column(nullable = false)
    private String definition;

}
