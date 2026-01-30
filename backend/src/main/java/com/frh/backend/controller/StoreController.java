package com.frh.backend.controller;

import com.frh.backend.dto.StoreRequest;
import com.frh.backend.dto.StoreResponse;
import com.frh.backend.service.StoreService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stores")
@CrossOrigin(origins = "*")
public class StoreController {

    @Autowired
    private StoreService storeService;

    @PostMapping("/create")
    public ResponseEntity<StoreResponse> createNewStore(@Valid @RequestBody StoreRequest request) {
        StoreResponse newStore = storeService.createStore(request); // Service now returns DTO
        return ResponseEntity.status(HttpStatus.CREATED).body(newStore);
    }

    @GetMapping("/supplier/{supplierId}") // GET /api/stores/supplier/1
    public ResponseEntity<List<StoreResponse>> getStoresBySupplier(@PathVariable Long supplierId) {
        List<StoreResponse> stores = storeService.getAllStores(supplierId);
        return ResponseEntity.ok(stores);
    }

    @PutMapping("/update/{storeId}") // PUT /api/stores/update/5
    public ResponseEntity<StoreResponse> updateStore(@PathVariable Long storeId, @Valid @RequestBody StoreRequest request) {
        StoreResponse response = storeService.updateStore(storeId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete/{storeId}") // DELETE /api/stores/delete/5
    public ResponseEntity<Void> deleteStore(@PathVariable Long storeId) {
        storeService.deleteStore(storeId);
        return ResponseEntity.noContent().build(); // Returns 204 No Content
    }
}
