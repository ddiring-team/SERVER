package com.ddiring.ddiring_server.domain.photo.domain.entity;

import com.ddiring.ddiring_server.domain.photo.domain.entity.enums.EmojiType;
import com.ddiring.ddiring_server.domain.user.domain.entity.User;
import com.ddiring.ddiring_server.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "photo_reaction",
        uniqueConstraints = @UniqueConstraint(columnNames = {"daily_photo_id", "user_id", "emoji_type"}))
public class PhotoReaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_photo_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private DailyPhoto dailyPhoto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmojiType emojiType;
}