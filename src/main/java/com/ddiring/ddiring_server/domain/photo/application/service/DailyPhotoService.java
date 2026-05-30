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
import com.ddiring.ddiring_server.domain.photo.exception.DailyPhotoNotFoundException;
import com.ddiring.ddiring_server.domain.photo.exception.PhotoAccessDeniedException;
import com.ddiring.ddiring_server.domain.photo.presentation.dto.request.CreateDailyPhotoRequest;
import com.ddiring.ddiring_server.domain.photo.presentation.dto.response.DailyPhotoFeedResponse;
import com.ddiring.ddiring_server.domain.photo.presentation.dto.response.DailyPhotoResponse;
import com.ddiring.ddiring_server.domain.user.domain.entity.User;
import com.ddiring.ddiring_server.domain.user.domain.repository.UserRepository;
import com.ddiring.ddiring_server.domain.user.exception.UserNotFoundException;
import com.ddiring.ddiring_server.domain.distance.application.event.DistanceResetEvent;
import com.ddiring.ddiring_server.domain.distance.domain.entity.enums.DistanceActionType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void createDailyPhoto(Long userId, CreateDailyPhotoRequest request) {
        Long familyId = familyMemberRepository.findFamilyIdByUserId(userId)
                .orElseThrow(FamilyMemberNotFoundException::new);

        LocalDate today = LocalDate.now();

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

    @Transactional(readOnly = true)
    public DailyPhotoFeedResponse getDailyPhotoFeed(Long userId, Long cursor, int size) {
        Long familyId = familyMemberRepository.findFamilyIdByUserId(userId)
                .orElseThrow(FamilyMemberNotFoundException::new);

        Pageable pageable = PageRequest.of(0, size + 1);

        List<DailyPhoto> photos = (cursor == null)
                ? dailyPhotoRepository.findByFamily_IdOrderByIdDesc(familyId, pageable)
                : dailyPhotoRepository.findByFamily_IdAndIdLessThanOrderByIdDesc(familyId, cursor, pageable);

        boolean hasNext = photos.size() > size;
        if (hasNext) {
            photos = photos.subList(0, size);
        }

        if (photos.isEmpty()) {
            return new DailyPhotoFeedResponse(List.of(), null, false);
        }

        List<Long> photoIds = photos.stream().map(DailyPhoto::getId).toList();
        Map<Long, List<PhotoReaction>> reactionsByPhotoId = photoReactionRepository
                .findAllWithUserByPhotoIdIn(photoIds)
                .stream()
                .collect(Collectors.groupingBy(r -> r.getDailyPhoto().getId()));

        List<DailyPhotoResponse> responses = photos.stream()
                .map(p -> toResponse(p, userId, reactionsByPhotoId.getOrDefault(p.getId(), List.of())))
                .toList();

        Long nextCursor = hasNext ? photos.get(photos.size() - 1).getId() : null;

        User viewer = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        if (viewer.getRole() != null
                && dailyPhotoRepository.existsTodayPhotoByOppositeRole(familyId, LocalDate.now(), viewer.getRole())) {
            eventPublisher.publishEvent(
                    DistanceResetEvent.broadcast(userId, DistanceActionType.PHOTO_VIEW));
        }

        return new DailyPhotoFeedResponse(responses, nextCursor, hasNext);
    }

    @Transactional(readOnly = true)
    public DailyPhotoResponse getPhotoById(Long userId, Long photoId) {
        DailyPhoto photo = dailyPhotoRepository.findById(photoId)
                .orElseThrow(DailyPhotoNotFoundException::new);

        Long userFamilyId = familyMemberRepository.findFamilyIdByUserId(userId)
                .orElseThrow(FamilyMemberNotFoundException::new);

        if (!userFamilyId.equals(photo.getFamily().getId())) {
            throw new PhotoAccessDeniedException();
        }

        List<PhotoReaction> reactions = photoReactionRepository
                .findAllWithUserByPhotoIdIn(List.of(photoId));
        return toResponse(photo, userId, reactions);
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
