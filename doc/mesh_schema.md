# Mesh Data JSON Schema

This document describes the JSON schema used for validating 3D mesh data in the Meshifier project.

## Schema Overview

The schema defines the required structure for mesh data JSON files. It enforces the presence of two mandatory properties while allowing any additional properties.

### Required Properties

1. **vertices**: An array of 3D vertex coordinates
   - Each vertex must be an array of exactly 3 numbers (x, y, z coordinates)
   - Example: `[0.0, 1.0, 0.0]`

2. **faces**: An array of faces (polygons)
   - Each face must be an array of at least 3 integers
   - Each integer must be a valid index into the vertices array
   - Faces can be triangular (3 vertices) or polygonal (4+ vertices)
   - Examples: 
     - Triangular face: `[0, 1, 2]`
     - Quadrilateral face: `[0, 1, 2, 3]`
     - Pentagonal face: `[0, 1, 2, 3, 4]`

### Additional Properties

The schema allows any additional properties to be included in the mesh data. These might include:

- **color**: RGB color values for the mesh
- **name**: A descriptive name for the mesh
- **metadata**: Additional information about the mesh
- **materials**: Material properties for rendering
- **normals**: Normal vectors for smooth shading
- **uvs**: Texture coordinates

## Schema Definition

```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "title": "Mesh Data Schema",
  "description": "Schema for validating 3D mesh data with vertices and faces",
  "type": "object",
  "required": ["vertices", "faces"],
  "properties": {
    "vertices": {
      "type": "array",
      "description": "Array of 3D vertex coordinates",
      "items": {
        "type": "array",
        "minItems": 3,
        "maxItems": 3,
        "items": {
          "type": "number"
        }
      }
    },
    "faces": {
      "type": "array",
      "description": "Array of faces defined by vertex indices (can be triangular or polygonal)",
      "items": {
        "type": "array",
        "minItems": 3,
        "items": {
          "type": "integer",
          "minimum": 0
        }
      }
    }
  },
  "additionalProperties": true
}
```

## Validation

The Meshifier project includes utilities for validating mesh data against this schema:

1. **validate-mesh**: Validates a mesh data structure
2. **validate-mesh-json**: Validates a JSON string containing mesh data
3. **validate-mesh-file**: Validates a JSON file containing mesh data
4. **validate-all-mesh-types**: Validates all available mesh types in the project

To validate all mesh types, run:

```bash
lein validate-meshes
```

## Example Valid Meshes

### Triangular Faces Example

```json
{
  "vertices": [
    [0.0, 0.0, 0.0],
    [1.0, 0.0, 0.0],
    [0.0, 1.0, 0.0],
    [0.0, 0.0, 1.0]
  ],
  "faces": [
    [0, 1, 2],
    [0, 1, 3],
    [0, 2, 3],
    [1, 2, 3]
  ],
  "name": "Tetrahedron",
  "color": [1.0, 0.0, 0.0]
}
```

This example defines a tetrahedron with 4 vertices and 4 triangular faces, along with optional name and color properties.

### Mixed Face Types Example

```json
{
  "vertices": [
    [0.0, 0.0, 0.0],
    [1.0, 0.0, 0.0],
    [1.0, 1.0, 0.0],
    [0.0, 1.0, 0.0],
    [0.5, 0.5, 1.0]
  ],
  "faces": [
    [0, 1, 2, 3],  // Quadrilateral base
    [0, 1, 4],     // Triangular side 1
    [1, 2, 4],     // Triangular side 2
    [2, 3, 4],     // Triangular side 3
    [3, 0, 4]      // Triangular side 4
  ],
  "name": "Pyramid",
  "material": {
    "color": [0.0, 0.0, 1.0],
    "roughness": 0.5
  }
}
```

This example defines a pyramid with a square base and triangular sides, demonstrating the use of both quadrilateral and triangular faces.