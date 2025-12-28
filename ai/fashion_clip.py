from pathlib import Path
from PIL import Image
import numpy as np
import torch
from transformers import CLIPModel, CLIPProcessor

# ==========================
#  English label dictionaries
# ==========================
ITEM_LABELS = {
    # Upper
    "tshirt": "t-shirt",
    "shirt": "shirt",
    "blouse": "blouse",
    "sweater": "sweater",
    "cardigan": "cardigan",
    "sweatshirt": "sweatshirt",
    "hoodie": "hoodie",
    "polo_shirt": "polo shirt",
    "tank_top": "tank top",
    "bodysuit": "bodysuit",
    "bustier": "bustier",
    "tunic": "tunic",

    # Bottom
    "pants": "pants",
    "jeans": "jeans",
    "dress_pants": "dress pants",
    "cargo_pants": "cargo pants",
    "skirt": "skirt",
    "mini_skirt": "mini skirt",
    "midi_skirt": "midi skirt",
    "maxi_skirt": "maxi skirt",
    "shorts": "shorts",
    "capri_pants": "capri pants",
    "leggings": "leggings",
    "sweatpants": "sweatpants",
    "wide_leg_skirt_pants": "wide-leg skirt pants",

    # Outerwear
    "jacket": "jacket",
    "blazer": "blazer",
    "denim_jacket": "denim jacket",
    "leather_jacket": "leather jacket",
    "bomber_jacket": "bomber jacket",
    "coat": "coat",
    "trench_coat": "trench coat",
    "raincoat": "raincoat",
    "parka": "parka",
    "vest": "vest",

    # One-piece
    "dress": "dress",
    "jumpsuit": "jumpsuit",
    "overalls": "overalls",

    # Shoes
    "sneakers": "sneakers",
    "boots": "boots",
    "sandals": "sandals",
    "heels": "heels",
    "flats": "flats",
    "loafers": "loafers",

    # Accessories
    "hat": "hat",
    "scarf": "scarf",
    "gloves": "gloves",
    "belt": "belt",
    "tie": "tie",
    "handbag": "handbag",
}

COLOR_LABELS = {
    "red": "red",
    "blue": "blue",
    "yellow": "yellow",
    "green": "green",
    "orange": "orange",
    "purple": "purple",
    "black": "black",
    "white": "white",
    "gray": "gray",
    "brown": "brown",
    "beige": "beige",
    "cream": "cream",
    "burgundy": "burgundy",
    "pink": "pink",
    "navy_blue": "navy blue",
    "indigo": "indigo",
    "khaki": "khaki",
    "gold": "gold",
    "silver": "silver",
    "bronze": "bronze",
    "patterned": "patterned",
    "striped": "striped",
}

CATEGORY_MAPPING = {
    # top
    "tshirt": "top",
    "shirt": "top",
    "blouse": "top",
    "sweater": "top",
    "cardigan": "top",
    "sweatshirt": "top",
    "hoodie": "top",
    "polo_shirt": "top",
    "tank_top": "top",
    "bodysuit": "top",
    "bustier": "top",
    "tunic": "top",

    # bottom
    "pants": "bottom",
    "jeans": "bottom",
    "dress_pants": "bottom",
    "cargo_pants": "bottom",
    "skirt": "bottom",
    "mini_skirt": "bottom",
    "midi_skirt": "bottom",
    "maxi_skirt": "bottom",
    "shorts": "bottom",
    "capri_pants": "bottom",
    "leggings": "bottom",
    "sweatpants": "bottom",
    "wide_leg_skirt_pants": "bottom",

    # outerwear
    "jacket": "outerwear",
    "blazer": "outerwear",
    "denim_jacket": "outerwear",
    "leather_jacket": "outerwear",
    "bomber_jacket": "outerwear",
    "coat": "outerwear",
    "trench_coat": "outerwear",
    "raincoat": "outerwear",
    "parka": "outerwear",
    "vest": "outerwear",

    # one piece
    "dress": "one_piece",
    "jumpsuit": "one_piece",
    "overalls": "one_piece",

    # shoes
    "sneakers": "shoes",
    "boots": "shoes",
    "sandals": "shoes",
    "heels": "shoes",
    "flats": "shoes",
    "loafers": "shoes",

    # accessories
    "hat": "accessories",
    "scarf": "accessories",
    "gloves": "accessories",
    "belt": "accessories",
    "tie": "accessories",
    "handbag": "accessories",
}

CATEGORY_SEASONS = {
    "top": ["spring", "summer", "fall"],
    "bottom": ["spring", "summer", "fall"],
    "outerwear": ["fall", "winter"],
    "one_piece": ["spring", "summer"],
    "shoes": ["spring", "summer", "fall", "winter"],
    "accessories": ["spring", "summer", "fall", "winter"],
}

CATEGORY_WEIGHT = {
    "top": 0.4,
    "bottom": 0.5,
    "outerwear": 0.9,
    "one_piece": 0.5,
    "shoes": 0.6,
    "accessories": 0.2,
}

# ==========================
#  Model loading (LAION CLIP)
# ==========================
device = "cuda" if torch.cuda.is_available() else "cpu"
MODEL_NAME = "laion/CLIP-ViT-B-32-laion2B-s34B-b79K"

print(f"[DEBUG] Using base CLIP model: {MODEL_NAME} on {device}")

model = CLIPModel.from_pretrained(MODEL_NAME)
model.to(device)
model.eval()

processor = CLIPProcessor.from_pretrained(MODEL_NAME)

# ==========================
#  Helpers
# ==========================
def _normalize(x: torch.Tensor) -> torch.Tensor:
    x = torch.nan_to_num(x, nan=0.0, posinf=0.0, neginf=0.0)
    norm = x.norm(p=2, dim=-1, keepdim=True)
    eps = 1e-12
    norm = torch.clamp(norm, min=eps)
    return x / norm


def _prepare_text_features():
    item_keys = list(ITEM_LABELS.keys())
    item_texts = [f"a photo of a {ITEM_LABELS[k]} clothing item" for k in item_keys]

    color_keys = list(COLOR_LABELS.keys())
    color_texts = [f"a photo of a {COLOR_LABELS[c]} colored clothing item" for c in color_keys]

    with torch.no_grad():
        item_inputs = processor(text=item_texts, return_tensors="pt", padding=True).to(device)
        item_text_features_raw = model.get_text_features(**item_inputs).float()
        item_text_features = _normalize(item_text_features_raw)

        color_inputs = processor(text=color_texts, return_tensors="pt", padding=True).to(device)
        color_text_features_raw = model.get_text_features(**color_inputs).float()
        color_text_features = _normalize(color_text_features_raw)

    return item_keys, item_text_features, color_keys, color_text_features


ITEM_KEYS, ITEM_TEXT_FEATURES, COLOR_KEYS, COLOR_TEXT_FEATURES = _prepare_text_features()

# ==========================
#  Main function
# ==========================
def extract_fashion_features(image_path: str, emb_dir: str = "embeddings") -> dict:
    img_path = Path(image_path)

    original_path_str = str(img_path.resolve())
    original_filename = img_path.name

    image = Image.open(img_path).convert("RGB")

    inputs = processor(images=image, return_tensors="pt").to(device)
    with torch.no_grad():
        image_features_raw = model.get_image_features(**inputs).float()
    image_features = _normalize(image_features_raw)

    # save embedding
    emb_dir_path = Path(emb_dir)
    emb_dir_path.mkdir(parents=True, exist_ok=True)
    emb_filename = f"{img_path.stem}.npy"
    emb_path = emb_dir_path / emb_filename
    np.save(emb_path, image_features.squeeze(0).cpu().numpy())

    # item type
    item_sims = (image_features @ ITEM_TEXT_FEATURES.T).squeeze(0)
    item_best_idx = int(torch.argmax(item_sims).item())
    item_key = ITEM_KEYS[item_best_idx]
    item_label_en = ITEM_LABELS[item_key]
    item_score = float(item_sims[item_best_idx].item())

    # color
    color_sims = (image_features @ COLOR_TEXT_FEATURES.T).squeeze(0)
    color_best_idx = int(torch.argmax(color_sims).item())
    color_key = COLOR_KEYS[color_best_idx]
    color_label_en = COLOR_LABELS[color_key]
    color_score = float(color_sims[color_best_idx].item())

    category = CATEGORY_MAPPING.get(item_key, "unknown")
    seasons = CATEGORY_SEASONS.get(category, [])
    weight = CATEGORY_WEIGHT.get(category, 0.5)

    tags = {
        "item_type": item_key,
        "item_label": item_label_en,
        "item_score": item_score,
        "color": color_key,
        "color_label": color_label_en,
        "color_score": color_score,
    }

    result = {
        "original_path": original_path_str,
        "original_filename": original_filename,
        "embedding_file": str(emb_path),
        "category": category,
        "color": color_key,
        "item_type": item_key,
        "seasons": seasons,
        "weight": weight,
        "ai_metadata": tags,
    }

    return result

