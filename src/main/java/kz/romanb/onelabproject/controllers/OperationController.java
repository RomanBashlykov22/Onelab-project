package kz.romanb.onelabproject.controllers;

import kz.romanb.onelabproject.exceptions.DBRecordNotFoundException;
import kz.romanb.onelabproject.models.dto.*;
import kz.romanb.onelabproject.models.entities.Operation;
import kz.romanb.onelabproject.services.OperationService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class OperationController {
    private final OperationService operationService;
    private final ModelMapper modelMapper;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/operations/create")
    public OperationDto createOperation(@RequestParam Long bankAccountId,
                                        @RequestParam Long costCategoryId,
                                        @RequestParam BigDecimal amount) {
        Operation operation = operationService.createOperation(bankAccountId, costCategoryId, amount);
        return makeDto(operation);
    }

    @GetMapping("/operations/{operationId}")
    public OperationDto getOperationById(@PathVariable Long operationId) {
        Optional<Operation> operationOptional = operationService.findOperationById(operationId);
        if (operationOptional.isEmpty()) {
            throw new DBRecordNotFoundException("Операция с id " + operationId + " не существует");
        }
        return makeDto(operationOptional.get());
    }

    @GetMapping("/operations")
    public List<OperationDto> getOperations(@RequestParam(required = false) @DateTimeFormat(pattern = "dd.MM.yyyy") Optional<LocalDate> fromDate,
                                            @RequestParam(required = false) @DateTimeFormat(pattern = "dd.MM.yyyy") Optional<LocalDate> toDate) {
        List<Operation> operations;
        if(fromDate.isPresent() && toDate.isPresent()){
            operations = operationService.findAllOperationsBetweenDates(fromDate.get(), toDate.get());
        }
        else if(fromDate.isPresent()){
            operations = operationService.findAllOperationsForDate(fromDate.get());
        }
        else{
            operations = operationService.findAllOperations();
        }
        return operations.stream()
                .map(this::makeDto)
                .sorted(Comparator.comparing(OperationDto::getDate).reversed())
                .toList();
    }

    @GetMapping("/users/{userId}/operations")
    public List<OperationDto> getAllOperationsByUser(@PathVariable Long userId){
        List<Operation> operations = operationService.findAllOperationsByUser(userId);
        return operations.stream()
                .map(this::makeDto)
                .sorted(Comparator.comparing(OperationDto::getDate).reversed())
                .toList();
    }

    @GetMapping("/cost-categories/{costCategoryId}/operations")
    public List<OperationDto> getAllOperationsByCostCategory(@PathVariable Long costCategoryId){
        List<Operation> operations = operationService.findAllOperationsByCostCategory(costCategoryId);
        return operations.stream()
                .map(this::makeDto)
                .sorted(Comparator.comparing(OperationDto::getDate).reversed())
                .toList();
    }

    @GetMapping("/sum")
    public SumResponse getSum(@RequestBody List<OperationDto> operations){
        return operationService.getSum(operations);
    }

    private OperationDto makeDto(Operation operation){
        OperationDto dto = modelMapper.map(operation, OperationDto.class);
        dto.setBankAccountDto(modelMapper.map(operation.getBankAccount(), BankAccountDto.class));
        dto.setCostCategoryDto(modelMapper.map(operation.getCostCategory(), CostCategoryDto.class));
        UserDto userDto = modelMapper.map(operation.getCostCategory().getUser(), UserDto.class);
        dto.getBankAccountDto().setUserDto(userDto);
        dto.getCostCategoryDto().setUserDto(userDto);
        return dto;
    }
}
