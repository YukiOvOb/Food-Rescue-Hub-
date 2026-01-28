package com.frh.backend.controller;

import com.frh.backend.DTO.StoreRequestDTO;
import com.frh.backend.Model.Store;
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
    public ResponseEntity<Store> createNewStore(@Valid @RequestBody StoreRequestDTO request){
        Store newStore = storeService.createStore(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(newStore);
    }

    @GetMapping // Maps to GET http://localhost:8081/api/stores
    public ResponseEntity<List<Store>> getAllStores() {
        List<Store> stores = storeService.getAllStores();
        return ResponseEntity.ok(stores);
    }
}
