import { useQueryClient } from '@tanstack/react-query';
import { Plus } from 'lucide-react';
import { useState } from 'react';
import { Link } from 'react-router-dom';
import { resumeApi } from '../../../api/resumeApi';
import { refreshUser } from '../../../api/userApi';
import { ConfirmDialog } from '../../../components/ui/ConfirmDialog';
import { ErrorBox } from '../../../components/ui/ErrorBox';
import { Loading } from '../../../components/ui/Loading';
import { templates as initialTemplates } from '../../../data/templateCatalog';
import { api } from '../../../lib/http';
import { notify } from '../../../stores/noticeStore';
import { useSession } from '../../../stores/sessionStore';
import type { Order, Resume } from '../../../types';
import { ExportHistory } from '../components/ExportHistory';
import { OrderList } from '../components/OrderList';
import { ProfileCard } from '../components/ProfileCard';
import { ProfileDialog, type ProfileInput } from '../components/ProfileDialog';
import { RenameResumeDialog } from '../components/RenameResumeDialog';
import { ResumeCard } from '../components/ResumeCard';
import { useWorkspaceData, type WorkspaceTab } from '../hooks/useWorkspaceData';

const tabs: { key: WorkspaceTab; label: string }[] = [
  { key: 'resumes', label: '我的简历' },
  { key: 'orders', label: '会员订单' },
  { key: 'exports', label: '导出记录' },
];

export function WorkspacePage() {
  const user = useSession((state) => state.user)!;
  const client = useQueryClient();
  const [tab, setTab] = useState<WorkspaceTab>('resumes');
  const [renaming, setRenaming] = useState<Resume | null>(null);
  const [removing, setRemoving] = useState<Resume | null>(null);
  const [refunding, setRefunding] = useState<Order | null>(null);
  const [editingProfile, setEditingProfile] = useState(false);
  const [busy, setBusy] = useState(false);
  const { resumes, orders, exports, templates, config } = useWorkspaceData(tab);
  const catalog = templates.data || initialTemplates;

  async function run(action: () => Promise<unknown>, message: string) {
    setBusy(true);
    try {
      await action();
      notify(message);
      setRenaming(null);
      setRemoving(null);
      setRefunding(null);
      await Promise.all([
        client.invalidateQueries({ queryKey: ['resumes'] }),
        client.invalidateQueries({ queryKey: ['orders'] }),
        refreshUser(),
      ]);
      void client.invalidateQueries({ queryKey: ['user'] });
      return true;
    } catch (error) {
      notify((error as Error).message);
      return false;
    } finally {
      setBusy(false);
    }
  }
  async function saveProfile(profile: ProfileInput) {
    const succeeded = await run(() => api('PUT', '/users/me', profile), '资料已更新');
    if (succeeded) setEditingProfile(false);
  }

  return (
    <main className="page-shell me-page">
      <div className="me-heading">
        <div>
          <span className="eyebrow muted">MY WORKSPACE</span>
          <h1>每一步成长，都在这里。</h1>
          <p>欢迎回来，{user.nickname || user.username}。继续完善你的职业故事。</p>
        </div>
        <Link className="button primary" to="/templates">
          <Plus size={17} />
          新建简历
        </Link>
      </div>
      <div className="me-layout">
        <ProfileCard user={user} onEdit={() => setEditingProfile(true)} />
        <section className="workspace-main">
          <div className="workspace-tabs">
            {tabs.map((item) => (
              <button
                key={item.key}
                onClick={() => setTab(item.key)}
                className={item.key === tab ? 'active' : ''}
              >
                {item.label}
                {item.key === 'resumes' && <span>{resumes.data?.length || 0}</span>}
              </button>
            ))}
          </div>
          {tab === 'resumes' && (
            <>
              {resumes.isLoading && <Loading />}
              {resumes.error && (
                <ErrorBox error={resumes.error} retry={() => void resumes.refetch()} />
              )}
              <div className="my-resume-grid">
                {resumes.data?.map((resume) => (
                  <ResumeCard
                    key={resume.id}
                    resume={resume}
                    template={
                      catalog.find((template) => template.id === resume.templateId) || catalog[0]
                    }
                    busy={busy}
                    onRename={() => setRenaming(resume)}
                    onRemove={() => setRemoving(resume)}
                    onDuplicate={() =>
                      void run(() => resumeApi.duplicate(resume.id), '已创建简历副本')
                    }
                  />
                ))}
                <Link className="new-resume-card" to="/templates">
                  <span>
                    <Plus size={25} />
                  </span>
                  <h3>开启一份新简历</h3>
                  <p>从适合你的模板开始</p>
                </Link>
              </div>
            </>
          )}
          {tab === 'orders' && (
            <>
              {orders.isLoading && <Loading />}
              {orders.error && <ErrorBox error={orders.error} />}
              <OrderList
                orders={orders.data}
                mockEnabled={Boolean(config.data?.mockEnabled)}
                onRefund={setRefunding}
              />
            </>
          )}
          {tab === 'exports' && (
            <>
              {exports.isLoading && <Loading />}
              {exports.error && <ErrorBox error={exports.error} />}
              <ExportHistory records={exports.data} />
            </>
          )}
        </section>
      </div>
      {renaming && (
        <RenameResumeDialog
          title={renaming.title}
          busy={busy}
          onClose={() => setRenaming(null)}
          onSubmit={(title) => void run(() => resumeApi.save({ ...renaming, title }), '标题已更新')}
        />
      )}
      {removing && (
        <ConfirmDialog
          title="删除这份简历？"
          confirmLabel="确认删除"
          danger
          busy={busy}
          onClose={() => setRemoving(null)}
          onConfirm={() => void run(() => resumeApi.remove(removing.id), '简历已删除')}
        >
          「{removing.title}」删除后无法恢复，请确认已保存需要的内容。
        </ConfirmDialog>
      )}
      {refunding && (
        <ConfirmDialog
          title="模拟会员退款"
          confirmLabel="确认退款"
          busy={busy}
          onClose={() => setRefunding(null)}
          onConfirm={() =>
            void run(
              () => api('POST', `/orders/${refunding.id}/refund`),
              '退款演示完成，会员权益已更新',
            )
          }
        >
          确认对订单 {refunding.orderNo} 进行沙箱退款？该订单对应的会员天数将被扣回。
        </ConfirmDialog>
      )}
      {editingProfile && (
        <ProfileDialog
          user={user}
          busy={busy}
          onClose={() => setEditingProfile(false)}
          onSubmit={(profile) => void saveProfile(profile)}
        />
      )}
    </main>
  );
}
