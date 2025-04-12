## Using the Tetrahedron Mesh in Blender

The tetrahedron-mesh function returns a map with two keys:
- :vertices - The vertex coordinates
- :faces - The face indices

### Manual Usage in Blender

To use this mesh in Blender manually, you can use the following Python code:

```python
import bpy
import bmesh
from mathutils import Vector

# Assuming mesh_data contains the output from tetrahedron-mesh
vertices = mesh_data["vertices"]
faces = mesh_data["faces"]

# Create mesh
mesh = bpy.data.meshes.new(name="Tetrahedron")
obj = bpy.data.objects.new("Tetrahedron", mesh)

# Link object to scene
bpy.context.collection.objects.link(obj)

# Create mesh from vertices and faces
mesh.from_pydata(vertices, [], faces)
mesh.update()
```

This code will:
1. Create a new mesh from the vertex and face data
2. Link it to the current scene

The resulting tetrahedron will use Blender's default shading settings.

### Automated Rendering

The repository includes scripts to automatically render the mesh to a PNG file:

1. **render_mesh.py** - A Python script that creates a scene in Blender and renders the mesh
2. **render_mesh.sh** - A bash script that:
   - Runs the Clojure program to generate mesh data
   - Passes the data to Blender
   - Renders the result to a PNG file

To use the automated rendering:

1. Make sure you have Blender installed with the bpy module
2. Make the script executable:
   ```bash
   chmod +x src/meshifier/render_mesh.sh
   ```
3. Run the script:
   ```bash
   ./src/meshifier/render_mesh.sh output.png
   ```

The script will create a scene with:
- A camera positioned for a good view of the mesh
- A sun light for illumination
- A basic red material applied to the mesh
- Transparent background
- 800x800 resolution

The output will be saved to the specified PNG file.