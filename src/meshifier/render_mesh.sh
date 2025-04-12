#!/bin/bash

# Check if required arguments are provided
if [ "$#" -ne 1 ]; then
    echo "Usage: $0 <output_png>"
    exit 1
fi

OUTPUT_PNG="$1"
SCRIPT_DIR="$(dirname "$(readlink -f "$0")")"
TEMP_JSON="/tmp/mesh_data.json"

# Run Clojure program and capture output
# Note: Assumes the Clojure program prints JSON to stdout
lein run > "$TEMP_JSON"

# Check if the JSON file was created successfully
if [ ! -f "$TEMP_JSON" ]; then
    echo "Error: Failed to generate mesh data"
    exit 1
fi

# Run Blender with the Python script
blender -b -P "$SCRIPT_DIR/render_mesh.py" -- "$TEMP_JSON" "$OUTPUT_PNG"

# Clean up temporary file
rm "$TEMP_JSON"

echo "Render completed: $OUTPUT_PNG"