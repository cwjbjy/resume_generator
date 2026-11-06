import { Crown, Search } from 'lucide-react';

export function TemplateFilters({
  filter,
  onFilterChange,
  search,
  onSearchChange,
}: {
  filter: string;
  onFilterChange: (filter: string) => void;
  search: string;
  onSearchChange: (search: string) => void;
}) {
  return (
    <div className="catalog-toolbar">
      <div className="filter-tabs">
        {[
          ['all', '全部模板'],
          ['free', '免费模板'],
          ['vip', '会员专享'],
        ].map(([key, label]) => (
          <button
            className={filter === key ? 'active' : ''}
            key={key}
            onClick={() => onFilterChange(key)}
          >
            {key === 'vip' && <Crown size={14} />}
            {label}
          </button>
        ))}
      </div>
      <label className="search-box">
        <Search size={16} />
        <input
          aria-label="搜索模板"
          placeholder="搜索模板名称"
          value={search}
          onChange={(event) => onSearchChange(event.target.value)}
        />
      </label>
    </div>
  );
}
