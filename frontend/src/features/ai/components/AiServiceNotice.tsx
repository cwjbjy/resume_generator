import { ErrorBox } from '../../../components/ui/ErrorBox';
import { Button } from '../../../components/ui/Button';
import type { AiStatus } from '../model/types';

export function AiServiceNotice({
  data,
  loading,
  error,
  refresh,
}: {
  data?: AiStatus;
  loading: boolean;
  error?: Error;
  refresh: () => void;
}) {
  if (loading) return <output className="ai-note">正在检查 AI 服务…</output>;
  if (error) return <ErrorBox error={error} retry={refresh} />;
  if (!data?.available) {
    return (
      <div className="ai-note">
        <p>AI 服务尚未配置，请在后端设置 DEEPSEEK_API_KEY 后重启服务。</p>
        <Button className="small" onClick={refresh}>
          重新检查服务
        </Button>
      </div>
    );
  }
  return null;
}
