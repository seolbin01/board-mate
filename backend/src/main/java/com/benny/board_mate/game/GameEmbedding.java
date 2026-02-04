package com.benny.board_mate.game;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "game_embeddings")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class GameEmbedding {

    @Id
    private Long bggId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(columnDefinition = "vector(768)")
    private float[] embedding;
}
