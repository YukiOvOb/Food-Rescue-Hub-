package com.frh.backend.controller;

import com.frh.backend.DTO.StoreRequestDTO;
import com.frh.backend.Model.Store;
import com.frh.backend.interfaces.StoreInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stores")
@CrossOrigin(origins = "*")
public class StoreController {

    @Autowired
    private StoreInterface storeInterface;

    @PostMapping("/create")
    public ResponseEntity<Store> createNewStore(@RequestBody StoreRequestDTO request){
        Store newStore = storeInterface.createStore(request);
        return ResponseEntity.ok(newStore);
    }
}
