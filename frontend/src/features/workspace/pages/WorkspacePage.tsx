import { useBoolean, useLatest, useLockFn, useRequest, useUnmountedRef } from 'ahooks';
import { Plus, Sparkles } from 'lucide-react';
import { useState } from 'react';
import { Link } from 'react-router-dom';
import { ConfirmDialog } from '../../../components/ui/ConfirmDialog';
import { ErrorBox } from '../../../components/ui/ErrorBox';
import { Loading } from '../../../components/ui/Loading';
import { Pagination } from '../../../components/ui/Pagination';
import { notify } from '../../../stores/noticeStore';
import { useSession } from '../../../stores/sessionStore';
import type { Order, ResumeSummary } from '../../../types';
import { membershipApi } from '../../membership/api/membershipApi';
import { resumeApi } from '../../resumes/api/resumeApi';
import { refreshUser, userApi } from '../../user/api/userApi';
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
  const [tab, setTab] = useState<WorkspaceTab>('resumes');
  const currentTab = useLatest(tab);
  const unmounted = useUnmountedRef();
  const [renaming, setRenaming] = useState<ResumeSummary | null>(null);
  const [removing, setRemoving] = useState<ResumeSummary | null>(null);
  const [refunding, setRefunding] = useState<Order | null>(null);
  const [editingProfile, { setTrue: openProfile, setFalse: closeProfile }] = useBoolean(false);
  const { resumes, orders, exports, templates, config } = useWorkspaceData(tab);
  const catalog = templates.data ?? [];

  const { runAsync, loading: busy } = useRequest(
    async (action: () => Promise<unknown>, message: string) => {
      const token = useSession.getState().token;
      await action();
      if (unmounted.current || useSession.getState().token !== token) return false;
      notify(message);
      setRenaming(null);
      setRemoving(null);
      setRefunding(null);
      await Promise.all([
        resumes.refreshAsync(),
        currentTab.current === 'orders' ? orders.refreshAsync() : Promise.resolve(),
        refreshUser(),
      ]);
      return true;
    },
    { manual: true, onError: (error) => notify(error.message) },
  );
  const run = useLockFn((action: () => Promise<unknown>, message: string) =>
    runAsync(action, message).catch(() => false),
  );
  async function saveProfile(profile: ProfileInput) {
    const succeeded = await run(() => userApi.updateProfile(profile), '资料已更新');
    if (succeeded) closeProfile();
  }
  const activeList = { resumes, orders, exports }[tab];

  return (
    <main className="page-shell me-page">
      <div className="me-heading">
        <div>
          <span className="eyebrow muted">MY WORKSPACE</span>
          <h1>每一步成长，都在这里。</h1>
          <p>欢迎回来，{user.nickname || user.username}。继续完善你的职业故事。</p>
        </div>
        <div className="ai-action-row">
          <Link className="button" to="/ai/generate">
            <Sparkles size={17} />
            AI 生成简历
          </Link>
          <Link className="button primary" to="/templates">
            <Plus size={17} />
            新建简历
          </Link>
        </div>
      </div>
      <div className="me-layout">
        <ProfileCard user={user} onEdit={openProfile} />
        <section className="workspace-main">
          <div className="workspace-tabs">
            {tabs.map((item) => (
              <button
                key={item.key}
                onClick={() => setTab(item.key)}
                className={item.key === tab ? 'active' : ''}
              >
                {item.label}
                {item.key === 'resumes' && <span>{resumes.data?.total || 0}</span>}
              </button>
            ))}
          </div>
          {tab === 'resumes' && (
            <>
              {(resumes.loading || templates.loading) && <Loading />}
              {resumes.error && <ErrorBox error={resumes.error} retry={resumes.refresh} />}
              <div className="my-resume-grid">
                {resumes.data?.list.map((resume) => (
                  <ResumeCard
                    key={resume.id}
                    resume={resume}
                    template={catalog.find((template) => template.id === resume.templateId)}
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
              {orders.loading && <Loading />}
              {orders.error && <ErrorBox error={orders.error} retry={orders.refresh} />}
              <OrderList
                orders={orders.data?.list}
                mockEnabled={Boolean(config.data?.mockEnabled)}
                onRefund={setRefunding}
              />
            </>
          )}
          {tab === 'exports' && (
            <>
              {exports.loading && <Loading />}
              {exports.error && <ErrorBox error={exports.error} retry={exports.refresh} />}
              <ExportHistory records={exports.data?.list} />
            </>
          )}
          <Pagination
            current={activeList.pagination.current}
            totalPage={activeList.pagination.totalPage}
            total={activeList.pagination.total}
            loading={activeList.loading}
            onChange={activeList.pagination.changeCurrent}
          />
        </section>
      </div>
      {renaming && (
        <RenameResumeDialog
          title={renaming.title}
          busy={busy}
          onClose={() => setRenaming(null)}
          onSubmit={(title) =>
            void run(async () => {
              const resume = await resumeApi.get(renaming.id);
              return resumeApi.save({ ...resume, title });
            }, '标题已更新')
          }
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
            void run(() => membershipApi.refundOrder(refunding.id), '退款演示完成，会员权益已更新')
          }
        >
          确认对订单 {refunding.orderNo} 进行沙箱退款？该订单对应的会员天数将被扣回。
        </ConfirmDialog>
      )}
      {editingProfile && (
        <ProfileDialog
          user={user}
          busy={busy}
          onClose={closeProfile}
          onSubmit={(profile) => void saveProfile(profile)}
        />
      )}
    </main>
  );
}
