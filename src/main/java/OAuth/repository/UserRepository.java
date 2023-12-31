package OAuth.repository;

import OAuth.entity.AuthProvider;
import OAuth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    boolean existsByIdAndAuthProvider(String id, AuthProvider authProvider);
}
