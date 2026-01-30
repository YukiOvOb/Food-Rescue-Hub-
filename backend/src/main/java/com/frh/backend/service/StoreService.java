package com.frh.backend.service;

import com.frh.backend.dto.StoreRequest;
import com.frh.backend.Model.Store;
import com.frh.backend.Model.SupplierProfile;
import com.frh.backend.dto.StoreResponse;
import com.frh.backend.repository.StoreRepository;
import com.frh.backend.repository.SupplierProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StoreService {

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private SupplierProfileRepository supplierProfileRepository;

    // create new store
    @Transactional
    public StoreResponse createStore(StoreRequest dto) { // Change return type to StoreResponse
        SupplierProfile supplier = supplierProfileRepository.findById(dto.getSupplierId())
                .orElseThrow(() -> new RuntimeException("Supplier not found"));

        Store store = new Store();
        store.setSupplierProfile(supplier);
        store.setStoreName(dto.getStoreName());
        store.setAddressLine(dto.getAddressLine());
        store.setPostalCode(dto.getPostalCode());
        store.setLat(dto.getLat());
        store.setLng(dto.getLng());
        store.setOpeningHours(dto.getOpeningHours());
        store.setDescription(dto.getDescription());
        store.setPickupInstructions(dto.getPickupInstructions());
        store.setActive(true);

        Store savedStore = storeRepository.save(store);
        return mapToResponse(savedStore);
    }

    // Get List of stores
    @Transactional(readOnly = true)
    public List<StoreResponse> getAllStores(Long supplierId) {
        List<Store> stores = storeRepository.findBySupplierProfile_SupplierId(supplierId);
        // Convert the entire list of Entities to a list of DTOs
        return stores.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Store getStoreById(Long storeId){
        return storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found"));
    };

    @Transactional(readOnly = true)
    public StoreResponse getStoreResponseById(Long storeId) {
        Store store = getStoreById(storeId);
        return mapToResponse(store);
    }

    private StoreResponse mapToResponse(Store store) {
        StoreResponse response = new StoreResponse();
        response.setStoreId(store.getStoreId());
        response.setStoreName(store.getStoreName());
        response.setAddressLine(store.getAddressLine());
        response.setPostalCode(store.getPostalCode());
        response.setLat(store.getLat());
        response.setLng(store.getLng());
        response.setOpeningHours(store.getOpeningHours());
        response.setDescription(store.getDescription());
        response.setPickupInstructions(store.getPickupInstructions());
        response.setActive(store.isActive());

        if (store.getSupplierProfile() != null) {
            response.setSupplierId(store.getSupplierProfile().getSupplierId());
        }
        return response;
    }

    @Transactional
    public StoreResponse updateStore(Long storeId, StoreRequest dto) {
        Store existingStore = getStoreById(storeId);

        existingStore.setStoreName(dto.getStoreName());
        existingStore.setAddressLine(dto.getAddressLine());
        existingStore.setLat(dto.getLat());
        existingStore.setLng(dto.getLng());
        existingStore.setPostalCode(dto.getPostalCode());
        existingStore.setOpeningHours(dto.getOpeningHours());
        existingStore.setDescription(dto.getDescription());
        existingStore.setPickupInstructions(dto.getPickupInstructions());

        Store savedStore = storeRepository.save(existingStore);

        return mapToResponse(savedStore);
    }

    // Delete a store
    @Transactional
    public void deleteStore(Long storeId) {
        if (!storeRepository.existsById(storeId)) {
            throw new RuntimeException("Store not found");
        }
        storeRepository.deleteById(storeId);
    }
}
