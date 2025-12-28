from fastapi import FastAPI, UploadFile, File, HTTPException
from pathlib import Path
import tempfile
import os

# import your function + model init file
# e.g. put your current code into fashion_clip.py and expose extract_fashion_features
from fashion_clip import extract_fashion_features

app = FastAPI()

@app.post("/extract")
async def extract(file: UploadFile = File(...)):
    # basic validation
    if file.content_type not in {"image/jpeg", "image/png", "image/webp"}:
        raise HTTPException(status_code=400, detail="Unsupported image type")

    # save to a temp file, run extraction, return JSON
    suffix = Path(file.filename).suffix or ".jpg"
    with tempfile.NamedTemporaryFile(delete=False, suffix=suffix) as tmp:
        tmp_path = tmp.name
        content = await file.read()
        if len(content) > 10 * 1024 * 1024:
            raise HTTPException(status_code=400, detail="File too large (max 10MB)")
        tmp.write(content)

    try:
        result = extract_fashion_features(tmp_path, emb_dir="embeddings")
        # If you DON'T want to expose local paths to Java, remove these fields:
        result.pop("original_path", None)
        result.pop("embedding_file", None)
        return result
    finally:
        try:
            os.remove(tmp_path)
        except OSError:
            pass

