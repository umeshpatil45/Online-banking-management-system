package com.banking.repository;

import com.banking.entity.Transaction;
import com.banking.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByTransactionReference(String transactionReference);

    @Query("SELECT t FROM Transaction t WHERE (t.senderAccount = :accNumber OR t.receiverAccount = :accNumber) ORDER BY t.createdAt DESC")
    List<Transaction> findByAccount(@Param("accNumber") String accNumber);

    @Query("SELECT t FROM Transaction t WHERE " +
           "(t.senderAccount = :accNumber OR t.receiverAccount = :accNumber) AND " +
           "(:type IS NULL OR t.transactionType = :type) AND " +
           "(:startDate IS NULL OR t.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR t.createdAt <= :endDate) AND " +
           "(:query IS NULL OR :query = '' OR " +
           " LOWER(t.transactionReference) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(t.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " t.senderAccount LIKE CONCAT('%', :query, '%') OR " +
           " t.receiverAccount LIKE CONCAT('%', :query, '%')) " +
           "ORDER BY t.createdAt DESC")
    List<Transaction> filterUserTransactions(
            @Param("accNumber") String accNumber,
            @Param("type") TransactionType type,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("query") String query
    );

    @Query("SELECT t FROM Transaction t WHERE " +
           "(:type IS NULL OR t.transactionType = :type) AND " +
           "(:query IS NULL OR :query = '' OR " +
           " LOWER(t.transactionReference) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(t.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " t.senderAccount LIKE CONCAT('%', :query, '%') OR " +
           " t.receiverAccount LIKE CONCAT('%', :query, '%')) " +
           "ORDER BY t.createdAt DESC")
    List<Transaction> filterAllTransactions(
            @Param("type") TransactionType type,
            @Param("query") String query
    );

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.transactionType = 'DEPOSIT' AND t.transactionStatus = 'SUCCESS'")
    BigDecimal getTotalDepositVolume();

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.transactionType = 'TRANSFER' AND t.transactionStatus = 'SUCCESS'")
    BigDecimal getTotalTransferVolume();
}
