"""Optional build-time utility; fonttools is not an application dependency."""
import argparse
from pathlib import Path
from fontTools.ttLib import TTFont
from fontTools.varLib.instancer import instantiateVariableFont
from fontTools.pens.ttGlyphPen import TTGlyphPen

parser = argparse.ArgumentParser()
parser.add_argument('source', type=Path)
args = parser.parse_args()
output = Path(__file__).resolve().parent.parent / 'public' / 'fonts'
output.mkdir(parents=True, exist_ok=True)
for weight, label in [(400, 'Regular'), (700, 'Bold')]:
    font = instantiateVariableFont(TTFont(args.source), {'wght': weight}, inplace=True)
    names = {1: 'Noto Sans SC', 2: label, 4: f'Noto Sans SC {label}',
             6: f'NotoSansSC-{label}', 16: 'Noto Sans SC', 17: label}
    for record in font['name'].names:
        if record.nameID in names:
            record.string = names[record.nameID].encode(record.getEncoding())
    font['OS/2'].usWeightClass = weight
    glyph = 'zeroWidthSpace'
    order = font.getGlyphOrder()
    if glyph not in order:
        font.setGlyphOrder(order + [glyph])
        font['glyf'][glyph] = TTGlyphPen(None).glyph()
        font['hmtx'][glyph] = (0, 0)
    for table in font['cmap'].tables:
        if table.isUnicode():
            table.cmap[0x200A] = glyph
            table.cmap[0x200B] = glyph
    font.save(output / f'NotoSansSC-{label}.ttf')
