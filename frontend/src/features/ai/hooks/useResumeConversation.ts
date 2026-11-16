import { useLatest, useLockFn, useRequest } from 'ahooks';
import { useState } from 'react';
import { useSession } from '../../../stores/sessionStore';
import { aiApi } from '../api/aiApi';
import { nextConversation } from '../model/conversation';
import type { ChatMessage, ChatResult } from '../model/types';

export function useResumeConversation() {
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [input, setInput] = useState('');
  const [generated, setGenerated] = useState<ChatResult | null>(null);
  const latestInput = useLatest(input);
  const request = useRequest(
    async (generate: boolean) => {
      const token = useSession.getState().token;
      const submittedInput = input;
      const conversation = nextConversation(messages, submittedInput, generate);
      const result = await aiApi.chat(conversation, generate, token ?? undefined);
      return { result, conversation, token, submittedInput };
    },
    {
      manual: true,
      onSuccess(response) {
        if (useSession.getState().token !== response.token) return;
        setMessages([
          ...response.conversation,
          { role: 'assistant', content: response.result.reply },
        ]);
        setGenerated(response.result.content ? response.result : null);
        if (latestInput.current === response.submittedInput) setInput('');
      },
    },
  );
  const submit = useLockFn((generate: boolean) => request.runAsync(generate).catch(() => {}));
  function reset() {
    request.cancel();
    request.mutate(undefined);
    setMessages([]);
    setGenerated(null);
    setInput('');
  }
  return {
    messages,
    input,
    setInput,
    generated,
    submit,
    reset,
    loading: request.loading,
    error: request.error,
  };
}
