package kz.romanb.onelabproject.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import kz.romanb.onelabproject.exceptions.DBRecordNotFoundException;
import kz.romanb.onelabproject.models.dto.CostCategoryDto;
import kz.romanb.onelabproject.models.dto.ErrorDto;
import kz.romanb.onelabproject.models.dto.UserDto;
import kz.romanb.onelabproject.models.entities.CostCategory;
import kz.romanb.onelabproject.services.CostCategoryService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(
        name = "Cost category controller",
        description = "Контроллер для управления категориями доходов и расходов пользователя"
)
@SecurityRequirement(name = "JWT")
public class CostCategoryController {
    private final CostCategoryService costCategoryService;
    private final ModelMapper modelMapper;

    @GetMapping("/users/{userId}/cost-categories")
    @Operation(
            summary = "Получение категорий пользователя",
            description = "Позволяет получить все категории расходов и доходов конкретного пользователя"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Получен список категорий расходов",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CostCategoryDto.class))),
            @ApiResponse(responseCode = "400", description = "Пользователь не найден",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)))
    })
    public List<CostCategoryDto> getUsersCostCategories(@PathVariable @Parameter(description = "ID пользователя", example = "1") Long userId) {
        return costCategoryService.getAllUserCostCategories(userId).stream().map(this::makeDto).toList();
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/users/{userId}/cost-categories")
    @Operation(
            summary = "Добавление категории",
            description = "Позволяет добавить пользователю новую категорию"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Добавлена новая категория",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CostCategoryDto.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка при добавлении категории",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)))
    })
    public CostCategoryDto addNewCostCategory(@PathVariable @Parameter(description = "ID пользователя", example = "1") Long userId,
                                              @RequestBody @Parameter(description = "Сведения о категории") CostCategoryDto costCategoryDto) {
        return makeDto(costCategoryService.addNewCostCategoryToUser(userId, costCategoryDto));
    }

    @GetMapping("/cost-categories/{costCategoryId}")
    @Operation(
            summary = "Получение категории по ID",
            description = "Позволяет получить информацию о категории по ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Получена категория",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CostCategoryDto.class))),
            @ApiResponse(responseCode = "400", description = "Категория не найдена",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)))
    })
    public CostCategoryDto getCostCategoryById(@PathVariable @Parameter(description = "ID категории", example = "1") Long costCategoryId) {
        Optional<CostCategory> costCategoryOptional = costCategoryService.findById(costCategoryId);
        if (costCategoryOptional.isEmpty()) {
            throw new DBRecordNotFoundException(String.format(CostCategoryService.COST_CATEGORY_WITH_ID_DOES_NOT_EXISTS, costCategoryId));
        }
        return makeDto(costCategoryOptional.get());
    }

    private CostCategoryDto makeDto(CostCategory costCategory) {
        CostCategoryDto costCategoryDto = modelMapper.map(costCategory, CostCategoryDto.class);
        costCategoryDto.setUserDto(modelMapper.map(costCategory.getUser(), UserDto.class));
        return costCategoryDto;
    }
}
