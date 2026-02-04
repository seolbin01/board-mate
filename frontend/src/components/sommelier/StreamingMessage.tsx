import ReactMarkdown from 'react-markdown';
import { Wine } from 'lucide-react';

interface StreamingMessageProps {
  content: string;
}

export default function StreamingMessage({ content }: StreamingMessageProps) {
  return (
    <div className="flex gap-3">
      <div className="w-8 h-8 rounded-full flex items-center justify-center flex-shrink-0 bg-purple-100">
        <Wine className="w-4 h-4 text-purple-600" />
      </div>
      <div className="max-w-[80%] rounded-2xl px-4 py-2 bg-gray-100 text-gray-800">
        <div className="prose prose-sm max-w-none prose-p:my-1 prose-ul:my-1 prose-ol:my-1">
          <ReactMarkdown>{content}</ReactMarkdown>
          <span className="inline-block w-2 h-4 bg-purple-400 animate-pulse ml-1" />
        </div>
      </div>
    </div>
  );
}
