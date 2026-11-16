import { useLockFn, useRequest, useUnmountedRef, useUpdateEffect } from 'ahooks';
import { useEffect, useState } from 'react';
import { notify } from '../../../stores/noticeStore';
import { useSession } from '../../../stores/sessionStore';
import type { Resume, Template } from '../../../types';
import { exportResumePdf } from '../../resumes/pdf/exportResumePdf';

export function usePdfExport(
  save: () => Promise<Resume>,
  templates: Template[],
  isMember: boolean,
) {
  const token = useSession((session) => session.token);
  const unmounted = useUnmountedRef();
  const [document, setDocument] = useState<{
    url: string;
    filename: string;
    exportId: number;
    token: string;
  } | null>(null);
  const url = document?.url;
  useUpdateEffect(() => setDocument(null), [token]);
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
    async (requestToken: string) => {
      const isCurrent = () => !unmounted.current && useSession.getState().token === requestToken;
      if (!isCurrent()) throw new Error('登录状态已变化，导出已取消');
      const resume = await save();
      if (!isCurrent()) throw new Error('登录状态已变化，导出已取消');
      const template = templates.find((item) => item.id === resume.templateId);
      if (!template) throw new Error('简历模板已不存在，请重新选择');
      const { blob, exportId } = await exportResumePdf(
        resume,
        template,
        !isMember,
        requestToken,
        isCurrent,
      );
      const filename =
        [resume.content.basic.name || '我的简历', resume.content.basic.job || '求职简历']
          .join('-')
          .replace(/[\\/:*?"<>|]/g, '_') + '.pdf';
      return { blob, filename, exportId, token: requestToken };
    },
    {
      manual: true,
      onSuccess({ blob, filename, exportId, token: requestToken }) {
        if (unmounted.current || useSession.getState().token !== requestToken) return;
        setDocument({ url: URL.createObjectURL(blob), filename, exportId, token: requestToken });
        mutate(undefined);
        notify('PDF 已生成，可以预览并下载');
      },
      onError(error, [requestToken]) {
        if (!unmounted.current && useSession.getState().token === requestToken)
          notify(error.message);
      },
    },
  );
  const exportPdf = useLockFn(() => {
    const requestToken = useSession.getState().token;
    if (!requestToken) {
      notify('请先登录后再导出');
      return Promise.resolve(undefined);
    }
    return runAsync(requestToken).catch(() => undefined);
  });

  return {
    exporting,
    document: document?.token === token ? document : null,
    exportPdf,
    closePreview: () => setDocument(null),
  };
}
