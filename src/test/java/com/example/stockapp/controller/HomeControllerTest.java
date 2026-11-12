// package com.example.stockapp.controllers;

// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.ArgumentMatchers.eq;
// import static org.mockito.Mockito.when;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

// import java.time.LocalDate;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.boot.test.mock.mockito.MockBean;
// import org.springframework.test.web.servlet.MockMvc;

// import com.example.stockapp.controllers.HomeController;
// import com.example.stockapp.entities.Product;
// import com.example.stockapp.repositories.CategoryRepository;
// import com.example.stockapp.repositories.FamilyRepository;
// import com.example.stockapp.repositories.IconRepository;
// import com.example.stockapp.repositories.ProductRepository;
// import com.example.stockapp.repositories.UnitRepository;
// import com.example.stockapp.services.ProductService;



// @SpringBootTest
// @Transactional
// @WebMvcTest(HomeController.class)
// class HomeControllerTest {

//     @Autowired
//     private MockMvc mockMvc;

//     @MockBean
//     private ProductRepository productRepository;

// // =========================================================================
// // UT-HOME-001: ホーム画面表示
// // =========================================================================

//     @Test
//     @DisplayName("UT-HOME-001 ホーム画面を表示できる")
//     void testDisplayHome() throws Exception {

//         when(productRepository
//             .countByFamily_IdAndStockAndIsDeletedFalse(
//                     eq(1L), eq(0)))
//             .thenReturn(0L);

//         when(productRepository
//             .countByFamily_IdAndExpiryDateBeforeAndIsDeletedFalse(
//                     eq(1L), any(LocalDate.class)))
//             .thenReturn(0L);

//         when(productRepository.countLowStock(1L))
//             .thenReturn(0L);

//         when(productRepository.countExpiringSoon(
//             eq(1L), any(LocalDate.class)))
//                 .thenReturn(0L);

//         mockMvc.perform(get("/"))
//             .andExpect(status().isOk())
//             .andExpect(view().name("home"));
//     }

// // =========================================================================
// // UT-HOME-002: 在庫切れ商品件数表示
// // =========================================================================

//     @Test
//     @DisplayName("UT-HOME-002 在庫切れ件数をModelへ格納する")
//     void testOutOfStockCountDisplay() throws Exception {

//         when(productRepository
//             .countByFamily_IdAndStockAndIsDeletedFalse(
//                     eq(1L), eq(0)))
//             .thenReturn(3L);

//         when(productRepository
//             .countByFamily_IdAndExpiryDateBeforeAndIsDeletedFalse(
//                     eq(1L), any(LocalDate.class)))
//             .thenReturn(0L);

//         when(productRepository.countLowStock(1L))
//             .thenReturn(0L);

//         when(productRepository.countExpiringSoon(
//             eq(1L), any(LocalDate.class)))
//             .thenReturn(0L);

//         mockMvc.perform(get("/"))
//             .andExpect(status().isOk())
//             .andExpect(model().attributeExists("homeSummary"));
//     }

// // =========================================================================
// //  UT-HOME-003: 賞味期限通知表示
// // =========================================================================

//     @Test
//     @DisplayName("UT-HOME-003 賞味期限通知情報をModelへ格納する")
//     void testExpiryAlertDisplay() throws Exception {

//         when(productRepository
//             .countByFamily_IdAndStockAndIsDeletedFalse(
//                     eq(1L), eq(0)))
//             .thenReturn(0L);

//         when(productRepository
//             .countByFamily_IdAndExpiryDateBeforeAndIsDeletedFalse(
//                     eq(1L), any(LocalDate.class)))
//             .thenReturn(2L);

//         when(productRepository.countLowStock(1L))
//             .thenReturn(0L);

//         when(productRepository.countExpiringSoon(
//             eq(1L), any(LocalDate.class)))
//                 .thenReturn(1L);

//         mockMvc.perform(get("/"))
//             .andExpect(status().isOk())
//             .andExpect(model().attributeExists("homeSummary"));
//     }
// }