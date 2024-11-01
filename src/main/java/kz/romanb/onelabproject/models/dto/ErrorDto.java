package kz.romanb.onelabproject.models.dto;

import lombok.*;

@Data
@Builder
public class ErrorDto {

    private String error;
    private String description;
}