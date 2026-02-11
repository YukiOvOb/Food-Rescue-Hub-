import os
import re

def fix_star_import_for_file(filepath, package, classes):
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
    except UnicodeDecodeError:
        with open(filepath, 'r', encoding='latin-1') as f:
            content = f.read()

    star_import = f'import {package}.*;'
    if star_import in content:
        used_classes = set(re.findall(r'\b(' + '|'.join(classes) + r')\b', content))
        if used_classes:
            new_imports = '\n'.join(f'import {package}.{cls};' for cls in sorted(list(used_classes)))
            new_content = content.replace(star_import, new_imports)
            if new_content != content:
                print(f"Fixing {package}.* in: {filepath}")
                with open(filepath, 'w', encoding='utf-8') as f:
                    f.write(new_content)

# Fix RecommendationService.java
recommendation_service_path = '/home/ubuntu/Food-Rescue-Hub-/backend/src/main/java/com/frh/backend/service/RecommendationService.java'
http_classes = ["HttpEntity", "HttpHeaders", "HttpMethod", "ResponseEntity", "HttpStatus"]
model_classes_rec = ["Listing", "Store", "User", "UserInteraction"]
repo_classes_rec = ["ListingRepository", "StoreRepository", "UserInteractionRepository", "UserRepository"]

fix_star_import_for_file(recommendation_service_path, 'org.springframework.http', http_classes)
fix_star_import_for_file(recommendation_service_path, 'com.frh.backend.model', model_classes_rec)
fix_star_import_for_file(recommendation_service_path, 'com.frh.backend.repository', repo_classes_rec)

# Fix OrderService.java
order_service_path = '/home/ubuntu/Food-Rescue-Hub-/backend/src/main/java/com/frh/backend/service/OrderService.java'
model_classes_order = ["Cart", "CartItem", "Inventory", "Listing", "Order", "OrderItem", "OrderStatus", "User"]
repo_classes_order = ["CartItemRepository", "CartRepository", "InventoryRepository", "ListingRepository", "OrderItemRepository", "OrderRepository", "UserRepository"]

fix_star_import_for_file(order_service_path, 'com.frh.backend.model', model_classes_order)
fix_star_import_for_file(order_service_path, 'com.frh.backend.repository', repo_classes_order)

# Fix StoreRequest.java
store_request_path = '/home/ubuntu/Food-Rescue-Hub-/backend/src/main/java/com/frh/backend/dto/StoreRequest.java'
validation_classes = ["NotBlank", "NotNull", "Size"]
fix_star_import_for_file(store_request_path, 'jakarta.validation.constraints', validation_classes)

