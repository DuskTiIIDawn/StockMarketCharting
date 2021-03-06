package com.example.StockMarketCharting.repositories;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.StockMarketCharting.entities.UserEntity;

@Repository
@Transactional
public interface UserEntityRepository extends JpaRepository<UserEntity, Long> {

	List<UserEntity> findByUserName(String userName);

	Boolean existsByEmail(String email);

	Boolean existsByUserName(String userName);

	Boolean existsByMobileNumber(String mobileNumber);

	Boolean existsByUserNameAndPassword(String userName, String password);
}
