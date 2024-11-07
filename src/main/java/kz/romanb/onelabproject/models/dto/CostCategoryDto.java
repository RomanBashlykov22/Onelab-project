package kz.romanb.onelabproject.models.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import kz.romanb.onelabproject.models.entities.CostCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Информация о категории")
public class CostCategoryDto {
    @Schema(description = "ID категории", example = "1")
    private Long id;
    @Schema(description = "Имя категории", example = "Sport")
    private String name;
    @Schema(description = "Тип категории", allowableValues = {"EXPENSE", "INCOME"})
    private CostCategory.CostCategoryType categoryType;
    @Schema(description = "Данные о пользователе-хозяине категории")
    @JsonProperty("user")
    private UserDto userDto;
}
