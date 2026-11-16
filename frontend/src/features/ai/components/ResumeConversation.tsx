import { type FormEvent, useRef } from 'react';
import { useUpdateEffect } from 'ahooks';
import { Button } from '../../../components/ui/Button';
import { ErrorBox } from '../../../components/ui/ErrorBox';
import type { useResumeConversation } from '../hooks/useResumeConversation';

export function ResumeConversation({
  conversation,
  available,
  creating,
}: {
  conversation: ReturnType<typeof useResumeConversation>;
  available: boolean;
  creating: boolean;
}) {
  const log = useRef<HTMLDivElement>(null);
  useUpdateEffect(() => {
    if (log.current) log.current.scrollTop = log.current.scrollHeight;
  }, [conversation.messages.length]);
  const exhausted = conversation.messages.length >= 20;
  function submit(event: FormEvent) {
    event.preventDefault();
    void conversation.submit(false);
  }
  return (
    <section className="ai-conversation">
      <h2>整理你的职业经历</h2>
      <div
        className="ai-message-log"
        role="log"
        aria-label="简历生成对话"
        aria-live="polite"
        ref={log}
      >
        {conversation.messages.length === 0 && (
          <p className="ai-conversation-intro">
            告诉我你的求职方向、教育背景和真实经历。你可以分次补充，再生成简历预览。
          </p>
        )}
        {conversation.messages.map((message, index) => (
          <article key={index} className={`ai-message ${message.role}`}>
            <h3>{message.role === 'user' ? '你' : 'AI 简历助手'}</h3>
            <p>{message.content}</p>
          </article>
        ))}
        {conversation.loading && <output>正在整理内容，请稍候…</output>}
      </div>
      <form onSubmit={submit}>
        <label>
          补充个人信息与经历
          <textarea
            rows={5}
            maxLength={4000}
            value={conversation.input}
            disabled={conversation.loading || creating || exhausted}
            onChange={(event) => conversation.setInput(event.target.value)}
            placeholder="例如求职方向、学历、工作内容、项目职责和已取得的实际成果"
          />
        </label>
        {conversation.error && !conversation.loading && <ErrorBox error={conversation.error} />}
        {conversation.messages.length >= 18 && (
          <p className="ai-note">本轮对话已接近长度上限，可以生成简历或开始新对话。</p>
        )}
        <div className="ai-action-row">
          <Button
            type="submit"
            busy={conversation.loading}
            disabled={
              !available ||
              creating ||
              conversation.messages.length >= 18 ||
              !conversation.input.trim()
            }
          >
            发送补充信息
          </Button>
          <Button
            type="button"
            className="primary"
            busy={conversation.loading}
            disabled={
              !available ||
              creating ||
              exhausted ||
              (!conversation.messages.length && !conversation.input.trim())
            }
            onClick={() => void conversation.submit(true)}
          >
            生成简历
          </Button>
        </div>
      </form>
      {conversation.messages.length > 0 && (
        <Button
          className="text-button"
          disabled={conversation.loading || creating}
          onClick={conversation.reset}
        >
          开始新对话
        </Button>
      )}
    </section>
  );
}
