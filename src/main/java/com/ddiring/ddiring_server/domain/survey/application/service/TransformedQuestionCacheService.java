package com.ddiring.ddiring_server.domain.survey.application.service;

import com.ddiring.ddiring_server.domain.survey.domain.entity.TransformedQuestionCache;
import com.ddiring.ddiring_server.domain.survey.domain.repository.TransformedQuestionCacheRepository;
import com.ddiring.ddiring_server.domain.user.domain.entity.User;
import com.ddiring.ddiring_server.domain.user.domain.repository.UserRepository;
import com.ddiring.ddiring_server.domain.user.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransformedQuestionCacheService {

    private final TransformedQuestionCacheRepository cacheRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Map<String, String> findCached(Long elderId, LocalDate date, List<String> questionKeys) {
        return cacheRepository
                .findByElderIdAndDateAndQuestionKeyIn(elderId, date, questionKeys)
                .stream()
                .collect(Collectors.toMap(
                        TransformedQuestionCache::getQuestionKey,
                        TransformedQuestionCache::getTransformed
                ));
    }

    @Transactional
    public void saveAll(Long elderId, LocalDate date, Map<String, String> transformedByKey) {
        if (transformedByKey.isEmpty()) {
            return;
        }
        User elder = userRepository.getReferenceById(elderId);
        List<TransformedQuestionCache> entries = transformedByKey.entrySet().stream()
                .map(e -> TransformedQuestionCache.builder()
                        .elder(elder)
                        .date(date)
                        .questionKey(e.getKey())
                        .transformed(e.getValue())
                        .build())
                .toList();

        for (TransformedQuestionCache entry : entries) {
            try {
                cacheRepository.save(entry);
            } catch (Exception ignored) {
                // UNIQUE 충돌(같은 날 이미 저장된 경우) 무시
            }
        }
    }
}
