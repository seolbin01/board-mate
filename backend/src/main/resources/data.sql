INSERT INTO board_games (title, bgg_id, min_players, max_players, playtime, difficulty, description, created_at, updated_at)
SELECT '카탄', 13, 3, 4, 90, '중급', '자원을 모아 섬을 개척하는 전략 게임', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM board_games WHERE title = '카탄');

INSERT INTO board_games (title, bgg_id, min_players, max_players, playtime, difficulty, description, created_at, updated_at)
SELECT '루미큐브', 811, 2, 4, 60, '초급', '숫자 타일 조합 퍼즐 게임', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM board_games WHERE title = '루미큐브');

INSERT INTO board_games (title, bgg_id, min_players, max_players, playtime, difficulty, description, created_at, updated_at)
SELECT '스플렌더', 148228, 2, 4, 30, '초급', '보석 수집 전략 게임', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM board_games WHERE title = '스플렌더');

INSERT INTO board_games (title, bgg_id, min_players, max_players, playtime, difficulty, description, created_at, updated_at)
SELECT '티켓 투 라이드', 9209, 2, 5, 60, '초급', '기차 노선을 연결하는 전략 게임', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM board_games WHERE title = '티켓 투 라이드');

INSERT INTO board_games (title, bgg_id, min_players, max_players, playtime, difficulty, description, created_at, updated_at)
SELECT '아그리콜라', 31260, 1, 5, 120, '고급', '농장 경영 시뮬레이션', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM board_games WHERE title = '아그리콜라');

INSERT INTO board_games (title, bgg_id, min_players, max_players, playtime, difficulty, description, created_at, updated_at)
SELECT '뱅', 3955, 4, 7, 30, '초급', '서부시대 배경 역할 추리 게임', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM board_games WHERE title = '뱅');

INSERT INTO board_games (title, bgg_id, min_players, max_players, playtime, difficulty, description, created_at, updated_at)
SELECT '다빈치 코드', 2279, 2, 4, 20, '초급', '숫자 추리 게임', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM board_games WHERE title = '다빈치 코드');

INSERT INTO board_games (title, bgg_id, min_players, max_players, playtime, difficulty, description, created_at, updated_at)
SELECT '할리갈리', 2944, 2, 6, 15, '초급', '과일 카드 순발력 게임', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM board_games WHERE title = '할리갈리');
