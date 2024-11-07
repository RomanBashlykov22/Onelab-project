package kz.romanb.onelabproject.controllers;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Operation controller", description = "Контроллер для управления операциями пользователя")
@SecurityRequirement(name = "JWT")
public class OperationController {
    private final OperationService operationService;
    private final ModelMapper modelMapper;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/operations/create")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Создание операции",
            description = "Позволяет создать операцию расхода или дохода"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Создана операция",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = OperationDto.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка при создании операции",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)))
    })
    public OperationDto createOperation(@RequestParam @Parameter(description = "ID счета", example = "1") Long bankAccountId,
                                        @RequestParam @Parameter(description = "ID категории", example = "1") Long costCategoryId,
                                        @RequestParam @Parameter(description = "Сумма операции", example = "1000") BigDecimal amount) {
        return makeDto(operationService.createOperation(bankAccountId, costCategoryId, amount));
    }

    @GetMapping("/operations/{operationId}")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Получение операции по ID",
            description = "Позволяет получить информацию об операции по ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Получена операция",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = OperationDto.class))),
            @ApiResponse(responseCode = "400", description = "Операция не найдена",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)))
    })
    public OperationDto getOperationById(@PathVariable @Parameter(description = "ID операции", example = "1") Long operationId) {
        Optional<Operation> operationOptional = operationService.findOperationById(operationId);
        if (operationOptional.isEmpty()) {
            throw new DBRecordNotFoundException(String.format(OperationService.OPERATION_WITH_ID_DOES_NOT_EXISTS, operationId));
        }
        return makeDto(operationOptional.get());
    }

    @GetMapping("/operations")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Получение операций",
            description = """
                    Позволяет получить список операций в зависимости от передаваемых аргументов:
                    1) Без аргументов - получение всех операций в приложении
                    2) С одним аргументом даты - получение всех операций на конкретную дату
                    3) С двуми аргументами даты - получение всех операций в промежутке времени
                    """
    )
    @ApiResponse(responseCode = "200", description = "Получен список операций",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = OperationDto.class)))
    public List<OperationDto> getOperations(@RequestParam(required = false) @DateTimeFormat(pattern = "dd.MM.yyyy") @Parameter(description = "Начало временного промежутка (если является единственным аргументом - конкретная дата)") Optional<LocalDate> fromDate,
                                            @RequestParam(required = false) @DateTimeFormat(pattern = "dd.MM.yyyy") @Parameter(description = "Конец временного промежутка") Optional<LocalDate> toDate) {
        List<Operation> operations;
        if (fromDate.isPresent() && toDate.isPresent()) {
            operations = operationService.findAllOperationsBetweenDates(fromDate.get(), toDate.get());
        } else if (fromDate.isPresent()) {
            operations = operationService.findAllOperationsForDate(fromDate.get());
        } else {
            operations = operationService.findAllOperations();
        }
        return operations.stream()
                .map(this::makeDto)
                .sorted(Comparator.comparing(OperationDto::getDate).reversed())
                .toList();
    }

    @GetMapping("/users/{userId}/operations")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Получение операций пользователя",
            description = "Позволяет получить все операции конкретного пользователя"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Получен список операций",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = OperationDto.class))),
            @ApiResponse(responseCode = "400", description = "Пользователь не найден",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)))
    })
    public List<OperationDto> getAllOperationsByUser(@PathVariable @Parameter(description = "ID пользователя", example = "1") Long userId) {
        List<Operation> operations = operationService.findAllOperationsByUser(userId);
        return operations.stream()
                .map(this::makeDto)
                .sorted(Comparator.comparing(OperationDto::getDate).reversed())
                .toList();
    }

    @GetMapping("/cost-categories/{costCategoryId}/operations")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Получение операций по категории",
            description = "Позволяет получить все операции по конкретной категории расходов или доходов"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Получен список операций",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = OperationDto.class))),
            @ApiResponse(responseCode = "400", description = "Категория не найдена",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)))
    })
    public List<OperationDto> getAllOperationsByCostCategory(@PathVariable @Parameter(description = "ID категории", example = "1") Long costCategoryId) {
        List<Operation> operations = operationService.findAllOperationsByCostCategory(costCategoryId);
        return operations.stream()
                .map(this::makeDto)
                .sorted(Comparator.comparing(OperationDto::getDate).reversed())
                .toList();
    }

    @GetMapping("/sum")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Получение суммы расходов и доходов",
            description = "Позволяет получить по списку операций их количество операций и итоги по расходам и доходам"
    )
    @ApiResponse(responseCode = "200", description = "Расчет доходов и расходов",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = SumResponse.class)))
    public SumResponse getSum(@RequestBody @Parameter(description = "Список операций") List<OperationDto> operations) {
        return operationService.getSum(operations);
    }

    private OperationDto makeDto(Operation operation) {
        OperationDto dto = modelMapper.map(operation, OperationDto.class);
        dto.setBankAccountDto(modelMapper.map(operation.getBankAccount(), BankAccountDto.class));
        dto.setCostCategoryDto(modelMapper.map(operation.getCostCategory(), CostCategoryDto.class));
        UserDto userDto = modelMapper.map(operation.getCostCategory().getUser(), UserDto.class);
        dto.getBankAccountDto().setUserDto(userDto);
        dto.getCostCategoryDto().setUserDto(userDto);
        return dto;
    }
}
