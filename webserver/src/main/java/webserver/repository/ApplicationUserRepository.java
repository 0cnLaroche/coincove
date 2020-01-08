package webserver.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import webserver.model.ApplicationUser;

@Repository
public interface ApplicationUserRepository extends MongoRepository<ApplicationUser, String> {

    public ApplicationUser findByUsername(String username);

    public boolean existsByUsername(String username);
}
