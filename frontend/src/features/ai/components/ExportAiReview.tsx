import { useLockFn, useRequest } from 'ahooks';
import { Sparkles } from 'lucide-react';
import { Button } from '../../../components/ui/Button';
import { ErrorBox } from '../../../components/ui/ErrorBox';
import { useSession } from '../../../stores/sessionStore';
import { aiApi } from '../api/aiApi';

const priorityText = { high: '优先调整', medium: '建议调整', low: '可进一步完善' };

export function ExportAiReview({ exportId }: { exportId: number }) {
  const token = useSession((session) => session.token);
  const review = useRequest(
    async () => {
      const requestToken = useSession.getState().token;
      const result = await aiApi.exportReview(exportId, requestToken ?? undefined);
      return { result, token: requestToken, exportId };
    },
    { manual: true },
  );
  const analyze = useLockFn(() => review.runAsync().catch(() => {}));
  const result =
    review.data?.token === token && review.data.exportId === exportId
      ? review.data.result
      : undefined;
  return (
    <section className="ai-export-review" aria-label="导出后 AI 优化建议">
      <Button busy={review.loading} onClick={() => void analyze()}>
        <Sparkles size={16} />
        {result ? '重新分析导出内容' : '获取 AI 优化建议'}
      </Button>
      {review.error && !review.loading && <ErrorBox error={review.error} />}
      {result && !review.loading && (
        <div className="ai-result">
          <p>{result.summary}</p>
          {result.strengths.length > 0 && (
            <div className="ai-result-section">
              <h3>已具备的亮点</h3>
              <ul>
                {result.strengths.map((strength, index) => (
                  <li key={index}>{strength}</li>
                ))}
              </ul>
            </div>
          )}
          {result.suggestions.map((suggestion, index) => (
            <article className="ai-evidence" key={index}>
              <h3>
                {suggestion.section} · {priorityText[suggestion.priority]}
              </h3>
              <p>{suggestion.issue}</p>
              <p>{suggestion.suggestion}</p>
            </article>
          ))}
        </div>
      )}
    </section>
  );
}
