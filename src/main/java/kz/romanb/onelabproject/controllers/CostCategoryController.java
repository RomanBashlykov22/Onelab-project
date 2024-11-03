package kz.romanb.onelabproject.controllers;

import kz.romanb.onelabproject.exceptions.DBRecordNotFoundException;
import kz.romanb.onelabproject.models.dto.CostCategoryDto;
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
public class CostCategoryController {
    private final CostCategoryService costCategoryService;
    private final ModelMapper modelMapper;

    @GetMapping("/users/{userId}/cost-categories")
    public List<CostCategoryDto> getUsersCostCategories(@PathVariable Long userId) {
        return costCategoryService.getAllUserCostCategories(userId).stream().map(this::makeDto).toList();
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/users/{userId}/cost-categories")
    public CostCategoryDto addNewCostCategory(@PathVariable Long userId, @RequestBody CostCategoryDto costCategoryDto) {
        return makeDto(costCategoryService.addNewCostCategoryToUser(userId, costCategoryDto));
    }

    @GetMapping("/cost-categories/{costCategoryId}")
    public CostCategoryDto getCostCategoryById(@PathVariable Long costCategoryId) {
        Optional<CostCategory> costCategoryOptional = costCategoryService.findById(costCategoryId);
        if (costCategoryOptional.isEmpty()) {
            throw new DBRecordNotFoundException("Категории с id " + costCategoryId + " не существует");
        }
        return makeDto(costCategoryOptional.get());
    }

    private CostCategoryDto makeDto(CostCategory costCategory) {
        CostCategoryDto costCategoryDto = modelMapper.map(costCategory, CostCategoryDto.class);
        costCategoryDto.setUserDto(modelMapper.map(costCategory.getUser(), UserDto.class));
        return costCategoryDto;
    }
}
