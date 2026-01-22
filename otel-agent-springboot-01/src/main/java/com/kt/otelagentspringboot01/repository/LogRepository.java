package com.kt.otelagentspringboot01.repository;



import com.kt.otelagentspringboot01.domain.LogApi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


@Repository
@Transactional
public interface LogRepository extends JpaRepository<LogApi, Long> {

}
