package com.ddiring.ddiring_server.domain.photo.domain.repository;

import com.ddiring.ddiring_server.domain.photo.domain.entity.PhotoReaction;
import com.ddiring.ddiring_server.domain.photo.domain.entity.enums.EmojiType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PhotoReactionRepository extends JpaRepository<PhotoReaction, Long> {

    @Query("SELECT r FROM PhotoReaction r JOIN FETCH r.user WHERE r.dailyPhoto.id IN :photoIds")
    List<PhotoReaction> findAllWithUserByPhotoIdIn(@Param("photoIds") List<Long> photoIds);

    Optional<PhotoReaction> findByDailyPhoto_IdAndUser_IdAndEmojiType(Long photoId, Long userId, EmojiType emojiType);
}
