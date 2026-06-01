package com.ddiring.ddiring_server.domain.temperature.presentation.dto.response;

import com.ddiring.ddiring_server.domain.temperature.domain.entity.UserTemperature;

import java.math.BigDecimal;

public record UserTemperatureResponse(
        Long userId,
        BigDecimal temperature
) {
    public static UserTemperatureResponse of(UserTemperature entity) {
        return new UserTemperatureResponse(entity.getUserId(), entity.getTemperature());
    }

    public static UserTemperatureResponse defaultFor(Long userId) {
        return new UserTemperatureResponse(userId, UserTemperature.BASE);
    }
}
