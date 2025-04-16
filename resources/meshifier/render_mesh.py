import bpy
import json
import sys
import math
from mathutils import Vector

def setup_scene():
    # Clear existing objects
    bpy.ops.object.select_all(action='SELECT')
    bpy.ops.object.delete()
    
    # Create camera
    bpy.ops.object.camera_add(location=(5, -5, 5))
    camera = bpy.context.active_object
    camera.rotation_euler = (math.pi/4, 0, math.pi/4)
    
    # Create empty object as camera target
    bpy.ops.object.empty_add(type='PLAIN_AXES', location=(0, 0, 0))
    target = bpy.context.active_object
    target.name = "CameraTarget"
    
    # Create camera constraint to track target
    constraint = camera.constraints.new(type='TRACK_TO')
    constraint.target = target
    constraint.track_axis = 'TRACK_NEGATIVE_Z'
    constraint.up_axis = 'UP_Y'
    
    # Create light
    bpy.ops.object.light_add(type='SUN', location=(5, 5, 5))
    light = bpy.context.active_object
    light.data.energy = 5.0
    
    # Set active camera
    bpy.context.scene.camera = camera
    
    return camera, target

def create_mesh(mesh_data):
    # Create mesh
    mesh = bpy.data.meshes.new(name="MeshObject")
    obj = bpy.data.objects.new("MeshObject", mesh)
    
    # Link object to scene
    bpy.context.collection.objects.link(obj)
    
    # Create mesh from vertices and faces
    vertices = [tuple(v) for v in mesh_data["vertices"]]
    faces = mesh_data["faces"]
    mesh.from_pydata(vertices, [], faces)
    mesh.update()
    
    # Add basic material
    mat = bpy.data.materials.new(name="BasicMaterial")
    mat.use_nodes = True
    mat.node_tree.nodes["Principled BSDF"].inputs[0].default_value = (0.8, 0.2, 0.2, 1)
    obj.data.materials.append(mat)
    
    return obj

def setup_render():
    scene = bpy.context.scene
    scene.render.engine = 'CYCLES'
    scene.render.film_transparent = True
    scene.render.resolution_x = 800
    scene.render.resolution_y = 800
    scene.render.image_settings.file_format = 'PNG'
    scene.cycles.samples = 128
    
    # Disable denoising - handle different Blender versions
    scene.cycles.use_denoising = False
    
    # Handle different Blender versions' denoising settings
    if hasattr(scene.cycles, 'denoiser'):
        try:
            # Try to get valid enum values
            enum_items = scene.cycles.bl_rna.properties['denoiser'].enum_items
            if 'NONE' in [item.identifier for item in enum_items]:
                scene.cycles.denoiser = 'NONE'
            elif 'OPTIX' in [item.identifier for item in enum_items]:
                scene.cycles.denoiser = 'OPTIX'
            else:
                # If no recognized values found, just leave it at default
                pass
        except (AttributeError, KeyError):
            # If we can't access enum values, skip setting denoiser
            pass

def render_angles(camera, output_base, num_angles=8):
    # Calculate angle step
    angle_step = 2 * math.pi / num_angles
    
    # Get output path without extension
    output_path = output_base.rsplit('.', 1)[0]
    output_ext = output_base.rsplit('.', 1)[1]
    
    # Render from different angles
    for i in range(num_angles):
        angle = i * angle_step
        # Update camera position
        radius = math.sqrt(50)  # Distance from origin (5^2 + 5^2)
        camera.location.x = radius * math.cos(angle)
        camera.location.y = radius * math.sin(angle)
        camera.location.z = 5  # Keep constant height
        
        # Set output path for this angle
        bpy.context.scene.render.filepath = f"{output_path}_{i:02d}.{output_ext}"
        
        # Render
        bpy.ops.render.render(write_still=True)

def main():
    if len(sys.argv) < 2:
        print("Usage: blender -b -P render_mesh.py -- <input_json> <output_png>")
        sys.exit(1)
    
    # Get arguments after --
    argv = sys.argv[sys.argv.index("--") + 1:]
    input_json = argv[0]
    output_png = argv[1]
    
    # Read mesh data
    with open(input_json, 'r') as f:
        mesh_data = json.load(f)
    
    # Setup scene
    camera, target = setup_scene()
    
    # Create mesh
    obj = create_mesh(mesh_data)
    
    # Setup render settings
    setup_render()
    
    # Render from multiple angles
    render_angles(camera, output_png)

if __name__ == "__main__":
    main()