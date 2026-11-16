import type { Content } from '../../../types';

export interface AiStatus {
  available: boolean;
  model: string;
}

export type PolishModule = 'basic' | 'work' | 'proj';

export interface PolishTarget {
  module: PolishModule;
  entryId?: string;
}

export interface PolishResult extends PolishTarget {
  original: string;
  suggested: string;
  notes: string[];
  version: number;
}

export interface MatchResult {
  score: number;
  summary: string;
  matches: { requirement: string; evidenceIds: string[]; analysis: string }[];
  gaps: string[];
  suggestions: string[];
  evidence: { id: string; section: string; text: string; score: number }[];
}

export interface ChatMessage {
  role: 'user' | 'assistant';
  content: string;
}

export interface ChatResult {
  reply: string;
  title?: string;
  content?: Content;
}

export interface ExportReview {
  summary: string;
  strengths: string[];
  suggestions: {
    section: string;
    issue: string;
    suggestion: string;
    priority: 'high' | 'medium' | 'low';
  }[];
}
