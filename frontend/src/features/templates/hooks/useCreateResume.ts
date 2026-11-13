import { useLockFn, useMemoizedFn, useRequest } from 'ahooks';
import { useNavigate } from 'react-router-dom';
import { resumeApi } from '../../resumes/api/resumeApi';
import { notify } from '../../../stores/noticeStore';
import { useSession } from '../../../stores/sessionStore';
import type { Template } from '../../../types';

export function useCreateResume() {
  const user = useSession((state) => state.user);
  const navigate = useNavigate();
  const { runAsync, loading } = useRequest((template: Template) => resumeApi.create(template.id), {
    manual: true,
    onSuccess(resume) {
      navigate(`/editor/${resume.id}`);
    },
    onError(error) {
      notify(error.message);
    },
  });
  const createResume = useLockFn(
    useMemoizedFn(async (template: Template) => {
      if (!user) {
        navigate(`/login?redirect=${encodeURIComponent(`/templates?use=${template.id}`)}`);
        return;
      }
      if (template.isVip && !user.member) {
        navigate('/pricing');
        notify('开通会员即可使用这套模板');
        return;
      }
      await runAsync(template).catch(() => undefined);
    }),
  );
  return { createResume, loading };
}
