package com.re.rebankapp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import com.re.rebankapp.enums.Gender;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KycSubmitRequest {

    @NotBlank(message = "Số CCCD/CMND không được để trống")
    @Size(min = 9, max = 12, message = "Số CCCD/CMND phải từ 9 đến 12 ký tự")
    private String idNumber;

    @NotBlank(message = "Họ và tên không được để trống")
    @Size(max = 100, message = "Họ và tên không được quá 100 ký tự")
    private String fullName;

    @NotNull(message = "Ngày sinh không được để trống")
    @Past(message = "Ngày sinh phải là ngày trong quá khứ")
    private LocalDate dob;

    @NotNull(message = "Giới tính không được để trống")
    private Gender gender;

    private String address;

    @NotNull(message = "Ảnh mặt trước CCCD không được để trống")
    private MultipartFile idCardFrontImage;
}
