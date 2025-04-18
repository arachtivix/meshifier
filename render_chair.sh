#!/bin/bash
set -e

# Ensure output directory exists
mkdir -p output

# Check if interactive mode is requested
if [ "$1" = "-i" ]; then
    # Run in interactive mode
    lein run -i
else
    # Run the chair rendering (default behavior)
    lein run output/chair_render
    echo "Chair rendering complete. Check output/chair_render_XX.png files."
fi