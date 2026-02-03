INSERT INTO board_games (title, min_players, max_players, playtime, difficulty, description, created_at, updated_at)
VALUES
    ('카탄', 3, 4, 90, '중급', '자원을 모아 섬을 개척하는 전략 게임', NOW(), NOW()),
    ('루미큐브', 2, 4, 60, '초급', '숫자 타일 조합 퍼즐 게임', NOW(), NOW()),
    ('스플렌더', 2, 4, 30, '초급', '보석 수집 전략 게임', NOW(), NOW()),
    ('티켓 투 라이드', 2, 5, 60, '초급', '기차 노선을 연결하는 전략 게임', NOW(), NOW()),
    ('아그리콜라', 1, 5, 120, '고급', '농장 경영 시뮬레이션', NOW(), NOW()),
    ('뱅', 4, 7, 30, '초급', '서부시대 배경 역할 추리 게임', NOW(), NOW()),
    ('다빈치 코드', 2, 4, 20, '초급', '숫자 추리 게임', NOW(), NOW()),
    ('할리갈리', 2, 6, 15, '초급', '과일 카드 순발력 게임', NOW(), NOW())
ON CONFLICT DO NOTHING;