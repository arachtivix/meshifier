#!/bin/bash

# Check if required arguments are provided
if [ "$#" -ne 1 ]; then
    echo "Usage: $0 <output_prefix>"
    exit 1
fi

OUTPUT_PREFIX="$1"
SCRIPT_DIR="$(dirname "$(readlink -f "$0")")"
TEMP_JSON="/tmp/mesh_data.json"

# Create output directory if it doesn't exist
OUTPUT_DIR=$(dirname "$OUTPUT_PREFIX")
mkdir -p "$OUTPUT_DIR"

# Run Clojure program and capture output
# Note: Assumes the Clojure program prints JSON to stdout
lein run > "$TEMP_JSON"

# Check if the JSON file was created successfully
if [ ! -f "$TEMP_JSON" ]; then
    echo "Error: Failed to generate mesh data"
    exit 1
fi

# Run Blender with the Python script
# The script will generate multiple files with names like output_00.png, output_01.png, etc.
blender -b -P "$SCRIPT_DIR/render_mesh.py" -- "$TEMP_JSON" "${OUTPUT_PREFIX}.png"

# Clean up temporary file
rm "$TEMP_JSON"

echo "Renders completed: ${OUTPUT_PREFIX}_XX.png (multiple files)"
