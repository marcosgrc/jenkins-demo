package es.codeurjc.unit;

import es.codeurjc.model.Account;
import es.codeurjc.model.AccountNumber;
import es.codeurjc.model.Amount;
import es.codeurjc.model.Notification;
import es.codeurjc.model.Transaction;
import es.codeurjc.model.User;
import es.codeurjc.repository.AccountRepository;
import es.codeurjc.repository.TransactionRepository;
import es.codeurjc.service.AccountService;
import es.codeurjc.service.RandomService;
import es.codeurjc.service.notifications.EmailNotificationService;
import es.codeurjc.service.notifications.NotificationService;
import es.codeurjc.service.notifications.SmsNotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

    private AccountRepository accountRepository = mock(AccountRepository.class);
    private TransactionRepository transactionRepository = mock(TransactionRepository.class);
    private EmailNotificationService emailService = mock(EmailNotificationService.class);
    private SmsNotificationService smsService = mock(SmsNotificationService.class);
    private RandomService randomService = mock(RandomService.class);

    private AccountService accountService = new AccountService(accountRepository, transactionRepository, emailService,
            smsService, randomService);

    private User user;
    private User user2;
    private Account account;
    private Account account2;
    private final AccountNumber accountNumber = new AccountNumber("ES0123456789");
    private final AccountNumber accountNumber2 = new AccountNumber("ES9876543210");
    private final Amount amount100 = new Amount(100.0);

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUsername("testuser");
        user.setEmail("test@test.com");

        user2 = new User();
        user2.setUsername("testuser2");
        user2.setEmail("test2@test2.com");

        account = new Account(accountNumber.getValue(), Account.AccountType.SAVINGS, 1000.0);
        account.setUser(user);

        account2 = new Account(accountNumber2.getValue(), Account.AccountType.SAVINGS, 1000.0);
        account2.setUser(user2);
    }

    // --- Tests for constructor) ---

    @Test
    void constructor_explicitTest_shouldInitializeCorrectly() {
        // GIVEN: Creamos mocks locales para esta prueba específica
        AccountRepository localRepo = mock(AccountRepository.class);
        TransactionRepository localTransRepo = mock(TransactionRepository.class);
        EmailNotificationService localEmail = mock(EmailNotificationService.class);
        SmsNotificationService localSms = mock(SmsNotificationService.class);
        RandomService localRandom = mock(RandomService.class);

        // WHEN: Invocamos el constructor de AccountService directamente
        AccountService service = new AccountService(
                localRepo,
                localTransRepo,
                localEmail,
                localSms,
                localRandom);

        // THEN: Verificamos que el objeto no es nulo
        org.junit.jupiter.api.Assertions.assertNotNull(service,
                "El servicio debería instanciarse correctamente con todas sus dependencias.");

        // Verificación de comportamiento básico para asegurar que las dependencias
        // internas funcionan
        when(localRepo.findByAccountNumber(accountNumber.getValue())).thenReturn(Optional.of(account));
        assertEquals(1000.0, service.getBalance(accountNumber));
    }

    // --- Tests for createAccount(User user, Account.AccountType accountType) ---

    @Test
    void createAccount_validUserAndType_success() {
        // GIVEN
        when(randomService.nextInt(anyInt())).thenReturn(12345);
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        Account result = accountService.createAccount(user, Account.AccountType.CHECKING);

        // THEN
        assertEquals("ES0000012345", result.getAccountNumber());
        assertEquals(Account.AccountType.CHECKING, result.getAccountType());
        assertEquals(0.0, result.getBalance());
        assertEquals(user, result.getUser());

        verify(randomService).nextInt(1000000000);
        verify(accountRepository).save(any(Account.class));
    }

    // --- Tests for deposit(String accountNumber, double amount, String
    // description) ---

    @Test
    void depositWithDescription_amountZero_throwsException() {
        // Exception thrown from Amount class constructor when trying to create Amount
        // with 0.0
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            accountService.deposit(accountNumber, new Amount(0.0), "Test deposit");
        });
        assertEquals("Amount must be greater than zero", exception.getMessage());
    }

    @Test
    void depositWithDescription_amountNegative_throwsException() {
        // Exception thrown from Amount class constructor when trying to create Amount
        // with -50.0
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            accountService.deposit(accountNumber, new Amount(-50.0), "Test deposit");
        });
        assertEquals("Amount must be positive", exception.getMessage());
    }

    @Test
    void depositWithDescription_amountGreaterThan10000_throwsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            accountService.deposit(accountNumber, new Amount(15000.0), "Test deposit");
        });
        assertEquals("Amount exceeds maximum deposit limit", exception.getMessage());
    }

    @Test
    void depositWithDescription_amountGreaterThan50000_throwsException() {
        // This branch in the service is unreachable because > 10000 throws first
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            accountService.deposit(accountNumber, new Amount(60000.0), "Test deposit");
        });
        assertEquals("Amount exceeds maximum limit of 20,000", exception.getMessage());
    }

    @Test
    void depositWithDescription_accountNotFound_throwsException() {
        when(accountRepository.findByAccountNumber(accountNumber.getValue())).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            accountService.deposit(accountNumber, amount100, "Test deposit");
        });
        assertEquals("Account not found", exception.getMessage());
    }

    @Test
    void depositWithDescription_validAmountAndEmailNotification_success() {
        user.setNotificationType(User.NotificationType.EMAIL);
        when(accountRepository.findByAccountNumber(accountNumber.getValue())).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        Account savedAccount = accountService.deposit(accountNumber, new Amount(100.0), "Test deposit");

        assertEquals(1100.0, savedAccount.getBalance());

        verify(transactionRepository).save(any(Transaction.class));
        verify(accountRepository).save(account);
        verify(emailService).sendNotification(
                eq(user),
                eq(Notification.NotificationType.DEPOSIT),
                eq(AccountService.DEPOSIT_CONFIRMATION),
                eq("Deposit of 100,00 EUR. New balance: 1100,00 EUR"));
        verifyNoInteractions(smsService);
    }

    @Test
    void depositWithDescription_validAmountAndSmsNotification_success() {
        user.setNotificationType(User.NotificationType.SMS);
        when(accountRepository.findByAccountNumber(accountNumber.getValue())).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        Account savedAccount = accountService.deposit(accountNumber, new Amount(100.0), "Test deposit");

        assertEquals(1100.0, savedAccount.getBalance());

        verify(transactionRepository).save(any(Transaction.class));
        verify(accountRepository).save(account);
        verify(smsService).sendNotification(
                eq(user),
                eq(Notification.NotificationType.DEPOSIT),
                eq(AccountService.DEPOSIT_CONFIRMATION),
                eq("Deposit: 100,00 EUR. Balance: 1100,00 EUR"));
        verifyNoInteractions(emailService);
    }

    @Test
    void depositWithDescription_validAmountAndNoNotification_success() {
        user.setNotificationType(null);
        when(accountRepository.findByAccountNumber(accountNumber.getValue())).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        Account savedAccount = accountService.deposit(accountNumber, new Amount(100.0), "Test deposit");

        assertEquals(1100.0, savedAccount.getBalance());

        verify(transactionRepository).save(any(Transaction.class));
        verify(accountRepository).save(account);
        verifyNoInteractions(emailService);
        verifyNoInteractions(smsService);
    }

    // --- Tests for deposit(String accountNumber, double amount) ---

    @Test
    void depositNoDescription_delegatesToDepositWithDescription_success() {
        // GIVEN
        user.setNotificationType(null);
        when(accountRepository.findByAccountNumber(accountNumber.getValue())).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        // WHEN
        Account savedAccount = accountService.deposit(accountNumber, amount100);

        // THEN
        assertEquals(1100.0, savedAccount.getBalance());

        // Verify that the transaction was saved with the default description
        verify(transactionRepository)
                .save(argThat(transaction -> "Quick deposit".equals(transaction.getDescription()) &&
                        transaction.getAmount() == 100.0));
    }

    // --- Tests for getAccount(String accountNumber) and getUserAccounts(User user)
    // ---

    @Test
    void getAccount_accountNotFound_throwsException() {
        // GIVEN
        when(accountRepository.findByAccountNumber("ES000")).thenReturn(Optional.empty());

        // WHEN & THEN
        // Exception thrown from AccountNumber constructor when trying to create
        // AccountNumber with "ES000"
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            accountService.getAccount(new AccountNumber("ES000"));
        });
        assertEquals("Invalid account number format. Must be ES followed by 10 digits.", exception.getMessage());
    }

    @Test
    void getAccount_validAccountNumber_success() {
        // GIVEN
        Account account = new Account("ES0000000123", Account.AccountType.CHECKING, 1000.0);
        when(accountRepository.findByAccountNumber("ES0000000123")).thenReturn(Optional.of(account));

        // WHEN
        // Exception thrown from AccountNumber constructor when trying to create
        // AccountNumber with "ES123" because it doesn't have 10 digits, but we want to
        // test getAccount, so we catch that exception and ignore it to proceed with the
        // test
        Account result = accountService.getAccount(new AccountNumber("ES0000000123"));

        // THEN
        assertEquals("ES0000000123", result.getAccountNumber());
        assertEquals(1000.0, result.getBalance());
    }

    @Test
    void getUserAccounts_noAccounts_returnsEmptyList() {
        // GIVEN
        User user = new User();
        when(accountRepository.findByUser(user)).thenReturn(Collections.emptyList());

        // WHEN
        List<Account> result = accountService.getUserAccounts(user);

        // THEN
        assertEquals(0, result.size());
    }

    @Test
    void getUserAccounts_withAccounts_returnsList() {
        // GIVEN
        User user = new User();
        List<Account> accounts = Arrays.asList(
                new Account("ES1", Account.AccountType.CHECKING, 100.0),
                new Account("ES2", Account.AccountType.SAVINGS, 200.0));
        when(accountRepository.findByUser(user)).thenReturn(accounts);

        // WHEN
        List<Account> result = accountService.getUserAccounts(user);

        // THEN
        assertEquals(2, result.size());
        assertEquals("ES1", result.get(0).getAccountNumber());
        assertEquals("ES2", result.get(1).getAccountNumber());
    }

    // --- Tests for getBalance(String accountNumber) ---

    @Test
    void getBalance_validAccountNumber_returnsBalance() {
        // GIVEN
        when(accountRepository.findByAccountNumber(accountNumber.getValue())).thenReturn(Optional.of(account));

        // WHEN
        double balance = accountService.getBalance(accountNumber);

        // THEN
        assertEquals(1000.0, balance);
        verify(accountRepository).findByAccountNumber(accountNumber.getValue());
    }

    @Test
    void getBalance_accountNotFound_throwsException() {
        // GIVEN
        when(accountRepository.findByAccountNumber("INVALID")).thenReturn(Optional.empty());

        // WHEN & THEN
        // Exception thrown from AccountNumber constructor when trying to create
        // AccountNumber with "INVALID"
        assertThrows(IllegalArgumentException.class, () -> {
            accountService.getBalance(new AccountNumber("INVALID"));
        });
    }

    // --- Tests for getTransactions(String accountNumber) ---

    @Test
    void getTransactions_validAccountWithTransactions_returnsList() {
        // GIVEN
        when(accountRepository.findByAccountNumber(accountNumber.getValue())).thenReturn(Optional.of(account));

        List<Transaction> transactions = Arrays.asList(
                new Transaction(account, Transaction.TransactionType.DEPOSIT, 100.0, "Test 1"),
                new Transaction(account, Transaction.TransactionType.WITHDRAWAL, 50.0, "Test 2"));
        when(transactionRepository.findByAccountOrderByTimestampDesc(account)).thenReturn(transactions);

        // WHEN
        List<Transaction> result = accountService.getTransactions(accountNumber);

        // THEN
        assertEquals(2, result.size());
        assertEquals(100.0, result.get(0).getAmount());
        verify(transactionRepository).findByAccountOrderByTimestampDesc(account);
    }

    @Test
    void getTransactions_noTransactions_returnsEmptyList() {
        // GIVEN
        when(accountRepository.findByAccountNumber(accountNumber.getValue())).thenReturn(Optional.of(account));
        when(transactionRepository.findByAccountOrderByTimestampDesc(account)).thenReturn(Collections.emptyList());

        // WHEN
        List<Transaction> result = accountService.getTransactions(accountNumber);

        // THEN
        assertEquals(0, result.size());
    }

    @Test
    void getTransactions_accountNotFound_throwsException() {
        // GIVEN
        when(accountRepository.findByAccountNumber("NON_EXISTENT")).thenReturn(Optional.empty());

        // WHEN & THEN
        // Exception thrown from AccountNumber constructor when trying to create
        // AccountNumber with "NON_EXISTENT"
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            accountService.getTransactions(new AccountNumber("NON_EXISTENT"));
        });
        assertEquals("Invalid account number format. Must be ES followed by 10 digits.", exception.getMessage());
    }

    // --- Tests for rm(String accountNumber) ---

    @Test
    void deleteAccount_accountWithNonZeroBalance_throwsException() {
        // GIVEN
        account.setBalance(100.0);

        when(accountRepository.findByAccountNumber(accountNumber.getValue())).thenReturn(Optional.of(account));

        // WHEN & THEN
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            accountService.deleteAccount(accountNumber);
        });

        assertEquals("Cannot delete account with non-zero balance", exception.getMessage());

        verify(accountRepository, never()).delete(any());
    }

    @Test
    void deleteAccount_accountWithZeroBalance_deletesAccount() {
        // GIVEN
        account.setBalance(0.0);

        when(accountRepository.findByAccountNumber(accountNumber.getValue())).thenReturn(Optional.of(account));

        // WHEN
        accountService.deleteAccount(accountNumber);

        // THEN
        verify(accountRepository).delete(account);
    }

    @Test
    void deleteAccount_accountNotFound_throwsException() {
        // GIVEN
        when(accountRepository.findByAccountNumber(accountNumber.getValue())).thenReturn(Optional.empty());

        // WHEN & THEN
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            accountService.deleteAccount(accountNumber);
        });

        assertEquals("Account not found", exception.getMessage());

        verify(accountRepository, never()).delete(any());
    }

    // --- Tests for withdraw(String accountNumber, double amount, String
    // description) ---

    @Test
    void withdraw_amountZeroOrNegative_throwsException() {
        // When & then
        IllegalArgumentException exceptionZero = assertThrows(IllegalArgumentException.class, () -> {
            accountService.withdraw(accountNumber, new Amount(0.0), "Compra");
        });
        assertEquals("Amount must be greater than zero", exceptionZero.getMessage());

        // When & then
        IllegalArgumentException exceptionNegative = assertThrows(IllegalArgumentException.class, () -> {
            accountService.withdraw(accountNumber, new Amount(-100.0), "Compra");
        });
        assertEquals("Amount must be positive", exceptionNegative.getMessage());
    }

    @Test
    void withdraw_amountExceedsLimit_throwsException() {
        // When & then
        IllegalArgumentException exceptionLimit = assertThrows(IllegalArgumentException.class, () -> {
            accountService.withdraw(accountNumber, new Amount(5001.0), "Compra");
        });
        assertEquals("Amount exceeds maximum withdrawal limit", exceptionLimit.getMessage());
    }

    @Test
    void withdraw_accountNotFound_throwsException() {
        // Given
        when(accountRepository.findByAccountNumber("NON_EXISTENT")).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            accountService.withdraw(new AccountNumber("NON_EXISTENT"), amount100, "Compra");
        });
        assertEquals("Invalid account number format. Must be ES followed by 10 digits.", exception.getMessage());
    }

    @Test
    void withdraw_insufficientFunds_throwsException() {
        // Given
        when(accountRepository.findByAccountNumber(accountNumber.getValue())).thenReturn(Optional.of(account));

        // When & then
        IllegalArgumentException exceptionFunds = assertThrows(IllegalArgumentException.class, () -> {
            accountService.withdraw(accountNumber, new Amount(1500.0), "Compra");
        });
        assertEquals("Insufficient funds", exceptionFunds.getMessage());
    }

    @Test
    void withdraw_validAmountAndEmailNotification_success() {
        // Given
        user.setNotificationType(User.NotificationType.EMAIL);
        when(accountRepository.findByAccountNumber(accountNumber.getValue())).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        // When
        Account result = accountService.withdraw(accountNumber, new Amount(200.0), "Compra");

        // Then
        assertEquals(800.0, result.getBalance());
        verify(transactionRepository).save(any(Transaction.class));
        verify(accountRepository).save(account);
        verify(emailService).sendNotification(
                eq(user),
                eq(Notification.NotificationType.WITHDRAWAL),
                eq("Withdrawal Confirmation"),
                contains("Withdrawal of 200,00 EUR. New balance: 800,00 EUR"));
        verifyNoInteractions(smsService);
    }

    @Test
    void withdraw_validAmountAndSmsNotification_success() {
        // Given
        user.setNotificationType(User.NotificationType.SMS);
        when(accountRepository.findByAccountNumber(accountNumber.getValue())).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        // When
        accountService.withdraw(accountNumber, new Amount(50.0), "Compra");

        // Then
        verify(smsService).sendNotification(
                eq(user),
                eq(Notification.NotificationType.WITHDRAWAL),
                eq("Withdrawal"),
                contains("Withdrawal of 50,00 EUR. New balance: 950,00 EUR"));
        verifyNoInteractions(emailService);
    }

    @Test
    void withdraw_validAmountAndNoNotification_success() {
        // Given
        user.setNotificationType(null);
        when(accountRepository.findByAccountNumber(accountNumber.getValue())).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        // When
        Account result = accountService.withdraw(accountNumber, new Amount(100.0), "Account not found");

        // Then
        assertEquals(900.0, result.getBalance());
        verify(accountRepository).save(account);
        verify(transactionRepository).save(any(Transaction.class));
        verifyNoInteractions(emailService);
        verifyNoInteractions(smsService);
    }

    // --- Tests for transfer(String fromAccountNumber, String toAccountNumber,
    // double amount) ---

    // We performed 3 tests that correspond to throwing an exception when amount is
    // zero,
    // negative, or greater than 20000
    @ParameterizedTest(name = "Cantidad {0} debe lanzar error: {1}")
    @CsvSource({
            "0.0, 'Amount must be greater than zero'",
            "-1.0, 'Amount must be positive'",
            "30000.0, 'Amount exceeds maximum limit of 20,000'"
    })
    void transfer_invalidAmounts_throwsException(double value, String expectedMessage) {
        // WHEN & THEN
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            Amount amount = new Amount(value); // This will throw for 0.0 and -1.0, but we want to test transfer, so we
                                               // catch that exception and ignore it to proceed with the test
            accountService.transfer(accountNumber, accountNumber2, amount);
        });

        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void transfer_sameAccount_throwsException() {
        // GIVEN
        when(accountRepository.findByAccountNumber(accountNumber.getValue())).thenReturn(Optional.of(account));

        // WHEN & THEN
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            accountService.transfer(accountNumber, accountNumber, new Amount(500.0));
        });
        assertEquals("Cannot transfer to same account", exception.getMessage());
    }

    @Test
    void transfer_notEnoughBalance_throwsException() {
        // GIVEN
        when(accountRepository.findByAccountNumber(accountNumber.getValue())).thenReturn(Optional.of(account));
        when(accountRepository.findByAccountNumber(accountNumber2.getValue())).thenReturn(Optional.of(account2));

        // WHEN & THEN
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            accountService.transfer(accountNumber, accountNumber2, new Amount(2000.0));
        });
        assertEquals("Insufficient funds", exception.getMessage());
    }

    // We performed two tests that correspond to throwing an exception when the
    // source or destination account is not found
    @ParameterizedTest(name = "Lanza error cuando origen existe={0} y destino existe={1}")
    @CsvSource({
            "false, true", // 1) Doesn't exist the origin account
            "true, false" // 2)Doesn't exist the destination account
    })
    void transfer_accountNotFound_throwsException(boolean fromExists, boolean toExists) {
        // GIVEN
        when(accountRepository.findByAccountNumber(accountNumber.getValue()))
                .thenReturn(fromExists ? Optional.of(account) : Optional.empty());

        if (fromExists) {
            when(accountRepository.findByAccountNumber(accountNumber2.getValue()))
                    .thenReturn(toExists ? Optional.of(account2) : Optional.empty());
        }

        // WHEN & THEN
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            accountService.transfer(accountNumber, accountNumber2, new Amount(500.0));
        });

        assertEquals("Account not found", exception.getMessage());
    }

    // We perform 3 tests that verify the success of a transfer and the different
    // ways of being notified, whether by email, SMS or without notification
    @ParameterizedTest(name = "Transferencia exitosa con notificación: {0}")
    @ValueSource(strings = { "EMAIL", "SMS", "NONE" })
    void transfer_validParametersAndSuccess_allNotificationScenarios(String notificationTypeStr) {
        // --- GIVEN ---
        // We convert the string into a type of notification or null
        User.NotificationType type = notificationTypeStr.equals("NONE") ? null
                : User.NotificationType.valueOf(notificationTypeStr);

        user.setNotificationType(type);
        user2.setNotificationType(type);
        when(accountRepository.findByAccountNumber(accountNumber.getValue())).thenReturn(Optional.of(account));
        when(accountRepository.findByAccountNumber(accountNumber2.getValue())).thenReturn(Optional.of(account2));
        when(accountRepository.save(account)).thenReturn(account);
        when(accountRepository.save(account2)).thenReturn(account2);

        // WHEN
        accountService.transfer(accountNumber, accountNumber2, new Amount(500.0));

        // THEN
        assertEquals(500.0, account.getBalance());
        assertEquals(1500.0, account2.getBalance());

        verify(transactionRepository, times(2)).save(any(Transaction.class));
        verify(accountRepository).save(account);
        verify(accountRepository).save(account2);

        NotificationService targetService = (type == User.NotificationType.EMAIL) ? emailService
                : (type == User.NotificationType.SMS) ? smsService : null;
        if (targetService != null) {
            verify(targetService).sendNotification(
                    eq(user),
                    eq(Notification.NotificationType.TRANSFER),
                    eq("Transfer Sent"),
                    eq("Transfer of 500,00 EUR to ES9876543210. New balance: 500,00 EUR"));
            verify(targetService).sendNotification(
                    eq(user2),
                    eq(Notification.NotificationType.TRANSFER),
                    eq("Transfer Received"),
                    eq("Transfer of 500,00 EUR from ES0123456789. New balance: 1500,00 EUR"));
            verifyNoInteractions(targetService == emailService ? smsService : emailService);
        } else {
            verifyNoInteractions(emailService, smsService);
        }
    }
}
