package io.github.xxyopen.novel.dao.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * 用户注册 请求DTO
 *
 * @author xiongxiaoyang
 * @date 2022/5/16
 */
@Data
public class UserRegisterDTO {

    @Schema(description = "用户名", required = true)
    @NotBlank(message = "用户名不能为空！")
    @Length(min = 2, max = 20, message = "用户名长度必须介于 2-20 之间！")
    @Pattern(regexp = "^[a-zA-Z0-9_\\u4e00-\\u9fa5]+$", message = "用户名只能包含字母、数字、下划线和中文！")
    private String username;

    @Schema(description = "邮箱", required = true)
    @NotBlank(message = "邮箱不能为空！")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "邮箱格式不正确！")
    private String email;

    @Schema(description = "密码", required = true)
    @NotBlank(message = "密码不能为空！")
    private String password;

    @Schema(description = "邮箱验证码", required = true)
    @NotBlank(message = "邮箱验证码不能为空！")
    @Pattern(regexp = "^\\d{6}$", message = "邮箱验证码格式不正确！")
    private String velCode;

}
