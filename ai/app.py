from fastapi import FastAPI, UploadFile, File, HTTPException
from pathlib import Path
import tempfile
import os
from typing import List

from PIL import Image

from fashion_clip import extract_fashion_features
from compat_infer import CompatScorer

app = FastAPI()

# ---- load compat scorer once (adjust checkpoint path!)
COMPAT_CKPT = "best_disjoint_image.pt"   # or "best_nondisjoint_image.pt"
compat = CompatScorer(COMPAT_CKPT, max_len=12, image_size=224)

ALLOWED_TYPES = {"image/jpeg", "image/png", "image/webp"}
MAX_BYTES = 10 * 1024 * 1024


@app.post("/extract")
async def extract(file: UploadFile = File(...)):
    if file.content_type not in ALLOWED_TYPES:
        raise HTTPException(status_code=400, detail="Unsupported image type")

    suffix = Path(file.filename).suffix or ".jpg"
    with tempfile.NamedTemporaryFile(delete=False, suffix=suffix) as tmp:
        tmp_path = tmp.name
        content = await file.read()
        if len(content) > MAX_BYTES:
            raise HTTPException(status_code=400, detail="File too large (max 10MB)")
        tmp.write(content)

    try:
        result = extract_fashion_features(tmp_path, emb_dir="embeddings")
        result.pop("original_path", None)
        result.pop("embedding_file", None)
        return result
    finally:
        try:
            os.remove(tmp_path)
        except OSError:
            pass


@app.post("/compat/score-images")
async def score_images(files: List[UploadFile] = File(...)):
    """
    Multipart: files=<img1>, files=<img2>, ... (2..12 recommended)
    """
    if files is None or len(files) < 2:
        raise HTTPException(status_code=400, detail="Provide at least 2 images.")
    if len(files) > 12:
        raise HTTPException(status_code=400, detail="Max 12 images allowed.")

    tensors = []
    for f in files:
        if f.content_type not in ALLOWED_TYPES:
            raise HTTPException(status_code=400, detail=f"Unsupported image type: {f.content_type}")

        content = await f.read()
        if len(content) > MAX_BYTES:
            raise HTTPException(status_code=400, detail=f"File too large: {f.filename}")

        try:
            img = Image.open(Path(f.filename))  # not used
        except Exception:
            pass

        # open from bytes
        try:
            from io import BytesIO
            pil = Image.open(BytesIO(content)).convert("RGB")
        except Exception:
            raise HTTPException(status_code=400, detail=f"Could not decode image: {f.filename}")

        tensors.append(compat.tf(pil))

    # score
    result = compat.score_preprocessed(tensors)
    return result
