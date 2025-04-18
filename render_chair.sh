#!/bin/bash

# Ensure output directory exists
mkdir -p output

# Run the chair rendering
lein run output/chair_render

echo "Chair rendering complete. Check output/chair_render_XX.png files."