import shutil
import os

src_dir = r"C:\Users\FERNANDO\Desktop\Projetos Pessoais\APP COMAV\assets"
dest_dir = r"C:\Users\FERNANDO\AndroidStudioProjects\COMAV2\app\src\main\res\drawable"

os.makedirs(dest_dir, exist_ok=True)

icon_src = os.path.join(src_dir, "icon.png")
adaptive_src = os.path.join(src_dir, "adaptive-icon.png")

icon_dest = os.path.join(dest_dir, "app_icon.png")
adaptive_dest = os.path.join(dest_dir, "splash_logo.png")

if os.path.exists(icon_src):
    shutil.copy(icon_src, icon_dest)
    print(f"Copiado: {icon_src} -> {icon_dest}")
else:
    print(f"Não encontrado: {icon_src}")

if os.path.exists(adaptive_src):
    shutil.copy(adaptive_src, adaptive_dest)
    print(f"Copiado: {adaptive_src} -> {adaptive_dest}")
else:
    print(f"Não encontrado: {adaptive_src}")
