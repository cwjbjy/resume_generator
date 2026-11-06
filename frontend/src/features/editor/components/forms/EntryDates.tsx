import { FormField } from '../../../../components/ui/FormField';
import type { Entry } from '../../../../types';

export function EntryDates({
  entry,
  onChange,
}: {
  entry: Entry;
  onChange: (field: keyof Entry, value: string) => void;
}) {
  return (
    <>
      <div className="field-grid">
        <FormField
          label="开始时间"
          type="month"
          value={entry.start}
          onChange={(value) => onChange('start', value)}
        />
        <FormField
          label="结束时间（留空为至今）"
          type="month"
          value={entry.end}
          onChange={(value) => onChange('end', value)}
        />
      </div>
      {entry.start && entry.end && entry.end < entry.start && (
        <div className="inline-error">结束时间不能早于开始时间</div>
      )}
    </>
  );
}
