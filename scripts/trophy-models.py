#!/usr/bin/env python3
"""Export the supplied trophy geometry with vanilla skin UVs; no texture copies."""
from pathlib import Path
import json

MOD = 'fantasy_feudalism_items'
# Texture, sheet dimensions, head box dimensions, texture origin. Piglin and
# dragon skulls use their own box layouts rather than humanoid 8x8 UVs.
HEADS = {
    'zombie': ('zombie/zombie', 64, 64, 8, 8, 8, 0, 0),
    'skeleton': ('skeleton/skeleton', 64, 32, 8, 8, 8, 0, 0),
    'creeper': ('creeper/creeper', 64, 32, 8, 8, 8, 0, 0),
    'wither_skeleton': ('skeleton/wither_skeleton', 64, 32, 8, 8, 8, 0, 0),
    'stray': ('skeleton/stray', 64, 32, 8, 8, 8, 0, 0),
    'bogged': ('skeleton/bogged', 64, 32, 8, 8, 8, 0, 0),
    'piglin': ('piglin/piglin', 64, 64, 10, 8, 8, 0, 0),
    'ender_dragon': ('enderdragon/dragon', 256, 256, 16, 16, 16, 112, 30),
    'player': ('player/wide/steve', 64, 64, 8, 8, 8, 0, 0),
}

def head_uvs(spec):
    _, sheet_w, sheet_h, width, height, depth, u, v = spec
    pixels = {
        'north': [u + depth, v + depth, u + depth + width, v + depth + height],
        'east': [u, v + depth, u + depth, v + depth + height],
        'south': [u + 2 * depth + width, v + depth, u + 2 * depth + 2 * width, v + depth + height],
        'west': [u + depth + width, v + depth, u + 2 * depth + width, v + depth + height],
        'up': [u + depth, v, u + depth + width, v + depth],
        'down': [u + depth + width, v + depth, u + depth + 2 * width, v],
    }
    return {side: [n * 16 / (sheet_w if i % 2 == 0 else sheet_h) for i, n in enumerate(uv)]
            for side, uv in pixels.items()}

def generate(root):
    root = Path(root)
    source = json.loads((root / 'models/Trophy.bbmodel').read_text())
    cubes = [e for e in source['elements'] if e['type'] == 'cube' and e.get('export', True)]
    head = max(cubes, key=lambda e: e['to'][1])
    resources = root / 'src/main/resources'
    def write(path, data):
        file = resources / path
        file.parent.mkdir(parents=True, exist_ok=True)
        file.write_text(json.dumps(data, indent=2) + '\n')
    sources = []
    for kind, spec in HEADS.items():
        sprite = f'{MOD}:trophy_heads/{kind}'
        sources.append({'type': 'minecraft:single', 'resource': f'minecraft:entity/{spec[0]}', 'sprite': sprite})
        elements = []
        for cube in cubes:
            faces = {}
            for side, face in cube['faces'].items():
                if cube is head:
                    faces[side] = {'uv': head_uvs(spec)[side], 'texture': '#head'}
                else:
                    faces[side] = {'uv': face['uv'], 'texture': '#wood_top' if side in ('up', 'down') else '#wood'}
                    if face.get('rotation'):
                        faces[side]['rotation'] = face['rotation']
            elements.append({'from': cube['from'], 'to': cube['to'], 'shade': cube.get('shade', True), 'faces': faces})
        write(f'assets/{MOD}/models/block/{kind}_trophy.json', {
            'parent': 'minecraft:block/block', 'ambientocclusion': source.get('ambientocclusion', True),
            'textures': {'wood': 'minecraft:block/stripped_spruce_log',
                         'wood_top': 'minecraft:block/stripped_spruce_log_top',
                         'head': sprite, 'particle': 'minecraft:block/stripped_spruce_log'},
            'elements': elements,
        })
    # Entity skins are not part of the block atlas by default. Referencing the
    # existing resources also respects resource-pack replacements of those skins.
    write('assets/minecraft/atlases/blocks.json', {'sources': sources})

if __name__ == '__main__':
    generate(Path(__file__).resolve().parents[1])
