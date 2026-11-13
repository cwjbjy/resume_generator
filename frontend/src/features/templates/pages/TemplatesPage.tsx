import { useSetState, useTimeout } from 'ahooks';
import { Search, ShieldCheck } from 'lucide-react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { Button } from '../../../components/ui/Button';
import { ErrorBox } from '../../../components/ui/ErrorBox';
import { Loading } from '../../../components/ui/Loading';
import { useSession } from '../../../stores/sessionStore';
import { MembershipBanner } from '../components/MembershipBanner';
import { TemplateCard } from '../components/TemplateCard';
import { TemplateCategories } from '../components/TemplateCategories';
import { TemplateFilters } from '../components/TemplateFilters';
import { TemplateHero } from '../components/TemplateHero';
import { useCreateResume } from '../hooks/useCreateResume';
import { useTemplates } from '../hooks/useTemplates';

export function TemplatesPage() {
  const { data, error, loading: templatesLoading, refresh } = useTemplates();
  const { createResume, loading: creating } = useCreateResume();
  const user = useSession((state) => state.user);
  const navigate = useNavigate();
  const [{ filter, category, search }, setFilters] = useSetState({
    filter: 'all',
    category: '全部',
    search: '',
  });
  const [params, setParams] = useSearchParams();
  const templateId = params.get('use');
  const templates = data ?? [];
  const visible = templates.filter(
    (template) =>
      (filter === 'all' || (filter === 'vip' ? template.isVip : !template.isVip)) &&
      (category === '全部' || template.category === category) &&
      (template.name + template.category).includes(search.trim()),
  );

  useTimeout(
    () => {
      if (!templateId || !user || !data) return;
      setParams({}, { replace: true });
      const template = data.find((item) => item.id === Number(templateId));
      if (template) void createResume(template);
    },
    templateId && user && data ? 0 : undefined,
  );

  function resetFilters() {
    setFilters({ search: '', category: '全部', filter: 'all' });
  }
  return (
    <main className="template-page">
      <TemplateHero />
      <section className="template-content">
        <div className="section-heading">
          <div>
            <div className="eyebrow muted">FIND YOUR STYLE</div>
            <h2>选一份模板，开启新的可能</h2>
          </div>
          <p>4 套原创设计 · 为不同阶段的你而准备</p>
        </div>
        <div className="catalog-layout">
          <TemplateCategories
            category={category}
            onSelect={(category) => setFilters({ category })}
            templates={templates}
          />
          <div className="catalog-main">
            <TemplateFilters
              filter={filter}
              onFilterChange={(filter) => setFilters({ filter })}
              search={search}
              onSearchChange={(search) => setFilters({ search })}
            />
            {error && <ErrorBox error={error} retry={refresh} />}
            {templatesLoading && <Loading />}
            <div className="template-grid">
              {visible.map((template) => (
                <TemplateCard
                  key={template.id}
                  template={template}
                  busy={creating}
                  onUse={() => createResume(template)}
                  onPreview={() => navigate(`/templates/${template.id}`)}
                />
              ))}
            </div>
            {!templatesLoading && !visible.length && (
              <div className="empty-state">
                <Search size={30} />
                <h3>暂时没有匹配的模板</h3>
                <p>试试其他分类或关键词</p>
                <Button onClick={resetFilters}>查看全部模板</Button>
              </div>
            )}
            <div className="catalog-bottom">
              <span>
                <ShieldCheck size={16} />
                你的内容只属于你，简历默认私密保存
              </span>
              <span>好设计，让表达更进一步。</span>
            </div>
          </div>
        </div>
        <MembershipBanner />
      </section>
    </main>
  );
}
