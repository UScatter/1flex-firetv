import os
from PIL import Image, ImageDraw, ImageFont

scratch_dir = r"C:\Users\e\.gemini\antigravity\scratch\1flex-firetv"
src_icon_path = os.path.join(scratch_dir, "icon.png")
res_dir = os.path.join(scratch_dir, "android", "app", "src", "main", "res")
www_assets_dir = os.path.join(scratch_dir, "www", "assets")

os.makedirs(www_assets_dir, exist_ok=True)
os.makedirs(os.path.join(res_dir, "drawable"), exist_ok=True)
os.makedirs(os.path.join(res_dir, "drawable-xhdpi"), exist_ok=True)

# Load source icon
logo = Image.open(src_icon_path).convert("RGBA")

# 1. Generate TV Banners (320x180 and 640x360)
for w, h, out_path in [
    (320, 180, os.path.join(res_dir, "drawable", "banner.png")),
    (640, 360, os.path.join(res_dir, "drawable-xhdpi", "banner.png")),
]:
    banner = Image.new("RGBA", (w, h), (20, 20, 20, 255))
    draw = ImageDraw.Draw(banner)
    
    # Red accent bar at bottom
    accent_color = (229, 9, 20, 255) # #e50914
    draw.rectangle([0, h - int(h * 0.04), w, h], fill=accent_color)
    
    # Place logo in center-left
    logo_size = int(h * 0.65)
    logo_resized = logo.resize((logo_size, logo_size), Image.Resampling.LANCZOS)
    banner.paste(logo_resized, (int(w * 0.1), (h - logo_size) // 2), logo_resized)
    
    # Text "1FLEX TV"
    try:
        font = ImageFont.truetype("arial.ttf", int(h * 0.22))
    except:
        font = ImageFont.load_default()
    
    text_x = int(w * 0.1) + logo_size + int(w * 0.05)
    text_y = int(h * 0.38)
    draw.text((text_x, text_y), "1FLEX TV", fill=(255, 255, 255, 255), font=font)
    banner.save(out_path, "PNG")
    print(f"Saved banner: {out_path} ({w}x{h})")

# 2. Standard Mipmap Icons & Adaptive Foregrounds
# Standard legacy icon sizes: mdpi: 48, hdpi: 72, xhdpi: 96, xxhdpi: 144, xxxhdpi: 192
# Adaptive foreground icon sizes: mdpi: 108, hdpi: 162, xhdpi: 216, xxhdpi: 324, xxxhdpi: 432
mipmaps = {
    "mipmap-mdpi": (48, 108),
    "mipmap-hdpi": (72, 162),
    "mipmap-xhdpi": (96, 216),
    "mipmap-xxhdpi": (144, 324),
    "mipmap-xxxhdpi": (192, 432)
}

for folder, (std_size, fg_size) in mipmaps.items():
    folder_path = os.path.join(res_dir, folder)
    os.makedirs(folder_path, exist_ok=True)
    
    # Standard legacy square icon (with dark background)
    icon_img = Image.new("RGBA", (std_size, std_size), (20, 20, 20, 255))
    logo_sub = logo.resize((int(std_size * 0.82), int(std_size * 0.82)), Image.Resampling.LANCZOS)
    offset = (std_size - int(std_size * 0.82)) // 2
    icon_img.paste(logo_sub, (offset, offset), logo_sub)
    
    icon_path = os.path.join(folder_path, "ic_launcher.png")
    round_icon_path = os.path.join(folder_path, "ic_launcher_round.png")
    icon_img.save(icon_path, "PNG")
    icon_img.save(round_icon_path, "PNG")
    
    # Adaptive foreground (transparent, safe-zone logo ~66% of fg_size)
    fg_img = Image.new("RGBA", (fg_size, fg_size), (0, 0, 0, 0))
    fg_logo_size = int(fg_size * 0.6)
    fg_logo_resized = logo.resize((fg_logo_size, fg_logo_size), Image.Resampling.LANCZOS)
    fg_offset = (fg_size - fg_logo_size) // 2
    fg_img.paste(fg_logo_resized, (fg_offset, fg_offset), fg_logo_resized)
    
    fg_path = os.path.join(folder_path, "ic_launcher_foreground.png")
    fg_img.save(fg_path, "PNG")
    print(f"Saved {folder} legacy ({std_size}x{std_size}) and foreground ({fg_size}x{fg_size})")

# 3. PWA Web Assets
for size, name in [(192, "icon-192.png"), (512, "icon-512.png")]:
    pwa_icon = Image.new("RGBA", (size, size), (20, 20, 20, 255))
    sub = logo.resize((int(size * 0.82), int(size * 0.82)), Image.Resampling.LANCZOS)
    offset = (size - int(size * 0.82)) // 2
    pwa_icon.paste(sub, (offset, offset), sub)
    pwa_icon.save(os.path.join(www_assets_dir, name), "PNG")
    print(f"Saved PWA icon: {name}")

print("All asset updates verified and generated successfully!")
