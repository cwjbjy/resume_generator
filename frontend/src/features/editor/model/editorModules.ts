import {
  Blocks,
  BriefcaseBusiness,
  FolderKanban,
  GraduationCap,
  Tags,
  UserRound,
} from 'lucide-react';

export const editorModules = [
  { key: 'basic', label: '基本信息', description: '让招聘者快速认识你。', icon: UserRound },
  {
    key: 'edu',
    label: '教育经历',
    description: '每一段经历，都是成长的证明。',
    icon: GraduationCap,
  },
  {
    key: 'work',
    label: '工作经历',
    description: '每一段经历，都是成长的证明。',
    icon: BriefcaseBusiness,
  },
  {
    key: 'proj',
    label: '项目经历',
    description: '每一段经历，都是成长的证明。',
    icon: FolderKanban,
  },
  {
    key: 'skills',
    label: '专业技能',
    description: '把最擅长的能力，放在最清晰的位置。',
    icon: Tags,
  },
  { key: 'custom', label: '自定义', description: '每一段经历，都是成长的证明。', icon: Blocks },
] as const;
export type EditorModule = (typeof editorModules)[number]['key'];
export type EntryGroup = 'edu' | 'work' | 'proj' | 'custom';
