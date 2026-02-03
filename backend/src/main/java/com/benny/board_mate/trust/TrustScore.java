package com.benny.board_mate.trust;

import com.benny.board_mate.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "trust_scores")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class TrustScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Builder.Default
    @Column(nullable = false)
    private int score = 100;

    @Builder.Default
    @Column(nullable = false)
    private int totalParticipations = 0;

    @Builder.Default
    @Column(nullable = false)
    private int attendedCount = 0;

    @Builder.Default
    @Column(nullable = false)
    private int noShowCount = 0;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // AuthService에서 사용하는 생성자
    public TrustScore(User user) {
        this.user = user;
        this.score = 100;
        this.totalParticipations = 0;
        this.attendedCount = 0;
        this.noShowCount = 0;
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        if (this.updatedAt == null) {
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void addAttendance() {
        this.totalParticipations++;
        this.attendedCount++;
        this.score += 10;
        this.updatedAt = LocalDateTime.now();
    }

    public void addNoShow() {
        this.totalParticipations++;
        this.noShowCount++;
        this.score = Math.max(0, this.score - 30);
        this.updatedAt = LocalDateTime.now();
    }
}