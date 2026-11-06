interface FormFieldProps {
  label: string;
  value?: string;
  onChange: (value: string) => void;
  type?: string;
  multiline?: boolean;
  placeholder?: string;
  maxLength?: number;
}

export function FormField({
  label,
  value = '',
  onChange,
  type = 'text',
  multiline = false,
  placeholder,
  maxLength,
}: FormFieldProps) {
  return (
    <label className={multiline ? 'field-full' : undefined}>
      {label}
      {multiline ? (
        <textarea
          value={value}
          onChange={(event) => onChange(event.target.value)}
          rows={5}
          maxLength={maxLength ?? 10000}
          placeholder={placeholder}
        />
      ) : (
        <input
          type={type}
          value={value}
          onChange={(event) => onChange(event.target.value)}
          maxLength={maxLength ?? 150}
          placeholder={placeholder}
        />
      )}
    </label>
  );
}
