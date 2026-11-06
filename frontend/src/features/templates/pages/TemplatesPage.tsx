import { Search, ShieldCheck } from 'lucide-react';
import { useEffect, useRef, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { Button } from '../../../components/ui/Button';
import { ErrorBox } from '../../../components/ui/ErrorBox';
import { templates as initialTemplates } from '../../../data/templateCatalog';
import { useSession } from '../../../stores/sessionStore';
import { MembershipBanner } from '../components/MembershipBanner';
import { TemplateCard } from '../components/TemplateCard';
import { TemplateCategories } from '../components/TemplateCategories';
import { TemplateFilters } from '../components/TemplateFilters';
import { TemplateHero } from '../components/TemplateHero';
import { useCreateResume } from '../hooks/useCreateResume';
import { useTemplates } from '../hooks/useTemplates';

export function TemplatesPage() {
  const { data, error, refetch } = useTemplates();
  const { createResume, isPending } = useCreateResume();
  const user = useSession((state) => state.user);
  const navigate = useNavigate();
  const [filter, setFilter] = useState('all');
  const [category, setCategory] = useState('全部');
  const [search, setSearch] = useState('');
  const [params, setParams] = useSearchParams();
  const handledTemplate = useRef(false);
  const templates = data || initialTemplates;
  const visible = templates.filter(
    (template) =>
      (filter === 'all' || (filter === 'vip' ? template.isVip : !template.isVip)) &&
      (category === '全部' || template.category === category) &&
      (template.name + template.category).includes(search.trim()),
  );

  useEffect(() => {
    const templateId = params.get('use');
    if (!templateId || !user || !data || handledTemplate.current) return;
    handledTemplate.current = true;
    setParams({}, { replace: true });
    const template = data.find((item) => item.id === Number(templateId));
    if (template) createResume(template);
  }, [params, user, data, setParams, createResume]);

  function resetFilters() {
    setSearch('');
    setCategory('全部');
    setFilter('all');
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
          <TemplateCategories category={category} onSelect={setCategory} templates={templates} />
          <div className="catalog-main">
            <TemplateFilters
              filter={filter}
              onFilterChange={setFilter}
              search={search}
              onSearchChange={setSearch}
            />
            {error && <ErrorBox error={error} retry={() => void refetch()} />}
            <div className="template-grid">
              {visible.map((template) => (
                <TemplateCard
                  key={template.id}
                  template={template}
                  busy={isPending}
                  onUse={() => createResume(template)}
                  onPreview={() => navigate(`/templates/${template.id}`)}
                />
              ))}
            </div>
            {!visible.length && (
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
