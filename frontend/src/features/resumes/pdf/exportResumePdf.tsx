import type { Resume, Template } from '../../../types';
import { exportApi } from '../api/exportApi';

async function generatePdf(resume: Resume, template: Template, watermark: boolean) {
  const [{ pdf }, { default: PdfDocument }] = await Promise.all([
    import('@react-pdf/renderer'),
    import('./PdfDocument'),
  ]);
  const timeout = { id: undefined as ReturnType<typeof setTimeout> | undefined };
  try {
    return await Promise.race([
      pdf(
        <PdfDocument content={resume.content} template={template} watermark={watermark} />,
      ).toBlob(),
      new Promise<never>((_, reject) => {
        timeout.id = setTimeout(() => reject(new Error('PDF 生成超时，请重试')), 60000);
      }),
    ]);
  } finally {
    clearTimeout(timeout.id);
  }
}

export async function exportResumePdf(resume: Resume, template: Template, watermark: boolean) {
  const reservation = await exportApi.reserve(resume.id);
  try {
    const blob = await generatePdf(resume, template, watermark);
    const result = await exportApi.complete(reservation.id, true);
    if (result.status !== 'COMPLETED') throw new Error('导出未完成，请重试');
    return blob;
  } catch (error) {
    await exportApi.complete(reservation.id, false).catch(() => {});
    throw error;
  }
}
