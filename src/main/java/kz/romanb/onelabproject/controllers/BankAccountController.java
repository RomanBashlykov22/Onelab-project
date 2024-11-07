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
import kz.romanb.onelabproject.models.dto.BankAccountDto;
import kz.romanb.onelabproject.models.dto.ErrorDto;
import kz.romanb.onelabproject.models.dto.UserDto;
import kz.romanb.onelabproject.models.entities.BankAccount;
import kz.romanb.onelabproject.services.BankAccountService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "Bank account controller", description = "Контроллер для управления счетами пользователя")
@SecurityRequirement(name = "JWT")
public class BankAccountController {
    private final BankAccountService bankAccountService;
    private final ModelMapper modelMapper;

    @GetMapping("/users/{userId}/bank-accounts")
    @Operation(
            summary = "Получение счетов пользователя",
            description = "Позволяет получить все счета конкретного пользователя"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Получен список счетов",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BankAccountDto.class))),
            @ApiResponse(responseCode = "400", description = "Пользователь не найден",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)))
    })
    public List<BankAccountDto> getUsersBankAccounts(@PathVariable @Parameter(description = "ID пользователя", example = "1") Long userId) {
        return bankAccountService.getAllUserAccounts(userId).stream().map(this::makeDto).toList();
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/users/{userId}/bank-accounts")
    @Operation(
            summary = "Добавление счета",
            description = "Позволяет добавить пользователю новый счет"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Добавлен новый счет",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BankAccountDto.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка при добавлении счета",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)))
    })
    public BankAccountDto addNewBankAccount(@PathVariable @Parameter(description = "ID пользователя", example = "1") Long userId,
                                            @RequestBody @Parameter(description = "Сведения о счете") BankAccountDto bankAccountDto) {
        return makeDto(bankAccountService.addNewBankAccountToUser(userId, bankAccountDto));
    }

    @PatchMapping("/bank-accounts/{bankAccountId}")
    @Operation(
            summary = "Изменение баланса",
            description = "Позволяет изменить баланс на счете"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Баланс изменен"),
            @ApiResponse(responseCode = "400", description = "Ошибка при изменении баланса",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)))
    })
    public ResponseEntity<String> changeBalance(@PathVariable @Parameter(description = "ID счета", example = "1") Long bankAccountId,
                                                @RequestParam @Parameter(description = "Новый баланс счета", example = "1000") BigDecimal amount) {
        return ResponseEntity.ok(bankAccountService.changeBalance(bankAccountId, amount));
    }

    @GetMapping("/bank-accounts/{bankAccountId}")
    @Operation(
            summary = "Получение счета по ID",
            description = "Позволяет получить информацию о счете по ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Получен счет",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BankAccountDto.class))),
            @ApiResponse(responseCode = "400", description = "Счет не найден",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)))
    })
    public BankAccountDto getBankAccountById(@PathVariable @Parameter(description = "ID счета", example = "1") Long bankAccountId) {
        Optional<BankAccount> bankAccountOptional = bankAccountService.findById(bankAccountId);
        if (bankAccountOptional.isEmpty()) {
            throw new DBRecordNotFoundException(String.format(BankAccountService.BANK_ACCOUNT_WITH_ID_DOES_NOT_EXISTS, bankAccountId));
        }
        return makeDto(bankAccountOptional.get());
    }

    private BankAccountDto makeDto(BankAccount bankAccount) {
        BankAccountDto bankAccountDto = modelMapper.map(bankAccount, BankAccountDto.class);
        bankAccountDto.setUserDto(modelMapper.map(bankAccount.getUser(), UserDto.class));
        return bankAccountDto;
    }
}
