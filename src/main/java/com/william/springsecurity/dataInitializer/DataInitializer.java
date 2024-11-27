package com.william.springsecurity.dataInitializer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.william.springsecurity.repositories.UserRepository;
import com.william.springsecurity.domain.user.User;
import com.william.springsecurity.domain.user.UserRole;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    public DataInitializer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Verifica se já existe um usuário ADMIN no banco de dados
        if (userRepository.findByLogin("admin") == null) {
            // Cria um usuário ADMIN se não existir
            User adminUser = new User("admin", new BCryptPasswordEncoder().encode("102030"), UserRole.ADMIN);
            userRepository.save(adminUser);
            System.out.println("Usuário ADMIN criado com sucesso!");
        }
    }
}
