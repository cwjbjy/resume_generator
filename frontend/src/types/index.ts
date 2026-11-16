export interface User {
  id: number;
  username: string;
  nickname?: string;
  email?: string;
  member: boolean;
  memberExpireAt?: string;
}
export interface Template {
  id: number;
  name: string;
  category: string;
  isVip: boolean;
  color: string;
  style: string;
  usedCount: number;
}
export interface Entry {
  id: string;
  school?: string;
  major?: string;
  degree?: string;
  company?: string;
  role?: string;
  name?: string;
  title?: string;
  start?: string;
  end?: string;
  desc?: string;
}
export interface Content {
  basic: { name: string; job: string; phone: string; email: string; city: string; intro: string };
  edu: Entry[];
  work: Entry[];
  proj: Entry[];
  skills: string[];
  custom: Entry[];
}
export interface Resume {
  id: number;
  userId: number;
  templateId: number;
  title: string;
  content: Content;
  version: number;
  updatedAt: string;
}
export interface ResumeSummary {
  id: number;
  templateId: number;
  title: string;
  version: number;
  updatedAt: string;
  basic: Content['basic'];
}
export interface PageResult<T> {
  records: T[];
  total: number;
  page: number;
  size: number;
}
export interface Plan {
  id: number;
  name: string;
  price: number;
  durationDays: number;
  features: string[];
}
export interface Order {
  id: number;
  orderNo: string;
  planId: number;
  planName: string;
  durationDays: number;
  amount: number;
  status: string;
  createdAt: string;
  paidAt?: string;
  expiredAt: string;
}
export interface ExportLog {
  id: number;
  resumeId: number;
  createdAt: string;
  status: string;
}
