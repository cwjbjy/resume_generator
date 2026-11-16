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

export async function exportResumePdf(
  resume: Resume,
  template: Template,
  watermark: boolean,
  token: string,
  isCurrent: () => boolean,
) {
  if (!isCurrent()) throw new Error('登录状态已变化，导出已取消');
  const reservation = await exportApi.reserve(resume.id, resume.version, token);
  try {
    if (!isCurrent()) throw new Error('登录状态已变化，导出已取消');
    const blob = await generatePdf(resume, template, watermark);
    if (!isCurrent()) throw new Error('登录状态已变化，导出已取消');
    const result = await exportApi.complete(reservation.id, true, token);
    if (result.status !== 'COMPLETED') throw new Error('导出未完成，请重试');
    if (!isCurrent()) throw new Error('登录状态已变化，导出已取消');
    return { blob, exportId: reservation.id };
  } catch (error) {
    await exportApi.complete(reservation.id, false, token).catch(() => {});
    throw error;
  }
}
