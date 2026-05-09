package com.ddiring.ddiring_server.domain.photo.application.service;

import com.ddiring.ddiring_server.domain.family.domain.entity.Family;
import com.ddiring.ddiring_server.domain.family.domain.repository.FamilyMemberRepository;
import com.ddiring.ddiring_server.domain.family.domain.repository.FamilyRepository;
import com.ddiring.ddiring_server.domain.family.exception.FamilyMemberNotFoundException;
import com.ddiring.ddiring_server.domain.family.exception.FamilyNotFoundException;
import com.ddiring.ddiring_server.domain.photo.domain.entity.DailyPhoto;
import com.ddiring.ddiring_server.domain.photo.domain.entity.PhotoReaction;
import com.ddiring.ddiring_server.domain.photo.domain.entity.enums.EmojiType;
import com.ddiring.ddiring_server.domain.photo.domain.repository.DailyPhotoRepository;
import com.ddiring.ddiring_server.domain.photo.domain.repository.PhotoReactionRepository;
import com.ddiring.ddiring_server.domain.photo.exception.AlreadyPostedTodayException;
import com.ddiring.ddiring_server.domain.photo.exception.DailyPhotoNotFoundException;
import com.ddiring.ddiring_server.domain.photo.exception.PhotoAccessDeniedException;
import com.ddiring.ddiring_server.domain.photo.presentation.dto.request.CreateDailyPhotoRequest;
import com.ddiring.ddiring_server.domain.photo.presentation.dto.response.DailyPhotoResponse;
import com.ddiring.ddiring_server.domain.user.domain.entity.User;
import com.ddiring.ddiring_server.domain.user.domain.repository.UserRepository;
import com.ddiring.ddiring_server.domain.user.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DailyPhotoService {

    private final DailyPhotoRepository dailyPhotoRepository;
    private final PhotoReactionRepository photoReactionRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final FamilyRepository familyRepository;
    private final UserRepository userRepository;

    @Transactional
    public void createDailyPhoto(Long userId, CreateDailyPhotoRequest request) {
        Long familyId = familyMemberRepository.findFamilyIdByUserId(userId)
                .orElseThrow(FamilyMemberNotFoundException::new);

        LocalDate today = LocalDate.now();

        if (dailyPhotoRepository.existsByUser_IdAndFamily_IdAndTakenDate(userId, familyId, today)) {
            throw new AlreadyPostedTodayException();
        }

        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        Family family = familyRepository.findById(familyId)
                .orElseThrow(FamilyNotFoundException::new);

        dailyPhotoRepository.save(DailyPhoto.builder()
                .family(family)
                .user(user)
                .photoUrl(request.photoUrl())
                .caption(request.caption())
                .takenDate(today)
                .build());
    }

    @Transactional(readOnly = true)
    public List<DailyPhotoResponse> getDailyPhotos(Long userId, LocalDate date) {
        Long familyId = familyMemberRepository.findFamilyIdByUserId(userId)
                .orElseThrow(FamilyMemberNotFoundException::new);

        List<DailyPhoto> photos = dailyPhotoRepository
                .findAllByFamily_IdAndTakenDateOrderByCreatedAtDesc(familyId, date);

        if (photos.isEmpty()) {
            return List.of();
        }

        List<Long> photoIds = photos.stream().map(DailyPhoto::getId).toList();
        Map<Long, List<PhotoReaction>> reactionsByPhotoId = photoReactionRepository
                .findAllWithUserByPhotoIdIn(photoIds)
                .stream()
                .collect(Collectors.groupingBy(r -> r.getDailyPhoto().getId()));

        return photos.stream()
                .map(photo -> toResponse(photo, userId, reactionsByPhotoId.getOrDefault(photo.getId(), List.of())))
                .toList();
    }

    @Transactional
    public void toggleReaction(Long userId, Long photoId, EmojiType emojiType) {
        DailyPhoto photo = dailyPhotoRepository.findById(photoId)
                .orElseThrow(DailyPhotoNotFoundException::new);

        Long userFamilyId = familyMemberRepository.findFamilyIdByUserId(userId)
                .orElseThrow(FamilyMemberNotFoundException::new);

        if (!userFamilyId.equals(photo.getFamily().getId())) {
            throw new PhotoAccessDeniedException();
        }

        photoReactionRepository
                .findByDailyPhoto_IdAndUser_IdAndEmojiType(photoId, userId, emojiType)
                .ifPresentOrElse(
                        photoReactionRepository::delete,
                        () -> photoReactionRepository.save(PhotoReaction.builder()
                                .dailyPhoto(photo)
                                .user(userRepository.getReferenceById(userId))
                                .emojiType(emojiType)
                                .build())
                );
    }

    private DailyPhotoResponse toResponse(DailyPhoto photo, Long currentUserId, List<PhotoReaction> reactions) {
        Map<EmojiType, Long> counts = reactions.stream()
                .collect(Collectors.groupingBy(PhotoReaction::getEmojiType, Collectors.counting()));
        Set<EmojiType> myReactionTypes = reactions.stream()
                .filter(r -> r.getUser().getId().equals(currentUserId))
                .map(PhotoReaction::getEmojiType)
                .collect(Collectors.toSet());

        List<DailyPhotoResponse.ReactionCount> reactionCounts = Arrays.stream(EmojiType.values())
                .map(type -> new DailyPhotoResponse.ReactionCount(
                        type,
                        counts.getOrDefault(type, 0L),
                        myReactionTypes.contains(type)
                ))
                .toList();

        return DailyPhotoResponse.of(photo, reactionCounts);
    }
}
