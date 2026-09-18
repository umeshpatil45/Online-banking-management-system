package com.banking.service;

import com.banking.dto.ChangePasswordRequest;
import com.banking.dto.UpdateProfileRequest;
import com.banking.dto.UserResponse;
import com.banking.entity.Account;
import com.banking.entity.User;
import com.banking.exception.BankingException;
import com.banking.exception.ResourceNotFoundException;
import com.banking.repository.AccountRepository;
import com.banking.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, AccountRepository accountRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse getCurrentUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        Account account = accountRepository.findByUserId(user.getId()).orElse(null);
        String accountNumber = account != null ? account.getAccountNumber() : "N/A";

        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getMobile(),
                user.getRole(),
                accountNumber,
                user.getCreatedAt()
        );
    }

    @Transactional
    public UserResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        // Check if mobile changed and is already taken
        if (!user.getMobile().equals(request.getMobile().trim()) &&
                userRepository.existsByMobile(request.getMobile().trim())) {
            throw new BankingException("Mobile number is already in use by another account.", HttpStatus.CONFLICT);
        }

        user.setFullName(request.getFullName().trim());
        user.setMobile(request.getMobile().trim());
        User updated = userRepository.save(user);

        Account account = accountRepository.findByUserId(updated.getId()).orElse(null);
        String accountNumber = account != null ? account.getAccountNumber() : "N/A";

        return new UserResponse(
                updated.getId(),
                updated.getFullName(),
                updated.getEmail(),
                updated.getMobile(),
                updated.getRole(),
                accountNumber,
                updated.getCreatedAt()
        );
    }

    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BankingException("Current password is incorrect.", HttpStatus.BAD_REQUEST);
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BankingException("New passwords do not match.", HttpStatus.BAD_REQUEST);
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BankingException("New password cannot be the same as the old password.", HttpStatus.BAD_REQUEST);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}
