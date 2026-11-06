import { FormField } from '../../../../components/ui/FormField';
import type { Entry } from '../../../../types';

export function CustomSectionFields({
  entry,
  onChange,
}: {
  entry: Entry;
  onChange: (field: keyof Entry, value: string) => void;
}) {
  return (
    <>
      <FormField
        label="模块标题"
        value={entry.title}
        onChange={(value) => onChange('title', value)}
      />
      <FormField
        label="模块内容"
        value={entry.desc}
        onChange={(value) => onChange('desc', value)}
        multiline
        placeholder="描述你做了什么，以及取得的成果…"
      />
    </>
  );
}
