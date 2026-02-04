package com.benny.board_mate.game;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GameEmbeddingRepository extends JpaRepository<GameEmbedding, Long> {

    @Query(value = """
        SELECT bgg_id, content, 1 - (embedding <=> CAST(:queryVector AS vector)) as similarity
        FROM game_embeddings
        ORDER BY embedding <=> CAST(:queryVector AS vector)
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> findSimilarGames(@Param("queryVector") String queryVector, @Param("limit") int limit);
}
