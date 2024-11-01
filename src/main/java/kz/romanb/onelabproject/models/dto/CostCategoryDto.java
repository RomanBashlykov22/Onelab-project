package kz.romanb.onelabproject.models.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import kz.romanb.onelabproject.models.entities.CostCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CostCategoryDto {
    private Long id;
    private String name;
    private CostCategory.CostCategoryType categoryType;
    @JsonProperty("user")
    private UserDto userDto;
}
