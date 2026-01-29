package com.frh.backend.service;

import com.frh.backend.dto.StoreRequest;
import com.frh.backend.Model.Store;
import com.frh.backend.Model.SupplierProfile;
import com.frh.backend.repository.StoreRepository;
import com.frh.backend.repository.SupplierProfileRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class StoreService {

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private SupplierProfileRepository supplierProfileRepository;

    public Store createStore(StoreRequest dto) {
        // find supplier using Id
        SupplierProfile supplier = supplierProfileRepository.findById(dto.getSupplierId())
                .orElseThrow(()-> new RuntimeException("Supplier not found"));

        // create store entity
        Store store = new Store();
        store.setSupplierProfile(supplier);
        store.setStoreName(dto.getStoreName());
        store.setAddressLine(dto.getAddressLine());
        store.setPostalCode(dto.getPostalCode());
        store.setLat(dto.getLat());
        store.setLng(dto.getLng());
        store.setOpeningHours(dto.getOpeningHours());
        store.setDescription(dto.getDescription());
        store.setActive(true);

        return storeRepository.save(store);
    }

    public List<Store> getAllStores() {
        return storeRepository.findAll();
    }
}
