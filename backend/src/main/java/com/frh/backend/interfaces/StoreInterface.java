package com.frh.backend.interfaces;

import com.frh.backend.DTO.StoreRequestDTO;
import com.frh.backend.Model.Store;

public interface StoreInterface {

    Store createStore(StoreRequestDTO dto);
}
