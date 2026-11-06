import { ArrowRight, LayoutTemplate, Sparkles } from 'lucide-react';
import { type Template } from '../../../types/index';

export function TemplateCategories({
  category,
  onSelect,
  templates,
}: {
  category: string;
  onSelect: (category: string) => void;
  templates: Template[];
}) {
  return (
    <aside className="catalog-sidebar">
      <div className="sidebar-title">
        <LayoutTemplate size={17} /> 模板分类
      </div>
      {['全部', '通用', '职场', '校招'].map((categoryName) => (
        <button
          key={categoryName}
          className={category === categoryName ? 'active' : ''}
          onClick={() => onSelect(categoryName)}
        >
          <span>
            {categoryName === '全部'
              ? '全部模板'
              : { 通用: '简约通用', 职场: '职场进阶', 校招: '应届校招' }[categoryName]}
          </span>
          <span>
            {categoryName === '全部'
              ? 4
              : templates.filter((template) => template.category === categoryName).length}
          </span>
        </button>
      ))}
      <div className="sidebar-tip">
        <div className="tip-icon">
          <Sparkles size={19} />
        </div>
        <b>好简历，从清晰开始</b>
        <p>突出关键成果，用真实的数字讲述你的价值。</p>
        <span>
          让经历自己发光 <ArrowRight size={14} />
        </span>
      </div>
    </aside>
  );
}
