package com.genc.omnichannel.productcatalog.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.genc.omnichannel.productcatalog.dto.ProductRequest;
import com.genc.omnichannel.productcatalog.dto.ProductResponse;
import com.genc.omnichannel.productcatalog.mapper.ProductMapper;
import com.genc.omnichannel.productcatalog.model.Product;
import com.genc.omnichannel.productcatalog.model.ProductStatus;
import com.genc.omnichannel.productcatalog.repository.ProductRepository;


@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public ProductResponse addProduct(ProductRequest request) {
        if (productRepository.existsBySkuCode(request.getSkuCode())) {
            throw new IllegalArgumentException(
                    "A product with SKU '" + request.getSkuCode() + "' already exists.");
        }
        Product product = ProductMapper.toEntity(request);
        if (product.getProductStatus() == null) {
            product.setProductStatus(ProductStatus.ACTIVE);
        }
        return ProductMapper.toResponse(productRepository.save(product));
    }


    @Transactional
    public ProductResponse updatePricing(Long productId, BigDecimal newPrice) {
        if (newPrice == null || newPrice.signum() < 0) {
            throw new IllegalArgumentException("New price must be a non-negative value.");
        }
        Product product = findEntity(productId);
        product.setBasePrice(newPrice);
        return ProductMapper.toResponse(productRepository.save(product));
    }


    @Transactional(readOnly = true)
    public ProductResponse getProductDetails(Long productId) {
        return ProductMapper.toResponse(findEntity(productId));
    }


    @Transactional
    public ProductResponse deactivateProduct(Long productId) {
        Product product = findEntity(productId);
        product.setProductStatus(ProductStatus.INACTIVE);
        return ProductMapper.toResponse(productRepository.save(product));
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(ProductMapper::toResponse)
                .toList();
    }

    private Product findEntity(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Product not found with id: " + productId));
    }
}
