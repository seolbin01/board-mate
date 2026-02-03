package com.benny.board_mate.review;

import com.benny.board_mate.common.entity.BaseEntity;
import com.benny.board_mate.room.Room;
import com.benny.board_mate.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reviews", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"reviewer_id", "reviewee_id", "room_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private User reviewer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewee_id", nullable = false)
    private User reviewee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(nullable = false)
    private int rating; // 1-5

    @Column(length = 500)
    private String comment;

    @Builder
    public Review(User reviewer, User reviewee, Room room, int rating, String comment) {
        this.reviewer = reviewer;
        this.reviewee = reviewee;
        this.room = room;
        this.rating = rating;
        this.comment = comment;
    }
}
