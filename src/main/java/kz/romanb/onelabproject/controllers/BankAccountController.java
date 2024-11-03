package kz.romanb.onelabproject.controllers;

import kz.romanb.onelabproject.exceptions.DBRecordNotFoundException;
import kz.romanb.onelabproject.models.dto.BankAccountDto;
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
public class BankAccountController {
    private final BankAccountService bankAccountService;
    private final ModelMapper modelMapper;

    @GetMapping("/users/{userId}/bank-accounts")
    public List<BankAccountDto> getUsersBankAccounts(@PathVariable Long userId) {
        return bankAccountService.getAllUserAccounts(userId).stream().map(this::makeDto).toList();
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/users/{userId}/bank-accounts")
    public BankAccountDto addNewBankAccount(@PathVariable Long userId, @RequestBody BankAccountDto bankAccountDto) {
        return makeDto(bankAccountService.addNewBankAccountToUser(userId, bankAccountDto));
    }

    @PatchMapping("/bank-accounts/{bankAccountId}")
    public ResponseEntity<String> changeBalance(@PathVariable Long bankAccountId, @RequestParam BigDecimal amount) {
        return ResponseEntity.ok(bankAccountService.changeBalance(bankAccountId, amount));
    }

    @GetMapping("/bank-accounts/{bankAccountId}")
    public BankAccountDto getBankAccountById(@PathVariable Long bankAccountId) {
        Optional<BankAccount> bankAccountOptional = bankAccountService.findById(bankAccountId);
        if (bankAccountOptional.isEmpty()) {
            throw new DBRecordNotFoundException("Счета с id " + bankAccountId + " не существует");
        }
        return makeDto(bankAccountOptional.get());
    }

    private BankAccountDto makeDto(BankAccount bankAccount) {
        BankAccountDto bankAccountDto = modelMapper.map(bankAccount, BankAccountDto.class);
        bankAccountDto.setUserDto(modelMapper.map(bankAccount.getUser(), UserDto.class));
        return bankAccountDto;
    }
}
