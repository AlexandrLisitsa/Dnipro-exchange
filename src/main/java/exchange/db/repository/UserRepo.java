package exchange.db.repository;

import exchange.db.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepo extends JpaRepository<Client, Long> {
    Client getClientByUserId(String userId);

    Client findClientByPhone(String phone);
}
