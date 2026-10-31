#!/usr/bin/env python3
"""Generate deterministic placeholder models and the explicitly approved recipes; no advancements."""
from pathlib import Path
import json,zipfile,struct,zlib
ROOT=Path(__file__).resolve().parents[1]/'src/main/resources'
MOD='fantasy_feudalism_items'
def write(path,data):
 p=ROOT/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(data,indent=2)+'\n')
def mod(path,data):write(f'data/{MOD}/{path}',data)
def model(name,data):write(f'assets/{MOD}/models/{name}.json',data)
def item(name,texture):model('item/'+name,{'parent':'minecraft:item/generated','textures':{'layer0':texture}})
def shaped(name,pattern,key,result):mod('recipe/'+name+'.json',{'type':'minecraft:crafting_shaped','category':'misc','pattern':pattern,'key':key,'result':{'id':MOD+':'+result,'count':1}})
shaped('bone_dagger',[' F ',' B ',' S '],{'F':{'item':'minecraft:flint'},'B':{'item':'minecraft:bone'},'S':{'item':'minecraft:stick'}},'bone_dagger')
shaped('trophy_mount',['SIS','SLS','WWW'],{'S':{'item':'minecraft:stick'},'I':{'item':'minecraft:iron_ingot'},'L':{'item':'minecraft:leather'},'W':{'tag':'minecraft:wooden_slabs'}},'trophy_mount')
shaped('saddlebag_half',['LLL','LIL','LLL'],{'L':{'item':'minecraft:leather'},'I':{'item':'minecraft:iron_block'}},'saddlebag_half')
shaped('saddlebag',['HLH'],{'H':{'item':MOD+':saddlebag_half'},'L':{'item':'minecraft:leather'}},'saddlebag')
for tier,material in [('copper','copper_ingot'),('iron','iron_ingot'),('gold','gold_ingot'),('diamond','diamond')]:
 shaped(tier+'_horseshoe',['M M','M M','MMM'],{'M':{'item':'minecraft:'+material}},tier+'_horseshoe')
mod('recipe/netherite_horseshoe.json',{'type':MOD+':netherite_horseshoe'})
mod('recipe/trophy.json',{'type':MOD+':trophy','category':'misc'})
mod('recipe/saddlebag_dye.json',{'type':MOD+':saddlebag_dye','category':'misc'})
mod('recipe/kelp_oil.json',{'type':'minecraft:crafting_shapeless','category':'misc','ingredients':[{'item':'minecraft:kelp'},{'item':'minecraft:glass_bottle'}],'result':{'id':MOD+':kelp_oil','count':1}})
write('data/minecraft/tags/item/swords.json',{'replace':False,'values':[MOD+':bone_dagger']})
write('data/minecraft/tags/item/dyeable.json',{'replace':False,'values':[MOD+':saddlebag']})
heads=['zombie_head','skeleton_skull','creeper_head','wither_skeleton_skull','piglin_head','dragon_head','player_head']
mod('tags/item/sigil_heads.json',{'replace':False,'values':['minecraft:'+h for h in heads]+[MOD+':stray_head',MOD+':bogged_head']})
mod('enchantment/sigil.json',{'description':{'translate':'enchantment.'+MOD+'.sigil'},'supported_items':'#'+MOD+':sigil_heads','primary_items':'#'+MOD+':sigil_heads','weight':1,'max_level':1,'min_cost':{'base':30,'per_level_above_first':0},'max_cost':{'base':100,'per_level_above_first':0},'anvil_cost':1,'slots':['head'],'effects':{}})
lang=json.loads((ROOT/f'assets/{MOD}/lang/en_us.json').read_text())
for name in ['kelp_oil','vial_of_milk','fishing_trap','trophy_mount','saddlebag','saddlebag_half','bone_dagger','stray_head','bogged_head','copper_horseshoe','iron_horseshoe','gold_horseshoe','diamond_horseshoe','netherite_horseshoe','awkward_milk_potion','intermediate_milk_potion','animal_allegiance_potion','extended_animal_allegiance_potion']:
 lang['item.'+MOD+'.'+name]=' '.join(w.capitalize() for w in name.split('_'))
lang['item.'+MOD+'.animal_allegiance_potion']='Potion of Animal Allegiance'
lang['item.'+MOD+'.extended_animal_allegiance_potion']='Potion of Animal Allegiance (Extended)'
lang['effect.'+MOD+'.animal_allegiance']='Animal Allegiance'
lang['enchantment.'+MOD+'.sigil']='Sigil'
lang['message.'+MOD+'.attuned']='Attuned to %s'
for name,texture in [('kelp_oil','honey_bottle'),('vial_of_milk','milk_bucket'),('trophy_mount','armor_stand'),('saddlebag_half','leather'),('bone_dagger','stone_sword'),('stray_head','skeleton_skull'),('bogged_head','zombie_head')]:item(name,'minecraft:item/'+texture)
for name in ['awkward_milk_potion','intermediate_milk_potion','animal_allegiance_potion','extended_animal_allegiance_potion']:model('item/'+name,{'parent':'minecraft:item/potion'})
for tier,texture in [('copper','copper_ingot'),('iron','iron_ingot'),('gold','gold_ingot'),('diamond','diamond'),('netherite','netherite_ingot')]:item(tier+'_horseshoe','minecraft:item/'+texture)
# Two-bag model is rendered once on either side of the animal, with the item dye tint.
faces={side:{'texture':'#bag','tintindex':0} for side in ['north','south','east','west','up','down']}
model('item/saddlebag',{'textures':{'bag':'minecraft:block/white_wool','particle':'minecraft:block/white_wool'},'elements':[{'from':[2,2,4],'to':[14,14,12],'faces':faces}]})
model('block/fishing_trap',{'parent':'minecraft:block/cube_all','textures':{'all':'minecraft:block/oak_trapdoor'}})
write(f'assets/{MOD}/blockstates/fishing_trap.json',{'variants':{'':{'model':MOD+':block/fishing_trap'}}})
model('item/fishing_trap',{'parent':MOD+':block/fishing_trap'})
for level,parent in [(1,'template_cauldron_level1'),(2,'template_cauldron_level2'),(3,'template_cauldron_full')]:
 model('block/milk_cauldron_'+str(level),{'parent':'minecraft:block/'+parent,'textures':{'bottom':'minecraft:block/cauldron_bottom','content':'minecraft:block/white_concrete','inside':'minecraft:block/cauldron_inner','particle':'minecraft:block/cauldron_side','side':'minecraft:block/cauldron_side','top':'minecraft:block/cauldron_top'}})
write(f'assets/{MOD}/blockstates/milk_cauldron.json',{'variants':{'level='+str(n):{'model':MOD+':block/milk_cauldron_'+str(n)} for n in (1,2,3)}})
def loot(block,itemid):mod('loot_table/blocks/'+block+'.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':itemid}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
loot('fishing_trap',MOD+':fishing_trap');loot('milk_cauldron','minecraft:cauldron')
colors={'zombie':'mossy_cobblestone','skeleton':'bone_block_side','creeper':'green_concrete','wither_skeleton':'black_concrete','stray':'light_blue_concrete','bogged':'moss_block','piglin':'gold_block','ender_dragon':'obsidian','player':'stone'}
for kind,texture in colors.items():
 name=kind+'_trophy';lang['block.'+MOD+'.'+name]=' '.join(w.capitalize() for w in name.split('_'))
 faces={side:{'texture':'#side'} for side in ['north','south','east','west','up','down']}
 topfaces={side:{'texture':'#head'} for side in faces}
 model('block/'+name,{'textures':{'side':'minecraft:block/stripped_oak_log','head':'minecraft:block/'+texture,'particle':'minecraft:block/'+texture},'elements':[{'from':[2,0,2],'to':[14,4,14],'faces':faces},{'from':[4,4,4],'to':[12,12,12],'faces':topfaces}]})
 write(f'assets/{MOD}/blockstates/'+name+'.json',{'variants':{'':{'model':MOD+':block/'+name}}})
 model('item/'+name,{'parent':MOD+':block/'+name})
 mod('loot_table/blocks/'+name+'.json',{'type':'minecraft:block','pools':[]})
for itemid in ['lantern','soul_lantern']:
 write('assets/minecraft/models/item/'+itemid+'.json',{'parent':'minecraft:block/'+itemid,'overrides':[{'predicate':{MOD+':off':1},'model':MOD+':block/unlit_lantern'}]})
write(f'assets/{MOD}/lang/en_us.json',lang)
# Original placeholder effect icon, not a modification of an existing image.
rows=[]
for y in range(18):
 row=bytearray()
 for x in range(18):
  visible=(5<=x<=12 and 6<=y<=13)or(3<=x<=6 and 2<=y<=7)or(11<=x<=14 and 2<=y<=7)
  row.extend((227,189,116,255) if visible else (0,0,0,0))
 rows.append(b'\0'+row)
def chunk(k,b):return struct.pack('>I',len(b))+k+b+struct.pack('>I',zlib.crc32(k+b)&0xffffffff)
p=ROOT/f'assets/{MOD}/textures/mob_effect/animal_allegiance.png';p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(b'\x89PNG\r\n\x1a\n'+chunk(b'IHDR',struct.pack('>IIBBBBB',18,18,8,6,0,0,0))+chunk(b'IDAT',zlib.compress(b''.join(rows)))+chunk(b'IEND',b''))

for mob,drop in [('zombie','rotten_flesh'),('skeleton','bone'),('spider','string'),('cave_spider','spider_eye'),('blaze','blaze_rod'),('magma_cube','magma_cream')]:
 name=mob+'_spawner_eye'
 shaped(name,['DDD','DED','DDD'],{'D':{'item':'minecraft:'+drop},'E':{'item':'minecraft:ender_eye'}},name)
 item(name,'minecraft:item/ender_eye')
 lang['item.'+MOD+'.'+name]=' '.join(w.capitalize() for w in name.split('_'))
write(f'assets/{MOD}/lang/en_us.json',lang)

# Components-aware ordinary recipes appear correctly in the vanilla recipe book.
head_map={'zombie':'minecraft:zombie_head','skeleton':'minecraft:skeleton_skull','creeper':'minecraft:creeper_head','wither_skeleton':'minecraft:wither_skeleton_skull','stray':MOD+':stray_head','bogged':MOD+':bogged_head','piglin':'minecraft:piglin_head','ender_dragon':'minecraft:dragon_head','player':'minecraft:player_head'}
for kind,head in head_map.items():
 mod('recipe/'+kind+'_trophy.json',{'type':'minecraft:crafting_shapeless','category':'misc','ingredients':[{'type':'neoforge:components','items':head,'components':{'minecraft:enchantments':{'levels':{MOD+':sigil':1}}},'strict':False},{'item':MOD+':trophy_mount'}],'result':{'id':MOD+':'+kind+'_trophy','count':1}})
old=ROOT/f'data/{MOD}/recipe/trophy.json'
if old.exists():old.unlink()
write('data/neoforge/loot_modifiers/global_loot_modifiers.json',{'replace':False,'entries':[MOD+':saddlebags']})
mod('loot_modifiers/saddlebags.json',{'type':MOD+':saddlebags','conditions':[{'condition':'minecraft:random_chance','chance':0.20},{'condition':'minecraft:any_of','terms':[{'condition':'neoforge:loot_table_id','loot_table_id':'minecraft:chests/village/village_'+table} for table in ['weaponsmith','toolsmith','armorer','tannery']]}]})
# Unlock recipes when their main material is first obtained, with no additional crafting gates.
for path in (ROOT/f'data/{MOD}/recipe').glob('*.json'):
 recipe=json.loads(path.read_text());name=path.stem
 if name=='saddlebag_dye':ingredient=MOD+':saddlebag'
 elif name.endswith('_trophy'):ingredient=MOD+':trophy_mount'
 elif name.endswith('_spawner_eye'):ingredient='minecraft:ender_eye'
 elif name.endswith('_horseshoe'):ingredient='minecraft:'+({'copper':'copper_ingot','iron':'iron_ingot','gold':'gold_ingot','diamond':'diamond','netherite':'netherite_ingot'}[name.split('_')[0]])
 elif name=='bone_dagger':ingredient='minecraft:bone'
 elif name=='kelp_oil':ingredient='minecraft:kelp'
 elif name=='saddlebag':ingredient=MOD+':saddlebag_half'
 elif name in ('saddlebag_half','trophy_mount'):ingredient='minecraft:leather'
 else:continue
 mod('advancement/recipes/'+name+'.json',{'criteria':{'has_material':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':ingredient}]}}},'requirements':[['has_material']],'rewards':{'recipes':[MOD+':'+name]}})
all_items=[MOD+':'+n for n in ['kelp_oil','vial_of_milk','fishing_trap','trophy_mount','saddlebag','saddlebag_half','bone_dagger','stray_head','bogged_head','animal_allegiance_potion','extended_animal_allegiance_potion']]+[MOD+':'+t+'_horseshoe' for t in ['copper','iron','gold','diamond','netherite']]+[MOD+':'+t+'_trophy' for t in head_map]+[MOD+':'+t+'_spawner_eye' for t in ['zombie','skeleton','spider','cave_spider','blaze','magma_cube']]
mod('tags/item/items.json',{'replace':False,'values':all_items})
def obtained(names):return {'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':names}]}}
clair={'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':['minecraft:potion','minecraft:splash_potion','minecraft:lingering_potion'],'components':{'minecraft:potion_contents':{'potion':MOD+':clairvoyance'}}}]}}
mod('advancement/root.json',{'display':{'icon':{'id':MOD+':trophy_mount'},'title':'Fantasy Feudalism Items','description':'Discover medieval and fantasy equipment','background':'minecraft:textures/block/stone.png','show_toast':False,'announce_to_chat':False},'criteria':{'equipment':obtained('#'+MOD+':items'),'clairvoyance':clair},'requirements':[['equipment','clairvoyance']]})
for name,title,description,icon,criterion in [('clairvoyance','A Clearer View','Obtain a Potion of Clairvoyance','minecraft:potion',clair),('animal_allegiance','Animal Allegiance','Obtain a Potion of Animal Allegiance',MOD+':animal_allegiance_potion',obtained([MOD+':animal_allegiance_potion',MOD+':extended_animal_allegiance_potion'])),('saddlebag','Pack for the Road','Obtain a Saddlebag',MOD+':saddlebag',obtained(MOD+':saddlebag')),('horseshoe','A Better Pace','Obtain a Horseshoe',MOD+':iron_horseshoe',obtained([MOD+':'+t+'_horseshoe' for t in ['copper','iron','gold','diamond','netherite']])),('trophy','A Lasting Trophy','Obtain a Mob Trophy',MOD+':zombie_trophy',obtained([MOD+':'+k+'_trophy' for k in head_map])),('fishing_trap','River Harvest','Obtain a Fishing Trap',MOD+':fishing_trap',obtained(MOD+':fishing_trap'))]:
 mod('advancement/'+name+'.json',{'parent':MOD+':root','display':{'icon':{'id':icon},'title':title,'description':description,'frame':'task','show_toast':True,'announce_to_chat':True},'criteria':{'obtain':criterion},'requirements':[['obtain']]})

for torch in ['torch','soul_torch']:
 write('assets/minecraft/models/item/'+torch+'.json',{'parent':'minecraft:item/generated','textures':{'layer0':'minecraft:block/'+torch},'overrides':[{'predicate':{MOD+':off':1},'model':MOD+':block/unlit_torch'}]})

for name,color in [('stray_head',(177,190,195)),('bogged_head',(102,121,71))]:
 rows=[]
 for y in range(16):
  row=bytearray()
  for x in range(16):
   pixel=(0,0,0,0)
   if 3<=x<=12 and 3<=y<=12:pixel=(*color,255)
   if 5<=y<=7 and (4<=x<=6 or 9<=x<=11):pixel=(30,35,26,255)
   if 9<=y<=10 and 6<=x<=9:pixel=(45,48,34,255)
   row.extend(pixel)
  rows.append(b'\0'+row)
 p=ROOT/f'assets/{MOD}/textures/item/{name}.png';p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(b'\x89PNG\r\n\x1a\n'+chunk(b'IHDR',struct.pack('>IIBBBBB',16,16,8,6,0,0,0))+chunk(b'IDAT',zlib.compress(b''.join(rows)))+chunk(b'IEND',b''))
 item(name,MOD+':item/'+name)

lang['item.'+MOD+'.vial_of_milk']='Vial of Milk'
lang['item.'+MOD+'.awkward_milk_potion']='Awkward Milk-Based Potion'
lang['item.'+MOD+'.intermediate_milk_potion']='Intermediate Potion'
write(f'assets/{MOD}/lang/en_us.json',lang)

# Trophy descriptions are separate translatable lines with shared instructions.
import re
source=(ROOT.parents[2]/'src/main/java/com/fantasyfeudalism/items/trophy/TrophyKind.java').read_text()
lang=json.loads((ROOT/f'assets/{MOD}/lang/en_us.json').read_text())
base='tooltip.'+MOD+'.trophy.'
lang.update({base+'bonus':'While attuned:',base+'drawback':'Drawback:',base+'attune':'Place, then right-click to attune. One active trophy.',base+'limit':'Only you can attune. One placement per type, ever.',base+'permanent':'Cannot be recovered. Breaking ends attunement.'})
for kind,benefits,drawbacks in re.findall(r'([A-Z_]+)\(new String\[\]\{([^}]+)\},new String\[\]\{([^}]+)\}\)',source):
 for category,values in [('benefit',benefits),('drawback',drawbacks)]:
  for index,line in enumerate(re.findall(r'"([^"\n]+)"',values)):lang[base+kind.lower()+'.'+category+'.'+str(index)]=line
write(f'assets/{MOD}/lang/en_us.json',lang)
