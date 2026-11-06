// Integration check: use the actual production document and verify Chinese text,
// A4 dimensions, all template variants and long-content pagination.
import { build } from 'esbuild';
import fs from 'node:fs/promises';
import path from 'node:path';
import assert from 'node:assert/strict';
import { pathToFileURL } from 'node:url';
import React from 'react';
import { pdf } from '@react-pdf/renderer';
import { getDocument } from 'pdfjs-dist/legacy/build/pdf.mjs';
const root = path.resolve(import.meta.dirname, '..');
const output = path.join(root, '.tools/pdf-check');
const standardFontDataUrl =
  path.join(root, 'node_modules/pdfjs-dist/standard_fonts/').replaceAll('\\', '/') + '/';
await fs.mkdir(output, { recursive: true });
await build({
  entryPoints: {
    document: path.join(root, 'src/features/pdf/PdfDocument.tsx'),
    fonts: path.join(root, 'src/features/pdf/fonts.ts'),
  },
  outdir: output,
  outExtension: { '.js': '.mjs' },
  bundle: true,
  platform: 'node',
  format: 'esm',
  packages: 'external',
  jsx: 'automatic',
});
const { default: PdfDocument } = await import(pathToFileURL(path.join(output, 'document.mjs')));
const { registerPdfFonts } = await import(pathToFileURL(path.join(output, 'fonts.mjs')));
registerPdfFonts(path.join(root, 'public/fonts'));
const content = JSON.parse(
  await fs.readFile(path.join(root, '../backend/src/main/resources/sample-resume.json'), 'utf8'),
);
const templates = [
  { id: 1, style: 'modern', color: '#3568d4' },
  { id: 2, style: 'classic', color: '#334155' },
  { id: 3, style: 'fresh', color: '#237b63' },
  { id: 4, style: 'mono', color: '#222222' },
];
for (const template of templates) {
  const blob = await pdf(
    React.createElement(PdfDocument, { content, template, watermark: true }),
  ).toBlob();
  const bytes = new Uint8Array(await blob.arrayBuffer());
  await fs.writeFile(path.join(output, `${template.style}.pdf`), bytes);
  const byteLength = bytes.length;
  const document = await getDocument({ data: bytes, standardFontDataUrl }).promise;
  const page = await document.getPage(1);
  const text = (await page.getTextContent()).items
    .map((item) => item.str || '')
    .join('')
    .replace(/[\u200b\s]/g, '');
  for (const expected of ['林予安', '个人简介', '华东理工大学', '星河科技', '专业技能', '简历工坊'])
    assert.ok(text.includes(expected), `${template.style}: missing ${expected}`);
  assert.ok(
    Math.abs(page.view[2] - 595.28) < 1 && Math.abs(page.view[3] - 841.89) < 1,
    'A4 page size',
  );
  assert.equal(document.numPages, 1, 'Sample should fit one page');
  console.log(`${template.style}: Chinese text, A4, watermark, ${byteLength} bytes verified`);
  await document.destroy();
}
const long = structuredClone(content);
long.basic.intro = '无空格中文长段落用于检查自动换行。'.repeat(20);
long.work = Array.from({ length: 15 }, (_, i) => ({
  ...content.work[0],
  id: `long-${i}`,
  company: `分页验证公司${i + 1}`,
  desc: (content.work[0].desc + '\n').repeat(3),
}));
const blob = await pdf(
  React.createElement(PdfDocument, { content: long, template: templates[0], watermark: false }),
).toBlob();
const bytes = new Uint8Array(await blob.arrayBuffer());
await fs.writeFile(path.join(output, 'multipage.pdf'), bytes);
const document = await getDocument({ data: bytes, standardFontDataUrl }).promise;
assert.ok(document.numPages > 2, 'Long resumes must paginate');
let text = '';
for (let i = 1; i <= document.numPages; i++) {
  const page = await document.getPage(i);
  const items = (await page.getTextContent()).items;
  text += items
    .map((item) => item.str || '')
    .join('')
    .replace(/[\u200b\s]/g, '');
  for (const item of items)
    if (item.str?.trim())
      assert.ok(
        item.transform[4] + item.width < page.view[2] - 20,
        `Text must stay within A4 margins: ${item.str}`,
      );
}
assert.equal(
  (text.match(/无空格/g) || []).length,
  20,
  'Continuous Chinese paragraphs must wrap without truncation',
);
for (let i = 1; i <= 15; i++)
  assert.ok(text.includes(`分页验证公司${i}`), `Entry ${i} must not be truncated`);
assert.ok(text.includes('专业技能'), 'Final section must survive pagination');
assert.ok(!text.includes('简历工坊'), 'Members must not have watermark');
console.log(`Long resume: ${document.numPages} pages, all 15 entries and final section verified`);
await document.destroy();
