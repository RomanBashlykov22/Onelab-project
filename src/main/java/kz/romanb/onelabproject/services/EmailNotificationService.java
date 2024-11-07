package kz.romanb.onelabproject.services;

import kz.romanb.onelabproject.models.dto.BankAccountDto;
import kz.romanb.onelabproject.models.dto.CostCategoryDto;
import kz.romanb.onelabproject.models.dto.OperationDto;
import kz.romanb.onelabproject.models.entities.User;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailNotificationService {
    private final JavaMailSender javaMailSender;
    private final UserService userService;
    private final OperationService operationService;
    private final ModelMapper modelMapper;

    public void sendMessage(String toAddress, String subject, String message) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(toAddress);
        mailMessage.setSubject(subject);
        mailMessage.setText(message);
        javaMailSender.send(mailMessage);
    }

    @Scheduled(initialDelay = 50000, fixedDelay = 50000)
//    @Scheduled(cron = "0 0 0 1 * ?")
    @Async
    void notifyUsersOfTheirMonthlyExpenses() {
        List<User> users = userService.findAllUsers();
        for (User user : users) {
            List<OperationDto> operations = operationService.findAllOperationsByUser(user.getId()).stream()
                    .filter(o -> o.getDate().getMonth().equals(LocalDate.now().minusMonths(1).getMonth()))
                    .map(o -> {
                        OperationDto dto = modelMapper.map(o, OperationDto.class);
                        dto.setBankAccountDto(modelMapper.map(o.getBankAccount(), BankAccountDto.class));
                        dto.setCostCategoryDto(modelMapper.map(o.getCostCategory(), CostCategoryDto.class));
                        return dto;
                    })
                    .toList();
            if (!operations.isEmpty()) {
                sendMessage(user.getEmail(), "Итоги за месяц", operationService.getSum(operations).toString());
            }
        }
    }
}
