package com.frh.backend.controller;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.CrossOrigin;


import com.frh.backend.dto.StoreRequest;
import com.frh.backend.dto.StoreResponse;
import com.frh.backend.service.StoreService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/stores")
@CrossOrigin(origins = "*")
public class StoreController {

  @Autowired private StoreService storeService;

  @PostMapping("/create")
  public ResponseEntity<StoreResponse> createNewStore(@Valid @RequestBody StoreRequest request) {
    StoreResponse newStore = storeService.createStore(request); // Service now returns DTO
    return ResponseEntity.status(HttpStatus.CREATED).body(newStore);
  }

  @GetMapping("/{storeId}")
  public ResponseEntity<StoreResponse> getStoreById(@PathVariable Long storeId) {
    StoreResponse store = storeService.getStoreResponseById(storeId);
    return ResponseEntity.ok(store);
  }

  @GetMapping("/supplier/{supplierId}") // GET /api/stores/supplier/1
  public ResponseEntity<List<StoreResponse>> getStoresBySupplier(@PathVariable Long supplierId) {
    List<StoreResponse> stores = storeService.getAllStores(supplierId);
    return ResponseEntity.ok(stores);
  }

  @PutMapping("/update/{storeId}") // PUT /api/stores/update/5
  public ResponseEntity<StoreResponse> updateStore(
      @PathVariable Long storeId, @Valid @RequestBody StoreRequest request) {
    StoreResponse response = storeService.updateStore(storeId, request);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/delete/{storeId}") // DELETE /api/stores/delete/5
  public ResponseEntity<Void> deleteStore(@PathVariable Long storeId) {
    storeService.deleteStore(storeId);
    return ResponseEntity.noContent().build(); // Returns 204 No Content
  }
}
