import { create } from 'zustand';
interface NoticeState {
  text: string;
  show: (text: string) => void;
}

export const useNotice = create<NoticeState>((set) => ({
  text: '',
  show(text) {
    set({ text });
    setTimeout(() => set((state) => (state.text === text ? { text: '' } : state)), 4500);
  },
}));

export function notify(text: string) {
  useNotice.getState().show(text);
}
