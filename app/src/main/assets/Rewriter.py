import re

def parse_file(file_path):
    with open(file_path, 'r', encoding='utf-8') as file:
        lines = file.readlines()

    struct_names = []
    keys = []
    values = []

    current_struct = None

    for line in lines:
        line = line.strip()
        if line.endswith('{'):
            current_struct = line[:-1].strip()
        elif line.endswith('}'):
            current_struct = None
        elif ':' in line and current_struct:
            key, value = map(str.strip, line.split(':', 1))
            struct_names.append(current_struct)
            keys.append(key)
            values.append(value)

    return struct_names, keys, values

def create_arrays(struct_names, keys, values):
    struct_array = [f'R.drawable.{name}' for name in struct_names]
    key_array = keys
    value_array = values

    return struct_array, key_array, value_array

def main():
    file_path = 'input.txt'  # замените на путь к вашему файлу
    struct_names, keys, values = parse_file(file_path)

    struct_array, key_array, value_array = create_arrays(struct_names, keys, values)

    print("String[] arrayStruct = {", ", ".join(struct_array), "};")
    print("String[] arrayKeys = {", ", ".join(f'"{key}"' for key in key_array), "};")
    print("String[] arrayValues = {", ", ".join(f'"{value}"' for value in value_array), "};")

if __name__ == "__main__":
    main()
