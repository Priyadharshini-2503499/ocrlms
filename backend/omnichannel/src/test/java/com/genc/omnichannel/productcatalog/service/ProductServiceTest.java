// package com.genc.omnichannel.productcatalog.service;

// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.junit.jupiter.api.Assertions.assertThrows;
// import static org.mockito.Mockito.never;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.when;
// import static org.mockito.ArgumentMatchers.any;

// import java.math.BigDecimal;
// import java.util.List;
// import java.util.NoSuchElementException;
// import java.util.Optional;

// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;

// import com.genc.omnichannel.productcatalog.dto.ProductRequest;
// import com.genc.omnichannel.productcatalog.dto.ProductResponse;
// import com.genc.omnichannel.productcatalog.model.Product;
// import com.genc.omnichannel.productcatalog.model.ProductStatus;
// import com.genc.omnichannel.productcatalog.repository.ProductRepository;

// /**
//  * Basic unit tests for {@link ProductService} using JUnit 5 + Mockito.
//  * The repository is mocked, so these tests run without a database or Spring context.
//  */
// @ExtendWith(MockitoExtension.class)
// class ProductServiceTest {

//     @Mock
//     private ProductRepository productRepository;

//     @InjectMocks
//     private ProductService productService;

//     /** Builds a sample request with no explicit status (so the service should default it). */
//     private ProductRequest sampleRequest() {
//         ProductRequest request = new ProductRequest();
//         request.setSkuCode("SKU-100");
//         request.setProductName("Merino Sweater");
//         request.setCategory("Apparel");
//         request.setBasePrice(new BigDecimal("49.99"));
//         return request;
//     }

//     /** Builds a stored product entity with the given id and status. */
//     private Product storedProduct(Long id, ProductStatus status) {
//         Product product = new Product("SKU-100", "Merino Sweater", "Apparel",
//                 new BigDecimal("49.99"), status);
//         product.setProductId(id);
//         return product;
//     }

//     @Test
//     void addProduct_savesAndDefaultsStatusToActive() {
//         ProductRequest request = sampleRequest();
//         when(productRepository.existsBySkuCode("SKU-100")).thenReturn(false);
//         when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
//             Product toSave = invocation.getArgument(0);
//             toSave.setProductId(1L);
//             return toSave;
//         });

//         ProductResponse response = productService.addProduct(request);

//         assertEquals(1L, response.getProductId());
//         assertEquals("SKU-100", response.getSkuCode());
//         assertEquals(ProductStatus.ACTIVE, response.getProductStatus());
//         verify(productRepository).save(any(Product.class));
//     }

//     @Test
//     void addProduct_duplicateSku_throwsAndDoesNotSave() {
//         ProductRequest request = sampleRequest();
//         when(productRepository.existsBySkuCode("SKU-100")).thenReturn(true);

//         assertThrows(IllegalArgumentException.class, () -> productService.addProduct(request));
//         verify(productRepository, never()).save(any(Product.class));
//     }

//     @Test
//     void updatePricing_updatesBasePrice() {
//         Product existing = storedProduct(1L, ProductStatus.ACTIVE);
//         when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
//         when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

//         ProductResponse response = productService.updatePricing(1L, new BigDecimal("59.99"));

//         assertEquals(new BigDecimal("59.99"), response.getBasePrice());
//     }

//     @Test
//     void updatePricing_negativePrice_throwsAndDoesNotSave() {
//         assertThrows(IllegalArgumentException.class,
//                 () -> productService.updatePricing(1L, new BigDecimal("-1.00")));
//         verify(productRepository, never()).save(any(Product.class));
//     }

//     @Test
//     void getProductDetails_notFound_throwsNoSuchElement() {
//         when(productRepository.findById(99L)).thenReturn(Optional.empty());

//         assertThrows(NoSuchElementException.class, () -> productService.getProductDetails(99L));
//     }

//     @Test
//     void deactivateProduct_setsStatusInactive() {
//         Product existing = storedProduct(1L, ProductStatus.ACTIVE);
//         when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
//         when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

//         ProductResponse response = productService.deactivateProduct(1L);

//         assertEquals(ProductStatus.INACTIVE, response.getProductStatus());
//     }

//     @Test
//     void getAllProducts_returnsMappedList() {
//         when(productRepository.findAll()).thenReturn(List.of(
//                 storedProduct(1L, ProductStatus.ACTIVE),
//                 storedProduct(2L, ProductStatus.INACTIVE)));

//         List<ProductResponse> responses = productService.getAllProducts();

//         assertEquals(2, responses.size());
//         assertEquals("SKU-100", responses.get(0).getSkuCode());
//     }
// }
