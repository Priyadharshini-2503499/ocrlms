package com.genc.omnichannel.productcatalog.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.genc.omnichannel.productcatalog.model.Product;
import com.genc.omnichannel.productcatalog.model.ProductStatus;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySkuCode(String skuCode);
    boolean existsBySkuCode(String skuCode);
    List<Product> findByProductStatus(ProductStatus productStatus);
    List<Product> findByCategory(String category);
}
