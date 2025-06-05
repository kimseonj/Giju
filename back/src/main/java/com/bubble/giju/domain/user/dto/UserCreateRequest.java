package com.bubble.giju.domain.user.dto;

import com.bubble.giju.domain.user.enums.Role;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Setter
@Getter
public class UserCreateRequest {
    @NotBlank(message = "아이디는 필수입니다")
    private String loginId;
    @NotBlank(message = "비밀번호는 필수입니다")
    private String password;
    @NotBlank(message = "이름은 필수입니다")
    private String name;
    @NotBlank(message = "Email은 필수입니다")
    @Email
    private String email;
    @NotBlank(message = "전화번호는 필수입니다.")
    private String phoneNumber;
    @Schema(example = "20020904", type = "string")
    @NotNull(message = "생년월일은 필수입니다.")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyyMMdd", timezone = "Asia/Seoul")
    @DateTimeFormat(pattern = "yyyyMMdd")
    private LocalDate birthDay;
    @NotNull(message = "역할은 필수입니다")
    private Role role;
}
