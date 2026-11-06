import { FormField } from '../../../../components/ui/FormField';
import type { Content } from '../../../../types';

export function BasicInfoForm({
  value,
  onChange,
}: {
  value: Content['basic'];
  onChange: (value: Content['basic']) => void;
}) {
  const update = (field: keyof Content['basic'], text: string) =>
    onChange({ ...value, [field]: text });
  return (
    <>
      <div className="field-grid">
        <FormField
          label="姓名"
          value={value.name}
          onChange={(text) => update('name', text)}
          placeholder="你的姓名"
        />
        <FormField
          label="求职意向"
          value={value.job}
          onChange={(text) => update('job', text)}
          placeholder="意向岗位"
        />
        <FormField
          label="联系电话"
          type="tel"
          value={value.phone}
          onChange={(text) => update('phone', text)}
        />
        <FormField
          label="电子邮箱"
          type="email"
          value={value.email}
          onChange={(text) => update('email', text)}
        />
        <FormField label="所在城市" value={value.city} onChange={(text) => update('city', text)} />
      </div>
      {value.phone && !/^[+0-9()\- ]{6,25}$/.test(value.phone) && (
        <p className="inline-error">请检查电话号码格式</p>
      )}
      <FormField
        label="个人简介"
        value={value.intro}
        onChange={(text) => update('intro', text)}
        multiline
        placeholder="用 2–3 句话，介绍你的经验和优势。"
      />
      <div className="editor-tip">
        <span className="tiny-symbol">✦</span>
        <span>小建议：用「年限 + 擅长领域 + 核心成果」介绍自己，让优势一目了然。</span>
      </div>
    </>
  );
}
