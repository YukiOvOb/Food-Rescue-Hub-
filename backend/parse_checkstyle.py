import xml.etree.ElementTree as ET
from collections import Counter

def parse_checkstyle_report(file_path):
    tree = ET.parse(file_path)
    root = tree.getroot()
    
    errors = []
    for file in root.findall('file'):
        for error in file.findall('error'):
            errors.append(error.get('source'))
            
    return Counter(errors)

if __name__ == "__main__":
    report_path = '/home/ubuntu/Food-Rescue-Hub-/backend/target/checkstyle-result.xml'
    error_counts = parse_checkstyle_report(report_path)
    
    for error, count in error_counts.items():
        print(f"{error}: {count}")
    
    print(f"\nTotal unique error types: {len(error_counts)}")
    print(f"Total errors: {sum(error_counts.values())}")
