#!/bin/bash

# Board-Mate 테스트 데이터 생성 스크립트
# 사용법: ./scripts/generate-test-data.sh

BASE_URL="http://localhost:8080/api"

# 색상 출력
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${YELLOW}=== Board-Mate 테스트 데이터 생성 ===${NC}"

# 테스트 유저 생성
USERS=("testuser1" "testuser2" "testuser3" "testuser4" "testuser5")
TOKENS=()

echo -e "\n${GREEN}1. 테스트 유저 생성 중...${NC}"
for i in "${!USERS[@]}"; do
  USER="${USERS[$i]}"
  EMAIL="${USER}@test.com"

  # 회원가입 (이미 존재하면 무시)
  curl -s -X POST "$BASE_URL/auth/signup" \
    -H "Content-Type: application/json" \
    -d "{\"email\":\"$EMAIL\",\"password\":\"test1234\",\"nickname\":\"$USER\"}" > /dev/null 2>&1

  # 로그인
  RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
    -H "Content-Type: application/json" \
    -d "{\"email\":\"$EMAIL\",\"password\":\"test1234\"}")

  TOKEN=$(echo "$RESPONSE" | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)
  TOKENS+=("$TOKEN")
  echo "  - $USER 생성 완료"
done

echo -e "\n${GREEN}2. 방 30개 생성 중...${NC}"

# 지역 목록
REGIONS=("서울 강남" "서울 홍대" "서울 신촌" "서울 건대" "서울 잠실"
         "부산 서면" "부산 해운대" "인천 부평" "대전 둔산" "광주 충장로"
         "대구 동성로" "수원 인계동" "성남 판교" "고양 일산")

# 카페 목록
CAFES=("레드버튼" "다이브다이스" "롤링다이스" "보드게임카페 주사위"
       "보드엠" "젬스톤" "딸기당구장" "카페 더 보드" "플레이팩토리" "게임박스")

# 설명 목록
DESCRIPTIONS=(
  "초보자 환영합니다! 친절하게 알려드려요"
  "실력자분들 모집합니다"
  "즐겁게 게임하실 분들 오세요~"
  "첫 만남이지만 편하게 놀아요"
  "매주 정기 모임입니다"
  "가벼운 마음으로 오세요"
  "진지하게 게임하실 분!"
  "음료는 각자 주문해요"
  "늦으시면 연락주세요"
  ""
)

# 방 생성
for i in $(seq 1 30); do
  # 랜덤 선택
  USER_IDX=$((RANDOM % 5))
  TOKEN="${TOKENS[$USER_IDX]}"
  GAME_ID=$((RANDOM % 8 + 1))
  REGION="${REGIONS[$((RANDOM % ${#REGIONS[@]}))]}"
  CAFE="${CAFES[$((RANDOM % ${#CAFES[@]}))]}"
  DESC="${DESCRIPTIONS[$((RANDOM % ${#DESCRIPTIONS[@]}))]}"
  MAX_PARTICIPANTS=$((RANDOM % 4 + 3))  # 3-6명

  # 날짜: 오늘부터 14일 내 랜덤
  DAYS_AHEAD=$((RANDOM % 14 + 1))
  HOUR=$((RANDOM % 8 + 14))  # 14:00 ~ 21:00
  MINUTE=$(( (RANDOM % 2) * 30 ))  # 00 or 30

  # macOS와 Linux 호환 날짜 계산
  if [[ "$OSTYPE" == "darwin"* ]]; then
    GAME_DATE=$(date -v+${DAYS_AHEAD}d -u +"%Y-%m-%dT$(printf '%02d' $HOUR):$(printf '%02d' $MINUTE):00")
  else
    GAME_DATE=$(date -d "+${DAYS_AHEAD} days" -u +"%Y-%m-%dT$(printf '%02d' $HOUR):$(printf '%02d' $MINUTE):00")
  fi

  # 방 생성 API 호출
  RESPONSE=$(curl -s -X POST "$BASE_URL/rooms" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d "{
      \"gameId\": $GAME_ID,
      \"region\": \"$REGION\",
      \"cafeName\": \"$CAFE\",
      \"gameDate\": \"$GAME_DATE\",
      \"maxParticipants\": $MAX_PARTICIPANTS,
      \"description\": \"$DESC\"
    }")

  # 결과 확인
  if echo "$RESPONSE" | grep -q '"status":201'; then
    echo "  - 방 #$i 생성: $REGION / 게임ID=$GAME_ID / $GAME_DATE"
  else
    echo "  - 방 #$i 생성 실패: $RESPONSE"
  fi

  # API 부하 방지
  sleep 0.1
done

echo -e "\n${GREEN}=== 테스트 데이터 생성 완료 ===${NC}"
echo "생성된 유저: ${USERS[*]}"
echo "비밀번호: test1234"
echo ""
echo "프론트엔드에서 확인: http://localhost:5173"
