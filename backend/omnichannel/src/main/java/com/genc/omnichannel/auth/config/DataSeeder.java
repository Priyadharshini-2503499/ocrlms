package com.genc.omnichannel.auth.config;

import com.genc.omnichannel.auth.model.AppUser;
import com.genc.omnichannel.auth.repository.AppUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(AppUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            seedUsers();
        } else {
            // Check if existing passwords are BCrypt encoded (start with $2a$ or $2b$)
            AppUser first = userRepository.findAll().get(0);
            if (!first.getPassword().startsWith("$2")) {
                // Old plain-text passwords detected — update them in-place with BCrypt
                for (AppUser user : userRepository.findAll()) {
                    user.setPassword(passwordEncoder.encode(getDefaultPassword(user.getUsername())));
                    userRepository.save(user);
                }
                System.out.println(">>> Updated existing users with BCrypt passwords.");
            }
        }
    }

    private void seedUsers() {
        userRepository.save(new AppUser("admin", passwordEncoder.encode("admin123"), "System Admin", "ADMIN"));
        userRepository.save(new AppUser("storemanager", passwordEncoder.encode("store123"), "Store Manager", "STORE_MANAGER"));
        userRepository.save(new AppUser("csagent", passwordEncoder.encode("agent123"), "Customer Service Agent", "CUSTOMER_SERVICE"));
        userRepository.save(new AppUser("merchandiser", passwordEncoder.encode("merch123"), "Merchandiser", "MERCHANDISER"));
        userRepository.save(new AppUser("marketing", passwordEncoder.encode("market123"), "Marketing Manager", "MARKETING_MANAGER"));
        System.out.println(">>> Default users seeded with BCrypt passwords.");
    }

    private String getDefaultPassword(String username) {
        return switch (username) {
            case "admin" -> "admin123";
            case "storemanager" -> "store123";
            case "csagent" -> "agent123";
            case "merchandiser" -> "merch123";
            case "marketing" -> "market123";
            default -> "password123";
        };
    }
}
