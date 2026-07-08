package com.genc.omnichannel.productcatalog.mapper;

import com.genc.omnichannel.productcatalog.dto.ProductRequest;
import com.genc.omnichannel.productcatalog.dto.ProductResponse;
import com.genc.omnichannel.productcatalog.model.Product;


public final class ProductMapper {

    private ProductMapper() {
    
    }
    public static Product toEntity(ProductRequest request) {
        Product product = new Product();
        product.setSkuCode(request.getSkuCode());
        product.setProductName(request.getProductName());
        product.setCategory(request.getCategory());
        product.setBasePrice(request.getBasePrice());
        product.setProductStatus(request.getProductStatus());
        return product;
    }

    public static ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getProductId(),
                product.getSkuCode(),
                product.getProductName(),
                product.getCategory(),
                product.getBasePrice(),
                product.getProductStatus());
    }
}
