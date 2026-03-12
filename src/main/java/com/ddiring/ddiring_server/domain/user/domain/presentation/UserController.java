package com.ddiring.ddiring_server.domain.user.domain.presentation;

import com.ddiring.ddiring_server.global.common.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping
    public ApiResponse<Void> test(){
        return ApiResponse.success(HttpStatus.OK, "test");
    }
}
