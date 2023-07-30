package com.example.openbanking.repository;

import com.example.openbanking.domain.AccountEntity;
import com.example.openbanking.domain.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<AccountEntity, Long> {
}
