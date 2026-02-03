import { useState, useEffect, useRef } from 'react';
import { Search, Loader2 } from 'lucide-react';
import { searchGames } from '../../api/rulemaster';
import type { BggGameSearchResult } from '../../types';

interface GameSearchInputProps {
  onSelectGame: (game: BggGameSearchResult) => void;
}

export default function GameSearchInput({ onSelectGame }: GameSearchInputProps) {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState<BggGameSearchResult[]>([]);
  const [loading, setLoading] = useState(false);
  const [showDropdown, setShowDropdown] = useState(false);
  const debounceTimer = useRef<ReturnType<typeof setTimeout> | null>(null);
  const dropdownRef = useRef<HTMLDivElement>(null);

  // 디바운싱 검색
  useEffect(() => {
    if (debounceTimer.current) {
      clearTimeout(debounceTimer.current);
    }

    if (query.trim().length < 2) {
      setResults([]);
      setShowDropdown(false);
      return;
    }

    debounceTimer.current = setTimeout(async () => {
      setLoading(true);
      try {
        const data = await searchGames(query.trim());
        setResults(data);
        setShowDropdown(true);
      } catch (error) {
        console.error('게임 검색 실패:', error);
        setResults([]);
      } finally {
        setLoading(false);
      }
    }, 300);

    return () => {
      if (debounceTimer.current) {
        clearTimeout(debounceTimer.current);
      }
    };
  }, [query]);

  // 외부 클릭 감지
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setShowDropdown(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleSelect = (game: BggGameSearchResult) => {
    onSelectGame(game);
    setQuery('');
    setResults([]);
    setShowDropdown(false);
  };

  return (
    <div className="relative" ref={dropdownRef}>
      <div className="relative">
        <Search
          className="absolute left-4 top-1/2 -translate-y-1/2 text-stone-400"
          size={20}
        />
        <input
          type="text"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          placeholder="게임 이름을 검색하세요..."
          className="w-full pl-12 pr-12 py-4 text-lg border-2 border-stone-200 rounded-xl focus:ring-2 focus:ring-orange-500 focus:border-orange-500 transition-all"
          autoFocus
        />
        {loading && (
          <Loader2
            className="absolute right-4 top-1/2 -translate-y-1/2 text-orange-500 animate-spin"
            size={20}
          />
        )}
      </div>

      {/* 자동완성 드롭다운 */}
      {showDropdown && results.length > 0 && (
        <div className="absolute z-10 w-full mt-2 bg-white border border-stone-200 rounded-xl shadow-lg max-h-96 overflow-y-auto">
          {results.map((game) => (
            <button
              key={game.bggId}
              onClick={() => handleSelect(game)}
              className="w-full flex items-center gap-4 p-4 hover:bg-orange-50 transition-colors text-left border-b border-stone-100 last:border-b-0"
            >
              {game.thumbnailUrl ? (
                <img
                  src={game.thumbnailUrl}
                  alt={game.name}
                  className="w-16 h-16 object-cover rounded-lg"
                />
              ) : (
                <div className="w-16 h-16 bg-stone-200 rounded-lg flex items-center justify-center">
                  <Search size={24} className="text-stone-400" />
                </div>
              )}
              <div className="flex-1">
                <h3 className="font-semibold text-stone-800">{game.name}</h3>
                <p className="text-sm text-stone-500">출시: {game.yearPublished}</p>
              </div>
            </button>
          ))}
        </div>
      )}

      {showDropdown && query.trim().length >= 2 && results.length === 0 && !loading && (
        <div className="absolute z-10 w-full mt-2 bg-white border border-stone-200 rounded-xl shadow-lg p-4 text-center text-stone-500">
          검색 결과가 없습니다
        </div>
      )}
    </div>
  );
}
