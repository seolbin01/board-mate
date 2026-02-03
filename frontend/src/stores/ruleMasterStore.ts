import { create } from 'zustand';

interface RuleMasterStore {
  isOpen: boolean;
  bggId: number | undefined;
  open: (bggId?: number) => void;
  close: () => void;
}

export const useRuleMasterStore = create<RuleMasterStore>((set) => ({
  isOpen: false,
  bggId: undefined,
  open: (bggId) => set({ isOpen: true, bggId }),
  close: () => set({ isOpen: false, bggId: undefined }),
}));
