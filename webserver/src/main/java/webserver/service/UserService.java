package webserver.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import webserver.model.ApplicationUser;
import webserver.repository.ApplicationUserRepository;

@Service
public class UserService {

    private ApplicationUserRepository applicationUserRepository;
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    public UserService(ApplicationUserRepository applicationUserRepository,
                       BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.applicationUserRepository = applicationUserRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public void signUp(ApplicationUser applicationUser) {
        if (!applicationUserRepository.existsByUsername(applicationUser.getUsername())) {
            applicationUser.setPassword(bCryptPasswordEncoder.encode(applicationUser.getPassword()));
            applicationUserRepository.save(applicationUser);
        } else {
            // Username already exists
            // TODO: Denie signup
        }

    }

}
