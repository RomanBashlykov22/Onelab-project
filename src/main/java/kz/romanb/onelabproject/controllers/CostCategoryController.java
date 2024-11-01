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
    public List<CostCategoryDto> getUsersCostCategories(@PathVariable Long userId){
        return costCategoryService.getAllUserCostCategories(userId).stream().map(c -> {
            CostCategoryDto dto = modelMapper.map(c, CostCategoryDto.class);
            dto.setUserDto(modelMapper.map(c.getUser(), UserDto.class));
            return dto;
        }).toList();
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/users/{userId}/cost-categories")
    public CostCategoryDto addNewCostCategory(@PathVariable Long userId, @RequestBody CostCategoryDto costCategoryDto){
        CostCategory costCategory = costCategoryService.addNewCostCategoryToUser(userId, costCategoryDto);
        CostCategoryDto saved = modelMapper.map(costCategory, CostCategoryDto.class);
        saved.setUserDto(modelMapper.map(costCategory.getUser(), UserDto.class));
        return saved;
    }

    @GetMapping("/cost-categories/{costCategoryId}")
    public CostCategoryDto getCostCategoryById(@PathVariable Long costCategoryId){
        Optional<CostCategory> costCategoryOptional = costCategoryService.findById(costCategoryId);
        if(costCategoryOptional.isEmpty()){
            throw new DBRecordNotFoundException("Категории с id " + costCategoryId + " не существует");
        }
        CostCategoryDto costCategoryDto = modelMapper.map(costCategoryOptional.get(), CostCategoryDto.class);
        costCategoryDto.setUserDto(modelMapper.map(costCategoryOptional.get().getUser(), UserDto.class));
        return costCategoryDto;
    }
}
