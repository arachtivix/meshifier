## Using the Tetrahedron Mesh in Blender

The tetrahedron-mesh function returns a map with two keys:
- :vertices - The vertex coordinates
- :faces - The face indices

To use this mesh in Blender, you can use the following Python code:

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