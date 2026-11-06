import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { resumeApi } from '../../../api/resumeApi';
import { notify } from '../../../stores/noticeStore';
import { useSession } from '../../../stores/sessionStore';
import type { Template } from '../../../types';

export function useCreateResume() {
  const user = useSession((state) => state.user);
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { mutate, isPending } = useMutation({
    mutationFn: (template: Template) => resumeApi.create(template.id),
    onSuccess(resume) {
      void queryClient.invalidateQueries({ queryKey: ['resumes'] });
      navigate(`/editor/${resume.id}`);
    },
    onError(error) {
      notify(error.message);
    },
  });
  const createResume = useCallback(
    (template: Template) => {
      if (!user) {
        navigate(`/login?redirect=${encodeURIComponent(`/templates?use=${template.id}`)}`);
        return;
      }
      if (template.isVip && !user.member) {
        navigate('/pricing');
        notify('开通会员即可使用这套模板');
        return;
      }
      mutate(template);
    },
    [user, navigate, mutate],
  );
  return { createResume, isPending };
}
