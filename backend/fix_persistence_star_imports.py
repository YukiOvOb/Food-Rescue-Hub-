import os
import re

def fix_star_imports(directory):
    for root, _, files in os.walk(directory):
        for file in files:
            if file.endswith(".java"):
                filepath = os.path.join(root, file)
                try:
                    with open(filepath, 'r', encoding='utf-8') as f:
                        content = f.read()
                except UnicodeDecodeError:
                    with open(filepath, 'r', encoding='latin-1') as f:
                        content = f.read()

                if 'import jakarta.persistence.*;' in content:
                    # Find all used classes from the package
                    used_classes = set(re.findall(r'@(Entity|Id|GeneratedValue|GenerationType|ManyToOne|JoinColumn|OneToMany|Column|Table|Enumerated|EnumType|Temporal|TemporalType|EmbeddedId|MapsId|OneToOne|ManyToMany|JoinTable|Inheritance|InheritanceType|DiscriminatorColumn|DiscriminatorValue)\b', content))
                    
                    if used_classes:
                        new_imports = '\n'.join(f'import jakarta.persistence.{cls};' for cls in sorted(used_classes))
                        
                        # Be more specific with the replacement to avoid unintended changes
                        # Add common annotations that might not be preceded by @
                        common_annotations = {
                            "Entity", "Id", "GeneratedValue", "GenerationType", "ManyToOne", 
                            "JoinColumn", "OneToMany", "Column", "Table", "Enumerated", 
                            "EnumType", "Temporal", "TemporalType", "EmbeddedId", "MapsId", 
                            "OneToOne", "ManyToMany", "JoinTable", "Inheritance", 
                            "InheritanceType", "DiscriminatorColumn", "DiscriminatorValue",
                            "CascadeType" # Often used inside other annotations
                        }
                        
                        # Also find usages that are not annotations
                        non_annotation_usages = set(re.findall(r'\b(Entity|Id|GeneratedValue|GenerationType|ManyToOne|JoinColumn|OneToMany|Column|Table|Enumerated|EnumType|Temporal|TemporalType|EmbeddedId|MapsId|OneToOne|ManyToMany|JoinTable|Inheritance|InheritanceType|DiscriminatorColumn|DiscriminatorValue|CascadeType)\b', content))

                        all_used = used_classes.union(non_annotation_usages).intersection(common_annotations)

                        if all_used:
                            new_imports = '\n'.join(f'import jakarta.persistence.{cls};' for cls in sorted(list(all_used)))
                            
                            # Replace the star import
                            new_content = content.replace('import jakarta.persistence.*;', new_imports)
                            
                            # Also replace lombok star import if present
                            if 'import lombok.*;' in new_content:
                                lombok_classes = set(re.findall(r'@(Data|Getter|Setter|NoArgsConstructor|AllArgsConstructor|Builder|RequiredArgsConstructor|EqualsAndHashCode|ToString|Value)\b', new_content))
                                if lombok_classes:
                                    new_lombok_imports = '\n'.join(f'import lombok.{cls};' for cls in sorted(list(lombok_classes)))
                                    new_content = new_content.replace('import lombok.*;', new_lombok_imports)

                            if new_content != content:
                                print(f"Fixing imports in: {filepath}")
                                with open(filepath, 'w', encoding='utf-8') as f:
                                    f.write(new_content)

fix_star_imports('/home/ubuntu/Food-Rescue-Hub-/backend/src/main/java/com/frh/backend/model')
