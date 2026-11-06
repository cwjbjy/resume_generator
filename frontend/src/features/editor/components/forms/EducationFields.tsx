import { FormField } from '../../../../components/ui/FormField';
import type { Entry } from '../../../../types';
import { EntryDates } from './EntryDates';

export function EducationFields({
  entry,
  onChange,
}: {
  entry: Entry;
  onChange: (field: keyof Entry, value: string) => void;
}) {
  return (
    <>
      <FormField
        label="学校名称"
        value={entry.school}
        onChange={(value) => onChange('school', value)}
      />
      <div className="field-grid">
        <FormField
          label="专业"
          value={entry.major}
          onChange={(value) => onChange('major', value)}
        />
        <FormField
          label="学历"
          value={entry.degree}
          onChange={(value) => onChange('degree', value)}
        />
      </div>
      <EntryDates entry={entry} onChange={onChange} />
    </>
  );
}
