package kz.romanb.onelabproject.aop;

import kz.romanb.onelabproject.models.dto.BankAccountDto;
import kz.romanb.onelabproject.models.dto.CostCategoryDto;
import kz.romanb.onelabproject.models.dto.RegistrationRequest;
import kz.romanb.onelabproject.models.entities.BankAccount;
import kz.romanb.onelabproject.models.entities.CostCategory;
import kz.romanb.onelabproject.models.entities.Operation;
import kz.romanb.onelabproject.models.entities.User;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingAspect {
    @Pointcut("execution(* kz.romanb.onelabproject.services.UserService.registration(kz.romanb.onelabproject.models.dto.RegistrationRequest)) && args(request)")
    public void aroundRegistrationPointcut(RegistrationRequest request){}

    @Pointcut("execution(* kz.romanb.onelabproject.services.CostCategoryService.addNewCostCategoryToUser(..)) && args(userId, costCategoryDto)")
    public void afterCreatingCostCategoryPointcut(Long userId, CostCategoryDto costCategoryDto){}

    @Pointcut("execution(* kz.romanb.onelabproject.services.BankAccountService.addNewBankAccountToUser(..)) && args(userId, bankAccountDto)")
    public void beforeCreatingBankAccountPointcut(Long userId, BankAccountDto bankAccountDto){}

    @Pointcut("execution(* kz.romanb.onelabproject.repositories.OperationRepository.save(..)) && args(operation)")
    public void aroundSaveOperationToDBPointcut(Operation operation){}

    @Around(value = "aroundRegistrationPointcut(request)")
    public User aroundRegistration(ProceedingJoinPoint joinPoint, RegistrationRequest request) throws Throwable{
        log.debug("Попытка создать нового пользователя");
        User retVal = (User)joinPoint.proceed();
        log.info("Пользователь с id" + retVal.getId() + " - " + retVal.getEmail() + " успешно создан");
        return retVal;
    }

    @After(value = "afterCreatingCostCategoryPointcut(userId, costCategoryDto)")
    public void afterCreatingCostCategory(Long userId, CostCategoryDto costCategoryDto){
        log.info("Пользователь с id " + userId + " создал новую категорию типа " + costCategoryDto.getCategoryType().name());
    }

    @Before(value = "beforeCreatingBankAccountPointcut(userId, bankAccountDto)")
    public void beforeCreatingBankAccount(Long userId, BankAccountDto bankAccountDto){
        log.debug("""
                Попытка пользователя с id {} создать новый счет
                Начальный счет на балансе - {}
                """,
                userId, bankAccountDto.getBalance().toString());
    }

    @Around(value = "aroundSaveOperationToDBPointcut(operation)")
    public Operation aroundSaveOperationToDB(ProceedingJoinPoint joinPoint, Operation operation) throws Throwable{
        log.info("Создана операция в размере " + operation.getAmount().toString() + " на категорию " + operation.getCostCategory().getName() + " со счета " + operation.getBankAccount().getName());
        Operation o = (Operation) joinPoint.proceed();
        log.info("Операция " + o.getId() + " сохранена");
        return o;
    }
}
