package com.benny.board_mate.room;

import com.benny.board_mate.room.dto.RoomSearchRequest;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RoomSpecification {

    public static Specification<Room> searchRooms(RoomSearchRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 필수: roomStatus = WAITING
            predicates.add(cb.equal(root.get("roomStatus"), RoomStatus.WAITING));

            // 필수: deletedAt IS NULL (소프트 삭제 제외)
            predicates.add(cb.isNull(root.get("deletedAt")));

            // 선택: region LIKE (대소문자 무시)
            if (request.getRegion() != null && !request.getRegion().isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("region")),
                        "%" + request.getRegion().toLowerCase() + "%"
                ));
            }

            // 선택: game.id = gameId
            if (request.getGameId() != null) {
                predicates.add(cb.equal(root.get("game").get("id"), request.getGameId()));
            }

            // 선택: gameDate가 해당 날짜 범위 내
            if (request.getDate() != null) {
                LocalDateTime startOfDay = request.getDate().atStartOfDay();
                LocalDateTime endOfDay = request.getDate().plusDays(1).atStartOfDay();
                predicates.add(cb.greaterThanOrEqualTo(root.get("gameDate"), startOfDay));
                predicates.add(cb.lessThan(root.get("gameDate"), endOfDay));
            }

            // 정렬: createdAt DESC
            query.orderBy(cb.desc(root.get("createdAt")));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
