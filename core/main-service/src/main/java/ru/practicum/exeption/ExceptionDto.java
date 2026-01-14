package ru.practicum.exeption;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExceptionDto {
    private String status;
    private String reason;
    private String message;
    private String timestamp;
}
