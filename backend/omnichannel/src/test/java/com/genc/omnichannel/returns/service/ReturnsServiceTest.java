// package com.genc.omnichannel.returns.service;

// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.junit.jupiter.api.Assertions.assertThrows;
// import static org.mockito.Mockito.never;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.when;
// import static org.mockito.ArgumentMatchers.any;

// import java.math.BigDecimal;
// import java.time.LocalDate;
// import java.util.NoSuchElementException;
// import java.util.Optional;

// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;

// import com.genc.omnichannel.returns.dto.InitiateReturnRequest;
// import com.genc.omnichannel.returns.dto.ReturnResponse;
// import com.genc.omnichannel.returns.model.ReturnRequest;
// import com.genc.omnichannel.returns.model.ReturnStatus;
// import com.genc.omnichannel.returns.repository.ReturnRepository;

// /**
//  * Basic unit tests for {@link ReturnsService} using JUnit 5 + Mockito.
//  * The repository is mocked, so these tests run without a database or Spring context.
//  */
// @ExtendWith(MockitoExtension.class)
// class ReturnsServiceTest {

//     @Mock
//     private ReturnRepository returnRepository;

//     @InjectMocks
//     private ReturnsService returnsService;

//     /** Builds a valid request dated today (well inside the return window). */
//     private InitiateReturnRequest sampleRequest() {
//         InitiateReturnRequest request = new InitiateReturnRequest();
//         request.setOrderId(100L);
//         request.setReturnReason("Wrong size");
//         request.setRefundAmount(new BigDecimal("25.00"));
//         request.setRequestDate(LocalDate.now());
//         return request;
//     }

//     /** Builds a stored return entity with the given id and status. */
//     private ReturnRequest storedReturn(Long id, ReturnStatus status) {
//         ReturnRequest entity = new ReturnRequest(100L, "Wrong size",
//                 new BigDecimal("25.00"), LocalDate.now(), status);
//         entity.setReturnId(id);
//         return entity;
//     }

//     @Test
//     void initiateReturn_savesWithRequestedStatus() {
//         InitiateReturnRequest request = sampleRequest();
//         when(returnRepository.save(any(ReturnRequest.class))).thenAnswer(invocation -> {
//             ReturnRequest toSave = invocation.getArgument(0);
//             toSave.setReturnId(10L);
//             return toSave;
//         });

//         ReturnResponse response = returnsService.initiateReturn(request);

//         assertEquals(10L, response.getReturnId());
//         assertEquals(100L, response.getOrderId());
//         assertEquals(ReturnStatus.REQUESTED, response.getReturnStatus());
//     }

//     @Test
//     void initiateReturn_missingOrderId_throwsAndDoesNotSave() {
//         InitiateReturnRequest request = sampleRequest();
//         request.setOrderId(null);

//         assertThrows(IllegalArgumentException.class, () -> returnsService.initiateReturn(request));
//         verify(returnRepository, never()).save(any(ReturnRequest.class));
//     }

//     @Test
//     void initiateReturn_outsideWindow_throwsAndDoesNotSave() {
//         InitiateReturnRequest request = sampleRequest();
//         request.setRequestDate(LocalDate.now().minusDays(60));

//         assertThrows(IllegalArgumentException.class, () -> returnsService.initiateReturn(request));
//         verify(returnRepository, never()).save(any(ReturnRequest.class));
//     }

//     @Test
//     void approveReturn_movesRequestedToApproved() {
//         when(returnRepository.findById(5L)).thenReturn(Optional.of(storedReturn(5L, ReturnStatus.REQUESTED)));
//         when(returnRepository.save(any(ReturnRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

//         ReturnResponse response = returnsService.approveReturn(5L);

//         assertEquals(ReturnStatus.APPROVED, response.getReturnStatus());
//     }

//     @Test
//     void approveReturn_wrongState_throwsIllegalState() {
//         when(returnRepository.findById(5L)).thenReturn(Optional.of(storedReturn(5L, ReturnStatus.APPROVED)));

//         assertThrows(IllegalStateException.class, () -> returnsService.approveReturn(5L));
//         verify(returnRepository, never()).save(any(ReturnRequest.class));
//     }

//     @Test
//     void processRefund_movesApprovedToRefunded() {
//         when(returnRepository.findById(7L)).thenReturn(Optional.of(storedReturn(7L, ReturnStatus.APPROVED)));
//         when(returnRepository.save(any(ReturnRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

//         ReturnResponse response = returnsService.processRefund(7L);

//         assertEquals(ReturnStatus.REFUNDED, response.getReturnStatus());
//     }

//     @Test
//     void processRefund_notApproved_throwsIllegalState() {
//         when(returnRepository.findById(7L)).thenReturn(Optional.of(storedReturn(7L, ReturnStatus.REQUESTED)));

//         assertThrows(IllegalStateException.class, () -> returnsService.processRefund(7L));
//         verify(returnRepository, never()).save(any(ReturnRequest.class));
//     }

//     @Test
//     void getReturnRequest_notFound_throwsNoSuchElement() {
//         when(returnRepository.findById(404L)).thenReturn(Optional.empty());

//         assertThrows(NoSuchElementException.class, () -> returnsService.getReturnRequest(404L));
//     }
// }
