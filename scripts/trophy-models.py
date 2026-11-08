#!/usr/bin/env python3
"""Export trophies and standalone heads with vanilla skin layers; no texture copies."""
from pathlib import Path
import json
from copy import deepcopy

MOD = 'fantasy_feudalism_items'
# Texture, sheet dimensions, head box dimensions, texture origin. Piglin uses
# its native wider head plus the full snout, tusks and ears exported below.
HEADS = {
    'zombie': ('zombie/zombie', 64, 64, 8, 8, 8, 0, 0),
    'skeleton': ('skeleton/skeleton', 64, 32, 8, 8, 8, 0, 0),
    'creeper': ('creeper/creeper', 64, 32, 8, 8, 8, 0, 0),
    'wither_skeleton': ('skeleton/wither_skeleton', 64, 32, 8, 8, 8, 0, 0),
    'stray': ('skeleton/stray', 64, 32, 8, 8, 8, 0, 0),
    'bogged': ('skeleton/bogged', 64, 32, 8, 8, 8, 0, 0),
    'piglin': ('piglin/piglin', 64, 64, 10, 8, 8, 0, 0),
    'ender_dragon': ('enderman/enderman', 64, 32, 8, 8, 8, 0, 0),
    'player': ('player/wide/steve', 64, 64, 8, 8, 8, 0, 0),
}

# Match the vanilla clothing/head-layer expansion for these skeleton variants.
HEAD_OVERLAYS = {'bogged': 0.2, 'stray': 0.25}

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

def piglin_part(bounds, dimensions, texture_origin):
    spec = ('piglin/piglin', 64, 64, *dimensions, *texture_origin)
    return {'from': bounds[0], 'to': bounds[1],
            'faces': {side: {'uv': uv, 'texture': '#head'} for side, uv in head_uvs(spec).items()}}

def piglin_model(stand, textures):
    # PiglinModel.addHead / PiglinHeadModel: native dimensions and texture regions.
    # Entity x/y axes are inverted to convert its skull coordinates to block space.
    parts = [
        piglin_part(([3, 6, 4], [13, 14, 12]), (10, 8, 8), (0, 0)),
        piglin_part(([6, 6, 3], [10, 10, 4]), (4, 4, 1), (31, 1)),
        piglin_part(([5, 6, 3], [6, 8, 4]), (1, 2, 1), (2, 4)),
        piglin_part(([10, 6, 3], [11, 8, 4]), (1, 2, 1), (2, 0)),
    ]
    children = {'stand_and_head': {'textures': textures, 'render_type': 'minecraft:cutout', 'elements': stand + parts}}
    for name, bounds, uv, pivot, angle in [
        ('left_ear', ([2.5, 7, 6], [3.5, 12, 10]), (51, 6), [3.5, 12, 8], -30),
        ('right_ear', ([12.5, 7, 6], [13.5, 12, 10]), (39, 6), [12.5, 12, 8], 30),
    ]:
        # NeoForge root transforms allow the real 30-degree ear angles, unlike
        # vanilla element rotations which only allow multiples of 22.5 degrees.
        children[name] = {'textures': textures, 'render_type': 'minecraft:cutout',
                          'transform': {'origin': [n / 16 for n in pivot], 'rotation': {'z': angle}},
                          'elements': [piglin_part(bounds, (1, 5, 4), uv)]}
    return {'parent': 'minecraft:block/block', 'loader': 'neoforge:composite',
            'render_type': 'minecraft:cutout', 'textures': textures, 'children': children}

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
    def overlay_cube(cube, amount):
        overlay = deepcopy(cube)
        overlay['from'] = [n - amount for n in cube['from']]
        overlay['to'] = [n + amount for n in cube['to']]
        for face in overlay['faces'].values():
            face['texture'] = '#head_overlay'
        return overlay
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
        textures = {'wood': 'minecraft:block/stripped_spruce_log',
                    'wood_top': 'minecraft:block/stripped_spruce_log_top',
                    'head': sprite, 'particle': 'minecraft:block/stripped_spruce_log'}
        if kind in HEAD_OVERLAYS:
            overlay_sprite = f'{MOD}:trophy_heads/{kind}_overlay'
            sources.append({'type': 'minecraft:single', 'resource': f'minecraft:entity/{spec[0]}_overlay', 'sprite': overlay_sprite})
            textures['head_overlay'] = overlay_sprite
            head_element = elements[cubes.index(head)]
            elements.append(overlay_cube(head_element, HEAD_OVERLAYS[kind]))
            # The standalone drop uses exactly the same base skin and outer layer.
            item_head = deepcopy(head_element)
            item_head['from'] = [4, 4, 4]
            item_head['to'] = [12, 12, 12]
            write(f'assets/{MOD}/models/item/{kind}_head.json', {
                'parent': 'minecraft:block/block', 'render_type': 'minecraft:cutout',
                'textures': {'head': sprite, 'head_overlay': overlay_sprite, 'particle': sprite},
                'display': {
                    'gui': {'rotation': [30, 45, 0], 'translation': [0, 0, 0], 'scale': [1, 1, 1]},
                    'ground': {'rotation': [0, 0, 0], 'translation': [0, 3, 0], 'scale': [0.5, 0.5, 0.5]},
                    'thirdperson_righthand': {'rotation': [45, 45, 0], 'translation': [0, 3, 0], 'scale': [0.5, 0.5, 0.5]},
                },
                'elements': [item_head, overlay_cube(item_head, HEAD_OVERLAYS[kind])],
            })
        if kind == 'ender_dragon':
            eye_sprite = f'{MOD}:trophy_heads/ender_dragon_eyes'
            sources.append({'type': 'minecraft:single', 'resource': 'minecraft:entity/enderman/enderman_eyes', 'sprite': eye_sprite})
            textures['head_overlay'] = eye_sprite
            eyes = overlay_cube(elements[cubes.index(head)], 0.01)
            eyes['shade'] = False
            eyes['neoforge_data'] = {'block_light': 15, 'sky_light': 15, 'ambient_occlusion': False}
            elements.append(eyes)
        result = {
            'parent': 'minecraft:block/block', 'ambientocclusion': source.get('ambientocclusion', True),
            **({'render_type': 'minecraft:cutout'} if kind in HEAD_OVERLAYS or kind == 'ender_dragon' else {}),
            'textures': textures, 'elements': elements,
        }
        if kind == 'piglin':
            result = piglin_model([elements[i] for i, cube in enumerate(cubes) if cube is not head], textures)
        write(f'assets/{MOD}/models/block/{kind}_trophy.json', result)
        write(f'assets/{MOD}/blockstates/{kind}_trophy.json', {
            'variants': {f'facing={direction}': {'model': f'{MOD}:block/{kind}_trophy', **({'y': angle} if angle else {})}
                         for direction, angle in [('north', 0), ('east', 90), ('south', 180), ('west', 270)]}
        })
    # Entity skins are not part of the block atlas by default. Referencing the
    # existing resources also respects resource-pack replacements of those skins.
    write('assets/minecraft/atlases/blocks.json', {'sources': sources})

if __name__ == '__main__':
    generate(Path(__file__).resolve().parents[1])
