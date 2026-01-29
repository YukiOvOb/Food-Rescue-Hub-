package com.frh.backend.controller;

import com.frh.backend.Model.DietaryTag;
import com.frh.backend.Model.Inventory;
import com.frh.backend.Model.Listing;
import com.frh.backend.Model.ListingPhoto;
import com.frh.backend.Model.Store;
import com.frh.backend.repository.DietaryTagRepository;
import com.frh.backend.repository.ListingPhotoRepository;
import com.frh.backend.repository.ListingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.UUID;

@Controller
@RequestMapping("/supplier")
public class SupplierWebController {

    @Autowired
    private ListingRepository listingRepository;

    @Autowired
    private DietaryTagRepository dietaryTagRepository;

    @Autowired
    private ListingPhotoRepository listingPhotoRepository;


    // display Dashboard
    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        model.addAttribute("listings", listingRepository.findAll());
        return "dashboard";
    }

    // create listing
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        Listing listing = new Listing();

        // set store id bc we don't login
        Store defaultStore = new Store();
        defaultStore.setStoreId(99L);
        listing.setStore(defaultStore);

        //set inventory
        Inventory inventory = new Inventory();
        inventory.setQtyAvailable(1);
        inventory.setListing(listing);
        listing.setInventory(inventory);

        model.addAttribute("listing", listing);
        return "listing-form";
    }

    //edit listing form
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        return listingRepository.findById(id).map(listing -> {

            //if inventory=null
            if (listing.getInventory() == null) {
                Inventory inv = new Inventory();
                inv.setQtyAvailable(0);
                inv.setListing(listing);
                listing.setInventory(inv);
            }

            model.addAttribute("listing", listing);
            return "listing-form";
        }).orElse("redirect:/supplier/dashboard");
    }



    //save
    @PostMapping("/save")
    public String saveListing(@ModelAttribute Listing listing,
                              @RequestParam("imageFile") MultipartFile imageFile,
                              @RequestParam(value = "isHalal", required = false) Boolean isHalal) throws IOException {


        if (listing.getStore() == null || listing.getStore().getStoreId() == null) {
            Store s = new Store();
            s.setStoreId(99L);
            listing.setStore(s);
        }

        if (listing.getInventory() != null) {
            // inventory belongs to listing
            listing.getInventory().setListing(listing);
        }

        //halal tag
        DietaryTag halalTag = dietaryTagRepository.findByTagName("Halal")
                .orElseGet(() -> {
                    DietaryTag t = new DietaryTag();
                    t.setTagName("Halal");
                    return dietaryTagRepository.save(t);
                });


        if (listing.getDietaryTags() == null) {
            listing.setDietaryTags(new ArrayList<>());
        }

        if (Boolean.TRUE.equals(isHalal)) {

            boolean hasHalal = listing.getDietaryTags().stream()
                    .anyMatch(t -> t.getTagName().equals("Halal"));

            if (!hasHalal) {
                listing.getDietaryTags().add(halalTag);
            }
        } else {
            // if don't choose halal then remove it
            listing.getDietaryTags().removeIf(t -> "Halal".equals(t.getTagName()));
        }

        // upload image
        if (!imageFile.isEmpty()) {
            Path uploadPath = Paths.get("uploads");
            if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);

            String fileName = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();
            Files.copy(imageFile.getInputStream(), uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);

            ListingPhoto photo = new ListingPhoto();
            photo.setPhotoUrl(fileName);
            photo.setListing(listing);
            photo.setSortOrder(1);


            if (listing.getPhotos() == null) {
                listing.setPhotos(new ArrayList<>());
            }
            listing.getPhotos().add(photo);
        }


        listingRepository.save(listing);

        return "redirect:/supplier/dashboard";
    }

    // delete
    @GetMapping("/delete/{id}")
    public String deleteListing(@PathVariable("id") Long id) {
        listingRepository.deleteById(id);
        return "redirect:/supplier/dashboard";
    }
}