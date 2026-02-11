import os
import sys

def fix_dto_star_imports(file_path):
    with open(file_path, 'r') as f:
        lines = f.readlines()

    new_lines = []
    star_import_found = False
    specific_imports = set()
    
    dtos = [
        "AuthResponse", "CartResponseDto", "Co2CategoryBreakdownDto", "Co2SummaryDto",
        "ConsumerDTO", "CreateListingReviewRequest", "CreateOrderRequest",
        "CreateOrderResponseDto", "ErrorResponse", "InventoryAdjustRequest",
        "InventoryResponseDto", "ListingCategoryWeightDTO", "ListingDTO",
        "ListingReviewResponse", "LoginRequest", "OrderResponseDto", "OrderSummaryDTO",
        "PickupTokenResponseDto", "RegisterRequest", "RejectOrderRequest",
        "StoreRecommendationDTO", "StoreRequest", "StoreResponse", "TopSellingItemDto",
        "UpdateLocationRequest", "UserInteractionRequest"
    ]

    for line in lines:
        if "import com.frh.backend.dto.*;" in line:
            star_import_found = True
            # Don't add the star import line to new_lines
            continue
        
        for dto in dtos:
            if dto in line:
                specific_imports.add(f"import com.frh.backend.dto.{dto};")
        
        new_lines.append(line)

    if star_import_found:
        # Add the specific imports at the top of the file, after the package declaration
        package_line_index = -1
        for i, line in enumerate(new_lines):
            if line.strip().startswith("package"):
                package_line_index = i
                break
        
        if package_line_index != -1:
            for imp in sorted(list(specific_imports)):
                new_lines.insert(package_line_index + 1, imp + '\n')
            # Add a blank line after imports
            new_lines.insert(package_line_index + 1 + len(specific_imports), '\n')


    with open(file_path, 'w') as f:
        f.writelines(new_lines)

if __name__ == "__main__":
    file_path = sys.argv[1]
    fix_dto_star_imports(file_path)
