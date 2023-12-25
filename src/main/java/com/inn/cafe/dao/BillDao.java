package com.inn.cafe.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import com.inn.cafe.POJO.Bill;

public interface BillDao extends JpaRepository<Bill, Integer> {

    List<Bill> getAllBills();

    List<Bill> getBillByUsername(@Param("username") String username);
    
}
