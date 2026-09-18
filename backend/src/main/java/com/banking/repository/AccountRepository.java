package com.banking.repository;

import com.banking.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByAccountNumber(String accountNumber);

    Optional<Account> findByUserId(Long userId);

    @Query("SELECT a FROM Account a WHERE a.user.email = :email")
    Optional<Account> findByUserEmail(@Param("email") String email);

    boolean existsByAccountNumber(String accountNumber);

    @Query("SELECT a FROM Account a JOIN a.user u WHERE " +
           "a.accountNumber LIKE CONCAT('%', :query, '%') OR " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Account> searchAccounts(@Param("query") String query);
}
