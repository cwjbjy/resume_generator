import { type Content } from '../types';
export const sample: Content = {
 basic: { name: '林予安', job: '前端开发工程师', phone: '13800001234', email: 'linyuan@example.com', city: '上海', intro: '热爱用技术解决实际问题，拥有 5 年 Web 应用开发经验。关注产品体验与工程质量，擅长将复杂业务转化为清晰、高效的用户界面。' },
 edu: [{ id: 'e1', school: '华东理工大学', major: '计算机科学与技术', degree: '本科', start: '2015-09', end: '2019-06' }],
 work: [{ id: 'w1', company: '星河科技有限公司', role: '高级前端工程师', start: '2022-03', end: '', desc: '负责企业协作平台前端开发，服务 10 万+ 企业用户。\n主导 React + TypeScript 技术升级，页面加载速度提升 40%。\n搭建通用组件库与自动化测试流程，提升团队交付效率。' }, { id: 'w2', company: '知行互联网有限公司', role: '前端工程师', start: '2019-07', end: '2022-02', desc: '参与核心业务系统建设，完成 20+ 业务模块。\n推动响应式设计落地，持续优化用户体验。' }],
 proj: [{ id: 'p1', name: '企业协作工作台', role: '前端负责人', start: '2023-03', end: '2024-06', desc: '使用 React、TypeScript 与 Vite 构建模块化工作台。\n设计权限体系与可视化看板，支持多角色协同办公。' }],
 skills: ['React', 'TypeScript', 'Vue 3', 'Node.js', '工程化', '团队协作'], custom: [],
};

