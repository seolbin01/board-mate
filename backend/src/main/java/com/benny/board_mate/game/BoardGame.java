package com.benny.board_mate.game;

import com.benny.board_mate.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "board_games")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class BoardGame extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private Long bggId;

    @Column(nullable = false)
    private String title;

    @Column
    private String titleKorean;

    @Column(nullable = false)
    private int minPlayers;

    @Column(nullable = false)
    private int maxPlayers;

    private Integer playtime;

    private Integer minPlaytime;

    private Integer maxPlaytime;

    @Column(length = 20)
    private String difficulty;

    private Double weight;

    @Column(length = 500)
    private String imageUrl;

    @Column(length = 500)
    private String thumbnailUrl;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Integer yearPublished;

    private Double averageRating;

    private Integer numRatings;

    @Column(columnDefinition = "TEXT")
    private String mechanics;

    @Column(columnDefinition = "TEXT")
    private String categories;
}