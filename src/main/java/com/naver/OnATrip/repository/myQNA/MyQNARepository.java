package com.naver.OnATrip.repository.myQNA;

import com.naver.OnATrip.entity.MyQNA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MyQNARepository extends JpaRepository<MyQNA, Long>, MyQNARepositoryCustom {

    Optional<MyQNA> findById(Long id);
}