import os
import re

directory = r'e:\FPT_Software_Engineer\Ky9\Capstone\RealVista-BE\src\main\resources\db\migration'
files = [f for f in os.listdir(directory) if f.endswith('.sql')]

# Parse and sort
parsed_files = []
for f in files:
    match = re.match(r'V(\d+)__(.*)\.sql', f)
    if match:
        version = int(match.group(1))
        description = match.group(2)
        parsed_files.append({
            'old_name': f,
            'version': version,
            'description': description
        })

parsed_files.sort(key=lambda x: x['version'])

# Find where to start re-sequencing
# We keep versions 1 to 123 as they are.
# From the next file after V123, we start numbering from 124.

resequence_start_index = -1
for i, f in enumerate(parsed_files):
    if f['version'] == 123:
        resequence_start_index = i + 1
        break

if resequence_start_index != -1:
    next_version = 124
    for i in range(resequence_start_index, len(parsed_files)):
        file_info = parsed_files[i]
        new_name = f"V{next_version}__{file_info['description']}.sql"
        
        if file_info['old_name'] != new_name:
            old_path = os.path.join(directory, file_info['old_name'])
            new_path = os.path.join(directory, new_name)
            print(f"Renaming: {file_info['old_name']} -> {new_name}")
            os.rename(old_path, new_path)
        
        next_version += 1
else:
    print("V123 not found or no files after V123.")
