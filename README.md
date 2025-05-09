# Meshifier

A Clojure library for generating and rendering 3D mesh data.

## Installation

Clone this repository and ensure you have the following prerequisites installed:
- Java
- Leiningen
- Blender (for rendering)

## Usage

### Interactive Mode

To use the interactive shape selection mode:

```bash
lein run -i
```

This will:
1. Display a list of available shapes
2. Let you choose a shape to render
3. Generate the selected shape and create renders from multiple angles
4. Save the renders as PNG files in the output directory

Available shapes:
- tetrahedron: A regular tetrahedron with four equilateral triangular faces
- joined-tetrahedrons: Two tetrahedrons joined by one face
- chair: A simple chair with seat, backrest and four legs
- spaceship: A space ship with main body, wings, cockpit, and propulsion elements

### Direct Rendering

To generate mesh data and save renders directly:

```bash
lein run output/render
```

This will:
1. Generate a chair mesh (default shape)
2. Create renders from multiple angles
3. Save the renders as PNG files (output/render_00.png, output/render_01.png, etc.)

### Output JSON Only

To output the mesh data as JSON without rendering:

```bash
lein run -
```

### Validate Mesh Data

To validate all mesh types against the JSON schema:

```bash
lein validate-meshes
```

This will:
1. Generate all available mesh types
2. Validate each mesh against the JSON schema
3. Report which meshes are valid and which are invalid

### Development

Run tests:

```bash
lein test
```

Build an uberjar:

```bash
lein uberjar
```

## Features

- Interactive shape selection mode
- Generate 3D mesh data for multiple shapes:
  - Regular tetrahedron
  - Joined tetrahedrons
  - Chair
  - Stylized Cat
  - Space Ship with propulsion elements
- Render meshes using Blender from multiple angles
- Export mesh data in JSON format
- Point-in-tetrahedron testing
- Volume calculations
- JSON schema validation for mesh data

## License

Copyright © 2023

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.


