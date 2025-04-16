# Meshifier

A Clojure library for generating and rendering 3D mesh data.

## Installation

Clone this repository and ensure you have the following prerequisites installed:
- Java
- Leiningen
- Blender (for rendering)

## Usage

### Basic Usage

To generate mesh data and save renders:

```bash
lein run output/render
```

This will:
1. Generate a tetrahedron mesh
2. Create renders from multiple angles
3. Save the renders as PNG files (output/render_00.png, output/render_01.png, etc.)

### Output JSON Only

To output the mesh data as JSON without rendering:

```bash
lein run -
```

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

- Generate 3D mesh data (currently supports tetrahedron)
- Render meshes using Blender from multiple angles
- Export mesh data in JSON format
- Point-in-tetrahedron testing
- Volume calculations

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