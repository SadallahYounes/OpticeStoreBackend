package com.opticstore.security.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

  /*  @Query("SELECT COUNT(DISTINCT o.user) FROM Order o WHERE o.user IS NOT NULL")
    Long countUsersWithOrders();*/
}