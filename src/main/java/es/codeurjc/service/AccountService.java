package es.codeurjc.service;

import es.codeurjc.model.Account;
import es.codeurjc.model.Amount;
import es.codeurjc.model.AccountNumber;
import es.codeurjc.model.User;
import es.codeurjc.model.Notification;
import es.codeurjc.model.Transaction;
import es.codeurjc.repository.AccountRepository;
import es.codeurjc.repository.TransactionRepository;
import es.codeurjc.service.notifications.EmailNotificationService;
import es.codeurjc.service.notifications.SmsNotificationService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing bank accounts.
 */
@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final EmailNotificationService emailService;
    private final SmsNotificationService smsService;
    private final RandomService randomService;
    private static final double MAX_DEPOSIT_LIMIT = 10000;
    private static final double MAX_WITHDRAW_LIMIT = 5000;

    public AccountService(AccountRepository accountRepository,
            TransactionRepository transactionRepository,
            EmailNotificationService emailService,
            SmsNotificationService smsService,
            RandomService randomService) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.emailService = emailService;
        this.smsService = smsService;
        this.randomService = randomService;
    }

    /**
     * Create a new account
     */
    public Account createAccount(User user, Account.AccountType accountType) {
        AccountNumber accountNumber = generateAccountNumber();
        Account account = new Account(accountNumber.getValue(), accountType, 0);
        account.setUser(user);
        return accountRepository.save(account);
    }

    /**
     * Generate account number
     */
    private AccountNumber generateAccountNumber() {
        return new AccountNumber(String.format("ES%010d", randomService.nextInt(1000000000)));
    }

    /**
     * Get account by account number
     */
    public Account getAccount(AccountNumber accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber.getValue())
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
    }

    /**
     * Get all accounts for a user
     */
    public List<Account> getUserAccounts(User user) {
        return accountRepository.findByUser(user);
    }

    /**
     * Deposit money into account
     */
    @Transactional
    public Account deposit(AccountNumber accountNumber, Amount amount, String description) {
        if (amount.getValue() > MAX_DEPOSIT_LIMIT) {
            throw new IllegalArgumentException("Amount exceeds maximum deposit limit");
        }

        Account account = getAccount(new AccountNumber(accountNumber.getValue()));
        account.deposit(amount.getValue());

        // Record transaction
        Transaction transaction = new Transaction(account, Transaction.TransactionType.DEPOSIT,
                amount.getValue(), description);
        transactionRepository.save(transaction);

        Account savedAccount = accountRepository.save(account);

        // Send notification
        sendNotification(account.getUser(), Notification.NotificationType.DEPOSIT,
                "Deposit Confirmation", "Deposit of %.2f EUR. New balance: %.2f EUR",
                "Deposit Confirmation", "Deposit: %.2f EUR. Balance: %.2f EUR",
                amount.getValue(), account.getBalance());

        return savedAccount;
    }

    /**
     * Quick deposit without description
     */
    @Transactional
    public Account deposit(AccountNumber accountNumber, Amount amount) {

        if (amount.getValue() > MAX_DEPOSIT_LIMIT) {
            throw new IllegalArgumentException("Amount exceeds maximum deposit limit");
        }

        Account account = getAccount(accountNumber);
        account.deposit(amount.getValue());

        // Record transaction
        Transaction transaction = new Transaction(account, Transaction.TransactionType.DEPOSIT,
                amount.getValue(), "Quick deposit");
        transactionRepository.save(transaction);

        Account savedAccount = accountRepository.save(account);

        // Send notification
        sendNotification(account.getUser(), Notification.NotificationType.DEPOSIT,
                "Deposit Confirmation", "Deposit of %.2f EUR. New balance: %.2f EUR",
                "Deposit Confirmation", "Deposit: %.2f EUR. Balance: %.2f EUR",
                amount.getValue(), account.getBalance());

        return savedAccount;
    }

    /**
     * Withdraw money from account
     */
    @Transactional
    public Account withdraw(AccountNumber accountNumber, Amount amount, String description) {
        if (amount.getValue() > MAX_WITHDRAW_LIMIT) {
            throw new IllegalArgumentException("Amount exceeds maximum withdrawal limit");
        }
        Account account = getAccount(accountNumber);

        account.withdraw(amount.getValue());

        // Record transaction
        Transaction transaction = new Transaction(account, Transaction.TransactionType.WITHDRAWAL,
                amount.getValue(), description);
        transactionRepository.save(transaction);

        Account savedAccount = accountRepository.save(account);

        // Notification
        sendNotification(account.getUser(), Notification.NotificationType.WITHDRAWAL,
                "Withdrawal Confirmation", "Withdrawal of %.2f EUR. New balance: %.2f EUR",
                "Withdrawal", "Withdrawal of %.2f EUR. New balance: %.2f EUR",
                amount.getValue(), account.getBalance());

        return savedAccount;
    }

    /**
     * Transfer money between accounts
     */
    @Transactional
    public void transfer(AccountNumber fromAccountNumber, AccountNumber toAccountNumber, Amount amount) {

        Account fromAccount = getAccount(fromAccountNumber);
        Account toAccount = getAccount(toAccountNumber);

        // Validate same account
        if (fromAccount.getAccountNumber().equals(toAccount.getAccountNumber())) {
            throw new IllegalArgumentException("Cannot transfer to same account");
        }

        // Perform transfer
        fromAccount.withdraw(amount.getValue());
        toAccount.deposit(amount.getValue());

        // Record transactions
        Transaction sentTransaction = new Transaction(fromAccount,
                Transaction.TransactionType.TRANSFER_SENT,
                amount.getValue(),
                "Transfer to " + toAccountNumber);
        sentTransaction.setDestinationAccountNumber(toAccountNumber.getValue());
        transactionRepository.save(sentTransaction);

        Transaction receivedTransaction = new Transaction(toAccount,
                Transaction.TransactionType.TRANSFER_RECEIVED,
                amount.getValue(),
                "Transfer from " + fromAccountNumber);
        receivedTransaction.setDestinationAccountNumber(fromAccountNumber.getValue());
        transactionRepository.save(receivedTransaction);

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        // Notification to the issuer
        sendNotification(fromAccount.getUser(), Notification.NotificationType.TRANSFER,
                "Transfer Sent", "Transfer of %.2f EUR to %s. New balance: %.2f EUR",
                "Transfer Sent", "Transfer of %.2f EUR to %s. New balance: %.2f EUR",
                amount.getValue(), toAccountNumber, fromAccount.getBalance());

        // Notification to the recipient
        sendNotification(toAccount.getUser(), Notification.NotificationType.TRANSFER,
                "Transfer Received", "Transfer of %.2f EUR from %s. New balance: %.2f EUR",
                "Transfer Received", "Transfer of %.2f EUR from %s. New balance: %.2f EUR",
                amount.getValue(), fromAccountNumber, toAccount.getBalance());
    }

    /**
     * Delete account
     */
    public void rm(AccountNumber accountNumber) {
        Account account = getAccount(accountNumber);

        if (account.getBalance() != 0) {
            throw new IllegalArgumentException("Cannot delete account with non-zero balance");
        }

        accountRepository.delete(account);
    }

    /**
     * Get account balance
     */
    public double getBalance(AccountNumber accountNumber) {
        Account account = getAccount(accountNumber);
        return account.getBalance();
    }

    /**
     * Get account transactions
     */
    public List<Transaction> getTransactions(AccountNumber accountNumber) {
        Account account = getAccount(accountNumber);
        return transactionRepository.findByAccountOrderByTimestampDesc(account);
    }

    /**
     * Send notification
     */
    private void sendNotification(User user, Notification.NotificationType type, String emailSubject,
            String emailFormat, String smsSubject, String smsFormat, Object... args) {
        User.NotificationType notifType = user.getNotificationType();
        if (notifType == User.NotificationType.EMAIL) {
            emailService.sendNotification(user, type, emailSubject, String.format(emailFormat, args));
        } else if (notifType == User.NotificationType.SMS) {
            smsService.sendNotification(user, type, smsSubject, String.format(smsFormat, args));
        }
    }

}
