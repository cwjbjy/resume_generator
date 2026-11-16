import type { ChatMessage } from './types';

const generationInstruction = '请根据以上已确认的信息生成完整简历，缺失信息留空。';

export function nextConversation(
  messages: ChatMessage[],
  input: string,
  generate: boolean,
): ChatMessage[] {
  const content = input.trim() || (generate ? generationInstruction : '');
  if (!content) throw new Error('请先描述你的求职方向或经历');
  if (content.length > 4000) throw new Error('每次补充最多 4000 字');
  if (!generate && messages.length >= 18)
    throw new Error('本轮对话已接近长度上限，请生成简历或开始新对话');
  const next: ChatMessage[] = [...messages, { role: 'user', content }];
  if (next.length > 20) throw new Error('本轮对话已达到长度上限，请开始新对话');
  const length = next.reduce((total, message) => total + message.content.length, 0);
  if (length > 30000) {
    throw new Error('本轮对话内容过长，请精简本次补充后重新生成');
  }
  if (!generate && length > 30000 - 4000 - generationInstruction.length) {
    throw new Error('对话内容已接近长度上限，请精简本次补充，或直接生成简历');
  }
  return next;
}
