package com.bubble.giju.domain.user.dto;

import com.bubble.giju.domain.user.entity.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

@NoArgsConstructor
public class UserDto {

    @Getter
    public static class UserRequest {
        String userId;
        String name;
        @Email
        String email;
        String phoneNumber;
        @Schema(example = "20020904", type = "string")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyyMMdd", timezone = "Asia/Seoul")
        @DateTimeFormat(pattern = "yyyyMMdd")
        LocalDate birthday;
    }

    @Getter
    @AllArgsConstructor
    public static class Response {
        String userId;
        String loginId;
        String name;
        String email;
        String phoneNumber;
        LocalDate birthday;
        LocalDateTime createdAt;
        String role;

        public static Response fromEntity(User user) {
            return new Response(
                    user.getUserId().toString(),
                    user.getLoginId(),
                    user.getName(),
                    user.getEmail(),
                    user.getPhoneNumber(),
                    user.getBirthday(),
                    user.getCreatedAt(),
                    user.getRole().toString()
            );
        }

    }

}
