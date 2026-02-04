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

-- 인기 게임 한글명 매핑 (Kaggle 데이터 임포트 후 실행)
UPDATE board_games SET title_korean = '카탄' WHERE bgg_id = 13 AND title_korean IS NULL;
UPDATE board_games SET title_korean = '팬데믹' WHERE bgg_id = 30549 AND title_korean IS NULL;
UPDATE board_games SET title_korean = '7 원더스' WHERE bgg_id = 68448 AND title_korean IS NULL;
UPDATE board_games SET title_korean = '도미니언' WHERE bgg_id = 36218 AND title_korean IS NULL;
UPDATE board_games SET title_korean = '티켓 투 라이드' WHERE bgg_id = 9209 AND title_korean IS NULL;
UPDATE board_games SET title_korean = '카르카손' WHERE bgg_id = 822 AND title_korean IS NULL;
UPDATE board_games SET title_korean = '스플렌더' WHERE bgg_id = 148228 AND title_korean IS NULL;
UPDATE board_games SET title_korean = '아그리콜라' WHERE bgg_id = 31260 AND title_korean IS NULL;
UPDATE board_games SET title_korean = '푸에르토 리코' WHERE bgg_id = 3076 AND title_korean IS NULL;
UPDATE board_games SET title_korean = '테라포밍 마스' WHERE bgg_id = 167791 AND title_korean IS NULL;
UPDATE board_games SET title_korean = '글룸헤이븐' WHERE bgg_id = 174430 AND title_korean IS NULL;
UPDATE board_games SET title_korean = '윙스팬' WHERE bgg_id = 266192 AND title_korean IS NULL;
UPDATE board_games SET title_korean = '아줄' WHERE bgg_id = 230802 AND title_korean IS NULL;
UPDATE board_games SET title_korean = '브라스: 버밍엄' WHERE bgg_id = 224517 AND title_korean IS NULL;
UPDATE board_games SET title_korean = '스컬킹' WHERE bgg_id = 316377 AND title_korean IS NULL;
UPDATE board_games SET title_korean = '코드네임' WHERE bgg_id = 178900 AND title_korean IS NULL;
UPDATE board_games SET title_korean = '디셉션: 홍콩 살인사건' WHERE bgg_id = 156129 AND title_korean IS NULL;
UPDATE board_games SET title_korean = '루미큐브' WHERE bgg_id = 811 AND title_korean IS NULL;
UPDATE board_games SET title_korean = '뱅!' WHERE bgg_id = 3955 AND title_korean IS NULL;
UPDATE board_games SET title_korean = '다빈치 코드' WHERE bgg_id = 2279 AND title_korean IS NULL;
UPDATE board_games SET title_korean = '할리갈리' WHERE bgg_id = 2944 AND title_korean IS NULL;
UPDATE board_games SET title_korean = '딕싯' WHERE bgg_id = 39856 AND title_korean IS NULL;
UPDATE board_games SET title_korean = '러브레터' WHERE bgg_id = 129622 AND title_korean IS NULL;
UPDATE board_games SET title_korean = '쿠' WHERE bgg_id = 131357 AND title_korean IS NULL;
UPDATE board_games SET title_korean = '마라케시' WHERE bgg_id = 29223 AND title_korean IS NULL;
UPDATE board_games SET title_korean = '블러핑' WHERE bgg_id = 45 AND title_korean IS NULL;
UPDATE board_games SET title_korean = '체스' WHERE bgg_id = 171 AND title_korean IS NULL;
UPDATE board_games SET title_korean = '모노폴리' WHERE bgg_id = 1406 AND title_korean IS NULL;
UPDATE board_games SET title_korean = '우노' WHERE bgg_id = 2223 AND title_korean IS NULL;
UPDATE board_games SET title_korean = '젠가' WHERE bgg_id = 2452 AND title_korean IS NULL;
UPDATE board_games SET title_korean = '클루' WHERE bgg_id = 1294 AND title_korean IS NULL;
UPDATE board_games SET title_korean = '스크래블' WHERE bgg_id = 320 AND title_korean IS NULL;
UPDATE board_games SET title_korean = '트위스터' WHERE bgg_id = 5894 AND title_korean IS NULL;
UPDATE board_games SET title_korean = '스도쿠' WHERE bgg_id = 20277 AND title_korean IS NULL;
UPDATE board_games SET title_korean = '오셀로' WHERE bgg_id = 2389 AND title_korean IS NULL;
UPDATE board_games SET title_korean = '젝스님트' WHERE bgg_id = 432 AND title_korean IS NULL;
UPDATE board_games SET title_korean = '보난자' WHERE bgg_id = 11 AND title_korean IS NULL;
UPDATE board_games SET title_korean = '라미' WHERE bgg_id = 190 AND title_korean IS NULL;
UPDATE board_games SET title_korean = '사보타지' WHERE bgg_id = 9220 AND title_korean IS NULL;
UPDATE board_games SET title_korean = '킹 오브 도쿄' WHERE bgg_id = 70323 AND title_korean IS NULL;
UPDATE board_games SET title_korean = '스몰 월드' WHERE bgg_id = 40692 AND title_korean IS NULL;
UPDATE board_games SET title_korean = '레지스탕스: 아발론' WHERE bgg_id = 128882 AND title_korean IS NULL;
UPDATE board_games SET title_korean = '시타델' WHERE bgg_id = 478 AND title_korean IS NULL;
UPDATE board_games SET title_korean = '라스베가스' WHERE bgg_id = 117959 AND title_korean IS NULL;
UPDATE board_games SET title_korean = '센추리: 스파이스 로드' WHERE bgg_id = 209685 AND title_korean IS NULL;
UPDATE board_games SET title_korean = '패치워크' WHERE bgg_id = 163412 AND title_korean IS NULL;
UPDATE board_games SET title_korean = '자이푸르' WHERE bgg_id = 54043 AND title_korean IS NULL;
UPDATE board_games SET title_korean = '포션 익스플로전' WHERE bgg_id = 180974 AND title_korean IS NULL;
UPDATE board_games SET title_korean = '마법의 미로' WHERE bgg_id = 41916 AND title_korean IS NULL;
UPDATE board_games SET title_korean = '드래곤을 쓰러뜨려라' WHERE bgg_id = 172933 AND title_korean IS NULL;
