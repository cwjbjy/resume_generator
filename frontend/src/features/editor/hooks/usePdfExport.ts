import { useLockFn, useRequest } from 'ahooks';
import { useEffect, useState } from 'react';
import { notify } from '../../../stores/noticeStore';
import type { Resume, Template } from '../../../types';
import { exportResumePdf } from '../../pdf/exportResumePdf';

export function usePdfExport(
  save: () => Promise<Resume>,
  templates: Template[],
  isMember: boolean,
) {
  const [document, setDocument] = useState<{ url: string; filename: string } | null>(null);
  const url = document?.url;
  useEffect(
    () => () => {
      if (url) URL.revokeObjectURL(url);
    },
    [url],
  );

  const {
    loading: exporting,
    runAsync,
    mutate,
  } = useRequest(
    async () => {
      const resume = await save();
      const template = templates.find((item) => item.id === resume.templateId)!;
      const blob = await exportResumePdf(resume, template, !isMember);
      const filename =
        [resume.content.basic.name || '我的简历', resume.content.basic.job || '求职简历']
          .join('-')
          .replace(/[\\/:*?"<>|]/g, '_') + '.pdf';
      return { blob, filename };
    },
    {
      manual: true,
      onSuccess({ blob, filename }) {
        setDocument({ url: URL.createObjectURL(blob), filename });
        mutate(undefined);
        notify('PDF 已生成，可以预览并下载');
      },
      onError(error) {
        notify(error.message);
      },
    },
  );
  const exportPdf = useLockFn(() => runAsync().catch(() => undefined));

  return { exporting, document, exportPdf, closePreview: () => setDocument(null) };
}
