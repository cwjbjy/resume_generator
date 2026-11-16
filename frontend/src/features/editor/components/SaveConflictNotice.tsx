import { Button } from '../../../components/ui/Button';

interface SaveConflictNoticeProps {
  loading: boolean;
  onLoadServer: () => void;
  onKeepDraft: () => void;
}

export function SaveConflictNotice({
  loading,
  onLoadServer,
  onKeepDraft,
}: SaveConflictNoticeProps) {
  return (
    <div className="inline-error" role="alert">
      <p>服务器已有新版本，自动保存已暂停。本地草稿已保留，可以加载服务器版本后选择如何恢复。</p>
      <Button busy={loading} onClick={onLoadServer}>
        加载服务器版本
      </Button>
      <Button onClick={onKeepDraft}>保留草稿并返回</Button>
    </div>
  );
}
