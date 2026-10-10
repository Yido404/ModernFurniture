#!/usr/bin/env python3
"""Generates every JSON resource (models, blockstates, items, loot, recipes, tags, lang).
Run from the project root:  python3 tools/generate.py"""
import json, os, shutil
MOD = "modernfurniture"
HERE = os.path.dirname(os.path.abspath(__file__))
RES = os.path.join(HERE, "..", "src", "main", "resources")
A = f"{RES}/assets/{MOD}"; D = f"{RES}/data"

COLORS = [("white","白色","White"),("orange","橙色","Orange"),("magenta","品红色","Magenta"),
 ("light_blue","淡蓝色","Light Blue"),("yellow","黄色","Yellow"),("lime","黄绿色","Lime"),
 ("pink","粉红色","Pink"),("gray","灰色","Gray"),("light_gray","淡灰色","Light Gray"),
 ("cyan","青色","Cyan"),("purple","紫色","Purple"),("blue","蓝色","Blue"),("brown","棕色","Brown"),
 ("green","绿色","Green"),("red","红色","Red"),("black","黑色","Black")]
CN = {a: b for a, b, _ in COLORS}; EN = {a: c for a, _, c in COLORS}

def w(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(obj, f, ensure_ascii=False, separators=(",", ":"))

# ------------------------------------------------------------ model helpers
FACES = ["north","east","south","west","up","down"]
def B(f, t, tex, em=0, rot=None):
    faces = {}
    for d in FACES:
        fc = {"texture": "#" + tex}
        if em: fc["light_emission"] = em
        faces[d] = fc
    e = {"from": list(f), "to": list(t), "faces": faces}
    if rot: e["rotation"] = rot
    return e

FIXED = {"w":"minecraft:block/oak_planks","m":"minecraft:block/iron_block","d":"minecraft:block/black_concrete",
         "wh":"minecraft:block/white_wool","t":"minecraft:block/smooth_stone","aq":"minecraft:block/light_blue_concrete",
         "gs":"minecraft:block/glowstone","gsoff":"minecraft:block/white_concrete","g":"minecraft:block/gold_block",
         "particle":"#c"}
def display(k):
    return {"gui":{"rotation":[30,225,0],"translation":[0,-1,0],"scale":[0.55*k]*3},
     "ground":{"translation":[0,3,0],"scale":[0.35*k]*3},"fixed":{"scale":[0.5*k]*3},
     "thirdperson_righthand":{"rotation":[75,45,0],"translation":[0,2.5,0],"scale":[0.35*k]*3},
     "firstperson_righthand":{"rotation":[0,45,0],"scale":[0.4*k]*3}}

def template(name, elements, k=1.0, textures=None):
    w(f"{A}/models/block/tpl/{name}.json",
      {"textures": textures or FIXED, "elements": elements, "display": display(k), "gui_light": "side"})

def clip(elements, dims, cell):
    """Cut big-space elements (unit = 1/16 block; x right, y up, z back) into the slice for one cell."""
    lo = [cell[i]*16 if cell[i] > 0 else -1e9 for i in range(3)]
    hi = [(cell[i]+1)*16 if cell[i] < dims[i]-1 else 1e9 for i in range(3)]
    out = []
    for e in elements:
        nf = [max(e["from"][i], lo[i]) for i in range(3)]
        nt = [min(e["to"][i], hi[i]) for i in range(3)]
        if any(nf[i] >= nt[i] for i in range(3)): continue
        sh = [cell[i]*16 for i in range(3)]
        n = dict(e); n["from"] = [nf[i]-sh[i] for i in range(3)]; n["to"] = [nt[i]-sh[i] for i in range(3)]
        out.append(n)
    assert out, f"empty cell {cell}"
    return out

def mirror_x(elements):
    """Reflect one 16-unit cell while keeping its block-entity cell index unchanged."""
    out = []
    for e in elements:
        n = dict(e)
        n["from"] = [16 - e["to"][0], *e["from"][1:]]
        n["to"] = [16 - e["from"][0], *e["to"][1:]]
        n["faces"] = {({"east":"west", "west":"east"}.get(face, face)): data
                      for face, data in e["faces"].items()}
        out.append(n)
    return out

def compact_item(elements, dims):
    """Fit a multi-block model inside the model parser's coordinate bounds."""
    scale = 1 / max(dims)
    offset = [(16 - size * 16 * scale) / 2 for size in dims]
    out = []
    for e in elements:
        n = dict(e)
        for side in ("from", "to"):
            n[side] = [e[side][i] * scale + offset[i] for i in range(3)]
        out.append(n)
    return out

def legs(xs, zs, h, size=2, tex="w"):
    return [B([x,0,z],[x+size,h,z+size],tex) for x in xs for z in zs]

# ------------------------------------------------------------ elements (facing NORTH)
def sofa(shape):
    la = shape in ("single","left"); ra = shape in ("single","right")
    e = legs((0,14),(0,14),3)
    e.append(B([0,3,0],[16,8,16],"c"))
    e.append(B([3 if la else 0,8,0],[13 if ra else 16,10,12],"c"))
    e.append(B([0,8,12],[16,16,16],"c"))
    if la: e.append(B([0,8,0],[3,12,12],"c"))
    if ra: e.append(B([13,8,0],[16,12,12],"c"))
    return e

def corner_sofa(mirror):
    e = legs((0,14),(0,14),3)
    e.append(B([0,3,0],[16,8,16],"c"))
    e.append(B([0,8,12],[16,16,16],"c"))                       # back
    if not mirror:
        e += [B([4,8,0],[16,10,12],"c"), B([0,8,0],[4,16,12],"c")]   # cushion + left-side back
    else:
        e += [B([0,8,0],[12,10,12],"c"), B([12,8,0],[16,16,12],"c")] # cushion + right-side back
    return e

def armchair():
    e = legs((1,13),(1,13),4)
    e += [B([0,4,0],[16,8,16],"c"),B([2,8,0],[14,10,12],"c"),B([0,8,0],[2,13,12],"c"),B([14,8,0],[16,13,12],"c"),
          B([0,8,12],[16,16,16],"c"),B([0,13,0],[2,14,12],"w"),B([14,13,0],[16,14,12],"w")]
    return e

def recliner():
    e = legs((1,13),(1,13),3,tex="m")
    e += [B([0,3,4],[16,8,16],"c"),B([2,3,0],[14,7,4],"c"),B([2,8,4],[14,10,12],"c"),
          B([0,8,4],[2,12,14],"c"),B([14,8,4],[16,12,14],"c"),
          B([1,8,12],[15,16,15],"c",rot={"angle":22.5,"axis":"x","origin":[8,8,12]}),B([4,3,1],[12,4,2],"m")]
    return e

def dining_chair():
    e = legs((2,12),(2,12),8)
    e += [B([1,8,1],[15,10,15],"c"),B([1,10,13],[3,16,15],"w"),B([13,10,13],[15,16,15],"w"),B([3,11,13],[13,15,15],"c")]
    return e

def stool():
    return legs((3,11),(3,11),9) + [B([3,9,3],[13,11,13],"c"),B([3,3,3.5],[13,4,4.5],"m")]

def table_top(): return [B([0,13,0],[16,16,16],"c")]
def table_full(): return table_top() + legs((1,13),(1,13),13)
def coffee_table(): return [B([0,7,0],[16,9,16],"c")] + legs((1,13),(1,13),7) + [B([3,2,3],[13,3,13],"w")]
def side_table():
    return [B([4,0,4],[12,1,12],"w"),B([7,1,7],[9,10,9],"w"),B([3,10,3],[13,12,13],"c")]
def bar_table():
    return [B([3,0,3],[13,1,13],"m"),B([7,1,7],[9,15,9],"m"),B([0,15,0],[16,18,16],"c")]
def desk():
    return [B([0,13,0],[16,16,16],"c"),B([1,0,1],[3,13,3],"w"),B([1,0,13],[3,13,15],"w"),B([9,0,1],[16,13,15],"w"),
            B([9.5,1.5,0.5],[15.5,6,1],"c"),B([9.5,7,0.5],[15.5,12,1],"c"),
            B([11.5,3.5,0],[13.5,4.5,0.5],"m"),B([11.5,9,0],[13.5,10,0.5],"m")]

def counter():
    return [B([1,0,1],[15,2,15],"d"),B([0,2,0],[16,14,16],"c"),B([0,14,0],[16,16,16],"t"),
            B([7.75,3,-0.25],[8.25,13,0],"d"),B([6,7,-0.75],[7,11,-0.25],"m"),B([9,7,-0.75],[10,11,-0.25],"m")]
def cabinet():
    return [B([0,0,0],[16,16,16],"w"),B([1,1,-0.5],[7.75,15,0],"c"),B([8.25,1,-0.5],[15,15,0],"c"),
            B([6,6,-1],[7,10,-0.5],"m"),B([9,6,-1],[10,10,-0.5],"m")]
def wardrobe():
    return [B([0,0,0],[16,16,16],"w"),B([1,6,-0.5],[7.75,15,0],"c"),B([8.25,6,-0.5],[15,15,0],"c"),
            B([1,1,-0.5],[15,5,0],"c"),B([6,9,-1],[7,13,-0.5],"m"),B([9,9,-1],[10,13,-0.5],"m"),B([6.5,2.5,-1],[9.5,3.5,-0.5],"m")]
def fridge():
    return [B([0,0,0],[16,16,16],"c"),B([0,10,-0.25],[16,10.75,0],"d"),
            B([13,11,-1],[14,15,-0.25],"m"),B([13,3,-1],[14,8,-0.25],"m")]
def big_fridge():   # 32 wide x 32 tall x 16 deep, French doors on top + freezer drawer below
    return [B([0,0,0],[32,32,16],"c"),B([15.75,12,-0.25],[16.25,32,0],"d"),B([0,11.75,-0.25],[32,12.25,0],"d"),
            B([13,20,-1],[14,30,-0.25],"m"),B([18,20,-1],[19,30,-0.25],"m"),B([12,6,-1],[20,7,-0.25],"m"),
            B([5,24,-0.5],[9,28,-0.25],"d")]
def big_wardrobe(): # 32x32x16, double doors
    return [B([0,0,0],[32,32,16],"w"),B([1,3,-0.5],[15.5,31,0],"c"),B([16.5,3,-0.5],[31,31,0],"c"),
            B([1,0,-0.5],[31,2.5,0],"c"),B([14,13,-1],[15,19,-0.5],"m"),B([17,13,-1],[18,19,-0.5],"m"),
            B([0,31,-0.25],[32,32,0],"m")]

def stove():
    e = [B([0,0,0],[16,14,16],"c"),B([0,14,0],[16,15,16],"d")]
    for x in (2,9):
        for z in (2,9): e.append(B([x,15,z],[x+5,16,z+5],"f"))
    e += [B([2,3,-0.5],[14,10,0],"d"),B([3,10.5,-1],[13,11.5,-0.5],"m")]
    for x in (2,6,10,13): e.append(B([x,12,-0.75],[x+1.5,13.5,-0.25],"m"))
    return e
def oven():
    return [B([0,0,0],[16,16,16],"c"),B([0,12,-0.25],[16,16,0],"d"),B([1,1,-0.5],[15,11,0],"m"),
            B([2,2,-0.75],[14,10,-0.5],"f"),B([3,10.5,-1.25],[13,11.5,-0.75],"m")] + \
           [B([x,13,-0.75],[x+2,15,-0.25],"m") for x in (2,7,12)]
def sink():
    return [B([0,0,0],[16,13,16],"w"),B([1,1,-0.5],[7.75,12,0],"c"),B([8.25,1,-0.5],[15,12,0],"c"),
         B([0,13,0],[16,16,2],"m"),B([0,13,14],[16,16,16],"m"),B([0,13,2],[2,16,14],"m"),B([14,13,2],[16,16,14],"m"),
         B([2,13,2],[14,14,14],"aq"),B([7,16,13],[9,20,15],"m"),B([7,19,9],[9,20,15],"m")]

def floor_lamp(on):
    return [B([4,0,4],[12,1,12],"m"),B([7.5,1,7.5],[8.5,11,8.5],"m"),B([4,11,4],[12,16,12],"c",em=15 if on else 0)]
def table_lamp(on):
    return [B([5,0,5],[11,1,11],"m"),B([7.5,1,7.5],[8.5,6,8.5],"m"),B([4,6,4],[12,11,12],"c",em=15 if on else 0)]
def ceiling_lamp(on):
    return [B([7.75,9,7.75],[8.25,16,8.25],"m"),B([6,9,6],[10,10,10],"m"),B([3,4,3],[13,9,13],"c",em=15 if on else 0),
            B([6,3,6],[10,4,10],"gs" if on else "gsoff",em=15 if on else 0)]

# ---- crystal chandeliers (crystals use the dye colour; frame is gold)
def _bulb(on): return "gs" if on else "gsoff"
def chandelier_small(on):
    ce = 10 if on else 0
    e = [B([7.75,11,7.75],[8.25,16,8.25],"g"),B([6,14,6],[10,16,10],"g"),B([3,9,3],[13,10.5,13],"g"),
         B([7,10.5,7],[9,13,9],_bulb(on),em=15 if on else 0),
         B([6,3,6],[10,9,10],"c",em=ce),B([7,1,7],[9,3,9],"c",em=ce)]
    for x in (3.5,10.5):
        for z in (3.5,10.5): e.append(B([x,5,z],[x+2,9,z+2],"c",em=ce))
    return e
def chandelier_medium(on):      # 16 x 32 x 16
    ce = 10 if on else 0
    e = [B([7.5,16,7.5],[8.5,32,8.5],"g"),B([5,30,5],[11,32,11],"g"),
         B([1,16,1],[15,18,3],"g"),B([1,16,13],[15,18,15],"g"),B([1,16,3],[3,18,13],"g"),B([13,16,3],[15,18,13],"g"),
         B([5,4,5],[11,16,11],"c",em=ce),B([7,0,7],[9,4,9],"c",em=ce)]
    for x in (1.5,12.5):
        for z in (1.5,12.5):
            e.append(B([x,18,z],[x+2,22,z+2],_bulb(on),em=15 if on else 0))
            e.append(B([x,6,z],[x+2,16,z+2],"c",em=ce))
    return e
def chandelier_large(on):       # 48 x 32 x 48
    ce = 10 if on else 0
    e = [B([23.25,16,23.25],[24.75,32,24.75],"g"),B([20,30,20],[28,32,28],"g"),
         B([4,16,4],[44,18,6],"g"),B([4,16,42],[44,18,44],"g"),B([4,16,6],[6,18,42],"g"),B([42,16,6],[44,18,42],"g"),
         B([6,16,23.25],[42,17.5,24.75],"g"),B([23.25,16,6],[24.75,17.5,42],"g"),
         B([20,4,20],[28,16,28],"c",em=ce),B([22,0,22],[26,4,26],"c",em=ce)]
    for (x,z) in [(5,5),(41,5),(5,41),(41,41),(23,5),(23,41),(5,23),(41,23)]:
        e.append(B([x-1.5,18,z-1.5],[x+1.5,22,z+1.5],_bulb(on),em=15 if on else 0))
        e.append(B([x-1,6,z-1],[x+1,16,z+1],"c",em=ce))
        e.append(B([x-0.5,3,z-0.5],[x+0.5,6,z+0.5],"c",em=ce))
    return e

# ---- beds
def bed(kind, part, shape="single", upper=False):
    head = part == "head"
    la = shape in ("single","left"); ra = shape in ("single","right")
    e = []
    z0 = 0 if head else 13
    for x in (0,13):
        if kind == "double" and not ((x == 0 and la) or (x == 13 and ra)): continue
        e.append(B([x,0,z0],[x+3,3,z0+3],"w"))
    e.append(B([0,3,0],[16,6,16],"w"))
    ms, me = (2,16) if head else (0,14)
    e.append(B([0,6,ms],[16,9,me],"c"))
    if head:
        e.append(B([0,3,0],[16,14 if kind in ("canopy","double") else 13,2],"w"))
        if kind == "double" and not (la and ra): e.append(B([3.5,9,3],[12.5,11,9],"wh"))
        else: e += [B([1,9,3],[7.5,11,9],"wh"),B([8.5,9,3],[15,11,9],"wh")]
    else:
        e.append(B([0,3,14],[16,10,16],"w"))
    pz = 0 if head else 14
    if kind == "canopy":
        e += [B([x,6,pz],[x+2,15,pz+2],"w") for x in (0,14)]
        e.append(B([0,15,0],[16,16,16],"c"))
    if kind == "bunk":
        if not upper:      # lower bunk: corner posts up to the upper bunk, ladder at the foot
            e += [B([x,3,pz],[x+2,16,pz+2],"w") for x in (0,14)]
            if not head:
                e += [B([3,3,16],[4,16,17],"w"),B([12,3,16],[13,16,17],"w")]
                e += [B([4,y,16],[12,y+1,17],"w") for y in (5,9,13)]
        else:              # upper bunk: side guard rails
            e += [B([x,3,pz],[x+2,12,pz+2],"w") for x in (0,14)]
            e += [B([0,9,0],[1,12,16],"w"),B([15,9,0],[16,12,16],"w")]
    return e

# ------------------------------------------------------------ family table
# kind: sofa corner facing plain ctable lamp cooker bed mstorage chand
FAM = {
 "sofa":         dict(kind="sofa",  mat="wool",     zh="沙发",       en="Sofa",           tool="axe"),
 "corner_sofa":  dict(kind="corner",mat="wool",     zh="转角沙发",   en="Corner Sofa",    tool="axe"),
 "armchair":     dict(kind="facing",mat="wool",     zh="扶手椅",     en="Armchair",       tool="axe", fn=armchair),
 "recliner":     dict(kind="facing",mat="wool",     zh="躺椅",       en="Recliner",       tool="axe", fn=recliner),
 "dining_chair": dict(kind="facing",mat="concrete", zh="餐椅",       en="Dining Chair",   tool="axe", fn=dining_chair),
 "stool":        dict(kind="facing",mat="wool",     zh="圆凳",       en="Stool",          tool="axe", fn=stool),
 "table":        dict(kind="ctable",mat="concrete", zh="餐桌(可拼接)",en="Dining Table",    tool="axe"),
 "coffee_table": dict(kind="plain", mat="concrete", zh="茶几",       en="Coffee Table",   tool="axe", fn=coffee_table),
 "side_table":   dict(kind="plain", mat="concrete", zh="边桌",       en="Side Table",     tool="axe", fn=side_table),
 "bar_table":    dict(kind="plain", mat="concrete", zh="吧台桌",     en="Bar Table",      tool="axe", fn=bar_table),
 "desk":         dict(kind="facing",mat="concrete", zh="书桌",       en="Desk",           tool="axe", fn=desk),
 "kitchen_counter":dict(kind="facing",mat="concrete",zh="厨房操作台",en="Kitchen Counter",tool="axe", fn=counter),
 "cabinet":      dict(kind="facing",mat="concrete", zh="储物柜",     en="Cabinet",        tool="axe", fn=cabinet),
 "wardrobe":     dict(kind="facing",mat="concrete", zh="衣柜",       en="Wardrobe",       tool="axe", fn=wardrobe),
 "big_wardrobe": dict(kind="mstorage",mat="concrete",zh="双开门大衣柜",en="Large Wardrobe",tool="axe", fn=big_wardrobe, dims=(2,2,1), pivot=(0,0,0)),
 "fridge":       dict(kind="facing",mat="concrete", zh="冰箱",       en="Refrigerator",   tool="pickaxe", fn=fridge),
 "big_fridge":   dict(kind="mstorage",mat="concrete",zh="双开门大冰箱",en="Double-Door Fridge",tool="pickaxe", fn=big_fridge, dims=(2,2,1), pivot=(0,0,0)),
 "stove":        dict(kind="cooker",mat="concrete", zh="燃气灶",     en="Stove",          tool="pickaxe", fn=stove, off="minecraft:block/gray_concrete", on="minecraft:block/magma"),
 "oven":         dict(kind="cooker",mat="concrete", zh="烤箱",       en="Oven",           tool="pickaxe", fn=oven,  off="minecraft:block/black_concrete", on="minecraft:block/shroomlight"),
 "sink":         dict(kind="facing",mat="concrete", zh="水槽",       en="Kitchen Sink",   tool="pickaxe", fn=sink),
 "simple_bed":   dict(kind="bed",   mat="wool",     zh="单人床",     en="Simple Bed",     tool="axe", bed="simple"),
 "canopy_bed":   dict(kind="bed",   mat="wool",     zh="天蓬床",     en="Canopy Bed",     tool="axe", bed="canopy"),
 "double_bed":   dict(kind="bed",   mat="wool",     zh="双人床(可拼接)",en="Double Bed",   tool="axe", bed="double"),
 "bunk_bed":     dict(kind="bed",   mat="wool",     zh="上下铺(上下均可睡)",en="Bunk Bed", tool="axe", bed="bunk"),
 "floor_lamp":   dict(kind="lamp",  mat="wool",     zh="落地灯",     en="Floor Lamp",     tool="axe", fn=floor_lamp),
 "table_lamp":   dict(kind="lamp",  mat="wool",     zh="台灯",       en="Table Lamp",     tool="axe", fn=table_lamp),
 "ceiling_lamp": dict(kind="lamp",  mat="wool",     zh="吊灯",       en="Ceiling Lamp",   tool="axe", fn=ceiling_lamp),
 "crystal_chandelier_small":  dict(kind="lamp", mat="concrete", zh="小水晶灯(1格)",   en="Small Crystal Chandelier",  tool="pickaxe", fn=chandelier_small),
 "crystal_chandelier_medium": dict(kind="chand",mat="concrete", zh="中水晶灯(1x2格)", en="Medium Crystal Chandelier", tool="pickaxe", fn=chandelier_medium, dims=(1,2,1), pivot=(0,1,0)),
 "crystal_chandelier_large":  dict(kind="chand",mat="concrete", zh="大水晶灯(3x3x2格)",en="Large Crystal Chandelier",  tool="pickaxe", fn=chandelier_large,  dims=(3,2,3), pivot=(1,1,1)),
}

R = lambda p, k, n=1: dict(pattern=p, key=k, count=n)
WW, OP, ST, IR, TO = "minecraft:white_wool","minecraft:oak_planks","minecraft:stick","minecraft:iron_ingot","minecraft:torch"
AM, NU, GD = "minecraft:amethyst_shard","minecraft:iron_nugget","minecraft:glowstone_dust"
RECIPES = {
 "sofa":         R(["WWW","WWW","P P"], {"W":WW,"P":OP}),
 "corner_sofa":  R(["WW ","WWW","PPP"], {"W":WW,"P":OP}),
 "armchair":     R(["W  ","WWW","P P"], {"W":WW,"P":OP}),
 "recliner":     R(["WW ","WWW","IPI"], {"W":WW,"P":OP,"I":IR}),
 "dining_chair": R(["P  ","PPP","S S"], {"P":OP,"S":ST}),
 "stool":        R(["W","P","S"],       {"W":WW,"P":OP,"S":ST}),
 "table":        R(["PPP","S S","S S"], {"P":OP,"S":ST}),
 "coffee_table": R(["PPP","S S"],       {"P":OP,"S":ST}),
 "side_table":   R(["P","S","P"],       {"P":OP,"S":ST}),
 "bar_table":    R(["PPP"," S "," S "], {"P":OP,"S":ST}),
 "desk":         R(["PPP","PCS","S S"], {"P":OP,"S":ST,"C":"minecraft:chest"}),
 "kitchen_counter":R(["PPP","PTP","PPP"],{"P":OP,"T":"minecraft:crafting_table"}),
 "cabinet":      R(["PPP","PCP","PPP"], {"P":OP,"C":"minecraft:chest"}),
 "wardrobe":     R(["PPP","PDP","PDP"], {"P":OP,"D":"minecraft:oak_door"}),
 "big_wardrobe": R(["PPP","WCW","PPP"], {"P":OP,"W":"#modernfurniture:wardrobe","C":"minecraft:chest"}),
 "fridge":       R(["III","IPI","ICI"], {"I":IR,"P":"minecraft:packed_ice","C":"minecraft:chest"}),
 "big_fridge":   R(["III","FCF","III"], {"I":IR,"F":"#modernfurniture:fridge","C":"minecraft:chest"}),
 "stove":        R(["III","IFI","IRI"], {"I":IR,"F":"minecraft:furnace","R":"minecraft:redstone"}),
 "oven":         R(["III","IGI","ISI"], {"I":IR,"G":"minecraft:glass_pane","S":"minecraft:smoker"}),
 "sink":         R(["III","ICI","PPP"], {"I":IR,"C":"minecraft:cauldron","P":OP}),
 "simple_bed":   R(["WWW","PPP","S S"], {"W":WW,"P":OP,"S":ST}),
 "canopy_bed":   R(["TTT","WWW","PPP"], {"T":"minecraft:string","W":WW,"P":OP}),
 "double_bed":   R(["WWW","WWW","PPP"], {"W":WW,"P":OP}),
 "bunk_bed":     R(["B","L","B"],       {"B":"#minecraft:beds","L":"minecraft:ladder"}),
 "floor_lamp":   R(["W","T","I"],       {"W":WW,"T":TO,"I":IR}),
 "table_lamp":   R(["W","T","P"],       {"W":WW,"T":TO,"P":OP}),
 "ceiling_lamp": R(["N","T","W"],       {"N":NU,"T":TO,"W":WW}),
 "crystal_chandelier_small":  R(["N","T","A"],       {"N":NU,"T":TO,"A":AM}),
 "crystal_chandelier_medium": R([" N ","ATA","AAA"], {"N":NU,"T":TO,"A":AM}),
 "crystal_chandelier_large":  R(["GNG","ATA","AAA"], {"G":GD,"N":NU,"T":TO,"A":AM}),
}

ROT = {"north":0,"east":90,"south":180,"west":270}
SHAPES = ["single","left","middle","right"]
def M(x): return f"{MOD}:block/{x}"
def mat_tex(mat, c): return f"minecraft:block/{c}_{'wool' if mat=='wool' else 'concrete'}"
def var(model, y=0):
    v = {"model": M(model)}
    if y: v["y"] = y
    return v
def cells(dims): return [(x,y,z) for x in range(dims[0]) for y in range(dims[1]) for z in range(dims[2])]
def cs(c): return f"{c[0]}{c[1]}{c[2]}"

def gen():
    for d in (f"{A}/models", f"{A}/blockstates", f"{A}/items", f"{D}/{MOD}", f"{D}/minecraft"):
        shutil.rmtree(d, ignore_errors=True)
    lang_en = {f"itemGroup.{MOD}.main":"Modern Furniture", f"entity.{MOD}.seat":"Seat",
               f"container.{MOD}.storage":"Furniture", f"container.{MOD}.big_storage":"Large Furniture",
               f"container.{MOD}.stove":"Stove", f"container.{MOD}.oven":"Oven",
               f"message.{MOD}.cannot_sit":"You can't sit down right now, try again in a moment"}
    lang_zh = {f"itemGroup.{MOD}.main":"现代家具", f"entity.{MOD}.seat":"座位",
               f"container.{MOD}.storage":"家具储物", f"container.{MOD}.big_storage":"大型家具储物",
               f"container.{MOD}.stove":"燃气灶", f"container.{MOD}.oven":"烤箱",
               f"message.{MOD}.cannot_sit":"现在坐不下,稍等一下再试"}
    axe, pick, beds_ids = [], [], []
    # shared table leg parts
    for nm, (fr, to) in {"nw":([1,0,1],[3,13,3]),"ne":([13,0,1],[15,13,3]),"sw":([1,0,13],[3,13,15]),"se":([13,0,13],[15,13,15])}.items():
        w(f"{A}/models/block/parts/table_leg_{nm}.json", {"textures":{"w":FIXED["w"],"particle":FIXED["w"]},"elements":[B(fr,to,"w")]})

    for name, f in FAM.items():
        k = f["kind"]
        # ------------- templates
        if k == "sofa":
            for s in SHAPES: template(f"sofa_{s}", sofa(s))
        elif k == "corner":
            for m in ("false","true"): template(f"corner_sofa_{m}", corner_sofa(m == "true"))
        elif k in ("facing","plain","cooker"):
            template(name, f["fn"]())
        elif k == "ctable":
            template("table", table_full()); template("table_top", table_top())
        elif k == "lamp":
            template(f"{name}_off", f["fn"](False)); template(f"{name}_on", f["fn"](True))
        elif k == "bed":
            b = f["bed"]
            if b == "double":
                for p in ("head","foot"):
                    for s in SHAPES: template(f"{name}_{p}_{s}", bed("double", p, s))
            elif b == "bunk":
                for p in ("head","foot"):
                    template(f"{name}_lo_{p}", bed("bunk", p, upper=False)); template(f"{name}_up_{p}", bed("bunk", p, upper=True))
            else:
                for p in ("head","foot"): template(f"{name}_{p}", bed(b, p))
        elif k == "mstorage":
            full = f["fn"](); dims = f["dims"]
            template(f"{name}_item", full, k=0.5)
            for c3 in cells(dims):
                part = clip(full, dims, c3)
                template(f"{name}_{cs(c3)}", part)
                template(f"{name}_{cs(c3)}_mirror", mirror_x(part))
        elif k == "chand":
            dims = f["dims"]; k_item = 0.35 if dims[0] > 1 else 0.5
            item_elements = f["fn"](False)
            if dims[0] > 1:
                item_elements = compact_item(item_elements, dims)
                k_item = 1.0
            template(f"{name}_item", item_elements, k=k_item)
            for on in (False, True):
                full = f["fn"](on)
                for c3 in cells(dims): template(f"{name}_{cs(c3)}_{'on' if on else 'off'}", clip(full, dims, c3))

        ids = []
        for c, _, _ in COLORS:
            bid = f"{name}_{c}"; ids.append(f"{MOD}:{bid}")
            ct = {"c": mat_tex(f["mat"], c)}
            def child(mname, tpl, extra=None):
                t = dict(ct); t.update(extra or {})
                w(f"{A}/models/block/{mname}.json", {"parent": M("tpl/"+tpl), "textures": t})
            cond = [{"condition":"minecraft:survives_explosion"}]
            # ------------- models + blockstate + item model
            if k == "sofa":
                for s in SHAPES: child(f"{bid}_{s}", f"sofa_{s}")
                bs = {"variants": {f"facing={d},shape={s}": var(f"{bid}_{s}", y) for d,y in ROT.items() for s in SHAPES}}
                item = f"{bid}_single"
            elif k == "corner":
                for m in ("false","true"): child(f"{bid}_{m}", f"corner_sofa_{m}")
                bs = {"variants": {f"facing={d},mirror={m}": var(f"{bid}_{m}", y) for d,y in ROT.items() for m in ("false","true")}}
                item = f"{bid}_false"
            elif k == "facing":
                child(bid, name)
                bs = {"variants": {f"facing={d}": var(bid, y) for d,y in ROT.items()}}; item = bid
            elif k == "plain":
                child(bid, name); bs = {"variants": {"": var(bid)}}; item = bid
            elif k == "ctable":
                child(bid, "table"); child(f"{bid}_top", "table_top")
                mp = [{"apply": var(f"{bid}_top")}]
                for leg, (a, b2) in {"nw":("north","west"),"ne":("north","east"),"sw":("south","west"),"se":("south","east")}.items():
                    mp.append({"when":{"AND":[{a:"false"},{b2:"false"}]},"apply":{"model":M(f"parts/table_leg_{leg}")}})
                bs = {"multipart": mp}; item = bid
            elif k == "cooker":
                child(f"{bid}_off", name, {"f": f["off"]}); child(f"{bid}_on", name, {"f": f["on"]})
                bs = {"variants": {f"facing={d},lit={l}": var(f"{bid}_{'on' if l=='true' else 'off'}", y)
                      for d,y in ROT.items() for l in ("false","true")}}
                item = f"{bid}_off"
            elif k == "lamp":
                child(f"{bid}_off", f"{name}_off"); child(f"{bid}_on", f"{name}_on")
                bs = {"variants": {"lit=false": var(f"{bid}_off"), "lit=true": var(f"{bid}_on")}}; item = f"{bid}_off"
            elif k == "bed":
                b = f["bed"]; beds_ids.append(f"{MOD}:{bid}")
                cond.insert(0, {"condition":"minecraft:block_state_property","block":f"{MOD}:{bid}","properties":{"part":"head"}})
                if b == "double":
                    for p in ("head","foot"):
                        for s in SHAPES: child(f"{bid}_{p}_{s}", f"{name}_{p}_{s}")
                    bs = {"variants": {f"facing={d},part={p},side={s}": var(f"{bid}_{p}_{s}", y)
                          for d,y in ROT.items() for p in ("head","foot") for s in SHAPES}}
                    item = f"{bid}_foot_single"
                elif b == "bunk":
                    for lv in ("lo","up"):
                        for p in ("head","foot"): child(f"{bid}_{lv}_{p}", f"{name}_{lv}_{p}")
                    bs = {"variants": {f"facing={d},part={p},upper={u}": var(f"{bid}_{'up' if u=='true' else 'lo'}_{p}", y)
                          for d,y in ROT.items() for p in ("head","foot") for u in ("false","true")}}
                    item = f"{bid}_lo_foot"
                    cond.insert(1, {"condition":"minecraft:block_state_property","block":f"{MOD}:{bid}","properties":{"upper":"false"}})
                else:
                    for p in ("head","foot"): child(f"{bid}_{p}", f"{name}_{p}")
                    bs = {"variants": {f"facing={d},part={p}": var(f"{bid}_{p}", y) for d,y in ROT.items() for p in ("head","foot")}}
                    item = f"{bid}_foot"
            elif k == "mstorage":
                dims = f["dims"]; px, py, pz = f["pivot"]
                child(f"{bid}_item", f"{name}_item")
                for c3 in cells(dims):
                    child(f"{bid}_{cs(c3)}", f"{name}_{cs(c3)}")
                    child(f"{bid}_{cs(c3)}_mirror", f"{name}_{cs(c3)}_mirror")
                # Shared PX/PZ properties allow 0..2; clamp impossible values to a valid model.
                bs = {"variants": {
                    f"facing={d},mirrored={str(mirrored).lower()},px={x},py={y2}":
                        var(f"{bid}_{min(x,dims[0]-1)}{y2}0{'_mirror' if mirrored else ''}", y)
                    for d,y in ROT.items() for mirrored in (False, True)
                    for x in range(3) for y2 in range(2)}}
                item = f"{bid}_item"
                cond.insert(0, {"condition":"minecraft:block_state_property","block":f"{MOD}:{bid}","properties":{"px":str(px),"py":str(py),"pz":str(pz)}})
            elif k == "chand":
                dims = f["dims"]; px, py, pz = f["pivot"]
                child(f"{bid}_item", f"{name}_item")
                for c3 in cells(dims):
                    child(f"{bid}_{cs(c3)}_off", f"{name}_{cs(c3)}_off"); child(f"{bid}_{cs(c3)}_on", f"{name}_{cs(c3)}_on")
                bs = {"variants": {f"lit={l},px={x},py={y2},pz={z}":
                      var(f"{bid}_{min(x,dims[0]-1)}{y2}{min(z,dims[2]-1)}_{'on' if l=='true' else 'off'}")
                      for x in range(3) for y2 in range(2) for z in range(3) for l in ("false","true")}}
                item = f"{bid}_item"
                cond.insert(0, {"condition":"minecraft:block_state_property","block":f"{MOD}:{bid}","properties":{"px":str(px),"py":str(py),"pz":str(pz)}})
            w(f"{A}/blockstates/{bid}.json", bs)
            w(f"{A}/items/{bid}.json", {"model": {"type":"minecraft:model","model": M(item)}})
            w(f"{D}/{MOD}/loot_table/blocks/{bid}.json", {"type":"minecraft:block","pools":[
                {"rolls":1,"bonus_rolls":0,"entries":[{"type":"minecraft:item","name":f"{MOD}:{bid}"}],"conditions":cond}]})
            w(f"{D}/{MOD}/recipe/{bid}_dye.json", {"type":"minecraft:crafting_shapeless","category":"misc",
                "group": f"{MOD}_dye_{name}", "ingredients":[f"#{MOD}:{name}", f"minecraft:{c}_dye"],
                "result":{"id":f"{MOD}:{bid}","count":1}})
            (axe if f["tool"]=="axe" else pick).append(f"{MOD}:{bid}")
            lang_en[f"block.{MOD}.{bid}"] = f"{f['en']} ({EN[c]})"
            lang_zh[f"block.{MOD}.{bid}"] = f"{f['zh']}（{CN[c]}）"
        w(f"{D}/{MOD}/tags/item/{name}.json", {"values": ids})
        r = RECIPES[name]
        w(f"{D}/{MOD}/recipe/{name}.json", {"type":"minecraft:crafting_shaped","category":"misc",
            "pattern": r["pattern"], "key": r["key"], "result":{"id":f"{MOD}:{name}_white","count":r["count"]}})
    w(f"{D}/minecraft/tags/block/mineable/axe.json", {"values": axe})
    w(f"{D}/minecraft/tags/block/mineable/pickaxe.json", {"values": pick})
    w(f"{D}/minecraft/tags/block/beds.json", {"values": beds_ids})
    w(f"{D}/minecraft/tags/item/beds.json", {"values": beds_ids})
    w(f"{A}/lang/en_us.json", lang_en); w(f"{A}/lang/zh_cn.json", lang_zh)

if __name__ == "__main__":
    gen(); print("resources generated")
