package kz.romanb.onelabproject.aop;

import kz.romanb.onelabproject.entities.BankAccount;
import kz.romanb.onelabproject.entities.CostCategory;
import kz.romanb.onelabproject.entities.Operation;
import kz.romanb.onelabproject.entities.User;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingAspect {
    @Pointcut("execution(* kz.romanb.onelabproject..*(..))")
    public void afterThrowingForAllMethodsPointcut(){}

    @Pointcut("execution(* kz.romanb.onelabproject.services.UserService.addNewUser(kz.romanb.onelabproject.entities.User)) && args(user)")
    public void aroundCreatingNewUserPointcut(User user){}

    @Pointcut("execution(* kz.romanb.onelabproject.services.CostCategoryService.addNewCostCategoryToUser(..)) && args(user, costCategory)")
    public void afterCreatingCostCategoryPointcut(User user, CostCategory costCategory){}

    @Pointcut("execution(* kz.romanb.onelabproject.services.BankAccountService.addNewBankAccountToUser(..)) && args(user, bankAccount)")
    public void beforeCreatingBankAccountPointcut(User user, BankAccount bankAccount){}

    @Pointcut("execution(* kz.romanb.onelabproject.repositories.OperationRepository.save(..)) && args(operation)")
    public void aroundSaveOperationToDBPointcut(Operation operation){}

    @AfterThrowing(value = "afterThrowingForAllMethodsPointcut()", throwing = "ex")
    public void logAfterThrowing(JoinPoint joinPoint, Exception ex) {
        log.error("При выполнении метода " + joinPoint.getSignature().getName() + " произошла ошибка: {}", ex.getMessage());
    }

    @Around(value = "aroundCreatingNewUserPointcut(user)")
    public User aroundCreatingNewUser(ProceedingJoinPoint joinPoint, User user) throws Throwable{
        log.debug("Попытка создать нового пользователя");
        User retVal = (User)joinPoint.proceed();
        log.info("Пользователь " + retVal.getId() + " " + retVal.getName() + " успешно создан");
        return retVal;
    }

    @After(value = "afterCreatingCostCategoryPointcut(user, costCategory)")
    public void afterCreatingCostCategory(User user, CostCategory costCategory){
        log.info("Пользователь с id " + user.getId() + " создал новую категорию типа " + costCategory.getCategoryType().name());
    }

    @Before(value = "beforeCreatingBankAccountPointcut(user, bankAccount)")
    public void beforeCreatingBankAccount(User user, BankAccount bankAccount){
        log.debug("""
                Попытка пользователя {} создать новый счет
                Начальный счет на балансе - {}
                """,
                user.getName(), bankAccount.getBalance().toString());
    }

    @Around(value = "aroundSaveOperationToDBPointcut(operation)")
    public Operation aroundSaveOperationToDB(ProceedingJoinPoint joinPoint, Operation operation) throws Throwable{
        log.info("Создана операция " + operation.getId() + " в размере " + operation.getAmount().toString() + " на категорию " + operation.getCostCategory().getName() + " со счета " + operation.getBankAccount().getName());
        Operation o = (Operation) joinPoint.proceed();
        log.info("Операция " + o.getId() + " сохранена");
        return o;
    }
}
