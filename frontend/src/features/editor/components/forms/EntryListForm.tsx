import { Plus } from 'lucide-react';
import { useState } from 'react';
import { Button } from '../../../../components/ui/Button';
import type { Entry } from '../../../../types';
import type { EntryGroup } from '../../model/editorModules';
import { CustomSectionFields } from './CustomSectionFields';
import { EducationFields } from './EducationFields';
import { EntryCard } from './EntryCard';
import { ExperienceFields } from './ExperienceFields';

const labels = { edu: '教育', work: '工作', proj: '项目', custom: '模块' };
const initialValues: Record<EntryGroup, Omit<Entry, 'id'>> = {
  edu: { school: '', major: '', degree: '本科', start: '', end: '' },
  work: { company: '', role: '', start: '', end: '', desc: '' },
  proj: { name: '', role: '', start: '', end: '', desc: '' },
  custom: { title: '自定义模块', desc: '' },
};

export function EntryListForm({
  group,
  entries,
  onChange,
}: {
  group: EntryGroup;
  entries: Entry[];
  onChange: (entries: Entry[]) => void;
}) {
  const [draggedIndex, setDraggedIndex] = useState<number | null>(null);
  function move(from: number, to: number) {
    if (to < 0 || to >= entries.length) return;
    const next = [...entries];
    const [entry] = next.splice(from, 1);
    next.splice(to, 0, entry);
    onChange(next);
  }
  function update(index: number, field: keyof Entry, value: string) {
    onChange(
      entries.map((entry, position) => (position === index ? { ...entry, [field]: value } : entry)),
    );
  }
  return (
    <div>
      {entries.map((entry, index) => {
        const updateField = (field: keyof Entry, value: string) => update(index, field, value);
        return (
          <EntryCard
            key={entry.id}
            label={labels[group]}
            index={index}
            count={entries.length}
            dragging={draggedIndex === index}
            onDragStart={(event) => {
              if ((event.target as HTMLElement).closest('input,textarea')) {
                event.preventDefault();
                return;
              }
              setDraggedIndex(index);
              event.dataTransfer.effectAllowed = 'move';
            }}
            onDragEnd={() => setDraggedIndex(null)}
            onDrop={() => {
              if (draggedIndex !== null) move(draggedIndex, index);
              setDraggedIndex(null);
            }}
            onMove={(destination) => move(index, destination)}
            onRemove={() => onChange(entries.filter((_, position) => position !== index))}
          >
            {group === 'edu' && <EducationFields entry={entry} onChange={updateField} />}
            {(group === 'work' || group === 'proj') && (
              <ExperienceFields entry={entry} isProject={group === 'proj'} onChange={updateField} />
            )}
            {group === 'custom' && <CustomSectionFields entry={entry} onChange={updateField} />}
          </EntryCard>
        );
      })}
      <Button
        className="add-entry"
        onClick={() => onChange([...entries, { id: crypto.randomUUID(), ...initialValues[group] }])}
      >
        <Plus size={16} />
        添加{group === 'custom' ? '自定义模块' : labels[group] + '经历'}
      </Button>
      <p className="field-hint">
        {group === 'edu'
          ? '预览会按结束时间倒序展示。'
          : '拖动条目或使用箭头调整顺序，修改后自动保存。'}
      </p>
    </div>
  );
}
