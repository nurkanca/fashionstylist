# compat_infer.py
from __future__ import annotations
from pathlib import Path
from typing import List, Tuple

import torch
import torch.nn as nn
import torchvision.transforms as T
import torchvision.models as models


class ImageCompatModel(nn.Module):
    def __init__(self, emb_dim=256, hidden=256, dropout=0.2, freeze_backbone: bool = False):
        super().__init__()
        backbone = models.resnet18(weights=models.ResNet18_Weights.IMAGENET1K_V1)
        backbone.fc = nn.Identity()  # outputs 512
        self.backbone = backbone

        self.proj = nn.Sequential(
            nn.Linear(512, emb_dim),
            nn.ReLU(),
        )

        self.head = nn.Sequential(
            nn.Linear(emb_dim, hidden),
            nn.ReLU(),
            nn.Dropout(dropout),
            nn.Linear(hidden, 1),
        )

        # inference: keep backbone trainable flag doesn't matter much, but set requires_grad True/False cleanly
        for p in self.backbone.parameters():
            p.requires_grad = not freeze_backbone

    def forward(self, imgs: torch.Tensor, mask: torch.Tensor) -> torch.Tensor:
        """
        imgs: [B,L,3,H,W], mask: [B,L]
        """
        B, L, C, H, W = imgs.shape
        x = imgs.view(B * L, C, H, W)
        feat = self.backbone(x)          # [B*L,512]
        feat = self.proj(feat)           # [B*L,D]
        feat = feat.view(B, L, -1)       # [B,L,D]

        m = mask.unsqueeze(-1)           # [B,L,1]
        feat = feat * m
        denom = m.sum(dim=1).clamp(min=1.0)
        pooled = feat.sum(dim=1) / denom # [B,D]
        logit = self.head(pooled).squeeze(-1)
        return logit


class CompatScorer:
    def __init__(
        self,
        ckpt_path: str | Path,
        max_len: int = 12,
        image_size: int = 224,
        device: str | None = None,
    ):
        self.ckpt_path = Path(ckpt_path)
        self.max_len = max_len
        self.image_size = image_size

        if device is None:
            self.device = (
                "mps" if torch.backends.mps.is_available()
                else ("cuda" if torch.cuda.is_available() else "cpu")
            )
        else:
            self.device = device

        state = torch.load(self.ckpt_path, map_location=self.device)
        args = state.get("args", {})

        self.model = ImageCompatModel(
            emb_dim=int(args.get("emb_dim", 256)),
            hidden=int(args.get("hidden", 256)),
            dropout=float(args.get("dropout", 0.2)),
            freeze_backbone=False,
        ).to(self.device)
        self.model.load_state_dict(state["model"])
        self.model.eval()

        self.tf = T.Compose([
            T.Resize((self.image_size, self.image_size)),
            T.ToTensor(),
            T.Normalize(mean=[0.485, 0.456, 0.406], std=[0.229, 0.224, 0.225]),
        ])

    @torch.no_grad()
    def score_preprocessed(self, item_tensors: List[torch.Tensor]) -> dict:
        """
        item_tensors: list of tensors [3,H,W] already normalized.
        """
        # limit & pad
        items = item_tensors[: self.max_len]
        found = len(items)

        imgs = list(items)
        mask = [1.0] * len(items)

        while len(imgs) < self.max_len:
            imgs.append(torch.zeros(3, self.image_size, self.image_size))
            mask.append(0.0)

        imgs_t = torch.stack(imgs, dim=0).unsqueeze(0).to(self.device)      # [1,L,3,H,W]
        mask_t = torch.tensor(mask, dtype=torch.float32).unsqueeze(0).to(self.device)  # [1,L]

        logit = float(self.model(imgs_t, mask_t).squeeze(0).item())
        prob = float(torch.sigmoid(torch.tensor(logit)).item())

        return {
            "probability": prob,
            "logit": logit,
            "images_found": found,
            "items_requested": len(item_tensors),
        }
