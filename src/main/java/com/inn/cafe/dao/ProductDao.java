package com.inn.cafe.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import com.inn.cafe.POJO.Product;
import com.inn.cafe.wrapper.ProductWrapper;

import jakarta.transaction.Transactional;

public interface ProductDao extends JpaRepository<Product, Integer> {

    List<ProductWrapper> getAllProduct();

    @Modifying
    @Transactional
    Integer updateProductStatus(@Param("status") String status, @Param("id") Integer id);

    List<ProductWrapper> getProductByCategoryId(@Param("id") Integer id);

    ProductWrapper getProductById(@Param("id") Integer id);

}
