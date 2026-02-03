import type { RuleMasterMessage } from '../../types';

interface MessageBubbleProps {
  message: RuleMasterMessage;
}

export default function MessageBubble({ message }: MessageBubbleProps) {
  const isUser = message.role === 'user';

  // 간단한 마크다운 렌더링 (볼드, 리스트)
  const renderContent = (text: string) => {
    const lines = text.split('\n');
    return lines.map((line, idx) => {
      // 볼드 처리 (**text**)
      const boldProcessed = line.split(/(\*\*.*?\*\*)/).map((part, i) => {
        if (part.startsWith('**') && part.endsWith('**')) {
          return <strong key={i}>{part.slice(2, -2)}</strong>;
        }
        return part;
      });

      // 리스트 처리
      if (line.trim().startsWith('- ') || line.trim().startsWith('* ')) {
        return (
          <li key={idx} className="ml-4">
            {boldProcessed}
          </li>
        );
      }

      return <p key={idx}>{boldProcessed}</p>;
    });
  };

  return (
    <div className={`flex ${isUser ? 'justify-end' : 'justify-start'} mb-4`}>
      <div className={`max-w-[80%] ${isUser ? 'order-2' : 'order-1'}`}>
        <div
          className={`px-4 py-3 rounded-2xl ${
            isUser
              ? 'bg-orange-500 text-white rounded-br-md'
              : 'bg-stone-100 text-stone-800 rounded-bl-md border border-stone-200'
          }`}
        >
          <div className="text-sm leading-relaxed space-y-1 whitespace-pre-wrap break-words">
            {renderContent(message.content)}
          </div>
        </div>
        <div
          className={`text-xs text-stone-400 mt-1 ${
            isUser ? 'text-right' : 'text-left'
          }`}
        >
          {new Date(message.timestamp).toLocaleTimeString('ko-KR', {
            hour: '2-digit',
            minute: '2-digit',
          })}
        </div>
      </div>
    </div>
  );
}
