## Using the Tetrahedron Mesh with Normals in Blender

The tetrahedron-mesh function now returns a map with three keys:
- :vertices - The vertex coordinates
- :faces - The face indices
- :normals - The vertex normal vectors

To use this mesh with normals in Blender, you can use the following Python code:

```python
import bpy
import bmesh
from mathutils import Vector

# Assuming mesh_data contains the output from tetrahedron-mesh
vertices = mesh_data["vertices"]
faces = mesh_data["faces"]
normals = mesh_data["normals"]

# Create mesh
mesh = bpy.data.meshes.new(name="Tetrahedron")
obj = bpy.data.objects.new("Tetrahedron", mesh)

# Link object to scene
bpy.context.collection.objects.link(obj)

# Create mesh from vertices and faces
mesh.from_pydata(vertices, [], faces)
mesh.update()

# Apply custom normals
mesh.use_auto_smooth = True
mesh.normals_split_custom_set_from_vertices(normals)
```

This code will:
1. Create a new mesh from the vertex and face data
2. Enable custom split normals (auto smooth)
3. Apply the custom vertex normals

The resulting tetrahedron will use the provided normal vectors for shading, which can create either sharp or smooth edges depending on the normal values.