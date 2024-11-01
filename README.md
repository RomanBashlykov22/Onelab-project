# Onelab-project
## Трекер расходов
### Описание
Трекер расходов - консольное приложение, позволяет отслеживать свои доходы и расходы, распределять их по категориям.
### Функционал
- Добавление пользователя, его счетов, категорий расходов и доходов
- Создание денежных операций (расхода и дохода)
- Отображение истории расходов с фильтрацией по счетам, категориям, датам
### Использованные технологии
- Java 17
- Spring Boot 3.3.4
- Spring Data JPA
- Spring Security & JWT
- H2 Database
- AOP
- Kafka
- Lombok
- JUnit5 & Mockito
- ModelMapper
- Maven

### Описание эндпоинтов
В папке resources лежит Postman-коллекция с запросами.
#### AuthController
- HTTP-POST /api/login - для входа в приложение. Генерирует JWT-токены.  
Пример тела запроса:
  {
  "email": "roman.bash14@mail.ru",
  "password": "123"
  }
- HTTP-POST /api/new-access-token - для обновления Access-токена.  
Пример тела запроса:
  {
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJyb21hbi5iYXNoMTRAbWFpbC5ydSIsImV4cCI6MTczMDk3NDc2OSwiaWF0IjoxNzMwMzY5OTY5fQ.osXLt32u6RuS8-ItQwidbFtUOvkBEW4Vv5aOuMIyGTA-sYViQL-zWDDAmF3jMcZ-bpY4Bvi1LViso0Suh6ygjg"
  }
- HTTP-POST /api/new-refresh-token - для обновления Refresh-токена.  
Пример тела запроса:
  {
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJyb21hbi5iYXNoMTRAbWFpbC5ydSIsImV4cCI6MTczMDk3NDc2OSwiaWF0IjoxNzMwMzY5OTY5fQ.osXLt32u6RuS8-ItQwidbFtUOvkBEW4Vv5aOuMIyGTA-sYViQL-zWDDAmF3jMcZ-bpY4Bvi1LViso0Suh6ygjg"
  }
- HTTP-POST /api/logout - для выхода из приложения. Удаляет токены пользователя.  
Пример тела запроса:
  {
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJyb21hbi5iYXNoMTRAbWFpbC5ydSIsImV4cCI6MTczMDk3NDc2OSwiaWF0IjoxNzMwMzY5OTY5fQ.osXLt32u6RuS8-ItQwidbFtUOvkBEW4Vv5aOuMIyGTA-sYViQL-zWDDAmF3jMcZ-bpY4Bvi1LViso0Suh6ygjg"
  }

#### UserController
- HTTP-POST /api/registration - для регистрации нового пользователя.  
Пример тела запроса:
  {
  "email": "test@gmail.com",
  "username": "testusername",
  "password": "123"
  }
- HTTP-GET /api/users/getAllUsers - получить всех пользователей из БД.  
- HTTP-GET /api/users/{userId} - получить пользователя из БД по id.  
Пример запроса: http://localhost:8080/api/users/1
- HTTP-DELETE /api/users/{userId} - удалить пользователя по id.  
Пример запроса: http://localhost:8080/api/users/1

#### BankAccountController
- HTTP-GET /api/users/{userId}/bank-accounts - получить все счета пользователя по его id.  
Пример запроса: http://localhost:8080/api/users/1/bank-accounts
- HTTP-POST /api/users/{userId}/bank-accounts - добавить пользователю новый счет.  
Пример запроса: http://localhost:8080/api/users/1/bank-accounts  
Пример тела запроса:
  {
  "name": "Forte",
  "balance": "1337.14"
  }
- HTTP-PATCH /api/bank-accounts/{bankAccountId} - изменить баланс на счете.  
Пример запроса: http://localhost:8080/api/bank-accounts/1?amount=666.8
- HTTP-GET /api/bank-accounts/{bankAccountId} - получить счет по id.  
Пример запроса: http://localhost:8080/api/bank-accounts/1

#### CostCategoryController
- HTTP-GET /api/users/{userId}/cost-categories - получить всех категории расходов пользователя по его id.  
Пример запроса: http://localhost:8080/api/users/1/cost-categories
- HTTP-POST /api/users/{userId}/cost-categories - добавить пользователю новую категорию.  
Пример запроса: http://localhost:8080/api/users/1/cost-categories
- HTTP-GET /api/cost-categories/{costCategoryId} - найти категорию по id.  
Пример запроса: http://localhost:8080/api/cost-categories/1

#### OperationController
- HTTP-POST /api/operations/create - создать операцию расхода.  
Пример запроса: http://localhost:8080/api/operations/create?bankAccountId=1&costCategoryId=1&amount=1000
- HTTP-GET /api/operations/{operationId} - найти операцию по id.  
Пример запроса: http://localhost:8080/api/operations/1
- HTTP-GET /api/operations - найти операции. Несколько вариаций исполнения.  
Примеры запросов:
  - http://localhost:8080/api/operations - получить все операции из БД.  
  - http://localhost:8080/api/operations?fromDate=01.10.2024 - получить все операции на конкретную дату.
  - http://localhost:8080/api/operations?fromDate=13.10.2024&toDate=02.11.2024 - получить все операции в период с ... по ...
- HTTP-GET /api/users/{userId}/operations - получить все операции пользователя.  
Пример запроса: http://localhost:8080/api/users/1/operations
- HTTP-GET /api/cost-categories/{costCategoryId}/operations - получить все операции по категории.  
Пример запроса: http://localhost:8080/api/cost-categories/1/operations
- HTTP-GET /api/sum - рассчитать сумму доходов и расходов у списка операций.  
Пример запроса: http://localhost:8080/api/sum  
Пример тела запроса: [
  {
  "id": 3,
  "amount": 1000.00,
  "date": "02.11.2024",
  "bankAccount": {
  "id": 1,
  "name": "Kaspi",
  "balance": 11266.12,
  "user": {
  "id": 1,
  "username": "ramioris",
  "email": "roman.bash14@mail.ru",
  "roles": [
  "ADMIN",
  "USER"
  ]
  }
  },
  "costCategory": {
  "id": 1,
  "name": "Sport",
  "categoryType": "EXPENSE",
  "user": {
  "id": 1,
  "username": "ramioris",
  "email": "roman.bash14@mail.ru",
  "roles": [
  "ADMIN",
  "USER"
  ]
  }
  }
  },
  {
  "id": 1,
  "amount": 6000.00,
  "date": "13.10.2024",
  "bankAccount": {
  "id": 1,
  "name": "Kaspi",
  "balance": 11266.12,
  "user": {
  "id": 1,
  "username": "ramioris",
  "email": "roman.bash14@mail.ru",
  "roles": [
  "ADMIN",
  "USER"
  ]
  }
  },
  "costCategory": {
  "id": 1,
  "name": "Sport",
  "categoryType": "EXPENSE",
  "user": {
  "id": 1,
  "username": "ramioris",
  "email": "roman.bash14@mail.ru",
  "roles": [
  "ADMIN",
  "USER"
  ]
  }
  }
  },
  {
  "id": 2,
  "amount": 9831.07,
  "date": "01.10.2024",
  "bankAccount": {
  "id": 1,
  "name": "Kaspi",
  "balance": 11266.12,
  "user": {
  "id": 1,
  "username": "ramioris",
  "email": "roman.bash14@mail.ru",
  "roles": [
  "ADMIN",
  "USER"
  ]
  }
  },
  "costCategory": {
  "id": 2,
  "name": "Salary",
  "categoryType": "INCOME",
  "user": {
  "id": 1,
  "username": "ramioris",
  "email": "roman.bash14@mail.ru",
  "roles": [
  "ADMIN",
  "USER"
  ]
  }
  }
  }
  ]