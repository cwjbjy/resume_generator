import { FormField } from '../../../../components/ui/FormField';
import type { Entry } from '../../../../types';
import { EntryDates } from './EntryDates';

export function ExperienceFields({
  entry,
  isProject,
  onChange,
}: {
  entry: Entry;
  isProject: boolean;
  onChange: (field: keyof Entry, value: string) => void;
}) {
  const nameField = isProject ? 'name' : 'company';
  return (
    <>
      <FormField
        label={isProject ? '项目名称' : '公司名称'}
        value={entry[nameField]}
        onChange={(value) => onChange(nameField, value)}
      />
      <FormField
        label="担任职位"
        value={entry.role}
        onChange={(value) => onChange('role', value)}
      />
      <EntryDates entry={entry} onChange={onChange} />
      <FormField
        label="经历与成果（每行一项）"
        value={entry.desc}
        onChange={(value) => onChange('desc', value)}
        multiline
        placeholder="描述你做了什么，以及取得的成果…"
      />
    </>
  );
}
