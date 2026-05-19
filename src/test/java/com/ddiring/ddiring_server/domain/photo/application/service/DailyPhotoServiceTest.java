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
import com.ddiring.ddiring_server.domain.photo.presentation.dto.response.DailyPhotoFeedResponse;
import com.ddiring.ddiring_server.domain.photo.exception.PhotoAccessDeniedException;
import com.ddiring.ddiring_server.domain.photo.presentation.dto.request.CreateDailyPhotoRequest;
import com.ddiring.ddiring_server.domain.photo.presentation.dto.response.DailyPhotoResponse;
import com.ddiring.ddiring_server.domain.user.domain.entity.User;
import com.ddiring.ddiring_server.domain.user.domain.entity.enums.Role;
import com.ddiring.ddiring_server.domain.user.domain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DailyPhotoServiceTest {

    @Mock private DailyPhotoRepository dailyPhotoRepository;
    @Mock private PhotoReactionRepository photoReactionRepository;
    @Mock private FamilyMemberRepository familyMemberRepository;
    @Mock private FamilyRepository familyRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private DailyPhotoService dailyPhotoService;

    private final Long userId = 1L;
    private final Long familyId = 10L;
    private final Long photoId = 100L;

    // ──────────── createDailyPhoto ────────────

    @DisplayName("가족방에 속한 유저가 오늘 처음 게시글을 작성하면 저장에 성공한다")
    @Test
    void createDailyPhoto_성공() {
        // given
        User user = User.builder().name("김현수").role(Role.GUARDIAN).build();
        Family family = Family.builder().inviteCode("ABC123").createdBy(user).build();

        given(familyMemberRepository.findFamilyIdByUserId(userId)).willReturn(Optional.of(familyId));
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(familyRepository.findById(familyId)).willReturn(Optional.of(family));

        // when
        dailyPhotoService.createDailyPhoto(userId, new CreateDailyPhotoRequest("https://s3.example.com/photo.jpg", "좋은 기분이에요~"));

        // then
        verify(dailyPhotoRepository).save(any(DailyPhoto.class));
    }

    @DisplayName("가족방에 속하지 않은 유저가 게시글을 작성하면 FamilyMemberNotFoundException 이 발생한다")
    @Test
    void createDailyPhoto_실패_가족방없음() {
        // given
        given(familyMemberRepository.findFamilyIdByUserId(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> dailyPhotoService.createDailyPhoto(userId,
                new CreateDailyPhotoRequest("https://s3.example.com/photo.jpg", "오늘도 좋은 하루")))
                .isInstanceOf(FamilyMemberNotFoundException.class);

        verify(dailyPhotoRepository, never()).save(any());
    }

    // ──────────── getDailyPhotos ────────────

    @DisplayName("날짜별 게시글 목록을 조회하면 이모지 반응 집계가 포함된 응답을 반환한다")
    @Test
    void getDailyPhotos_성공() {
        // given
        LocalDate date = LocalDate.of(2026, 5, 9);
        User author = User.builder().name("김현수").role(Role.GUARDIAN).build();

        Family family = Family.builder().inviteCode("ABC123").createdBy(author).build();
        ReflectionTestUtils.setField(family, "id", familyId);

        DailyPhoto photo = DailyPhoto.builder()
                .family(family).user(author)
                .photoUrl("https://s3.example.com/photo.jpg")
                .caption("좋은 기분이에요~")
                .takenDate(date)
                .build();
        ReflectionTestUtils.setField(photo, "id", photoId);

        given(familyMemberRepository.findFamilyIdByUserId(userId)).willReturn(Optional.of(familyId));
        given(dailyPhotoRepository.findAllByFamily_IdAndTakenDateOrderByCreatedAtDesc(familyId, date))
                .willReturn(List.of(photo));
        given(photoReactionRepository.findAllWithUserByPhotoIdIn(List.of(photoId)))
                .willReturn(List.of());

        // when
        List<DailyPhotoResponse> responses = dailyPhotoService.getDailyPhotos(userId, date);

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).authorName()).isEqualTo("김현수");
        assertThat(responses.get(0).caption()).isEqualTo("좋은 기분이에요~");
        assertThat(responses.get(0).reactions()).hasSize(4);
        assertThat(responses.get(0).reactions()).allMatch(r -> r.count() == 0 && !r.myReaction());
    }

    @DisplayName("내가 반응한 이모지는 myReaction=true 로 반환된다")
    @Test
    void getDailyPhotos_성공_내반응포함() {
        // given
        LocalDate date = LocalDate.now();
        User author = User.builder().name("박성제").role(Role.ELDER).build();

        Family family = Family.builder().inviteCode("ABC123").createdBy(author).build();
        ReflectionTestUtils.setField(family, "id", familyId);

        DailyPhoto photo = DailyPhoto.builder()
                .family(family).user(author)
                .photoUrl("https://s3.example.com/photo.jpg")
                .caption("너무 맛있었어요!")
                .takenDate(date)
                .build();
        ReflectionTestUtils.setField(photo, "id", photoId);

        User currentUser = User.builder().build();
        ReflectionTestUtils.setField(currentUser, "id", userId);

        PhotoReaction smileReaction = PhotoReaction.builder()
                .dailyPhoto(photo).user(currentUser).emojiType(EmojiType.SMILE).build();

        given(familyMemberRepository.findFamilyIdByUserId(userId)).willReturn(Optional.of(familyId));
        given(dailyPhotoRepository.findAllByFamily_IdAndTakenDateOrderByCreatedAtDesc(familyId, date))
                .willReturn(List.of(photo));
        given(photoReactionRepository.findAllWithUserByPhotoIdIn(List.of(photoId)))
                .willReturn(List.of(smileReaction));

        // when
        List<DailyPhotoResponse> responses = dailyPhotoService.getDailyPhotos(userId, date);

        // then
        DailyPhotoResponse.ReactionCount smileCount = responses.get(0).reactions().stream()
                .filter(r -> r.emojiType() == EmojiType.SMILE)
                .findFirst().orElseThrow();

        assertThat(smileCount.count()).isEqualTo(1L);
        assertThat(smileCount.myReaction()).isTrue();
    }

    @DisplayName("게시글이 없는 날짜를 조회하면 빈 목록을 반환한다")
    @Test
    void getDailyPhotos_성공_빈목록() {
        // given
        LocalDate date = LocalDate.of(2026, 1, 1);

        given(familyMemberRepository.findFamilyIdByUserId(userId)).willReturn(Optional.of(familyId));
        given(dailyPhotoRepository.findAllByFamily_IdAndTakenDateOrderByCreatedAtDesc(familyId, date))
                .willReturn(List.of());

        // when
        List<DailyPhotoResponse> responses = dailyPhotoService.getDailyPhotos(userId, date);

        // then
        assertThat(responses).isEmpty();
    }

    @DisplayName("가족방에 속하지 않은 유저가 목록을 조회하면 FamilyMemberNotFoundException 이 발생한다")
    @Test
    void getDailyPhotos_실패_가족방없음() {
        // given
        given(familyMemberRepository.findFamilyIdByUserId(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> dailyPhotoService.getDailyPhotos(userId, LocalDate.now()))
                .isInstanceOf(FamilyMemberNotFoundException.class);
    }

    // ──────────── getDailyPhotoFeed ────────────

    @DisplayName("cursor 없이 피드를 조회하면 최신 게시글부터 size 개수만큼 반환된다")
    @Test
    void getDailyPhotoFeed_성공_첫페이지() {
        // given
        User author = User.builder().name("김현수").role(Role.GUARDIAN).build();
        Family family = Family.builder().inviteCode("ABC123").createdBy(author).build();
        ReflectionTestUtils.setField(family, "id", familyId);

        DailyPhoto photo = DailyPhoto.builder()
                .family(family).user(author)
                .photoUrl("https://s3.example.com/photo.jpg")
                .caption("첫 게시글")
                .takenDate(LocalDate.now())
                .build();
        ReflectionTestUtils.setField(photo, "id", photoId);

        given(familyMemberRepository.findFamilyIdByUserId(userId)).willReturn(Optional.of(familyId));
        given(dailyPhotoRepository.findByFamily_IdOrderByIdDesc(eq(familyId), any(Pageable.class)))
                .willReturn(List.of(photo));
        given(photoReactionRepository.findAllWithUserByPhotoIdIn(List.of(photoId)))
                .willReturn(List.of());

        // when
        DailyPhotoFeedResponse response = dailyPhotoService.getDailyPhotoFeed(userId, null, 20);

        // then
        assertThat(response.photos()).hasSize(1);
        assertThat(response.hasNext()).isFalse();
        assertThat(response.nextCursor()).isNull();
    }

    @DisplayName("cursor 를 전달하면 해당 ID 이전 게시글을 반환한다")
    @Test
    void getDailyPhotoFeed_성공_커서페이지() {
        // given
        Long cursor = 50L;
        User author = User.builder().name("박성제").role(Role.ELDER).build();
        Family family = Family.builder().inviteCode("ABC123").createdBy(author).build();
        ReflectionTestUtils.setField(family, "id", familyId);

        DailyPhoto photo = DailyPhoto.builder()
                .family(family).user(author)
                .photoUrl("https://s3.example.com/photo2.jpg")
                .caption("두 번째 게시글")
                .takenDate(LocalDate.now())
                .build();
        ReflectionTestUtils.setField(photo, "id", 30L);

        given(familyMemberRepository.findFamilyIdByUserId(userId)).willReturn(Optional.of(familyId));
        given(dailyPhotoRepository.findByFamily_IdAndIdLessThanOrderByIdDesc(eq(familyId), eq(cursor), any(Pageable.class)))
                .willReturn(List.of(photo));
        given(photoReactionRepository.findAllWithUserByPhotoIdIn(List.of(30L)))
                .willReturn(List.of());

        // when
        DailyPhotoFeedResponse response = dailyPhotoService.getDailyPhotoFeed(userId, cursor, 20);

        // then
        assertThat(response.photos()).hasSize(1);
        assertThat(response.hasNext()).isFalse();
        assertThat(response.nextCursor()).isNull();
    }

    @DisplayName("게시글이 없는 피드를 조회하면 빈 목록을 반환한다")
    @Test
    void getDailyPhotoFeed_성공_빈목록() {
        // given
        given(familyMemberRepository.findFamilyIdByUserId(userId)).willReturn(Optional.of(familyId));
        given(dailyPhotoRepository.findByFamily_IdOrderByIdDesc(eq(familyId), any(Pageable.class)))
                .willReturn(List.of());

        // when
        DailyPhotoFeedResponse response = dailyPhotoService.getDailyPhotoFeed(userId, null, 20);

        // then
        assertThat(response.photos()).isEmpty();
        assertThat(response.hasNext()).isFalse();
        assertThat(response.nextCursor()).isNull();
    }

    @DisplayName("가족방에 속하지 않은 유저가 피드를 조회하면 FamilyMemberNotFoundException 이 발생한다")
    @Test
    void getDailyPhotoFeed_실패_가족방없음() {
        // given
        given(familyMemberRepository.findFamilyIdByUserId(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> dailyPhotoService.getDailyPhotoFeed(userId, null, 20))
                .isInstanceOf(FamilyMemberNotFoundException.class);
    }

    // ──────────── toggleReaction ────────────

    @DisplayName("반응이 없을 때 이모지를 누르면 반응이 저장된다")
    @Test
    void toggleReaction_성공_반응추가() {
        // given
        User user = User.builder().build();
        Family family = Family.builder().inviteCode("ABC123").createdBy(user).build();
        ReflectionTestUtils.setField(family, "id", familyId);

        DailyPhoto photo = DailyPhoto.builder()
                .family(family).user(user).photoUrl("url").caption("오늘").takenDate(LocalDate.now()).build();

        given(dailyPhotoRepository.findById(photoId)).willReturn(Optional.of(photo));
        given(familyMemberRepository.findFamilyIdByUserId(userId)).willReturn(Optional.of(familyId));
        given(photoReactionRepository.findByDailyPhoto_IdAndUser_IdAndEmojiType(photoId, userId, EmojiType.HEART))
                .willReturn(Optional.empty());
        given(userRepository.getReferenceById(userId)).willReturn(user);

        // when
        dailyPhotoService.toggleReaction(userId, photoId, EmojiType.HEART);

        // then
        verify(photoReactionRepository).save(any(PhotoReaction.class));
    }

    @DisplayName("이미 반응이 존재하면 이모지를 다시 눌렀을 때 반응이 삭제된다")
    @Test
    void toggleReaction_성공_반응취소() {
        // given
        User user = User.builder().build();
        Family family = Family.builder().inviteCode("ABC123").createdBy(user).build();
        ReflectionTestUtils.setField(family, "id", familyId);

        DailyPhoto photo = DailyPhoto.builder()
                .family(family).user(user).photoUrl("url").caption("오늘").takenDate(LocalDate.now()).build();

        PhotoReaction existing = PhotoReaction.builder()
                .dailyPhoto(photo).user(user).emojiType(EmojiType.HEART).build();

        given(dailyPhotoRepository.findById(photoId)).willReturn(Optional.of(photo));
        given(familyMemberRepository.findFamilyIdByUserId(userId)).willReturn(Optional.of(familyId));
        given(photoReactionRepository.findByDailyPhoto_IdAndUser_IdAndEmojiType(photoId, userId, EmojiType.HEART))
                .willReturn(Optional.of(existing));

        // when
        dailyPhotoService.toggleReaction(userId, photoId, EmojiType.HEART);

        // then
        verify(photoReactionRepository).delete(existing);
        verify(photoReactionRepository, never()).save(any());
    }

    @DisplayName("존재하지 않는 게시글에 반응하면 DailyPhotoNotFoundException 이 발생한다")
    @Test
    void toggleReaction_실패_게시글없음() {
        // given
        given(dailyPhotoRepository.findById(photoId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> dailyPhotoService.toggleReaction(userId, photoId, EmojiType.SMILE))
                .isInstanceOf(DailyPhotoNotFoundException.class);
    }

    @DisplayName("가족방에 속하지 않은 유저가 반응하면 FamilyMemberNotFoundException 이 발생한다")
    @Test
    void toggleReaction_실패_가족방없음() {
        // given
        User user = User.builder().build();
        Family family = Family.builder().inviteCode("ABC123").createdBy(user).build();
        ReflectionTestUtils.setField(family, "id", familyId);

        DailyPhoto photo = DailyPhoto.builder()
                .family(family).user(user).photoUrl("url").caption("오늘").takenDate(LocalDate.now()).build();

        given(dailyPhotoRepository.findById(photoId)).willReturn(Optional.of(photo));
        given(familyMemberRepository.findFamilyIdByUserId(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> dailyPhotoService.toggleReaction(userId, photoId, EmojiType.SMILE))
                .isInstanceOf(FamilyMemberNotFoundException.class);
    }

    @DisplayName("다른 가족방의 게시글에 반응하면 PhotoAccessDeniedException 이 발생한다")
    @Test
    void toggleReaction_실패_다른가족방게시글() {
        // given
        User user = User.builder().build();
        Family otherFamily = Family.builder().inviteCode("ZZZ999").createdBy(user).build();
        ReflectionTestUtils.setField(otherFamily, "id", 99L);

        DailyPhoto photo = DailyPhoto.builder()
                .family(otherFamily).user(user).photoUrl("url").caption("오늘").takenDate(LocalDate.now()).build();

        given(dailyPhotoRepository.findById(photoId)).willReturn(Optional.of(photo));
        given(familyMemberRepository.findFamilyIdByUserId(userId)).willReturn(Optional.of(familyId));

        // when & then
        assertThatThrownBy(() -> dailyPhotoService.toggleReaction(userId, photoId, EmojiType.LAUGH))
                .isInstanceOf(PhotoAccessDeniedException.class);
    }
}
