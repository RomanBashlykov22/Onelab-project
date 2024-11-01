package kz.romanb.onelabproject.models.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankAccountDto {

    private Long id;
    private String name;
    private BigDecimal balance;
    @JsonProperty("user")
    private UserDto userDto;
}
